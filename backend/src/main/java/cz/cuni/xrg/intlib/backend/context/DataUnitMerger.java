package cz.cuni.xrg.intlib.backend.context;

import java.util.List;

import cz.cuni.xrg.intlib.backend.data.DataUnitFactory;
import cz.cuni.xrg.intlib.commons.data.DataUnit;

/**
 * Basic interface for data merger.
 * 
 * @author Petyr
 *
 */
public interface DataUnitMerger {

	/**
	 * Merge the data, the result is store in 'left'. If 
	 * the two Lists of DataUnits can't be merge throw ContextException.
	 * 
	 * @param left List of DataUnits, results. Can already contain some DataUnits. 
	 * @param right Source of DataUnits, do not change!
	 * @param DataUnitFactory Factory used to create new DataUnits.
	 * @param instruction Instruction for merger. 
	 * See {@link cz.cuni.xrg.intlib.commons.app.execution.DataUnitMergerInstructions}
	 * @throw ContextException
	 */
	public void merger(List<DataUnit> left,	List<DataUnit> right, 
			DataUnitFactory factory, String instruction) throws ContextException;
	
}
