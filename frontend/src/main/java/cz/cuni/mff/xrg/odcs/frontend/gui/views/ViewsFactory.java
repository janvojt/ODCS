package cz.cuni.mff.xrg.odcs.frontend.gui.views;

import cz.cuni.mff.xrg.odcs.frontend.gui.views.executionmonitor.ExecutionMonitor;
import com.vaadin.navigator.View;

import cz.cuni.mff.xrg.odcs.frontend.gui.ViewComponent;
import cz.cuni.mff.xrg.odcs.frontend.gui.ViewNames;

/**
 * Factory for classes in {@link cz.cuni.mff.xrg.odcs.frontend.gui.views} package.
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
		case INITIAL:
			return new Initial();
		case ADMINISTRATOR:
			return new Settings();
		case DATA_BROWSER:
			return new DataBrowser();
		case DPU:
			return new DPU();
		case EXECUTION_MONITOR:
			return new ExecutionMonitor();
		case PIPELINE_EDIT:
		case PIPELINE_EDIT_NEW:
			return new PipelineEdit();
		case PIPELINE_LIST:
			return new PipelineList();
		case SCHEDULER:
			return new Scheduler();
                case LOGIN:
                        return new Login();
		default:
			return null;
		}
		
	}
	
	public static String getViewName(View view) {
		if(view.getClass() == PipelineList.class) {
			return PipelineList.NAME;
		} else if(view.getClass() == DPU.class) {
			return DPU.NAME;
		} else {
			return Initial.NAME;
		}
	}
	
}