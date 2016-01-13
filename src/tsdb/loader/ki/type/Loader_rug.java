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
 * data loader for rug
 * @author woellauer
 *
 */
class Loader_rug extends AbstractLoader {

	private static final Logger log = LogManager.getLogger();

	private enum ProcessingType {NONE,COPY,COPY_RH_200};

	private ProcessingType[] processingTypes = null;

	public Loader_rug(String[] inputSchema, StationProperties properties, String sourceInfo) {
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
			case "rH_200":
				processingTypes[schemaIndex] = ProcessingType.COPY_RH_200;
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
					case COPY:
						eventData[schemaIndex] = entry.data[sourceIndex];
						break;
					case COPY_RH_200: {
						final float NAN_1 = 0.0f;
						final float NAN_2 = 2.32831E-10f;
						final float v = entry.data[sourceIndex];
						if(v==NAN_1||v==NAN_2) {
							eventData[schemaIndex] = Float.NaN;
						} else {
							eventData[schemaIndex] = v;
						}
						break;
					}						
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
