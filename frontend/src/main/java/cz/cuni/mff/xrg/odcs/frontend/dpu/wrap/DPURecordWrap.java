package cz.cuni.mff.xrg.odcs.frontend.dpu.wrap;

import com.vaadin.ui.UI;
import java.io.FileNotFoundException;

import cz.cuni.mff.xrg.odcs.commons.app.dpu.DPURecord;
import cz.cuni.mff.xrg.odcs.commons.app.facade.ModuleFacade;
import cz.cuni.mff.xrg.odcs.commons.app.module.ModuleException;
import cz.cuni.mff.xrg.odcs.commons.configuration.ConfigException;
import cz.cuni.mff.xrg.odcs.commons.configuration.DPUConfigObject;
import cz.cuni.mff.xrg.odcs.commons.web.AbstractConfigDialog;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogContext;
import cz.cuni.mff.xrg.odcs.commons.web.ConfigDialogProvider;
import cz.cuni.mff.xrg.odcs.frontend.AppEntry;
import cz.cuni.mff.xrg.odcs.frontend.dpu.dialog.ConfigDialogContextImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class wrap {@line DPURecord} and provide functions that enable easy work with
 * configuration and configuration dialog.
 *
 * @author Petyr
 *
 */
class DPURecordWrap {

	private static final Logger LOG = LoggerFactory.getLogger(
			DPURecordWrap.class);
	
	/**
	 * Wrapped DPU.
	 */
	private DPURecord dpuRecord = null;
		
	/**
	 * DPU's configuration dialog.
	 */
	private AbstractConfigDialog<DPUConfigObject> configDialog = null;

	/**
	 * True if represents the template.
	 */
	private final boolean isTemplate;
	
	/**
	 * True if the {@link #configuredDialog()} has been called.
	 */
	private boolean dialogConfigured = false;
	
	protected DPURecordWrap(DPURecord dpuRecord, boolean isTemplate) {
		this.dpuRecord = dpuRecord;
		this.isTemplate = isTemplate;
	}

	/**
	 * Try to save configuration from {@link #configDialog} into
	 * {@link #dpuRecord}. If the {@link #configDialog} is null nothing happen.
	 * This function does not save data into database.
	 *
	 * @throws ConfigException
	 */
	public void saveConfig() throws ConfigException, DPUWrapException {
		if (configDialog == null) {
			return;
		}
		try {
			final String config = configDialog.getConfig();
			dpuRecord.setRawConf(config);
		} catch (ConfigException e) {
			throw e;
		} catch (Throwable e) {
			throw new DPUWrapException("Failed to save configuration.", e);
		}
	}

	/**
	 * Return configuration dialog for wrapped DPU. The configuration is not
	 * set. To set dialog configuration call {@link #configuredDialog}
	 *
	 * @return configuration dialog for wrapped DPU
	 * @throws ModuleException
	 * @throws FileNotFoundException
	 */
	public AbstractConfigDialog<DPUConfigObject> getDialog()
			throws ModuleException, FileNotFoundException, DPUWrapException {
		// load configuration dialog
		try {
			loadConfigDialog();
		} catch(ModuleException | FileNotFoundException e) {
			throw e;
		} catch (Throwable e) {
			throw new DPUWrapException("Failed to load dialog.", e);
		}
		return configDialog;
	}

	/**
	 * If respective configuration dialog for wrapped DPU exist, then set it's
	 * configuration. Otherwise do nothing.
	 *
	 * @throws ConfigException
	 */
	public void configuredDialog()
			throws ConfigException, DPUWrapException {
		// set dialog configuration
		try {
			loadConfigIntoDialog();
		} catch (ConfigException e) {
			throw e;
		} catch (Throwable e) {
			throw new DPUWrapException("Failed to configure dpu's dialog.", e);
		}
	}

	/**
	 * Check if the configuration in configuration dialog has been changed.
	 * The configuration is assumed to be changed if it satisfy all the 
	 * following conditions:
	 * <ul>
	 * <li>DPU has configuration dialog.</li>
	 * <li>The dialog has been obtained by calling {@link #getDialog().</li>
	 * <li>The dialog has been configurated by calling {@link #configuredDialog()}</li>
	 * </ul>
	 * @return True if the configuration changed.
	 */
	public boolean hasConfigChanged() throws DPUWrapException {
		if (configDialog == null || !dialogConfigured) {
			return false;
		}
		// ok we satisfy necesary conditions, we may ask the dialog 
		// for changes
		try {
			final boolean isChanged = configDialog.hasConfigChanged();
			return isChanged;
		} catch (Exception ex) {
			throw new DPUWrapException("Configuration dialog throws an exception.", ex);
		}
	}
	
	/**
	 * Load the configuration dialog for {@link #dpuRecord} and store it into
	 * {@link #configDialog}. If the dialog is already loaded
	 * ({@link #configDialog} is not null) then nothing is done. If the
	 * {@link #dpuRecord} does not provide configuration dialog set
	 * {@link #configDialog} to null.
	 * Can possibly emit runtime exception.
	 * 
	 * @throws ModuleException
	 * @throws FileNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private void loadConfigDialog() throws ModuleException, FileNotFoundException {
		if (configDialog == null) {
			// continue and load the dialog
		} else {
			// already loaded .. 
			return;
		}
		// first we need load instance of the DPU
		dpuRecord.loadInstance(((AppEntry)UI.getCurrent()).getBean(ModuleFacade.class));
		Object instance = dpuRecord.getInstance();
		// now try to load the dialog
		if (instance instanceof ConfigDialogProvider<?>) {
			ConfigDialogProvider<DPUConfigObject> dialogProvider;
			// 'unchecked casting' .. we check type in condition above
			dialogProvider = (ConfigDialogProvider<DPUConfigObject>) instance;
			// get configuration dialog
			configDialog = dialogProvider.getConfigurationDialog();
			if (configDialog != null) {
				// setup the dialog
				final ConfigDialogContext context 
						= new ConfigDialogContextImpl(isTemplate);
				configDialog.setContext(context);
			}
		} else {
			// no configuration dialog
			configDialog = null;
		}

	}

	/**
	 * Try to load configuration from {@link #dpuRecord} into
	 * {@link #configDialog}. Can possibly emit runtime exception.
	 *
	 * @throws ConfigException
	 */
	private void loadConfigIntoDialog() throws ConfigException {
		if (configDialog == null) {
			// no dialog .. nothing to do 
			return;
		}
		// we try to configure the dialog
		dialogConfigured = true;
		configDialog.setConfig(dpuRecord.getRawConf());
	}
	
}
