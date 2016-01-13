package tsdb.loader.ki.type;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.StationProperties;
import tsdb.util.DataRow;
import tsdb.util.Util;
import tsdb.util.iterator.TimestampSeries;

/**
 * Base class for data loaders
 * @author woellauer
 *
 */
public abstract class AbstractLoader {	
	private static final Logger log = LogManager.getLogger();

	protected final String[] inputSchema;
	protected final StationProperties properties;
	//protected final ASCTimeSeries csvtimeSeries;
	protected final String sourceInfo;
	
	protected String[] resultSchema = null;
	protected int[] sourcePos;

	public AbstractLoader(String[] inputSchema, StationProperties properties, String sourceInfo) {
		this.inputSchema = inputSchema;
		this.properties = properties;
		this.sourceInfo = sourceInfo;
	}

	protected abstract void createProcessingTypes();
	protected abstract void createResultSchema();	
	protected abstract List<DataRow> toDataRows(TimestampSeries timestampSeries);

	protected boolean createSourcePos(String[] targetSchema) {
		//sourcePos[targetIndex] => sourceIndex
		sourcePos = new int[targetSchema.length];
		for(int i=0;i<sourcePos.length;i++) {
			sourcePos[i] = -1;
		}
		boolean containsValidColumns = false;
		Map<String, Integer> targetIndexMap = Util.stringArrayToMap(targetSchema);
		for(int sourceIndex=0;sourceIndex<resultSchema.length;sourceIndex++) {
			String sensorName = resultSchema[sourceIndex];
			if(sensorName!=null) {
				if(targetIndexMap.containsKey(sensorName)) {
					sourcePos[targetIndexMap.get(sensorName)] = sourceIndex;
					containsValidColumns = true;
				} else {
					log.warn("sensor name not in target schema "+sensorName+" "+getClass().toGenericString());
				}
			} else {
				log.warn("no sensor translation: "+inputSchema[sourceIndex]+" in "+sourceInfo);
			}

		}
		return containsValidColumns;
	}

	public List<DataRow> load(Station station, String[] targetSchema, TimestampSeries timestampSeries) {
		//System.out.println("inputSchema: "+Util.arrayToString(inputSchema));
		createResultSchema();
		createProcessingTypes();

		//System.out.println("resultSchema: "+Util.arrayToString(resultSchema));

		boolean containsValidColumns = createSourcePos(targetSchema);
		if(containsValidColumns) {
			return toDataRows(timestampSeries);			
		} else {
			return null;
		}		
	}

	public String[] getResultSchema() {
		return resultSchema;
	}	
}
