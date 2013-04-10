package cz.cuni.xrg.intlib.frontend.gui.components.pipelinecanvas;


import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

import cz.cuni.xrg.intlib.auxiliaries.App;
import cz.cuni.xrg.intlib.commons.app.dpu.DPU;
import cz.cuni.xrg.intlib.commons.app.dpu.DPUInstance;
import cz.cuni.xrg.intlib.commons.app.pipeline.Pipeline;
import cz.cuni.xrg.intlib.commons.app.pipeline.graph.Edge;
import cz.cuni.xrg.intlib.commons.app.pipeline.graph.Node;
import cz.cuni.xrg.intlib.commons.app.pipeline.graph.PipelineGraph;
import cz.cuni.xrg.intlib.frontend.gui.components.DPUDetail;


/**
 * Component for visualization of the pipeline.
 * @author Bogo
 */
@SuppressWarnings("serial")
@JavaScript({ "js_pipelinecanvas.js", "kinetic-v4.3.3.min.js" })
public class PipelineCanvas extends AbstractJavaScriptComponent {

	int dpuCount = 0;
	int connCount = 0;

	private Pipeline pipeline;
	private PipelineGraph graph;

	public PipelineCanvas() {

		this.pipeline = App.getApp().getPipelines().createPipeline();
		this.graph = this.pipeline.getGraph();

		this.setId("container");
		//this.setWidth(1500,  Unit.PIXELS);
		//this.setHeight(800, Unit.PIXELS);
		this.setStyleName("pipelineContainer");

		registerRpc(new PipelineCanvasServerRpc() {

			@Override
			public void onDetailRequested(int dpuId) {
				// TODO Auto-generated method stub
				// propably publish event one level higher
                DPUInstance dpu = pipeline.getGraph().getDPUInstanceById(dpuId);
                if(dpu != null) {
                    showDPUDetail(dpu);
                }
			}

			@Override
			public void onConnectionRemoved(int connectionId) {
				graph.removeEdge(connectionId);
			}

			@Override
			public void onConnectionAdded(int dpuFrom, int dpuTo) {
				addConnection(dpuFrom, dpuTo);
			}

			@Override
			public void onDpuRemoved(int dpuId) {
				graph.removeDpu(dpuId);

			}
		});
	}

	public void init() {
        getRpcProxy(PipelineCanvasClientRpc.class).init();
    }

	public void addDpu(DPU dpu) {
		int dpuInstanceId = graph.addDpu(dpu);
		getRpcProxy(PipelineCanvasClientRpc.class).addNode(dpuInstanceId, dpu.getName(), dpu.getDescription(), -5, -5);
	}

	public void addConnection(int dpuFrom, int dpuTo) {
		int connectionId = graph.addEdge(dpuFrom, dpuTo);
		getRpcProxy(PipelineCanvasClientRpc.class).addEdge(connectionId, dpuFrom, dpuTo);
	}

	//OR loadPipeline
	public void showPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
		for(Node node : pipeline.getGraph().getNodes()) {
			getRpcProxy(PipelineCanvasClientRpc.class).addNode(node.getId(), node.getDpuInstance().getName(), node.getDpuInstance().getDescription(), node.getPosition().getX(), node.getPosition().getY());
		}
		for(Edge edge : pipeline.getGraph().getEdges()) {
			getRpcProxy(PipelineCanvasClientRpc.class).addEdge(edge.getId(), edge.getFrom().getId(), edge.getTo().getId());
		}
	}

	public Pipeline getPipeline() {
//		for(DpuInstance dpu : pipeline.getDpus()) {
//			int[] position = getRpcProxy(PipelineCanvasClientRpc.class).getDpuPosition(dpu.Id);
//			dpu.setX(position[0]);
//			dpu.setY(position[1]);
//		}
		//pipeline.setWidth(Math.round(this.getWidth()));
		//pipeline.setHeight(Math.round(this.getHeight()));

		return pipeline;
	}

	@Override
	  protected PipelineCanvasState getState() {
	    return (PipelineCanvasState) super.getState();
	  }

    /**
     * Shows detail of given DPUInstance in new sub-window
     *
     * @param dpu
     */
    public void showDPUDetail(DPUInstance dpu) {
        DPUDetail detailDialog = new DPUDetail(dpu);
        App.getApp().addWindow(detailDialog);

    }
}
