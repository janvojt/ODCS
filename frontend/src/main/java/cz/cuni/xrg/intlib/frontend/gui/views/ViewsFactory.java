package cz.cuni.xrg.intlib.frontend.gui.views;

import cz.cuni.xrg.intlib.frontend.gui.ViewComponent;
import cz.cuni.xrg.intlib.frontend.gui.ViewNames;

/**
 * Factory for classes in {@link cz.cuni.xrg.intlib.frontend.gui.views} package.
 * 
 * @author Petyr
 *
 */
public class ViewsFactory {
	
	/**
	 * Create and return view. 
	 * @param view Determine view.
	 * @return View or null in case of bad view.
	 */
	public static ViewComponent create(ViewNames view) {
		switch(view) {
		case Initial:
			return new Initial();
		case Administrator:
			return new Administrator();
		case DataBrowser:
			return new DataBrowser();
		case DPU:
			return new DPU();
		case ExecutionMonitor:
			return new ExecutionMonitor();
		case PipelineEdit:
		case PipelineEdit_New:
			return new PipelineEdit();
		case PipelineList:
			return new PipelineList();
		case Scheduler:
			return new Scheduler();
		default:
			return null;
		}
		
	}
	
}