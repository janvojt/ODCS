/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.xrg.intlib.frontend.gui.components;

import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.datefield.Resolution;
import cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.CANCELLED;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.FAILED;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.FINISHED_SUCCESS;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.FINISHED_WARNING;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.RUNNING;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.SCHEDULED;
import java.util.Locale;
import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

/**
 * Default {@link FilterDecorator} to be used in tables. Extend this class and
 * override needed methods for customizing.
 *
 * @author Bogo
 */
public class IntlibFilterDecorator implements FilterDecorator {

	@Override
	public String getEnumFilterDisplayName(Object propertyId, Object value) {
		return value.toString();
	}

	@Override
	public Resource getEnumFilterIcon(Object propertyId, Object value) {
		return null;
	}

	@Override
	public String getBooleanFilterDisplayName(Object propertyId, boolean value) {
		if (value) {
			return "True";
		} else {
			return "False";
		}
	}

	@Override
	public Resource getBooleanFilterIcon(Object propertyId, boolean value) {
		return null;
	}

	@Override
	public boolean isTextFilterImmediate(Object propertyId) {
		return true;
	}

	@Override
	public int getTextChangeTimeout(Object propertyId) {
		return 500;
	}

	@Override
	public String getFromCaption() {
		return "From";
	}

	@Override
	public String getToCaption() {
		return "To";
	}

	@Override
	public String getSetCaption() {
		return "Set";
	}

	@Override
	public String getClearCaption() {
		return "Clear";
	}

	@Override
	public Resolution getDateFieldResolution(Object propertyId) {
		return Resolution.DAY;
	}

	@Override
	public String getDateFormatPattern(Object propertyId) {
		return "dd.MM.yyyy";
	}

	@Override
	public Locale getLocale() {
		return Locale.getDefault();
	}

	@Override
	public String getAllItemsVisibleString() {
		return "ALL";
	}

	@Override
	public NumberFilterPopupConfig getNumberFilterPopupConfig() {
		NumberFilterPopupConfig config = new NumberFilterPopupConfig();
		return config;
	}

	@Override
	public boolean usePopupForNumericProperty(Object propertyId) {
		return true;
	}
}
