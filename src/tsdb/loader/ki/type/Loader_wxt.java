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
 * data loader for wxt
 * @author woellauer
 *
 */
class Loader_wxt extends AbstractLoader {

	private static final Logger log = LogManager.getLogger();

	private enum ProcessingType {
		NONE,
		COPY,
		SWDR_300,
		SWUR_300,
		LWDR_300,
		LWUR_300,
		COPY_P_200,
		COPY_RH_200,
		COPY_TA_200,
		COPY_WD,
		COPY_WV,
		COPY_TS_10,
		COPY_RAIN_CONTAINER};

		private ProcessingType[] processingTypes = null;

		private float calib_coefficient_SWDR_300 = Float.NaN;
		private float calib_coefficient_SWUR_300 = Float.NaN;
		private float calib_coefficient_LWDR_300 = Float.NaN;
		private float calib_coefficient_LWUR_300 = Float.NaN;

		private int pos_T_CNR = -1;	

		public Loader_wxt(String[] inputSchema, StationProperties properties, String sourceInfo) {
			super(inputSchema,properties, sourceInfo);
		}

		/*@Override
	protected void createResultSchema() {// old processing with fixed sensor order
		final String PLACE_HOLDER_W_R_300_U = "PLACE_HOLDER_W_R_300_U";
		resultSchema = new String[inputSchema.length];
		int PLACE_HOLDER_W_R_300_U_count = 0;
		for(int schemaIndex=0;schemaIndex<inputSchema.length;schemaIndex++) {
			switch(inputSchema[schemaIndex]) {
			case PLACE_HOLDER_W_R_300_U:
				//TODO: reverse entries for some time interval
				PLACE_HOLDER_W_R_300_U_count++;
				switch(PLACE_HOLDER_W_R_300_U_count) {
				case 1:
					resultSchema[schemaIndex] = "SWDR_300";
					break;
				case 2:
					resultSchema[schemaIndex] = "SWUR_300";
					break;
				case 3:
					resultSchema[schemaIndex] = "LWDR_300";
					break;
				case 4:
					resultSchema[schemaIndex] = "LWUR_300";
					break;
				default:
					log.warn("no real name for column "+schemaIndex+"  "+inputSchema[schemaIndex]);
					resultSchema[schemaIndex] = inputSchema[schemaIndex];
				}
				break;
			default:
				resultSchema[schemaIndex] = inputSchema[schemaIndex];	
			}
		}
	}*/

		@Override
		protected void createResultSchema() { // processing with sensor translation in inventory
			final String PLACE_HOLDER_W_R_300_U = "PLACE_HOLDER_W_R_300_U";
			resultSchema = new String[inputSchema.length];
			int PLACE_HOLDER_W_R_300_U_count = 0;
			for(int schemaIndex=0;schemaIndex<inputSchema.length;schemaIndex++) {
				switch(inputSchema[schemaIndex]) {
				case PLACE_HOLDER_W_R_300_U:
					PLACE_HOLDER_W_R_300_U_count++;
					switch(PLACE_HOLDER_W_R_300_U_count) {
					case 1:
						translateWithProperty(schemaIndex,"SERIAL_PYR01");
						break;
					case 2:
						translateWithProperty(schemaIndex,"SERIAL_PYR02");
						break;
					case 3:
						translateWithProperty(schemaIndex,"SERIAL_PAR01");
						break;
					case 4:
						translateWithProperty(schemaIndex,"SERIAL_PAR02");
						break;
					default:
						log.warn("no real name for column "+schemaIndex+"  "+inputSchema[schemaIndex]);
						resultSchema[schemaIndex] = inputSchema[schemaIndex];
					}
					break;
				default:
					resultSchema[schemaIndex] = inputSchema[schemaIndex];	
				}
			}
		}

		private String mapTitleToName(String title) {
			switch(title.toLowerCase()) {
			case "swdr":
				return "SWDR_300";
			case "swur":			
				return "SWUR_300";
			case "lwdr":
				return "LWDR_300";
			case "lwur":
				return "LWUR_300";
			default:
				return null;
			}
		}

		private void translateWithProperty(int schemaIndex, String propertyName) {
			String prop = properties.getProperty(propertyName);
			String name = mapTitleToName(prop);
			if(name!=null) {
				resultSchema[schemaIndex] = name;	
			} else {
				log.warn("no real name for column "+schemaIndex+"  "+inputSchema[schemaIndex]+"  propery "+prop+"    "+sourceInfo);
				resultSchema[schemaIndex] = inputSchema[schemaIndex];
			}
		}

		/*@Override
	protected void createResultSchema() {// processing with two separated source name translations
		final String PLACE_HOLDER_VOLTAGE_DVM = "PLACE_HOLDER_VOLTAGE_DVM";
		final String PLACE_HOLDER_VOLTAGE_HI_IMP = "PLACE_HOLDER_VOLTAGE_HI_IMP";
		resultSchema = new String[inputSchema.length];
		int PLACE_HOLDER_VOLTAGE_DVM_count = 0;
		int PLACE_HOLDER_VOLTAGE_HI_IMP_count = 0;
		for(int schemaIndex=0;schemaIndex<inputSchema.length;schemaIndex++) {
			switch(inputSchema[schemaIndex]) {
			case PLACE_HOLDER_VOLTAGE_DVM:
				PLACE_HOLDER_VOLTAGE_DVM_count++;
				switch(PLACE_HOLDER_VOLTAGE_DVM_count) {
				case 1:
					translateWithProperty(schemaIndex,"SERIAL_PYR01");					
					break;
				case 2: {
					translateWithProperty(schemaIndex,"SERIAL_PYR02");					
					break;
				}
				default:
					log.warn("no real name for column "+schemaIndex+"  "+inputSchema[schemaIndex]);
					resultSchema[schemaIndex] = inputSchema[schemaIndex];
				}
				break;
			case PLACE_HOLDER_VOLTAGE_HI_IMP:
				PLACE_HOLDER_VOLTAGE_HI_IMP_count++;
				switch(PLACE_HOLDER_VOLTAGE_HI_IMP_count) {
				case 1: 
					translateWithProperty(schemaIndex,"SERIAL_PAR01");					
					break;				
				case 2: {
					translateWithProperty(schemaIndex,"SERIAL_PAR02");					
					break;
				}
				default:
					log.warn("no real name for column "+schemaIndex+"  "+inputSchema[schemaIndex]);
					resultSchema[schemaIndex] = inputSchema[schemaIndex];
				}
				break;
			default:
				resultSchema[schemaIndex] = inputSchema[schemaIndex];	
			}
		}
	}*/

		@Override
		protected void createProcessingTypes() {
			processingTypes = new ProcessingType[resultSchema.length];

			for(int schemaIndex=0; schemaIndex<resultSchema.length; schemaIndex++) {
				switch(resultSchema[schemaIndex]) {
				case "SWDR_300":
					calib_coefficient_SWDR_300 = properties.getFloatProperty("wxt_SWDR_300");
					processingTypes[schemaIndex] = ProcessingType.SWDR_300;
					break;
				case "SWUR_300":
					calib_coefficient_SWUR_300 = properties.getFloatProperty("wxt_SWUR_300");
					processingTypes[schemaIndex] = ProcessingType.SWUR_300;
					break;
				case "LWDR_300":
					calib_coefficient_LWDR_300 = properties.getFloatProperty("wxt_LWDR_300");
					processingTypes[schemaIndex] = ProcessingType.LWDR_300;
					break;
				case "LWUR_300":
					calib_coefficient_LWUR_300 = properties.getFloatProperty("wxt_LWUR_300");
					processingTypes[schemaIndex] = ProcessingType.LWUR_300;
					break;
				case "T_CNR":
					pos_T_CNR = schemaIndex;
					processingTypes[schemaIndex] = ProcessingType.COPY; //TODO: maybe change
					break;
				case "p_200":
					processingTypes[schemaIndex] = ProcessingType.COPY_P_200;
					break;
				case "rH_200":
					processingTypes[schemaIndex] = ProcessingType.COPY_RH_200;
					break;
				case "Ta_200":
					processingTypes[schemaIndex] = ProcessingType.COPY_TA_200;
					break;		
				case "WD":
					processingTypes[schemaIndex] = ProcessingType.COPY_WD;
					break;
				case "WV":
					processingTypes[schemaIndex] = ProcessingType.COPY_WV;
					break;
				case "Ts_10":
					processingTypes[schemaIndex] = ProcessingType.COPY_TS_10;
					break;
				case "rain_container":
					processingTypes[schemaIndex] = ProcessingType.COPY_RAIN_CONTAINER;
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
						case COPY_P_200: {
							final float NAN_1 = 599.992F;
							final float NAN_2 = 599.99F;
							final float v = entry.data[sourceIndex];
							if(v==NAN_1||NAN_2==v) {
								eventData[schemaIndex] = Float.NaN;
							} else {
								eventData[schemaIndex] = v;
							}
							break;
						}
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
						case COPY_TA_200: {
							final float NAN_1 = 0.0f;
							final float NAN_2 = -60.0f;
							final float NAN_3 = 8.94303E-7f;
							final float v = entry.data[sourceIndex];
							if(v==NAN_1||v==NAN_2||v==NAN_3) {
								eventData[schemaIndex] = Float.NaN;
							} else {
								eventData[schemaIndex] = v;
							}
							break;
						}
						case COPY_WD: {
							final float NAN_1 = 0.0f;
							final float NAN_2 = 9.31323E-10f;
							final float v = entry.data[sourceIndex];
							if(v==NAN_1||v==NAN_2) {
								eventData[schemaIndex] = Float.NaN;
							} else {
								eventData[schemaIndex] = v;
							}
							break;
						}
						case COPY_WV: {
							final float NAN_1 = -0.0f;
							final float NAN_2 = 0.0998f;
							final float NAN_3 = -3.49246E-10f;
							final float NAN_4 = 0.0998168f;
							final float v = entry.data[sourceIndex];
							if(v==NAN_1||v==NAN_2||v==NAN_3||v==NAN_4) {
								eventData[schemaIndex] = Float.NaN;
							} else {
								eventData[schemaIndex] = v;
							}
							break;
						}
						case COPY_TS_10: {
							final float NAN_1 = -45.0F;
							final float NAN_2 = -45.002F;
							final float NAN_3 = -45.0019F;
							final float v = entry.data[sourceIndex];
							if(v==NAN_1||v==NAN_2||v==NAN_3) {
								eventData[schemaIndex] = Float.NaN;
							} else {
								eventData[schemaIndex] = v;
							}
							break;
						}
						case COPY_RAIN_CONTAINER: {
							final float NAN_1 = 4.65661E-10F;
							final float NAN_2 = 0.00915751F;
							final float NAN_3 = 0.009F;
							final float NAN_4 = 0.0F;
							final float v = entry.data[sourceIndex];
							if(v==NAN_1||v==NAN_2||v==NAN_3||v==NAN_4) {
								eventData[schemaIndex] = Float.NaN;
							} else {
								eventData[schemaIndex] = v;
							}
							break;
						}								
						case SWDR_300: //value <- (raw * 1000) / calib_coefficient
							eventData[schemaIndex] = (entry.data[sourceIndex]*1000f)/calib_coefficient_SWDR_300;
							break;
						case SWUR_300: //value <- (raw * 1000) / calib_coefficient
							eventData[schemaIndex] = (entry.data[sourceIndex]*1000f)/calib_coefficient_SWUR_300;
							break; 
						case LWDR_300: //value <- ((raw * 1000) / calib_coefficient) + (5.672E-08 * (T_CNR + 273.15)**4)
							eventData[schemaIndex] = (entry.data[sourceIndex]*1000f)/calib_coefficient_LWDR_300 + (5.672E-08f*((float)Math.pow(entry.data[pos_T_CNR] + 273.15, 4)));
							break;
						case LWUR_300: //value <- ((raw * 1000) / calib_coefficient) + (5.672E-08 * (T_CNR + 273.15)**4)
							eventData[schemaIndex] = (entry.data[sourceIndex]*1000f)/calib_coefficient_LWUR_300 + (5.672E-08f*((float)Math.pow(entry.data[pos_T_CNR] + 273.15, 4)));
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
