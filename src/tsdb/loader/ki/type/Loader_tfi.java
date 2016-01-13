package tsdb.loader.ki.type;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.StationProperties;
import tsdb.util.DataRow;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TimestampSeries;

/**
 * data loader for tfi (old format)
 * @author woellauer
 *
 */
@Deprecated
class Loader_tfi extends AbstractLoader {

	private static final Logger log = LogManager.getLogger();

	private enum ProcessingType {NONE,COPY,B,RAINFALL,FOG};

	private ProcessingType[] processingTypes = null;

	private float calib_coefficient_b = Float.NaN;
	private float calib_coefficient_rainfall = Float.NaN;
	private float calib_coefficient_fog = Float.NaN;


	public Loader_tfi(String[] inputSchema, StationProperties properties, String sourceInfo) {
		super(inputSchema,properties, sourceInfo);
	}

	@Override
	protected void createResultSchema() {
		resultSchema = new String[inputSchema.length];
		for(int schemaIndex=0;schemaIndex<inputSchema.length;schemaIndex++) {
			//switch(inputSchema[schemaIndex]) {
			//default:
			resultSchema[schemaIndex] = inputSchema[schemaIndex];	
			//}
		}
	}

	@Override
	protected void createProcessingTypes() {
		processingTypes = new ProcessingType[resultSchema.length];
		for(int schemaIndex=0; schemaIndex<resultSchema.length; schemaIndex++) {
			switch(resultSchema[schemaIndex]) {
			case "B_01":
			case "B_02":
			case "B_03":
			case "B_04":
			case "B_05":
			case "B_06":
			case "B_07": 
			case "B_08": 
			case "B_09": 
			case "B_10": 
			case "B_11": 
			case "B_12": 
			case "B_13": 
			case "B_14": 
			case "B_15": 
			case "B_16": 
			case "B_17": 
			case "B_18": 
			case "B_19": 
			case "B_20": 
			case "B_21": 
			case "B_22": 
			case "B_23": 
			case "B_24": 
			case "B_25": 
			case "B_26": 
			case "B_27": 
			case "B_28": 
			case "B_29": 
			case "B_30":
				processingTypes[schemaIndex] = ProcessingType.B;
				calib_coefficient_b = properties.getFloatProperty("pu1_P_RT_NRT");
				break;
			case "Rainfall":
				processingTypes[schemaIndex] = ProcessingType.RAINFALL;
				calib_coefficient_rainfall = properties.getFloatProperty("pu2_1");
				break;
			case "Fog":
				processingTypes[schemaIndex] = ProcessingType.FOG;
				calib_coefficient_fog = properties.getFloatProperty("pu2_2");
				break;
			default:
				processingTypes[schemaIndex] = ProcessingType.COPY;
			}
		}		

	}

	@Override
	protected List<DataRow> toDataRows(TimestampSeries timestampSeries) {
		List<DataRow> eventList = new ArrayList<DataRow>(timestampSeries.entryList.size());
		for(TsEntry entry:timestampSeries.entryList) {
			Float[] eventData = new Float[sourcePos.length];
			for(int schemaIndex=0;schemaIndex<sourcePos.length;schemaIndex++) {
				int sourceIndex = sourcePos[schemaIndex];
				if(sourceIndex==-1) {
					eventData[schemaIndex] = Float.NaN;
				} else {						
					switch(processingTypes[sourceIndex]) {
					case B: // value <- raw * calib_coefficient
						eventData[schemaIndex] = entry.data[sourceIndex]*calib_coefficient_b;
						break;
					case RAINFALL: // value <- raw * calib_coefficient
						eventData[schemaIndex] = entry.data[sourceIndex]*calib_coefficient_rainfall;
						break;						
					case FOG: // value <- raw * calib_coefficient
						eventData[schemaIndex] = entry.data[sourceIndex]*calib_coefficient_fog;
						break;							
					case COPY:
						eventData[schemaIndex] = entry.data[sourceIndex];
						break;
					default:
						log.warn("processingType unknown: "+processingTypes[sourceIndex]);
						eventData[schemaIndex] = Float.NaN;
					}						
				}
			}
			eventList.add(new DataRow(eventData, entry.timestamp));
		}
		return eventList;
	}



}
