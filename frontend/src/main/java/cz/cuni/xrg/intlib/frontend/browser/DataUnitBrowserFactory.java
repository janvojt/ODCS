package cz.cuni.xrg.intlib.frontend.browser;

import java.io.File;

import cz.cuni.xrg.intlib.commons.app.dpu.DPUInstance;
import cz.cuni.xrg.intlib.commons.app.execution.ExecutionContextReader;
import cz.cuni.xrg.intlib.commons.data.DataUnitType;

/**
 * Factory for DataUnitBrowsers.
 * 
 * @author Petyr
 *
 */
public class DataUnitBrowserFactory {

	/**
	 * Return browser for specified DataUnit.
	 * @param context The pipelineExecution context.
	 * @param dpuInstance Owner of DataUnit.
	 * @param dataUnitIndex Index of data unit.
	 * @return
	 * @throws DataUnitNotFoundException
	 * @throws BrowserInitFailedException 
	 */
	static DataUnitBrowser getBrowser(ExecutionContextReader context, DPUInstance dpuInstance, int dataUnitIndex)
		throws DataUnitNotFoundException, BrowserInitFailedException{
		// get type and directory
		DataUnitType type = context.getTypeForDataUnit(dpuInstance, dataUnitIndex);
		if (type == null) {
			// the context doesn't exist
			throw new DataUnitNotFoundException();
		}		
		File directory = context.getDirectoryForDataUnit(dpuInstance, dataUnitIndex);
		// TODO Petyr : return some component like "The data unit context can't be read ... " 
		switch(type) {
		case RDF_Local:
			DataUnitBrowser localRdfBrowser = new LocalRdfBrowser();			
			try {
				localRdfBrowser.loadDataUnit(directory);
			} catch (Exception e) {
				throw new BrowserInitFailedException(e);
			}
			return localRdfBrowser;
		case RDF_Virtuoso:
			return null;
		default:
			return null;
		}
	}
	
}
