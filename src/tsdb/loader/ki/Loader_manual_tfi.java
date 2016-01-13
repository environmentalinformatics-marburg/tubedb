package tsdb.loader.ki;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	
	private static final Logger log = LogManager.getLogger();

	private final TimestampSeries timestampSeries;
	private String[] inputSchema = null;

	private int[] sourcePos = null;

	public Loader_manual_tfi(TimestampSeries timestampSeries) {
		this.timestampSeries = timestampSeries;
		this.inputSchema = timestampSeries.sensorNames;
	}

	protected boolean createSourcePos(String[] targetSchema) {
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
					containsValidColumns = true;
				} else {
					log.warn("sensor name not in target schema '"+sensorName+"' "+getClass().toGenericString()+"   "+timestampSeries.name);
				}
			} else {
				log.warn("no sensor translation: "+inputSchema[sourceIndex]);
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
			Float[] rowData = new Float[sourcePos.length];
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
