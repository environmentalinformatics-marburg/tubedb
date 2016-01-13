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
 * data loader for pu2
 * @author woellauer
 *
 */
class Loader_pu2 extends AbstractLoader {
	
	private static final Logger log = LogManager.getLogger();

	private enum ProcessingType {NONE,COPY,PU2_1,PU2_2};

	private ProcessingType[] processingTypes = null;
	private float calib_coefficient_pu2_1 = Float.NaN;
	private float calib_coefficient_pu2_2 = Float.NaN;

	public Loader_pu2(String[] inputSchema, StationProperties properties, String sourceInfo) {
		super(inputSchema,properties, sourceInfo);
	}

	@Override
	protected void createResultSchema() {
		resultSchema = new String[inputSchema.length];	
		int place_holder_rt_nrt_count = 0;
		for(int schemaIndex=0;schemaIndex<inputSchema.length;schemaIndex++) {
			switch(inputSchema[schemaIndex]) {
			case "PLACE_HOLDER_RT_NRT_I":
				place_holder_rt_nrt_count++;
				switch(place_holder_rt_nrt_count) {
				case 1:
					String pu2_1_type = properties.getProperty("pu2_1_type");
					switch(pu2_1_type) {
					case "rain":
						resultSchema[schemaIndex] = "P_RT_NRT_01";
						break;
					case "fog":
						resultSchema[schemaIndex] = "F_RT_NRT_01";
						break;
					case "tf":
						resultSchema[schemaIndex] = "T_RT_NRT_01";
						break;
					default:
						log.warn("type unknown: "+pu2_1_type);
						resultSchema[schemaIndex] = inputSchema[schemaIndex];
					}
					break;
				case 2:
					String pu2_2_type = properties.getProperty("pu2_2_type");
					switch(pu2_2_type) {
					case "rain":
						resultSchema[schemaIndex] = "P_RT_NRT_02";
						break;
					case "fog":
						resultSchema[schemaIndex] = "F_RT_NRT_02";
						break;
					case "tf":
						resultSchema[schemaIndex] = "T_RT_NRT_02";
						break;
					default:
						log.warn("type unknown: "+pu2_2_type);
						resultSchema[schemaIndex] = inputSchema[schemaIndex];
					}
					break;
				default:
					log.warn("more than two place_holder_rt_nrt: "+place_holder_rt_nrt_count);
				}
				break;						
			default:
				resultSchema[schemaIndex] = inputSchema[schemaIndex];	
			}
		}
	}

	@Override
	protected void createProcessingTypes() {
		processingTypes = new ProcessingType[resultSchema.length];
		for(int schemaIndex=0; schemaIndex<resultSchema.length; schemaIndex++) {
			switch(resultSchema[schemaIndex]) {
			case "P_RT_NRT_01":
				calib_coefficient_pu2_1 = properties.getFloatProperty("pu2_1");
				processingTypes[schemaIndex] = ProcessingType.PU2_1;
				break;
			case "P_RT_NRT_02":
				calib_coefficient_pu2_2 = properties.getFloatProperty("pu2_2");
				processingTypes[schemaIndex] = ProcessingType.PU2_2;
				break;
			case "F_RT_NRT_01":
				calib_coefficient_pu2_1 = properties.getFloatProperty("pu2_1");
				processingTypes[schemaIndex] = ProcessingType.PU2_1;
				break;
			case "F_RT_NRT_02":
				calib_coefficient_pu2_2 = properties.getFloatProperty("pu2_2");
				processingTypes[schemaIndex] = ProcessingType.PU2_2;
				break;
			case "T_RT_NRT_01":
				calib_coefficient_pu2_1 = properties.getFloatProperty("pu2_1");
				processingTypes[schemaIndex] = ProcessingType.PU2_1;
				break;
			case "T_RT_NRT_02":
				calib_coefficient_pu2_2 = properties.getFloatProperty("pu2_2");
				processingTypes[schemaIndex] = ProcessingType.PU2_2;
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
					case PU2_1: // value <- raw * calib_coefficient
						eventData[schemaIndex] = entry.data[sourceIndex]*calib_coefficient_pu2_1;
						break;
					case PU2_2: // value <- raw * calib_coefficient
						eventData[schemaIndex] = entry.data[sourceIndex]*calib_coefficient_pu2_2;
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
