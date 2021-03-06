package cz.cuni.mff.xrg.odcs.commons.module.dpu;

import cz.cuni.mff.xrg.odcs.commons.dpu.DPU;

/**
 * Base class for DPUs without configuration and configuration dialog. Use this
 * for simple-testing DPU's.
 *  
 * @author Petyr
 *
 */
public abstract class NonConfigurableBase implements DPU {
	
	@Override
	public void cleanUp() {	}

}
