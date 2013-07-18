package cz.cuni.xrg.intlib.frontend.gui.views;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.*;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.CANCELLED;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.FAILED;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.FINISHED_SUCCESS;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.FINISHED_WARNING;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.RUNNING;
import static cz.cuni.xrg.intlib.commons.app.execution.ExecutionStatus.SCHEDULED;

import cz.cuni.xrg.intlib.commons.app.execution.PipelineExecution;
import cz.cuni.xrg.intlib.commons.app.execution.Record;
import cz.cuni.xrg.intlib.frontend.auxiliaries.App;
import cz.cuni.xrg.intlib.frontend.gui.ViewComponent;
import cz.cuni.xrg.intlib.frontend.gui.components.*;
import java.util.Locale;
import org.tepi.filtertable.FilterDecorator;
import org.tepi.filtertable.numberfilter.NumberFilterPopupConfig;

/**
 * @author Maria Kukhar
 */
class ExecutionMonitor extends ViewComponent implements ClickListener {

	@AutoGenerated
	private VerticalLayout monitorTableLayout;
	private VerticalLayout logLayout;
	private HorizontalSplitPanel hsplit;
	private Panel mainLayout;
	@AutoGenerated
	private Label label;
	private IntlibPagedTable monitorTable;
	private IndexedContainer tableData;
	private Long exeId;
	private String pipeName;
	int style = DateFormat.MEDIUM;
	static String[] visibleCols = new String[]{"date", "name", "user",
		"status", "debug", "obsolete", "actions", "report"};
	static String[] headers = new String[]{"Date", "Name", "User", "Status",
		"Debug", "Obsolete", "Actions", "Report"};

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */
	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	public ExecutionMonitor() {
	}
	private DateFormat localDateFormat = null;

	@AutoGenerated
	private Panel buildMainLayout() {
		// common part: create layout

		mainLayout = new Panel("");

		hsplit = new HorizontalSplitPanel();
		mainLayout.setContent(hsplit);

		monitorTableLayout = new VerticalLayout();
		monitorTableLayout.setImmediate(true);
		monitorTableLayout.setMargin(true);
		monitorTableLayout.setSpacing(true);
		monitorTableLayout.setWidth("100%");
		monitorTableLayout.setHeight("100%");


		// top-level component properties

		setWidth("100%");
		setHeight("100%");

		HorizontalLayout topLine = new HorizontalLayout();
		topLine.setSpacing(true);
		topLine.setWidth(100, Unit.PERCENTAGE);
		
		Button refreshButton = new Button("Refresh", new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				refreshData();
				monitorTable.setVisibleColumns(visibleCols);

			}
		});
		topLine.addComponent(refreshButton);
		topLine.setComponentAlignment(refreshButton, Alignment.MIDDLE_RIGHT);

		Button buttonDeleteFilters = new Button();
		buttonDeleteFilters.setCaption("Clear Filters");
		buttonDeleteFilters.setHeight("25px");
		buttonDeleteFilters.setWidth("110px");
		buttonDeleteFilters
				.addClickListener(new com.vaadin.ui.Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				monitorTable.resetFilters();

			}
		});
		topLine.addComponent(buttonDeleteFilters);
		topLine.setComponentAlignment(buttonDeleteFilters, Alignment.MIDDLE_RIGHT);
		
		Label topLineFiller = new Label();
		topLine.addComponentAsFirst(topLineFiller);
		topLine.setExpandRatio(topLineFiller, 1.0f);
		monitorTableLayout.addComponent(topLine);

		tableData = getTableData(App.getApp().getPipelines().getAllExecutions());



		monitorTable = new IntlibPagedTable();
		monitorTable.setSelectable(true);
		monitorTable.setContainerDataSource(tableData);
		monitorTable.setWidth("100%");
		monitorTable.setHeight("100%");
		monitorTable.setImmediate(true);
		monitorTable.setVisibleColumns(visibleCols); // Set visible columns
		monitorTable.setColumnHeaders(headers);

		Object property = "date";

		monitorTable.setSortContainerPropertyId(property);
		monitorTable.setSortAscending(false);
		monitorTable.sort();

		//"date", "name", "user",
		//"status", "debug", "obsolete", "actions", "report"
//		monitorTable.setColumnExpandRatio("date", 2);
//		monitorTable.setColumnExpandRatio("name", 4);
//		monitorTable.setColumnExpandRatio("user", 2);
//		monitorTable.setColumnExpandRatio("status", 1);
//		monitorTable.setColumnExpandRatio("debug", 1);
//		monitorTable.setColumnExpandRatio("obsolete", 1);
//		monitorTable.setColumnExpandRatio("actions", 2);
//		monitorTable.setColumnExpandRatio("report", 2);

		monitorTable.addGeneratedColumn("status", new CustomTable.ColumnGenerator() {
			@Override
			public Object generateCell(CustomTable source, Object itemId,
					Object columnId) {
				ExecutionStatus type = (ExecutionStatus) source.getItem(itemId)
						.getItemProperty(columnId).getValue();
				ThemeResource img = null;
				switch (type) {
					case FINISHED_SUCCESS:
						img = new ThemeResource("icons/ok.png");
						break;
					case FINISHED_WARNING:
						img = new ThemeResource("icons/warning.png");
						break;
					case FAILED:
						img = new ThemeResource("icons/error.png");
						break;
					case RUNNING:
						img = new ThemeResource("icons/running.png");
						break;
					case SCHEDULED:
						img = new ThemeResource("icons/scheduled.png");
						break;
					case CANCELLED:
						img = new ThemeResource("icons/cancelled.png");
						break;
					default:
						//no icon
						break;
				}
				Embedded emb = new Embedded(type.name(), img);
				emb.setDescription(type.name());
				return emb;
			}
		});

		monitorTable.addGeneratedColumn("debug", new CustomTable.ColumnGenerator() {
			@Override
			public Object generateCell(CustomTable source, Object itemId,
					Object columnId) {
				boolean inDebug = (boolean) source.getItem(itemId).getItemProperty(columnId).getValue();
				Embedded emb;
				if (inDebug) {
					emb = new Embedded("True", new ThemeResource("icons/debug.png"));
					emb.setDescription("TRUE");
				} else {
					emb = new Embedded("False", new ThemeResource("icons/no_debug.png"));
					emb.setDescription("FALSE");
				}
				return emb;
			}
		});


		monitorTable.addGeneratedColumn("actions",
				new GenerateActionColumnMonitor(this));

		monitorTableLayout.addComponent(monitorTable);
		monitorTableLayout.addComponent(monitorTable.createControls());
		monitorTable.setPageLength(20);
		monitorTable.setFilterDecorator(new filterDecorator());
		monitorTable.setFilterFieldVisible("actions", false);
		monitorTable.addItemClickListener(
				new ItemClickEvent.ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				//if (event.isDoubleClick()) {
				if (!monitorTable.isSelected(event.getItemId())) {
					exeId = (long) event.getItem().getItemProperty("exeid").getValue();
					logLayout = buildlogLayout();
					hsplit.setSplitPosition(55, Unit.PERCENTAGE);
					hsplit.setSecondComponent(logLayout);
					hsplit.setLocked(false);
				} else {
					
				}
			}
		});
		

		hsplit.setFirstComponent(monitorTableLayout);
		hsplit.setSecondComponent(null);
		hsplit.setSplitPosition(100, Unit.PERCENTAGE);
		hsplit.setLocked(true);

		monitorTable.refreshRowCache();

		return mainLayout;
	}

	private void refreshData() {
		int page = monitorTable.getCurrentPage();
		tableData = getTableData(App.getApp().getPipelines().getAllExecutions());
		monitorTable.setContainerDataSource(tableData);
		monitorTable.setFilterFieldVisible("actions", false);
		monitorTable.setCurrentPage(page);
		Object property = "date";
		monitorTable.setSortContainerPropertyId(property);
		monitorTable.setSortAscending(false);
		monitorTable.sort();

	}

	private VerticalLayout buildlogLayout() {

		logLayout = new VerticalLayout();
		logLayout.setImmediate(true);
		logLayout.setMargin(true);
		logLayout.setSpacing(true);
		logLayout.setWidth("100%");
		logLayout.setHeight("100%");

		PipelineExecution pipelineExec = App.getApp().getPipelines()
				.getExecution(exeId);
		DebuggingView debugView = new DebuggingView(pipelineExec, null,
				pipelineExec.isDebugging());
		logLayout.addComponent(debugView);
		logLayout.setExpandRatio(debugView, 1.0f);

		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setWidth("100%");

		Button buttonClose = new Button();
		buttonClose.setCaption("Close");
		buttonClose.setHeight("25px");
		buttonClose.setWidth("100px");
		buttonClose
				.addClickListener(new com.vaadin.ui.Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {

				hsplit.setSplitPosition(100, Unit.PERCENTAGE);
				hsplit.setLocked(true);
			}
		});
		buttonBar.addComponent(buttonClose);
		buttonBar.setComponentAlignment(buttonClose, Alignment.BOTTOM_LEFT);

		Button buttonExport = new Button();
		buttonExport.setCaption("Export");
		buttonExport.setHeight("25px");
		buttonExport.setWidth("100px");
		buttonExport
				.addClickListener(new com.vaadin.ui.Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
			}
		});
		buttonBar.addComponent(buttonExport);
		buttonBar.setComponentAlignment(buttonExport, Alignment.BOTTOM_RIGHT);

		logLayout.addComponent(buttonBar);
		logLayout.setExpandRatio(buttonBar, 0);

		return logLayout;

	}

	public static IndexedContainer getTableData(List<PipelineExecution> data) {

		IndexedContainer result = new IndexedContainer();

		for (String p : visibleCols) {
			//		if (p.equals("exeid")==false)
			switch (p) {
				case "status":
					result.addContainerProperty(p, ExecutionStatus.class, null);
					break;
				case "debug":
					result.addContainerProperty(p, Boolean.class, false);
					break;
				case "date":
					result.addContainerProperty(p, Date.class, null);
					break;
				default:
					result.addContainerProperty(p, String.class, "");
					break;
			}
		}

		result.addContainerProperty("exeid", Long.class, "");


		for (PipelineExecution item : data) {

			Object num = result.addItem();
			if (item.getStart() == null) {
				result.getContainerProperty(num, "date").setValue(null);
			} else {

//				Format formatter = new SimpleDateFormat();
//			    String s = formatter.format(item.getStart());
				result.getContainerProperty(num, "date").setValue(item
						.getStart());
			}


			result.getContainerProperty(num, "exeid").setValue(item.getId());
			result.getContainerProperty(num, "user").setValue(" ");
			result.getContainerProperty(num, "name").setValue(item.getPipeline()
					.getName());
			result.getContainerProperty(num, "status").setValue(item
					.getExecutionStatus());
			result.getContainerProperty(num, "debug").setValue(item
					.isDebugging());

		}

		return result;
	}

	@Override
	public void enter(ViewChangeEvent event) {
		buildMainLayout();
		setCompositionRoot(mainLayout);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		// TODO Auto-generated method stub
		Button senderButton = event.getButton();
		if (senderButton != null) {
			ActionButtonData senderData = (ActionButtonData) senderButton
					.getData();
			String caption = senderData.action;
			Object itemId = senderData.data;

			exeId = (Long) tableData.getContainerProperty(itemId, "exeid")
					.getValue();
			pipeName = (String) tableData.getContainerProperty(itemId, "name")
					.getValue();
			switch (caption) {
				case "stop":
					break;
				case "showlog":
					logLayout = buildlogLayout();
					hsplit.setSplitPosition(55, Unit.PERCENTAGE);
					hsplit.setSecondComponent(logLayout);
					//hsplit.setHeight("960px");
					hsplit.setLocked(false);
					break;
				case "debug":
					logLayout = buildlogLayout();
					hsplit.setSplitPosition(55, Unit.PERCENTAGE);
					hsplit.setSecondComponent(logLayout);
					//hsplit.setHeight("960px");
					hsplit.setLocked(false);
					break;
			}

		}
	}

	class filterDecorator extends IntlibFilterDecorator {

		@Override
		public String getEnumFilterDisplayName(Object propertyId, Object value) {
			if (propertyId == "status") {
				return ((ExecutionStatus) value).name();
			}
			return super.getEnumFilterDisplayName(propertyId, value);
		}

		@Override
		public Resource getEnumFilterIcon(Object propertyId, Object value) {
			if (propertyId == "status") {
				ExecutionStatus type = (ExecutionStatus) value;
				ThemeResource img = null;
				switch (type) {
					case FINISHED_SUCCESS:
						img = new ThemeResource("icons/ok.png");
						break;
					case FINISHED_WARNING:
						img = new ThemeResource("icons/warning.png");
						break;
					case FAILED:
						img = new ThemeResource("icons/error.png");
						break;
					case RUNNING:
						img = new ThemeResource("icons/running.png");
						break;
					case SCHEDULED:
						img = new ThemeResource("icons/scheduled.png");
						break;
					case CANCELLED:
						img = new ThemeResource("icons/cancelled.png");
						break;
					default:
						//no icon
						break;
				}
				return img;
			}
			return super.getEnumFilterIcon(propertyId, value);
		}

		@Override
		public String getBooleanFilterDisplayName(Object propertyId, boolean value) {
			if (propertyId == "debug") {
				if (value) {
					return "Debug";
				} else {
					return "Run";
				}
			}
			return super.getBooleanFilterDisplayName(propertyId, value);
		}

		@Override
		public Resource getBooleanFilterIcon(Object propertyId, boolean value) {
			if (propertyId == "debug") {
				if (value) {
					return new ThemeResource("icons/debug.png");
				} else {
					return new ThemeResource("icons/no_debug.png");
				}
			}
			return super.getBooleanFilterIcon(propertyId, value);
		}
	};
}
