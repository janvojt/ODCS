package cz.cuni.mff.xrg.odcs.commons.app.facade;

import cz.cuni.mff.xrg.odcs.commons.app.auth.AuthenticationContext;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPUType;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DbDPUInstanceRecord;
import cz.cuni.mff.xrg.odcs.commons.app.dpu.DbDPUTemplateRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.DbMessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.execution.message.MessageRecord;
import cz.cuni.mff.xrg.odcs.commons.app.pipeline.PipelineExecution;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade for working with DPUs.
 *
 * @author Jan Vojt
 */
@Transactional(readOnly = true)
class DPUFacadeImpl implements DPUFacade {

	private static final Logger LOG = LoggerFactory.getLogger(DPUFacadeImpl.class);

	@Autowired
	private DbDPUTemplateRecord templateDao;
	
	@Autowired
	private DbDPUInstanceRecord instanceDao;
	
	@Autowired
	private DbMessageRecord messageDao;
	
	@Autowired(required = false)
	private AuthenticationContext authCtx;

	/* ******************* Methods for DPUTemplateRecord management *********************** */
	
	/**
	 * Creates DPU template with given name of given type and assigns currently
	 * logged in user as owner.
	 * 
	 * @param name
	 * @param type
	 * @return newly created DPU template
	 */
	@Override
	public DPUTemplateRecord createTemplate(String name, DPUType type) {
		DPUTemplateRecord dpu = new DPUTemplateRecord(name, type);
		if (authCtx != null) {
			dpu.setOwner(authCtx.getUser());
		}
		return dpu;
	}

	/**
	 * Create copy of DPU template, as the owner the current user is set.
	 * 
	 * @param original
	 * @return
	 */
	@Override
	public DPUTemplateRecord createCopy(DPUTemplateRecord original) {
		DPUTemplateRecord copy = new DPUTemplateRecord(original);
		if (authCtx != null) {
			copy.setOwner(authCtx.getUser());
		}
		return copy;
	}
	
	/**
	 * Creates a new DPURecord with the same properties and configuration as in given
	 * {@link DPUInstance}. Note that newly created DPURecord is only returned, but
	 * not managed by database. To persist it, {@link #save(DPURecord)} must be called
	 * explicitly.
	 *
	 * @param instance
	 * @return new DPURecord
	 */
	@Override
	public DPUTemplateRecord createTemplateFromInstance(DPUInstanceRecord instance) {
		DPUTemplateRecord template = new DPUTemplateRecord(instance);
                if(authCtx != null) {
                    template.setOwner(authCtx.getUser());
                }
		if(instance.getTemplate().getParent() == null) {
			template.setParent(instance.getTemplate());
		} else {
			template.setParent(instance.getTemplate().getParent());
		}
		return template;
	}

	/**
	 * Returns list of all DPUTemplateRecords currently persisted in database.
	 * @return DPURecord list
	 */
	@PostFilter("hasPermission(filterObject,'view')")
	@Override
	public List<DPUTemplateRecord> getAllTemplates() {
		return templateDao.getAll();
	}
	
	/**
	 * Find DPUTemplateRecord in database by ID and return it.
	 * @param id
	 * @return
	 */
	@Override
	public DPUTemplateRecord getTemplate(long id) {
		return templateDao.getInstance(id);
	}
	
	/**
	 * Saves any modifications made to the DPUTemplateRecord into the database.
	 * 
	 * @param dpu
	 */
	@Transactional
	@PreAuthorize("hasPermission(#dpu,'save')")
	@Override
	public void save(DPUTemplateRecord dpu) {
		templateDao.save(dpu);
	}
	
	/**
	 * Deletes DPUTemplateRecord from the database.
	 * @param dpu
	 */
	@Transactional
	@PreAuthorize("hasPermission(#dpu,'delete')")
	@Override
	public void delete(DPUTemplateRecord dpu) {
		templateDao.delete(dpu);
	}

	/**
	 * Fetch all child DPU templates for a given DPU template.
	 * 
	 * @param parent DPU template
	 * @return list of child DPU templates or empty collection
	 */
	@Override
	public List<DPUTemplateRecord> getChildDPUs(DPUTemplateRecord parent) {
		return templateDao.getChilds(parent);
	}

	/* **************** Methods for DPUInstanceRecord Instance management ***************** */

	/**
	 * Creates DPUInstanceRecord with configuration copied from template without
	 * persisting it.
	 *
	 * @param dpuTemplate to create from
	 * @return newly created DPU instance
	 */
	@Override
	public DPUInstanceRecord createInstanceFromTemplate(DPUTemplateRecord dpuTemplate) {
		DPUInstanceRecord dpuInstance = new DPUInstanceRecord(dpuTemplate);		
		return dpuInstance;
	}

	/**
	 * Returns list of all DPUInstanceRecord currently persisted in database.
	 *
	 * @return DPUInstance list
	 */
	@Override
	public List<DPUInstanceRecord> getAllDPUInstances() {
		return instanceDao.getAll();
	}

	/**
	 * Find DPUInstanceRecord in database by ID and return it.
	 *
	 * @param id
	 * @return
	 */
	@Override
	public DPUInstanceRecord getDPUInstance(long id) {
		return instanceDao.getInstance(id);
	}

	/**
	 * Saves any modifications made to the DPUInstanceRecord into the database.
	 * @param dpu
	 */
	@Transactional
	@Override
	public void save(DPUInstanceRecord dpu) {
		instanceDao.save(dpu);
	}

	/**
	 * Deletes DPUInstance from the database.
	 * @param dpu
	 */
	@Transactional
	@Override
	public void delete(DPUInstanceRecord dpu) {
		instanceDao.delete(dpu);
	}

	/* **************** Methods for Record (messages) management ***************** */

	/**
	 * Fetches all DPURecords emitted by given PipelineExecution.
	 *
	 * @param pipelineExec
	 * @return
	 */
	@Override
	public List<MessageRecord> getAllDPURecords(PipelineExecution pipelineExec) {
		return messageDao.getAll(pipelineExec);
	}

	/**
	 * Find Record in database by ID and return it.
	 *
	 * @param id
	 * @return
	 */
	@Override
	public MessageRecord getDPURecord(long id) {
		return messageDao.getInstance(id);
	}

	/**
	 * Saves any modifications made to the Record into the database.
	 *
	 * @param record
	 */
	@Transactional
	@Override
	public void save(MessageRecord record) {
		messageDao.save(record);
	}

	/**
	 * Deletes Record from the database.
	 *
	 * @param record
	 */
	@Transactional
	@Override
	public void delete(MessageRecord record) {
		messageDao.delete(record);
	}
	
}
