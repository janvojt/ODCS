package cz.cuni.xrg.intlib.frontend.auxiliaries;

import java.util.List;

import com.vaadin.data.Container;
import com.vaadin.data.util.BeanContainer;

import cz.cuni.xrg.intlib.commons.app.dpu.DPU;
import cz.cuni.xrg.intlib.commons.app.execution.Record;
import cz.cuni.xrg.intlib.commons.app.pipeline.Pipeline;
import cz.cuni.xrg.intlib.commons.app.rdf.RDFTriple;

/**
 * Class support creating vaadin container from List<?>.
 * @author Petyr
 *
 */
public class ContainerFactory {

	/**
	 * Prevent from creating instance.
	 */
	private ContainerFactory() {

	}

	/**
	 * Create container for Pipelines and fill it with given data.
	 * @param data data for container
	 * @return
	 */
	public static Container CreatePipelines(List<Pipeline> data) {
		BeanContainer<Integer, Pipeline> container = new BeanContainer<>( Pipeline.class );
		// set container id
		container.setBeanIdProperty("id");

		for (Pipeline item : data) {
			container.addBean(item);
		}
		return container;
	}

	/**
	 * Create container for DPUs and fill it with given data.
	 * @param data data for container
	 * @return
	 */
	public static Container CreateDPUs(List<DPU> data) {
		BeanContainer<Integer, DPU> container = new BeanContainer<>( DPU.class );
		// set container id
		container.setBeanIdProperty("id");

		for (DPU item : data) {
			container.addBean(item);
		}
		return container;
	}

	public static Container CreateExecutionMessages(List<Record> data) {
		BeanContainer<Integer, Record> container = new BeanContainer<>( Record.class);
		container.setBeanIdProperty("id");
		container.addAll(data);

		return container;
	}

	public static Container CreateRDFData(List<RDFTriple> data) {
		BeanContainer<Integer, RDFTriple> container = new BeanContainer<> (RDFTriple.class);
		container.setBeanIdProperty("id");
		container.addAll(data);

		return container;
	}



}