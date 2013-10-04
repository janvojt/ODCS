package cz.cuni.mff.xrg.odcs.commons.module.config;

import cz.cuni.mff.xrg.odcs.commons.configuration.DPUConfigObject;

/**
 * Base class for DPU's configuration. Provide default implementation 
 * of {@link #isValid()} method.
 * 
 * @author Petyr
 *
 */
public class DPUConfigObjectBase implements DPUConfigObject {

	@Override
	public boolean isValid() {
		return true;
	}

}