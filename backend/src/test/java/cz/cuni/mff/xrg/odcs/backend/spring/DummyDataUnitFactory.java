package cz.cuni.mff.xrg.odcs.backend.spring;

import static org.mockito.Mockito.mock;

import java.io.File;

import cz.cuni.mff.xrg.odcs.backend.data.DataUnitFactory;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitCreateException;
import cz.cuni.mff.xrg.odcs.commons.data.DataUnitType;
import cz.cuni.mff.xrg.odcs.commons.data.ManagableDataUnit;

/**
 * Dummy {@link DataUnitFactory}. Does not create any real data unit.
 * @author Petyr
 *
 */
public class DummyDataUnitFactory extends DataUnitFactory {

	@Override
	public ManagableDataUnit create(DataUnitType type,
			String id,
			String name,
			File directory) throws DataUnitCreateException {
		// just return mocked object
		return mock(ManagableDataUnit.class);
	}
	
}
