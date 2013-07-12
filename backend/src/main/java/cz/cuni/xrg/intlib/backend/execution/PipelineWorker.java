package cz.cuni.xrg.intlib.backend.execution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cz.cuni.xrg.intlib.backend.DatabaseAccess;
import cz.cuni.xrg.intlib.backend.pipeline.event.PipelineContextErrorEvent;
import cz.cuni.xrg.intlib.backend.pipeline.event.PipelineFailedEvent;
import cz.cuni.xrg.intlib.backend.pipeline.event.PipelineFinished;
import cz.cuni.xrg.intlib.backend.pipeline.event.PipelineModuleErrorEvent;
import cz.cuni.xrg.intlib.backend.pipeline.event.PipelineStructureError;
import cz.cuni.xrg.intlib.commons.ProcessingContext;
import cz.cuni.xrg.intlib.commons.app.conf.AppConfig;
import cz.cuni.xrg.intlib.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.xrg.intlib.commons.app.execution.ExecutionContextInfo;
import cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus;
import cz.cuni.xrg.intlib.commons.app.execution.PipelineExecution;
import cz.cuni.xrg.intlib.commons.app.module.ModuleException;
import cz.cuni.xrg.intlib.commons.app.module.ModuleFacade;
import cz.cuni.xrg.intlib.commons.app.pipeline.Pipeline;
import cz.cuni.xrg.intlib.commons.app.pipeline.PipelineFacade;
import cz.cuni.xrg.intlib.commons.app.pipeline.graph.DependencyGraph;
import cz.cuni.xrg.intlib.commons.app.pipeline.graph.Edge;
import cz.cuni.xrg.intlib.commons.app.pipeline.graph.Node;
import cz.cuni.xrg.intlib.commons.configuration.Config;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.commons.configuration.Configurable;
import cz.cuni.xrg.intlib.commons.data.DataUnitCreateException;
import cz.cuni.xrg.intlib.commons.extractor.Extract;
import cz.cuni.xrg.intlib.backend.context.ContextException;
import cz.cuni.xrg.intlib.backend.context.DataUnitMerger;
import cz.cuni.xrg.intlib.backend.context.ExtendedContext;
import cz.cuni.xrg.intlib.backend.context.ExtendedExtractContext;
import cz.cuni.xrg.intlib.backend.context.ExtendedLoadContext;
import cz.cuni.xrg.intlib.backend.context.ExtendedTransformContext;
import cz.cuni.xrg.intlib.backend.context.impl.ContextFactory;
import cz.cuni.xrg.intlib.backend.context.impl.PrimitiveDataUnitMerger;
import cz.cuni.xrg.intlib.backend.extractor.events.ExtractCompletedEvent;
import cz.cuni.xrg.intlib.backend.extractor.events.ExtractStartEvent;
import cz.cuni.xrg.intlib.commons.extractor.ExtractException;
import cz.cuni.xrg.intlib.backend.extractor.events.ExtractFailedEvent;
import cz.cuni.xrg.intlib.commons.loader.Load;
import cz.cuni.xrg.intlib.backend.loader.events.LoadCompletedEvent;
import cz.cuni.xrg.intlib.backend.loader.events.LoadStartEvent;
import cz.cuni.xrg.intlib.commons.loader.LoadException;
import cz.cuni.xrg.intlib.backend.loader.events.LoadFailedEvent;
import cz.cuni.xrg.intlib.commons.transformer.Transform;
import cz.cuni.xrg.intlib.backend.transformer.events.TransformCompletedEvent;
import cz.cuni.xrg.intlib.backend.transformer.events.TransformStartEvent;
import cz.cuni.xrg.intlib.commons.transformer.TransformException;
import cz.cuni.xrg.intlib.backend.transformer.events.TransformFailedEvent;
import cz.cuni.xrg.intlib.commons.app.execution.LogMessage;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Worker responsible for running single PipelineExecution.
 *
 * @author Petyr
 * 
 */
class PipelineWorker implements Runnable {
	
	/**
	 * PipelineExecution record, determine pipeline to run.
	 */
	private PipelineExecution execution;

	/**
	 * Publisher instance for publishing pipeline execution events.
	 */
	private ApplicationEventPublisher eventPublisher;

	/**
	 * Provide access to DPURecord implementation.
	 */
	private ModuleFacade moduleFacade;
	
	/**
	 * Store context related to Nodes (DPUs).
	 */
	private Map<Node, ProcessingContext> contexts;
		
	/**
	 * Logger class.
	 */
	private static final Logger LOG = LoggerFactory.getLogger(PipelineWorker.class);
	
	/**
	 * Used data unit merger.
	 */
	private DataUnitMerger dataUnitMerger; 
	
	/**
	 * Access to the database
	 */
	protected DatabaseAccess database;	
	
	/**
	 * Manage mapping execution context into {@link #workDirectory}. 
	 */
	private ExecutionContextInfo contextInfo;	
	
	/**
	 * Application's configuraiton.
	 */
	private AppConfig appConfig;
	
	/**
	 * @param execution The pipeline execution record to run.
	 * @param moduleFacade Module facade for obtaining DPUs instances.
	 * @param eventPublisher Application event publisher.
	 * @param database Access to database.
	 */
	public PipelineWorker(PipelineExecution execution, ModuleFacade moduleFacade, 
			ApplicationEventPublisher eventPublisher, DatabaseAccess database,
			File workingDirectory, AppConfig appConfig) {
		this.execution = execution;
		this.moduleFacade = moduleFacade;
		this.eventPublisher = eventPublisher;
		this.contexts = new HashMap<>();
		// get working directory from pipelineExecution
		this.dataUnitMerger = new PrimitiveDataUnitMerger();
		this.database = database;
		// create or get existing .. 
		this.contextInfo = execution.createExecutionContext(workingDirectory);
		this.appConfig = appConfig;
		// TODO Petyr: Persist Iterator from DependecyGraph into ExecutionContext, and save into DB after every DPURecord (also save .. DataUnits .. )
		// TODO Petyr: Release context sooner then on the end of the execution
	}

	/**
	 * Called in case that the execution failed.
	 */
	private void executionFailed() {
		execution.setEnd(new Date());
		// set new state
		execution.setExecutionStatus(ExecutionStatus.FAILED);
		// save	into database
		database.getPipeline().save(execution);
		// 
		LOG.error("execution failed");
	}
	
	/**
	 * Called in case of successful execution.
	 */
	private void executionSuccessful() {
		execution.setEnd(new Date());
		// update state
		execution.setExecutionStatus(ExecutionStatus.FINISHED_SUCCESS);
		// save into database
		database.getPipeline().save(execution);
	}
	
	/**
	 * Do cleanup work after pipeline execution.
	 * Also delete the worker directory it the pipeline
	 * if not in debugMode.
	 */
	private void cleanUp() {
		LOG.debug("Clean up");
		// release all contexts 
		for (ProcessingContext item : contexts.values()) {
			if (item instanceof ExtendedContext) {
				ExtendedContext exCtx = (ExtendedContext)item;
				exCtx.release();
			} else {
				LOG.error("Unexpected ProcessingContext instance. Can't call release().");
			}	
		}
		// delete working folder ?
		if (execution.isDebugging()) {
			// do not delete anything
		} else {
			// try to delete the working directory
			try {
				FileUtils.deleteDirectory( contextInfo.getWorkingDirectory() );
			} catch (IOException e) {
				LOG.error("Can't delete directory after execution: " + execution.getId(), e);
			}
			// TODO Petyr: delete also directory with stored data units ?
		}			
	}
	
	/**
	 * Implementation of workers activity. Worker constantly keeps asking engine
	 * for jobs to run, until it is killed.
	 */
	@Override
	public void run() {		
		PipelineFacade pipelineFacade = database.getPipeline();
		
		// add marker to logs from this thread -> both must be specified !!
		MDC.put(LogMessage.MDPU_EXECUTION_KEY_NAME, Long.toString( execution.getId() ) );
		
		// get pipeline to run
		Pipeline pipeline = execution.getPipeline();

		// get dependency graph -> determine run order
		DependencyGraph dependencyGraph = null;
		
		// if in debug mode then pass the final DPU
		if(execution.isDebugging() && execution.getDebugNode() != null) {
			dependencyGraph = new DependencyGraph(pipeline.getGraph(), execution.getDebugNode());
		} else {
			dependencyGraph = new DependencyGraph(pipeline.getGraph());
		}
		LOG.debug("Started");

		// contextInfo is in pipeline so by saving pipeline we also save context
		pipelineFacade.save(execution);
		
		boolean executionFailed = false;
		// run DPUs ...
		for (Node node : dependencyGraph) {
			boolean result;
			
			// put dpuInstance id to MDC, so we can identify logs related to the dpuInstance
			MDC.put(LogMessage.MDC_DPU_INSTANCE_KEY_NAME, Long.toString(node.getDpuInstance().getId()) );
			try {
				result = runNode(node, dependencyGraph.getAncestors(node));
			} catch (ContextException e) {
				eventPublisher.publishEvent(new PipelineContextErrorEvent(e, node.getDpuInstance(), execution, this));				
				executionFailed = true;
				break;
			} catch (ModuleException e) {
				eventPublisher.publishEvent(new PipelineModuleErrorEvent(e, node.getDpuInstance(), execution, this));
				executionFailed = true;
				break;
			} catch (StructureException e) {
				eventPublisher.publishEvent(new PipelineStructureError(e, node.getDpuInstance(), execution, this));
				executionFailed = true;
				break;		
			} catch (DataUnitCreateException e) {
				// can't create DataUnit 
				eventPublisher.publishEvent(new PipelineFailedEvent(e, node.getDpuInstance(), execution, this));
				executionFailed = true;
				break;
			} catch (Exception e) {
				eventPublisher.publishEvent(new PipelineFailedEvent(e, node.getDpuInstance(),  execution, this));				
				executionFailed = true;
				break;
			}
			MDC.remove(LogMessage.MDC_DPU_INSTANCE_KEY_NAME);
				
			// TODO Petyr: save custom data .. !
			if (result) {
				// DPURecord executed successfully
				
				// save context after last execution
				pipelineFacade.save(execution);
				// also save new DataUnits
				ProcessingContext lastContext = contexts.get(node);
				if (lastContext instanceof ExtendedContext) {
					ExtendedContext exCtx = (ExtendedContext)lastContext;
					exCtx.save();
				} else {
					LOG.error("Unexpected ProcessingContext instance. Can't save context.");
				}
				
			} else {
				// error -> end pipeline
				executionFailed = true;
				eventPublisher.publishEvent(new PipelineFailedEvent("DPU execution failed.", node.getDpuInstance(), execution, this));
				// save data ?
				if (execution.isDebugging()) {
					// save new DataUnits
					ProcessingContext lastContext = contexts.get(node);
					if (lastContext instanceof ExtendedContext) {
						ExtendedContext exCtx = (ExtendedContext)lastContext;
						exCtx.save();
					} else {
						LOG.error("Unexpected ProcessingContext instance. Can't save context.");
					}
					// the context will be save at the end of the execution
				}
				// break execution
				break;	
			}
		}
		// ending ..
		LOG.debug("Finished");		
		// do clean up
		cleanUp();        
		// clear all threads markers		
		MDC.clear();
		// save context into DB
        if (executionFailed) {
			executionFailed();
		} else {
			executionSuccessful();
		}        
        // publish information for the rest of the application
        eventPublisher.publishEvent(new PipelineFinished(execution, this));
	}

	/**
	 * Return edge that connects the given nodes.
	 * @param source
	 * @param target
	 * @return Edge or null if there is no connection between the given nodes.
	 */
	private Edge getEdge(Node source, Node target) {
		for (Edge edge : execution.getPipeline().getGraph().getEdges()) {
			if (edge.getFrom() == source && edge.getTo() == target) {
				return edge;
			}
		}
		return null;
	}
	
	/**
	 * Return command associated with edge that connect given nodes.
	 * @param source
	 * @param target
	 * @return
	 */
	private String getCommandForEdge(Node source, Node target) {
		// try to get Edge that leads from item to node
		Edge edge = getEdge(source, target);			
		String command;
		if (edge == null) {
			// there is no edge for this connection ?
			command = "";
			LOG.error("Can't find edge between {} and {}",
					source.getDpuInstance().getName(),
					target.getDpuInstance().getName());
		} else {
			command = edge.getDataUnitName();
		}
		return command;
	}
	
	/**
	 * Return context that should be used when executing given Extractor.
	 * The context is also stored in {@link #contexts }
	 * 
	 * @param node Node for which DPURecord the context is.
	 * @param ancestors Ancestors of the given node.
	 * @return Context for the DPURecord execution.
	 * @throws ContextException 
	 * @throws StructureException 
	 * @throws IOException 
	 */		
	private ExtendedExtractContext getContextForNodeExtractor(Node node, 
			Set<Node> ancestors) throws ContextException, StructureException, IOException {
		DPUInstanceRecord dpuInstance = node.getDpuInstance();
		String contextId = "ex" + execution.getId() + "_dpu-" + dpuInstance.getId();
		// ...
		ExtendedExtractContext extractContext;
		extractContext = ContextFactory.create(contextId, execution, dpuInstance, 
				eventPublisher, contextInfo, ExtendedExtractContext.class, appConfig); 
		// store context
		contexts.put(node, extractContext);
		return extractContext;		
	}
	
	/**
	 * Return context that should be used when executing given Transform.
	 * The context is also stored in {@link #contexts }
	 * 
	 * @param node Node for which DPURecord the context is.
	 * @param ancestors Ancestors of the given node.
	 * @return Context for the DPURecord execution.
	 * @throws ContextException 
	 * @throws StructureException 
	 * @throws IOException 
	 */		
	private ExtendedTransformContext getContextForNodeTransform(Node node,
			Set<Node> ancestors) throws ContextException, StructureException, IOException {
		DPUInstanceRecord dpuInstance = node.getDpuInstance();
		String contextId = "ex" + execution.getId() + "_dpu-" + dpuInstance.getId();
		// ...
		ExtendedTransformContext transformContext;
		transformContext = ContextFactory.create(contextId, execution, dpuInstance, 
				eventPublisher, contextInfo, ExtendedTransformContext.class, appConfig);
		if (ancestors.isEmpty()) {
			// no ancestors ? -> error
			throw new StructureException("No inputs.");
		}
		for (Node item : ancestors) {
			// try to get Edge that leads from item to node
			String command = getCommandForEdge(item, node);
			// if there is context for given data ..
			if (contexts.containsKey(item)) {				
				transformContext.addSource(contexts.get(item), dataUnitMerger, command);
			} else {
				// can't find context ..
				throw new StructureException("Can't find context.");
			}
		}
		transformContext.sealInputs();
		// store context
		contexts.put(node, transformContext);
		return transformContext;		
	}
	
	/**
	 * Return context that should be used when executing given Loader.
	 * The context is also stored in {@link #contexts }
	 * 
	 * @param node Node for which DPURecord the context is.
	 * @param ancestors Ancestors of the given node.
	 * @return Context for the DPURecord execution.
	 * @throws ContextException 
	 * @throws StructureException 
	 * @throws IOException 
	 */	
	private ExtendedLoadContext getContextForNodeLoader(Node node, 
			Set<Node> ancestors) throws ContextException, StructureException, IOException {
		DPUInstanceRecord dpuInstance = node.getDpuInstance();
		String contextId = "ex" + execution.getId() + "_dpu-" + dpuInstance.getId();
		// ...
		ExtendedLoadContext loadContext;
		loadContext = ContextFactory.create(contextId, execution, dpuInstance, 
				eventPublisher, contextInfo, ExtendedLoadContext.class, appConfig);
		if (ancestors.isEmpty()) {
			// no ancestors ? -> error
			throw new StructureException("No inputs.");
		}				
		for (Node item : ancestors) {
			// try to get Edge that leads from item to node
			String command = getCommandForEdge(item, node);
			// if there is context for given data ..
			if (contexts.containsKey(item)) {
				loadContext.addSource(contexts.get(item), dataUnitMerger, command); 
			} else {
				// can't find context ..
				throw new StructureException("Can't find context.");
			}
		}	
		loadContext.sealInputs();
		// store context
		contexts.put(node, loadContext);		
		return loadContext;
	}

	/**
	 * @throws DataUnitCreateException 
	 * Executes a single DPURecord associated with given Node.
	 * 
	 * @param node
	 * @param ancestors Ancestors of the given node.
	 * @return false if execution failed
	 * @throws ContextException 
	 * @throws StructureException 
	 * @throws  
	 */
	private boolean runNode(Node node, Set<Node> ancestors) throws ContextException, StructureException, DataUnitCreateException {
		// prepare what we need to start the execution
		DPUInstanceRecord dpuInstanceRecord = node.getDpuInstance();
		
		// load instance
		try {
			dpuInstanceRecord.loadInstance(moduleFacade);
		} catch(FileNotFoundException e) {
			throw new StructureException("Failed to load instance of DPU from file.", e);
		} catch(ModuleException e) {
			throw new StructureException("Failed to create instance of DPU.", e);
		}
		
		// get instance
		Object dpuInstance = dpuInstanceRecord.getInstance();		
		Config configuration = dpuInstanceRecord.getConf();
		// set configuration
		if (dpuInstance instanceof Configurable<?>) {
			Configurable<Config> configurable = (Configurable<Config>)dpuInstance;			
			try {
				configurable.configure(configuration);
			} catch (ConfigException e) {
				throw new StructureException("Failed to configure DPU.", e);
			}
		}
		// run dpu instance
		if (dpuInstance instanceof Extract) {
			Extract extractor = (Extract)dpuInstance;
			
			ExtendedExtractContext context;
			try {
				context = getContextForNodeExtractor(node, ancestors);
			} catch (IOException e) {
				throw new StructureException("Failed to create context.", e);
			}
			
			return runExtractor(extractor, context);			
		} else if (dpuInstance instanceof Transform) {
			Transform transformer = (Transform)dpuInstance;
			
			ExtendedTransformContext context;
			try {
				context = getContextForNodeTransform(node, ancestors);
			} catch (IOException e) {
				throw new StructureException("Failed to create context.", e);
			}
			
			return runTransformer(transformer, context);
		} else if (dpuInstance instanceof Load) {
			Load loader = (Load)dpuInstance;
			
			ExtendedLoadContext context;
			try {
				context = getContextForNodeLoader(node, ancestors);
			} catch (IOException e) {
				throw new StructureException("Failed to create context.", e);
			}
			
			return runLoader(loader, context);
		} else {
			throw new RuntimeException("Unknown DPURecord type.");
		}
	}

	/**
	 * Runs a single extractor DPURecord module.
	 * 
	 * @param extractor
	 * @param ctx
	 * @return false if execution failed
	 * @throws DataUnitCreateException 
	 */
	private boolean runExtractor(Extract extractor, ExtendedExtractContext ctx) throws DataUnitCreateException {

		eventPublisher.publishEvent(new ExtractStartEvent(extractor, ctx, this));
		
		try {
			extractor.extract(ctx);
			eventPublisher.publishEvent(new ExtractCompletedEvent(extractor, ctx, this));
		} catch (ExtractException ex) {
			eventPublisher.publishEvent(new ExtractFailedEvent(ex, extractor, ctx, this));
			return false;
		}
		return true;
	}

	/**
	 * Runs a single Transformer DPURecord module.
	 * 
	 * @param transformer
	 * @param ctx
	 * @return false if execution failed
	 * @throws DataUnitCreateException 
	 */
	private boolean runTransformer(Transform transformer, ExtendedTransformContext ctx) throws DataUnitCreateException {

		eventPublisher.publishEvent(new TransformStartEvent(transformer, ctx, this));
		
		try {
			transformer.transform(ctx);
			eventPublisher.publishEvent(new TransformCompletedEvent(transformer, ctx, this));
		} catch (TransformException ex) {
			eventPublisher.publishEvent(new TransformFailedEvent(ex, transformer, ctx, this));
			return false;
		}
		return true;
	}

	/**
	 * Runs a single Loader DPURecord module.
	 * 
	 * @param loader
	 * @param ctx
	 * @return false if execution failed
	 * @throws DataUnitCreateException 
	 */
	private boolean runLoader(Load loader, ExtendedLoadContext ctx) throws DataUnitCreateException {
		
		eventPublisher.publishEvent(new LoadStartEvent(loader, ctx, this));
		
		try {
			loader.load(ctx);
			eventPublisher.publishEvent(new LoadCompletedEvent(loader, ctx, this));
		} catch (LoadException ex) {
			eventPublisher.publishEvent(new LoadFailedEvent(ex, loader, ctx, this));
			return false;
		}
		return true;
	}
}
