package com.atolcd.pdi.plugin.pentahometadata;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.*;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.exception.*;
import org.pentaho.di.core.row.*;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.*;
import org.pentaho.di.trans.*;
import org.pentaho.di.trans.step.*;
import org.w3c.dom.Node;

public class MetadataInputStepMeta extends BaseStepMeta implements StepMetaInterface {

	private static Class<?> PKG = MetadataInputStepMeta.class; // for i18n
	// purposes

	private String url;
	private String username;
	private String password;
	private String domain;
	private String model;
	private String filter;
	private boolean disableDistinct;
	private String[] modelName;
	private String[] modelId;
	private String[] columnId;
	private String[] columnName;
	private int[] columnType;
	private String[] viewId;
	private String[] viewName;

	private MetadataInputStepData data;

	private String conversionMask[];

	public MetadataInputStepMeta() {
		super();
	}

	public Object clone() {

		MetadataInputStepMeta retval = (MetadataInputStepMeta) super.clone();
		return retval;
	}

	public void setDefault() {

		url = "http://localhost:8080/pentaho";
		username = "joe";
		password = "password";
		domain = "steel-wheels";
		filter = "<!--Syntax:<constraint><operator>AND</operator><condition>[BC_CUSTOMER_W_TER_.BC_CUSTOMER_W_TER_COUNTRY]=\"France\"</condition></constraint>-->";

		// default is to have no key lookup settings
		allocate(0);
		allocateModels(0);
	}

	// method to allocate the arrays for the fiels
	public void allocate(int nrkeys) {

		columnId = new String[nrkeys];
		columnName = new String[nrkeys];
		columnType = new int[nrkeys];
		viewId = new String[nrkeys];
		viewName = new String[nrkeys];
	}

	// method to allocate the arrays for the models list
	public void allocateModels(int nrkeys) {

		modelId = new String[nrkeys];
		modelName = new String[nrkeys];

	}

	public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {

		// clear the output
		row.clear();

		// append the outputFields to the output
		for (int i = 0; i < columnName.length; i++) {
			ValueMetaInterface v = new ValueMeta(columnName[i], columnType[i]);
			// that would influence the output
			v.setOrigin(origin);
			row.addValueMeta(v);
		}
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer();

		retval.append("    " + XMLHandler.addTagValue("url", url));
		retval.append("    " + XMLHandler.addTagValue("username", username));
		retval.append("    " + XMLHandler.addTagValue("password", "Encrypted " + Encr.encryptPassword(password)));
		retval.append("    " + XMLHandler.addTagValue("domain", domain));
		retval.append("    " + XMLHandler.addTagValue("model", model));
		retval.append("    " + XMLHandler.addTagValue("filter", filter));
		retval.append("    " + XMLHandler.addTagValue("disableDistinct", disableDistinct));

		// Models list backup
		for (int i = 0; i < modelName.length; i++) {
			retval.append("      <modelsList>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("modelName", modelName[i]));
			retval.append("        ").append(XMLHandler.addTagValue("modelId", modelId[i]));
			retval.append("      </modelsList>").append(Const.CR);
		}

		// Columns list backup
		for (int i = 0; i < columnName.length; i++) {
			retval.append("      <columnsList>").append(Const.CR);
			retval.append("        ").append(XMLHandler.addTagValue("columnName", columnName[i]));
			retval.append("        ").append(XMLHandler.addTagValue("columnId", columnId[i]));
			retval.append("        ").append(XMLHandler.addTagValue("columnType", ValueMeta.getTypeDesc(columnType[i])));
			retval.append("        ").append(XMLHandler.addTagValue("viewName", viewName[i]));
			retval.append("        ").append(XMLHandler.addTagValue("viewId", viewId[i]));
			retval.append("      </columnsList>").append(Const.CR);
		}

		return retval.toString();
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {

		try {

			url = XMLHandler.getTagValue(stepnode, "url");
			username = XMLHandler.getTagValue(stepnode, "username");
			password = Encr.decryptPasswordOptionallyEncrypted(XMLHandler.getTagValue(stepnode, "password"));
			domain = XMLHandler.getTagValue(stepnode, "domain");
			model = XMLHandler.getTagValue(stepnode, "model");
			filter = XMLHandler.getTagValue(stepnode, "filter");
			disableDistinct = "Y".equals(XMLHandler.getTagValue(stepnode, "disableDistinct"));

			allocate(0);
			int nrFields = XMLHandler.countNodes(stepnode, "columnsList");
			allocate(nrFields);

			for (int i = 0; i < nrFields; i++) {
				Node knode = XMLHandler.getSubNodeByNr(stepnode, "columnsList", i);

				columnName[i] = XMLHandler.getTagValue(knode, "columnName");
				columnId[i] = XMLHandler.getTagValue(knode, "columnId");
				columnType[i] = ValueMeta.getType(XMLHandler.getTagValue(knode, "columnType"));
				viewName[i] = XMLHandler.getTagValue(knode, "viewName");
				viewId[i] = XMLHandler.getTagValue(knode, "viewId");
			}

			allocateModels(0);
			int nrModels = XMLHandler.countNodes(stepnode, "modelsList");
			allocateModels(nrModels);

			for (int i = 0; i < nrModels; i++) {
				Node knode = XMLHandler.getSubNodeByNr(stepnode, "modelsList", i);

				modelName[i] = XMLHandler.getTagValue(knode, "modelName");
				modelId[i] = XMLHandler.getTagValue(knode, "modelId");
			}

		} catch (Exception e) {
			throw new KettleXMLException(BaseMessages.getString(PKG, "InputMetadata.Error.UnableToReadFromXML"), e);
		}

	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		try {
			url = rep.getStepAttributeString(id_step, "url");
			username = rep.getStepAttributeString(id_step, "username");
			password = Encr.decryptPasswordOptionallyEncrypted(rep.getStepAttributeString(id_step, "password"));
			domain = rep.getStepAttributeString(id_step, "domain");
			model = rep.getStepAttributeString(id_step, "model");
			filter = rep.getStepAttributeString(id_step, "filter");
			disableDistinct = rep.getStepAttributeBoolean(id_step, "disableDistinct");

			allocate(0);
			int nrFields = rep.countNrStepAttributes(id_step, "columnName");
			allocate(nrFields);

			for (int i = 0; i < nrFields; i++) {

				columnName[i] = rep.getStepAttributeString(id_step, i, "columnName");
				columnId[i] = rep.getStepAttributeString(id_step, i, "columnId");
				columnType[i] = ValueMeta.getType(rep.getStepAttributeString(id_step, i, "columnType"));
				viewName[i] = rep.getStepAttributeString(id_step, i, "viewName");
				viewId[i] = rep.getStepAttributeString(id_step, i, "viewId");

			}

			allocateModels(0);
			int nrModels = rep.countNrStepAttributes(id_step, "modelName");
			allocateModels(nrModels);

			for (int i = 0; i < nrModels; i++) {

				modelName[i] = rep.getStepAttributeString(id_step, i, "modelName");
				modelId[i] = rep.getStepAttributeString(id_step, i, "modelId");

			}

		} catch (Exception e) {
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException {
		try {

			rep.saveStepAttribute(id_transformation, id_step, "url", url);
			rep.saveStepAttribute(id_transformation, id_step, "username", username);
			rep.saveStepAttribute(id_transformation, id_step, "password", "Encrypted " + Encr.encryptPassword(password));
			rep.saveStepAttribute(id_transformation, id_step, "model", model);
			rep.saveStepAttribute(id_transformation, id_step, "domain", domain);
			rep.saveStepAttribute(id_transformation, id_step, "filter", filter);
			rep.saveStepAttribute(id_transformation, id_step, "disableDistinct", disableDistinct);

			for (int i = 0; i < columnName.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "columnName", columnName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "columnId", columnId[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "columnType", ValueMeta.getTypeDesc(columnType[i]));
				rep.saveStepAttribute(id_transformation, id_step, i, "viewName", viewName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "viewId", viewId[i]);
			}

			for (int i = 0; i < modelName.length; i++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "modelName", modelName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "modelId", modelId[i]);
			}

		} catch (Exception e) {
			throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) {
		CheckResult cr;

		if (prev == null || prev.size() == 0) {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "MetadataInput.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "MetadataInput.CheckResult.StepRecevingData", prev.size() + ""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length > 0) {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "MetadataInput.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		} else {
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "MetadataInput.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}

	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name) {
		return new MetadataInputStepDialog(shell, meta, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans) {
		data = (MetadataInputStepData) stepDataInterface;
		return new MetadataInputStep(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData() {
		return new MetadataInputStepData();
	}

	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
			throws KettleStepException {
	}

	public RowMeta createRowMeta(String[] columnName, int[] columnType) {

		RowMeta outputRowMeta = new RowMeta();

		for (int i = 0; i < columnName.length; i++) {

			ValueMetaInterface valueMeta = new ValueMeta(columnName[i], columnType[i]);

			outputRowMeta.addValueMeta(valueMeta);

		}
		return outputRowMeta;
	}

	public void initData(VariableSpace space) throws KettleStepException {

		String url = space.environmentSubstitute(this.getUrl());
		String username = space.environmentSubstitute(this.getUsername());
		String password = space.environmentSubstitute(this.getPassword());

		int nbColumns = getColumnId().length;

		String selectionFilter = "";
		for (int i = 0; i < this.getColumnId().length; i++) {
			selectionFilter = selectionFilter + "<selection><view>" + this.getViewId()[i] + "</view><column>" + this.getColumnId()[i] + "</column></selection>";
		}

		data.metadataModel = new PentahoMetadataModel(url, username, password, domain, model);

		data.metadataModel.setSelectionFilter(selectionFilter);

		data.metadataModel.setConstraintFilter(this.getFilter());

		String disableDistinct = (isDisableDistinct() ? "true" : "false");

		String QueryURL = data.metadataModel.getServerName() + "/ServiceAction?userid=" + data.metadataModel.getUsername() + "&password=" + data.metadataModel.getPassword()
				+ "&model=" + data.metadataModel.getModelName() + "&selections=" + data.metadataModel.getSelectionFilter()
				+ "&solution=system&path=&action=mqlQuery.xaction&resultset=query_result&disable_distinct=" + disableDistinct + "&auditname=MQLSample&domain="
				+ data.metadataModel.getDomain() + "/metadata.xmi" + "&conditions=" + data.metadataModel.getConstraintFilter();

		logBasic("QueryURL = " + QueryURL);
		
		data.metadataModel.setPentahoResultSet(QueryURL, nbColumns);

		data.outputRowMeta = this.createRowMeta(this.getColumnName(), this.getColumnType()).clone();
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public boolean isDisableDistinct() {
		return disableDistinct;
	}

	public void setDisableDistinct(boolean disableDistinct) {
		this.disableDistinct = disableDistinct;
	}

	public String[] getModelName() {
		return modelName;
	}

	public void setModelName(String[] modelName) {
		this.modelName = modelName;
	}

	public String[] getModelId() {
		return modelId;
	}

	public void setModelId(String[] modelId) {
		this.modelId = modelId;
	}

	public String[] getColumnId() {
		return columnId;
	}

	public void setColumnId(String[] columnId) {
		this.columnId = columnId;
	}

	public String[] getColumnName() {
		return columnName;
	}

	public void setColumnName(String[] columnName) {
		this.columnName = columnName;
	}

	public int[] getColumnType() {
		return columnType;
	}

	public void setColumnType(int[] columnType) {
		this.columnType = columnType;
	}

	public String[] getViewId() {
		return viewId;
	}

	public void setViewId(String[] viewId) {
		this.viewId = viewId;
	}

	public String[] getViewName() {
		return viewName;
	}

	public void setViewName(String[] viewName) {
		this.viewName = viewName;
	}

	public String[] getConversionMask() {
		return conversionMask;
	}

	public void setConversionMask(String[] conversionMask) {
		this.conversionMask = conversionMask;
	}

}
