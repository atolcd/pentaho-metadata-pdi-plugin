package com.atolcd.pdi.plugin.pentahometadata;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

public class MetadataInputStep extends BaseStep implements StepInterface {

	private MetadataInputStepMeta meta;
	private MetadataInputStepData data;

	public MetadataInputStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

		try {

			if (first) // we just got started
			{
				
				first = false;
				meta.initData(this);
				
				data.rowNumber = 0;

				data.outputRowMeta = new RowMeta();
				meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

				data.conversionMeta = new ValueMetaInterface[meta.getColumnName().length];

				for (int i = 0; i < meta.getColumnName().length; i++) {

					// get output and from-string conversion format for each field
					ValueMetaInterface returnMeta = data.outputRowMeta.getValueMeta(i);

					ValueMetaInterface conversionMeta = returnMeta.clone();

					if (meta.getColumnType()[i] == ValueMetaInterface.TYPE_NUMBER) {
						conversionMeta.setType(ValueMetaInterface.TYPE_NUMBER);
						conversionMeta.setConversionMask("#.#");
						conversionMeta.setDecimalSymbol("."); 	//Pentaho Metadata use . as decimal separator
						
					} else if (meta.getColumnType()[i] == ValueMetaInterface.TYPE_DATE) {
						conversionMeta.setType(ValueMetaInterface.TYPE_DATE);
						conversionMeta.setConversionMask("yyyy-MM-dd");

					} else {
						conversionMeta.setType(ValueMetaInterface.TYPE_STRING);
					}

					data.conversionMeta[i] = conversionMeta;

				}

			}	
			
			
			for (; data.rowNumber < data.metadataModel.getNbRowsPentahoResultSet(); data.rowNumber++) {

				// Object[] row = data.metadataModel.getRows()[data.rowNumber];
				Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());

				for (int i = 0; i < meta.getColumnName().length; i++) {

					String currentValue = data.metadataModel.getRows()[data.rowNumber][i];

					if (currentValue == null || currentValue.equalsIgnoreCase("null") || currentValue.equalsIgnoreCase("")) {

						outputRowData[i] = null;

					} else {

						if (data.conversionMeta[i].getType() == ValueMetaInterface.TYPE_NUMBER) {
							// Number conversion
							outputRowData[i] = data.outputRowMeta.getValueMeta(i).convertData(data.conversionMeta[i], Double.parseDouble(currentValue));

						} else if (data.conversionMeta[i].getType() ==  ValueMetaInterface.TYPE_DATE) {
							// Date conversion
				
							SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");				
							Date dateValue = sdf.parse(currentValue);
							outputRowData[i] = data.outputRowMeta.getValueMeta(i).convertData(data.conversionMeta[i], dateValue);

						} else {
							// String Conversion
							outputRowData[i] = data.outputRowMeta.getValueMeta(i).convertData(data.conversionMeta[i], currentValue);

						}
					}
				}

				putRow(data.outputRowMeta, outputRowData);

			}

			setOutputDone(); // signal end to receiver(s)
			return false; // end of data or error.

		} catch (Exception e) {
			logError("An error occurred, processing will be stopped: " + e.fillInStackTrace());

			setErrors(1);

			stopAll();
			return false;
		}
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		if (log.isBasic())
			logBasic("Finished reading query, closing connection.");

		super.dispose(smi, sdi);
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (MetadataInputStepMeta) smi;
		data = (MetadataInputStepData) sdi;

		if (super.init(smi, sdi))
			try {
			
				return true;
			} catch (Exception e) {
				logError("An error occurred, processing will be stopped: " + e.getMessage());
				setErrors(1);
				stopAll();
			}

		return false;
	}

}