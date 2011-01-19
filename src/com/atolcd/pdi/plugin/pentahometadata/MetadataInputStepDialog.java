package com.atolcd.pdi.plugin.pentahometadata;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPreviewFactory;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.dialog.EnterNumberDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.StyledTextComp;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

public class MetadataInputStepDialog extends BaseStepDialog implements StepDialogInterface {
	private static Class<?> PKG = MetadataInputStepMeta.class; // for i18n
	// purposes,
	// needed by
	// Translator2!!
	// $NON-NLS-1$

	public static final String STRING_XACTION_QUERY_WARING = "PentahoMetadataXQueryWarning";
	private MetadataInputStepMeta input;

	private Label labelStepName;
	private Text textStepName;

	private Label labelUrl;
	private Text textUrl;

	private Label labelUsername;
	private Text textUsername;

	private Label labelPassword;
	private Text textPassword;

	private Label labelDomain;
	private Text textDomain;

	private Label labelModel;
	private Combo comboModel;

	private Label labelDisableDistinct;
	private Button buttonDisableDistinct;

	private Link advert;
	private TableView tableViewFields;
	
	private Label labelFilter;
	private StyledTextComp styledTextCompFilter;

	private Button buttonGetModels;
	private Button buttonOk;
	private Button buttonGetFields;
	private Button buttonCancel;
	private Button buttonPreview;

	public MetadataInputStepDialog(Shell parent, Object in, TransMeta transMeta, String sname) {
		super(parent, (BaseStepMeta) in, transMeta, sname);
		input = (MetadataInputStepMeta) in;
	}

	public String open() {

		final Display display = getParent().getDisplay();
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
		props.setLook(shell);
		setShellImage(shell, input);
		FormLayout formLayout = new FormLayout();
		formLayout.marginWidth = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;

		shell.setLayout(formLayout);
		int middle = props.getMiddlePct();
		int margin = Const.MARGIN;

		ModifyListener lsMod = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				input.setChanged();
			}
		};
		backupChanged = input.hasChanged();

		FormData fd;

		// Step Name
		labelStepName = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(0, margin);
		labelStepName.setLayoutData(fd);
		labelStepName.setText(BaseMessages.getString(PKG, "System.Label.StepName"));

		textStepName = new Text(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(0, margin);
		textStepName.setText(stepname);
		textStepName.setLayoutData(fd);

		// Pentaho Server URL
		labelUrl = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(textStepName, margin);
		labelUrl.setLayoutData(fd);
		labelUrl.setText(BaseMessages.getString(PKG, "MetadataInputDialog.Url"));

		textUrl = new Text(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(textStepName, margin);
		textUrl.setLayoutData(fd);

		// Username
		labelUsername = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(textUrl, margin);
		labelUsername.setLayoutData(fd);
		labelUsername.setText(BaseMessages.getString(PKG, "MetadataInputDialog.Username"));

		textUsername = new Text(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(textUrl, margin);
		textUsername.setLayoutData(fd);

		// Password
		labelPassword = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(textUsername, margin);
		labelPassword.setLayoutData(fd);
		labelPassword.setText(BaseMessages.getString(PKG, "MetadataInputDialog.Password"));

		textPassword = new Text(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(textUsername, margin);
		textPassword.setLayoutData(fd);
		textPassword.setEchoChar('*');

		// Domain
		labelDomain = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(textPassword, margin);
		labelDomain.setLayoutData(fd);
		labelDomain.setText(BaseMessages.getString(PKG, "MetadataInputDialog.Domain"));

		textDomain = new Text(shell, SWT.BORDER);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(textPassword, margin);
		textDomain.setLayoutData(fd);

		// Model
		labelModel = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(textDomain, margin);
		labelModel.setLayoutData(fd);
		labelModel.setText(BaseMessages.getString(PKG, "MetadataInputDialog.Model"));

		buttonGetModels = new Button(shell, SWT.NONE);
		buttonGetModels.setText(BaseMessages.getString(PKG, "MetadataInputDialog.GetModels"));

		props.setLook(buttonGetModels);
		buttonGetModels.pack(true);

		comboModel = new Combo(shell, SWT.READ_ONLY);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.top = new FormAttachment(textDomain, margin);
		fd.right = new FormAttachment(100, -buttonGetModels.getBounds().width - margin);
		comboModel.setLayoutData(fd);

		fd = new FormData();
		// fd.left = new FormAttachment(comboModel, margin);
		fd.left = new FormAttachment(comboModel, 0);
		fd.top = new FormAttachment(textDomain, margin);
		fd.right = new FormAttachment(100, 0);
		buttonGetModels.setLayoutData(fd);

		// Disable Distinct
		labelDisableDistinct = new Label(shell, SWT.RIGHT);
		labelDisableDistinct.setText(BaseMessages.getString(PKG, "MetadataInputDialog.DisableDistinct")); //$NON-NLS-1$
		props.setLook(labelDisableDistinct);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(middle, -margin);
		fd.top = new FormAttachment(comboModel, margin);
		labelDisableDistinct.setLayoutData(fd);
		
		buttonDisableDistinct = new Button(shell, SWT.CHECK);
		props.setLook(buttonDisableDistinct);
		fd = new FormData();
		fd.left = new FormAttachment(middle, 0);
		fd.right = new FormAttachment(100, 0);
		fd.top = new FormAttachment(comboModel, margin);
		buttonDisableDistinct.setLayoutData(fd);
		

		// Advertising !
		advert = new Link(this.shell, SWT.NONE);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		fd.bottom = new FormAttachment(100, -50);
		advert.setLayoutData(fd);
		advert.setText(BaseMessages.getString(PKG, "MetadataInputDialog.Advert"));

		// MQL Filter area
		styledTextCompFilter = new StyledTextComp(shell, SWT.MULTI | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL, "");
		props.setLook(styledTextCompFilter, Props.WIDGET_STYLE_FIXED);
		Font font = new Font(shell.getDisplay(), "Courier New", 8, SWT.NORMAL);
		styledTextCompFilter.setFont(font);

		fd = new FormData();
		fd.height = 100;
		fd.left = new FormAttachment(0, 0);
		fd.right = new FormAttachment(100, 0);
		fd.bottom = new FormAttachment(advert, -margin);
		styledTextCompFilter.setLayoutData(fd);

		labelFilter = new Label(shell, SWT.RIGHT);
		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		// fd.top = new FormAttachment(tableViewFields, margin*4);
		fd.bottom = new FormAttachment(styledTextCompFilter, -margin);
		labelFilter.setLayoutData(fd);
		labelFilter.setText(BaseMessages.getString(PKG, "MetadataInputDialog.Filter"));

		ColumnInfo[] colinf = new ColumnInfo[] { new ColumnInfo(getLocalizedColumn(0), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
				new ColumnInfo(getLocalizedColumn(1), ColumnInfo.COLUMN_TYPE_TEXT, false, true),
				new ColumnInfo(getLocalizedColumn(2), ColumnInfo.COLUMN_TYPE_TEXT, false, false),
				new ColumnInfo(getLocalizedColumn(3), ColumnInfo.COLUMN_TYPE_TEXT, false, false), 
				new ColumnInfo(getLocalizedColumn(4), ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes())};

		tableViewFields = new TableView(null, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL, colinf, 20, lsMod, props);

		fd = new FormData();
		fd.left = new FormAttachment(0, 0);
		fd.top = new FormAttachment(labelDisableDistinct, margin * 4);
		fd.right = new FormAttachment(100, 0);
		fd.bottom = new FormAttachment(labelFilter, -margin);
		tableViewFields.setLayoutData(fd);

		buttonOk = new Button(shell, SWT.PUSH);
		buttonGetFields = new Button(shell, SWT.PUSH);
		buttonPreview = new Button(shell, SWT.PUSH);
		buttonCancel = new Button(shell, SWT.PUSH);

		buttonOk.setText(BaseMessages.getString("System.Button.OK"));
		buttonGetFields.setText(BaseMessages.getString("System.Button.GetFields"));
		buttonPreview.setText(BaseMessages.getString("System.Button.Preview"));
		buttonCancel.setText(BaseMessages.getString("System.Button.Cancel"));
		setButtonPositions(new Button[] { buttonOk, buttonPreview, buttonCancel }, margin, null);

		shell.setText(BaseMessages.getString(PKG, "MetadataInputDialog.ShellTitleName"));

		buttonCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cancel();
			}
		});
		buttonPreview.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				preview();
			}
		});
		buttonOk.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ok();
			}
		});
		buttonGetFields.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doGetFields();
			}
		});

		comboModel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// doSelectDimension();
			}
		});

		buttonGetModels.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				doGetModels();
			}
		});

		advert.addListener(SWT.Selection, new Listener() {
			public void handleEvent(final Event event) {
				Program.launch(event.text);
			}
		});

		props.setLook(labelStepName);
		props.setLook(textStepName);
		props.setLook(labelUrl);
		props.setLook(textUrl);
		props.setLook(labelUsername);
		props.setLook(textUsername);
		props.setLook(labelPassword);
		props.setLook(textPassword);
		props.setLook(labelDomain);
		props.setLook(textDomain);
		props.setLook(labelModel);
		props.setLook(comboModel);
		props.setLook(buttonGetModels);
		props.setLook(tableViewFields);
		props.setLook(labelFilter);
		props.setLook(labelStepName);
		props.setLook(buttonOk);
		props.setLook(buttonCancel);
		props.setLook(buttonPreview);
		props.setLook(buttonGetFields);

		shell.addShellListener(new ShellAdapter() {
			public void shellClosed(ShellEvent e) {
				cancel();
			}
		});

		BaseStepDialog.positionBottomButtons(shell, new Button[] { buttonOk, buttonGetFields, buttonPreview, buttonCancel }, margin, advert);

		getData();
		input.setChanged(changed);
		setSize();
		shell.open();

		showWarningDialog(shell);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		return stepname;
	}


	private void doGetModels() {

		PentahoMetadataHelper metadataHelper = new PentahoMetadataHelper(textUrl.getText(), textUsername.getText(), textPassword.getText(), textDomain.getText());

		comboModel.removeAll();

		Map map = metadataHelper.getModels();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry) it.next();
			comboModel.add((String) pairs.getValue());
			comboModel.setData((String) pairs.getValue(), (String) pairs.getKey());
		}

		if (comboModel.getItemCount() > 0) {
			comboModel.select(0);

		}
	}

	private void doGetFields() {

		String model = (String) comboModel.getData(comboModel.getText());

		PentahoMetadataModel metadataModel = new PentahoMetadataModel(textUrl.getText(), textUsername.getText(), textPassword.getText(), textDomain.getText(), model);
		metadataModel.genereMetaData();

		int nbColumns = metadataModel.getNbBusinessColumns();

		tableViewFields.table.removeAll();

		for (int i = 0; i < nbColumns; i++) {

			tableViewFields.add(metadataModel.getPentahoModelColumnName()[i], metadataModel.getPentahoModelViewName()[i], metadataModel.getPentahoModelColumnId()[i], metadataModel
					.getPentahoModelViewId()[i], metadataModel.getPentahoModelColumnType()[i]);
		}

		tableViewFields.removeEmptyRows();
		tableViewFields.setRowNums();
		tableViewFields.optWidth(true);
		input.setChanged();
	}

	/**
	 * Copy information from the meta-data input to the dialog fields.
	 */
	public void getData() {

		textUrl.setText(Const.NVL(input.getUrl(), ""));
		textUsername.setText(Const.NVL(input.getUsername(), ""));
		textPassword.setText(Const.NVL(input.getPassword(), ""));
		textDomain.setText(Const.NVL(input.getDomain(), ""));
		buttonDisableDistinct.setSelection(input.isDisableDistinct());
		styledTextCompFilter.setText(Const.NVL(input.getFilter(), ""));
		

		// load combo
		comboModel.removeAll();

		for (int i = 0; i < input.getModelName().length; i++) {
			comboModel.add(input.getModelName()[i]);
			comboModel.setData(input.getModelName()[i], input.getModelId()[i]);
		}
		
		logBasic("========== input.getModel() = " + input.getModel() );

		// select the saved model
		for (int i = 0; i < input.getModelName().length; i++) {

			if ((String) comboModel.getData(comboModel.getItem(i)) == input.getModel()) {
				logBasic("========== comboModel.getData(comboModel.getItem(i) = " + (String) comboModel.getData(comboModel.getItem(i)) );
				comboModel.select(i);
			}
		}

		tableViewFields.table.removeAll();
		if (input.getColumnName() != null) {

			for (int i = 0; i < input.getColumnName().length; i++) {

				tableViewFields
						.add(input.getColumnName()[i], input.getViewName()[i], input.getColumnId()[i], input.getViewId()[i], ValueMeta.getTypeDesc(input.getColumnType()[i]));

			}
		}
		tableViewFields.setRowNums();
		tableViewFields.optWidth(true);

	}

	private void cancel() {
		stepname = null;
		input.setChanged(changed);
		dispose();
	}

	private void getInfo(MetadataInputStepMeta meta) {
		
		stepname = textStepName.getText(); // return value
		
		meta.setUrl(textUrl.getText());
		meta.setUsername(textUsername.getText());
		meta.setPassword(textPassword.getText());
		meta.setDomain(textDomain.getText());
		meta.setModel((String) comboModel.getData(comboModel.getText()));
		meta.setDisableDistinct(buttonDisableDistinct.getSelection());
		meta.setFilter(styledTextCompFilter.getText());

		int nbModels = comboModel.getItemCount();
		meta.allocateModels(nbModels);

		for (int i = 0; i < nbModels; i++) {
			meta.getModelName()[i] = comboModel.getItem(i);
			meta.getModelId()[i] = (String) comboModel.getData(comboModel.getItem(i));
		}

		int nrFields = tableViewFields.nrNonEmpty();
		meta.allocate(nrFields);

		for (int i = 0; i < nrFields; i++) {
			TableItem item = tableViewFields.getNonEmpty(i);
			meta.getColumnName()[i] = item.getText(1);
			meta.getViewName()[i] = item.getText(2);
			meta.getColumnId()[i] = item.getText(3);
			meta.getViewId()[i] = item.getText(4);
			meta.getColumnType()[i] = ValueMeta.getType(item.getText(5));
		}

	}

	private void ok() {
		if (Const.isEmpty(textStepName.getText()))
			return;

		stepname = textStepName.getText(); // return value
		// copy info to TextFileInputMeta class (input)

		getInfo(input);

		dispose();
	}

	/**
	 * Preview the data generated by this step. This generates a transformation
	 * using this step & a dummy and previews it.
	 * 
	 */

	private String getLocalizedColumn(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return BaseMessages.getString(PKG, "MetadataInputDialog.BusinessColumnName");
		case 1:
			return BaseMessages.getString(PKG, "MetadataInputDialog.BusinessViewName");
		case 2:
			return BaseMessages.getString(PKG, "MetadataInputDialog.BusinessColumnId");
		case 3:
			return BaseMessages.getString(PKG, "MetadataInputDialog.BusinessViewId");
		case 4:
			return BaseMessages.getString(PKG, "MetadataInputDialog.BusinessColumnType");
		default:
			return "";
		}
	}

	private void preview() {
		// Create the table input reader step...
		MetadataInputStepMeta oneMeta = new MetadataInputStepMeta();
		getInfo(oneMeta);

		TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(transMeta, oneMeta, textStepName.getText());

		EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props.getDefaultPreviewSize(),
				BaseMessages.getString(PKG, "MetadataInputDialog.EnterPreviewSize"), BaseMessages.getString(PKG, "MetadataInputDialog.NumberOfRowsToPreview")); //$NON-NLS-1$ //$NON-NLS-2$
		int previewSize = numberDialog.open();
		if (previewSize > 0) {
			TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { textStepName.getText() }, new int[] { previewSize });
			progressDialog.open();

			Trans trans = progressDialog.getTrans();
			String loggingText = progressDialog.getLoggingText();

			if (!progressDialog.isCancelled()) {
				if (trans.getResult() != null && trans.getResult().getNrErrors() > 0) {
					EnterTextDialog etd = new EnterTextDialog(shell, BaseMessages.getString(PKG, "System.Dialog.PreviewError.Title"), BaseMessages.getString(PKG,
							"System.Dialog.PreviewError.Message"), loggingText, true);
					etd.setReadOnly();
					etd.open();
				}
			}

			PreviewRowsDialog prd = new PreviewRowsDialog(shell, transMeta, SWT.NONE, textStepName.getText(), progressDialog.getPreviewRowsMeta(textStepName.getText()),
					progressDialog.getPreviewRows(textStepName.getText()), loggingText);
			prd.open();
		}
	}

	public static void showWarningDialog(Shell shell) {
		PropsUI props = PropsUI.getInstance();

		if ("Y".equalsIgnoreCase(props.getCustomParameter(STRING_XACTION_QUERY_WARING, "Y"))) //$NON-NLS-1$ //$NON-NLS-2$
		{
			MessageDialogWithToggle md = new MessageDialogWithToggle(shell, BaseMessages.getString(PKG, "MetadataInput.WarningDialog.DialogTitle"), //$NON-NLS-1$
					null, BaseMessages.getString(PKG, "MetadataInput.WarningDialog.DialogMessage", Const.CR) + Const.CR, //$NON-NLS-1$ //$NON-NLS-2$
					MessageDialog.WARNING, new String[] { BaseMessages.getString(PKG, "MetadataInput.WarningDialog.Option1") }, //$NON-NLS-1$
					0, BaseMessages.getString(PKG, "MetadataInput.WarningDialog.Option2"), //$NON-NLS-1$
					"N".equalsIgnoreCase(props.getCustomParameter(STRING_XACTION_QUERY_WARING, "Y")) //$NON-NLS-1$ //$NON-NLS-2$
			);
			MessageDialogWithToggle.setDefaultImage(GUIResource.getInstance().getImageSpoon());
			md.open();
			props.setCustomParameter(STRING_XACTION_QUERY_WARING, md.getToggleState() ? "N" : "Y"); //$NON-NLS-1$ //$NON-NLS-2$
			props.saveProps();
		}
	}
}
