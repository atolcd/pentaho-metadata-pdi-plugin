package com.atolcd.pdi.plugin.pentahometadata;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class MetadataInputStepData extends BaseStepData implements StepDataInterface {

	public PentahoMetadataHelper metadataHelper;
	public PentahoMetadataModel metadataModel;

	public RowMetaInterface outputRowMeta;
	public ValueMetaInterface[] conversionMeta;
	
	public MetadataInputStepMeta input;
	public int rowNumber;

	public MetadataInputStepData() {
		super();
	}
}
