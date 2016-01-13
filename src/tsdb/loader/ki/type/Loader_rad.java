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
 * data loader for rad
 * @author woellauer
 *
 */
class Loader_rad extends AbstractLoader {
	
	private static final Logger log = LogManager.getLogger();

	private enum ProcessingType {NONE,COPY,FACTOR_10};

	private ProcessingType[] processingTypes = null;

	public Loader_rad(String[] inputSchema, StationProperties properties, String sourceInfo) {
		super(inputSchema,properties, sourceInfo);
	}

	@Override
	protected void createResultSchema() {
		final String NaN = "NaN";		
		String serial_PYR01_name = properties.getProperty("SERIAL_PYR01");
		String serial_PYR02_name = properties.getProperty("SERIAL_PYR02");
		String serial_PAR01_name = properties.getProperty("SERIAL_PAR01");
		String serial_PAR02_name = properties.getProperty("SERIAL_PAR02");
		
		boolean serial_PYR01_is_value = serial_PYR01_name.equals(NaN)?false:true;
		boolean serial_PYR02_is_value = serial_PYR02_name.equals(NaN)?false:true;
		boolean serial_PAR01_is_value = serial_PAR01_name.equals(NaN)?false:true;
		boolean serial_PAR02_is_value = serial_PAR02_name.equals(NaN)?false:true;

		resultSchema = new String[inputSchema.length];
		int place_holder_count = 0;
		for(int schemaIndex=0;schemaIndex<inputSchema.length;schemaIndex++) {
			switch(inputSchema[schemaIndex]) {
			case "PLACE_HOLDER_RAD":
				place_holder_count++;
				switch(place_holder_count) {
				case 1:
					if(serial_PYR01_is_value&&!serial_PAR01_is_value) {
						resultSchema[schemaIndex] = "swdr_01";
					} else if(serial_PAR01_is_value&&!serial_PYR01_is_value) {
						resultSchema[schemaIndex] = "par_01";
					} else {
						//log.warn("no entry found");
						resultSchema[schemaIndex] = null;
					}
					break;
				case 2:
					if(serial_PYR02_is_value&&!serial_PAR02_is_value) {
						resultSchema[schemaIndex] = "swdr_02";
					} else if(serial_PAR02_is_value&&!serial_PYR02_is_value) {
						resultSchema[schemaIndex] = "par_02";
					} else {
						//log.warn("no entry found in "+csvtimeSeries.filename);
						resultSchema[schemaIndex] = null;
					}
					break;					
				default:
					log.warn("no entry found");
					resultSchema[schemaIndex] = inputSchema[schemaIndex];
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
			if(resultSchema[schemaIndex]!=null) {
			switch(resultSchema[schemaIndex]) {
			case "swdr_01":
				processingTypes[schemaIndex] = ProcessingType.FACTOR_10;
				break;
			case "swdr_02":
				processingTypes[schemaIndex] = ProcessingType.FACTOR_10;
				break;
			case "par_01":
				processingTypes[schemaIndex] = ProcessingType.FACTOR_10;
				break;
			case "par_02":
				processingTypes[schemaIndex] = ProcessingType.FACTOR_10;
				break;					
			default:
				processingTypes[schemaIndex] = ProcessingType.COPY;
			}
		} else {
			processingTypes[schemaIndex] = ProcessingType.NONE;
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
					case FACTOR_10: // value <- raw * 10
						eventData[schemaIndex] = entry.data[sourceIndex]*10f;
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
