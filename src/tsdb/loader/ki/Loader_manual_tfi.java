package tsdb.loader.ki;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.tinylog.Logger;

import tsdb.util.DataRow;
import tsdb.util.TsEntry;
import tsdb.util.Util;
import tsdb.util.iterator.TimestampSeries;

/**
 * loader for manual collected tfi data
 * @author woellauer
 *
 */
class Loader_manual_tfi {
	
	

	private final TimestampSeries timestampSeries;
	private String[] inputSchema = null;

	private int[] sourcePos = null;
	
	public String[] docuTranslation = null;

	public Loader_manual_tfi(TimestampSeries timestampSeries) {
		this.timestampSeries = timestampSeries;
		this.inputSchema = timestampSeries.sensorNames;
	}

	protected boolean createSourcePos(String[] targetSchema) {
		docuTranslation = new String[inputSchema.length];
		//sourcePos[targetIndex] => sourceIndex
		sourcePos = new int[targetSchema.length];
		for(int i=0;i<sourcePos.length;i++) {
			sourcePos[i] = -1;
		}
		boolean containsValidColumns = false;
		Map<String, Integer> targetIndexMap = Util.stringArrayToMap(targetSchema);
		for(int sourceIndex=0;sourceIndex<inputSchema.length;sourceIndex++) {
			String sensorName = inputSchema[sourceIndex];
			if(sensorName!=null) {
				if(targetIndexMap.containsKey(sensorName)) {
					sourcePos[targetIndexMap.get(sensorName)] = sourceIndex;
					docuTranslation[sourceIndex] = sensorName;
					containsValidColumns = true;
				} else {
					Logger.warn("sensor name not in target schema '"+sensorName+"' "+getClass().toGenericString()+"   "+timestampSeries.name);
				}
			} else {
				Logger.warn("no sensor translation: "+inputSchema[sourceIndex]);
			}

		}
		return containsValidColumns;
	}

	public List<DataRow> load(String[] targetSchema) {
		boolean containsValidColumns = createSourcePos(targetSchema);
		if(containsValidColumns) {
			return toDataRows();			
		} else {
			return null;
		}		
	}

	public List<DataRow> toDataRows() {
		List<DataRow> rowList = new ArrayList<DataRow>(timestampSeries.entryList.size());
		for(TsEntry entry:timestampSeries.entryList) {
			float[] rowData = new float[sourcePos.length];
			for(int schemaIndex=0;schemaIndex<sourcePos.length;schemaIndex++) {
				int sourceIndex = sourcePos[schemaIndex];
				if(sourceIndex==-1) {
					rowData[schemaIndex] = Float.NaN;
				} else {
					rowData[schemaIndex] = entry.data[sourceIndex];								
				}
			}
			rowList.add(new DataRow(rowData, entry.timestamp));
		}
		return rowList;
	}
}
