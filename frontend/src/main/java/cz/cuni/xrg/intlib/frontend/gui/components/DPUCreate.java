package cz.cuni.xrg.intlib.frontend.gui.components;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Upload.StartedEvent;

import cz.cuni.xrg.intlib.commons.app.dpu.DPUInstanceRecord;
import cz.cuni.xrg.intlib.commons.app.dpu.VisibilityType;
import cz.cuni.xrg.intlib.commons.app.module.ModuleException;
import cz.cuni.xrg.intlib.commons.configuration.ConfigException;
import cz.cuni.xrg.intlib.frontend.auxiliaries.dpu.DPUInstanceWrap;



public class DPUCreate extends Window {

	private TextField dpuName;

	private TextArea dpuDescription;
	private OptionGroup groupVisibility;
	private Upload selectFile;
	private LineBreakCounter lineBreakCounter;
	private UploadInfoWindow uploadInfoWindow;
	private GridLayout dpuGeneralSettingsLayout;

	public DPUCreate() {

		this.setResizable(false);
		this.setModal(true);
		this.setCaption("DPU Creation");

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setStyleName("dpuDetailMainLayout");
		mainLayout.setMargin(true);

		dpuGeneralSettingsLayout = new GridLayout(2, 6);
		dpuGeneralSettingsLayout.setSpacing(true);
		dpuGeneralSettingsLayout.setWidth("400px");
		dpuGeneralSettingsLayout.setColumnExpandRatio(0, 0.5f);
		dpuGeneralSettingsLayout.setColumnExpandRatio(1, 0.5f);

		Label nameLabel = new Label("Name");
		nameLabel.setImmediate(false);
		nameLabel.setWidth("-1px");
		nameLabel.setHeight("-1px");
		dpuGeneralSettingsLayout.addComponent(nameLabel, 0, 0);

		dpuName = new TextField();
		dpuName.setImmediate(false);
		dpuName.setWidth("310px");
		dpuName.setHeight("-1px");
		dpuName.addValueChangeListener(new Property.ValueChangeListener() {

			@Override
			public void valueChange(ValueChangeEvent event) {
				setCaption(dpuName.getValue());
			}
		});
		dpuGeneralSettingsLayout.addComponent(dpuName, 1, 0);

		Label descriptionLabel = new Label("Description");
		descriptionLabel.setImmediate(false);
		descriptionLabel.setWidth("-1px");
		descriptionLabel.setHeight("-1px");
		dpuGeneralSettingsLayout.addComponent(descriptionLabel, 0, 1);

		dpuDescription = new TextArea();
		dpuDescription.setImmediate(false);
		dpuDescription.setWidth("310px");
		dpuDescription.setHeight("60px");
		dpuGeneralSettingsLayout.addComponent(dpuDescription, 1, 1);

		Label visibilityLabel = new Label("Visibility");
		descriptionLabel.setImmediate(false);
		descriptionLabel.setWidth("-1px");
		descriptionLabel.setHeight("-1px");
		dpuGeneralSettingsLayout.addComponent(visibilityLabel, 0, 2);

		groupVisibility = new OptionGroup();
		groupVisibility.addStyleName("horizontalgroup");
		groupVisibility.addItem(VisibilityType.PRIVATE);
		groupVisibility.addItem(VisibilityType.PUBLIC);

		dpuGeneralSettingsLayout.addComponent(groupVisibility, 1, 2);

		Label selectLabel = new Label("Select .jar file");
		descriptionLabel.setImmediate(false);
		descriptionLabel.setWidth("-1px");
		descriptionLabel.setHeight("-1px");
		dpuGeneralSettingsLayout.addComponent(selectLabel, 0, 3);

		lineBreakCounter = new LineBreakCounter();
		lineBreakCounter.setSlow(true);

		selectFile = new Upload(null, lineBreakCounter);
		selectFile.addStyleName("horizontalgroup");

		/*
		 * Panel panel = new Panel("Cool Image Storage"); 
		 * Layout panelContent = new VerticalLayout(); 
		 * panel.setContent(panelContent);
		 * panelContent.addComponent(selectFile);
		 * 
		 * // Show uploaded file in this placeholder final 
		 * Embedded  image = new Embedded("Uploaded Image"); 
		 * image.setVisible(false);
		 * panelContent.addComponent(image);
		 */

		selectFile.addStartedListener(new StartedListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadStarted(final StartedEvent event) {
				// TODO Auto-generated method stub
				if (uploadInfoWindow.getParent() == null) {
					UI.getCurrent().addWindow(uploadInfoWindow);
				}
				uploadInfoWindow.setClosable(false);

			}
		});

		selectFile.addFinishedListener(new Upload.FinishedListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void uploadFinished(final FinishedEvent event) {
				uploadInfoWindow.setClosable(true);
				dpuGeneralSettingsLayout.removeComponent(1, 4);
				dpuGeneralSettingsLayout.addComponent(new Label(" Upload file: "+ event.getFilename()), 1, 4);
			}
		});

		uploadInfoWindow = new UploadInfoWindow(selectFile, lineBreakCounter);

		

		dpuGeneralSettingsLayout.addComponent(selectFile, 1, 3);

		Label typeLabel = new Label("Type");
		descriptionLabel.setImmediate(false);
		descriptionLabel.setWidth("-1px");
		descriptionLabel.setHeight("-1px");
		dpuGeneralSettingsLayout.addComponent(typeLabel, 0, 5);

		Label DPUType = new Label(" ");
		dpuGeneralSettingsLayout.addComponent(DPUType, 1, 5);

		dpuGeneralSettingsLayout.setMargin(new MarginInfo(false, false, true,
				false));
		mainLayout.addComponent(dpuGeneralSettingsLayout);

		HorizontalLayout buttonBar = new HorizontalLayout();
		buttonBar.setStyleName("dpuDetailButtonBar");
		buttonBar.setMargin(new MarginInfo(true, false, false, false));

		Button saveButton = new Button("Save");
		buttonBar.addComponent(saveButton);

		Button cancelButton = new Button("Cancel", new Button.ClickListener() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(Button.ClickEvent event) {
				close();
			}
		});
		buttonBar.addComponent(cancelButton);

		mainLayout.addComponent(buttonBar);

		this.setContent(mainLayout);
		setSizeUndefined();
	}

}

class UploadInfoWindow extends Window implements Upload.StartedListener,
		Upload.ProgressListener, Upload.FailedListener,
		Upload.SucceededListener, Upload.FinishedListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Label state = new Label();
	private final Label result = new Label();
	private final Label fileName = new Label();
	private final Label textualProgress = new Label();

	private final ProgressIndicator pi = new ProgressIndicator();
	private final Button cancelButton;
	private final LineBreakCounter counter;

	public UploadInfoWindow(final Upload upload,
			final LineBreakCounter lineBreakCounter) {
		super("Status");
		this.counter = lineBreakCounter;

		addStyleName("upload-info");

		setResizable(false);
		setDraggable(false);

		final FormLayout l = new FormLayout();
		setContent(l);
		l.setMargin(true);

		final HorizontalLayout stateLayout = new HorizontalLayout();
		stateLayout.setSpacing(true);
		stateLayout.addComponent(state);

		cancelButton = new Button("Cancel");
		cancelButton.addClickListener(new Button.ClickListener() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(final ClickEvent event) {
				upload.interruptUpload();
			}
		});
		cancelButton.setVisible(false);
		cancelButton.setStyleName("small");
		stateLayout.addComponent(cancelButton);

		stateLayout.setCaption("Current state");
		state.setValue("Idle");
		l.addComponent(stateLayout);

		fileName.setCaption("File name");
		l.addComponent(fileName);
		

		result.setCaption("Line breaks counted");
		l.addComponent(result);

		pi.setCaption("Progress");
		pi.setVisible(false);
		l.addComponent(pi);

		textualProgress.setVisible(false);
		l.addComponent(textualProgress);

		upload.addStartedListener(this);
		upload.addProgressListener(this);
		upload.addFailedListener(this);
		upload.addSucceededListener(this);
		upload.addFinishedListener(this);

	}

	@Override
	public void uploadFinished(final FinishedEvent event) {
		state.setValue("Idle");
		pi.setVisible(false);
		textualProgress.setVisible(false);
		cancelButton.setVisible(false);

	}

	@Override
	public void uploadStarted(final StartedEvent event) {
		// this method gets called immediatedly after upload is
		// started
		pi.setValue(0f);
		pi.setVisible(true);
		pi.setPollingInterval(500); // hit server frequantly to get
		textualProgress.setVisible(true);
		// updates to client
		state.setValue("Uploading");
		fileName.setValue(event.getFilename());

		cancelButton.setVisible(true);
	}

	@Override
	public void updateProgress(final long readBytes, final long contentLength) {
		// this method gets called several times during the update
		pi.setValue(new Float(readBytes / (float) contentLength));
		textualProgress.setValue("Processed " + readBytes + " bytes of "
				+ contentLength);
		result.setValue(counter.getLineBreakCount() + " (counting...)");
	}

	@Override
	public void uploadSucceeded(final SucceededEvent event) {
		result.setValue(counter.getLineBreakCount() + " (total)");
		
	}

	@Override
	public void uploadFailed(final FailedEvent event) {
		result.setValue(counter.getLineBreakCount()
				+ " (counting interrupted at "
				+ Math.round(100 * pi.getValue()) + "%)");
	}

}

class LineBreakCounter implements Receiver {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5099459605355200117L;
	private int counter;
	private int total;
	private boolean sleep;
	private File file;
	private FileOutputStream fstream = null;
	/**
	 * return an OutputStream that simply counts lineends
	 */
	public OutputStream receiveUpload(final String filename,
			final String MIMEType) {
		counter = 0;
		total = 0;
		//FileOutputStream fos = null; // Stream to write to
		OutputStream fos = null;
		try {
			file = new File("C:/tmp/intlib/" + filename);
			fstream = new FileOutputStream(file);
			fos= new OutputStream() {
				private static final int searchedByte = '\n';

				@Override
				public void write(final int b) throws IOException {
					total++;
					if (b == searchedByte) {
						counter++;
					}
					fstream.write(b);
					if (sleep && total % 1000 == 0) {
						try {
							Thread.sleep(100);
						} catch (final InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
				
				@Override
				public void close() throws IOException
				{
					fstream.close();
					super.close();
				}
			};
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			new Notification("Could not open file<br/>", e.getMessage(),
					Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
			return null;
		}
		return fos;
	}

	public int getLineBreakCount() {
		return counter;
	}

	public void setSlow(final boolean value) {
		sleep = value;
	}

}