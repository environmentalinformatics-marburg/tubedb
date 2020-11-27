package tsdb.run;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.GeneralStation;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.MiniCSV;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

public class Statistics {
	private static final Logger log = LogManager.getLogger();

	private static final String[] generalStationNames = new String[]{"AEG", "AEW", "HEG", "HEW", "SEG", "SEW"};
	//private static final String[] generalStationNames = new String[]{"SEG"};
	private static final String[] sensorNames = new String[]{"Ta_200", "P_RT_NRT", "SM_10","Rn_300", "rH_200", "SM_20", "sunshine", "SWDR_300", "SWDR_3700", "SWDR_4400", "P_RT_NRT_01", "P_RT_NRT_02"};
	//private static final String[] sensorNames = new String[]{"sunshine"};
	private static final int[] years = new int[]{2009, 2010, 2011, 2012, 2013, 2014, 2015};
	//private static final int[] years = new int[]{2015};


	private static final AggregationInterval aggregationInterval = AggregationInterval.HOUR;
	private static final DataQuality dataQuality = DataQuality.EMPIRICAL;
	private static final boolean interpolated = true;	

	public static void main(String[] args) {	
		TsDB tsdb = TsDBFactory.createDefault();
		try {
			String summary_filename = "c:/temp2/stat/summary.csv";
			MiniCSV csvSummary = new MiniCSV(summary_filename, true);
			csvSummary.writeString("parameter");
			csvSummary.writeString("year");
			csvSummary.writeString("general");
			csvSummary.writeString("plot_count");
			csvSummary.writeString("whole_count");
			csvSummary.writeString("real_count");
			csvSummary.writeString("interpolated_count");
			csvSummary.finishRow();
			for(String sensorName:sensorNames) {
				String[] columnNames = new String[]{sensorName};
				for(int year:years) {				
					for(String generalStationName:generalStationNames) {

						int summary_plot_count = 0;
						int summary_whole_count = 0;
						int summary_real_count = 0;
						int summary_interpolated_count = 0;

						String filename = "c:/temp2/stat/"+generalStationName+"_"+sensorName+"_"+year+".csv";
						MiniCSV csv = new MiniCSV(filename, true);
						csv.writeString("plotID");
						csv.writeString("parameter");
						csv.writeString("year");
						csv.writeString("whole_count");
						csv.writeString("real_count");
						csv.writeString("interpolated_count");
						csv.finishRow();
						GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);

						Iterator<String> plotIt = generalStation.getStationAndVirtualPlotNames().iterator();

						while(plotIt.hasNext()) {
							String plotID = plotIt.next();
							String[] plotColumnNames = tsdb.supplementSchema(plotID, columnNames);
							
							if(tsdb.getValidSchema(plotID, plotColumnNames).length>0) {
								//log.info(plotID+"  "+Arrays.toString(plotColumnNames));
								try {								
									Node node = QueryPlan.plot(tsdb, plotID, plotColumnNames, aggregationInterval, dataQuality, interpolated);
									TsIterator it = node.get(TimeUtil.ofDateStartHour(year), TimeUtil.ofDateEndHour(year));
									if(it!=null) {
										//log.info(it);
										int whole_count = 0;
										int real_count = 0;
										int interpolated_count = 0;
										while(it.hasNext()) {										
											TsEntry e = it.next();
											whole_count++;
											//log.info(e);
											if(Float.isFinite(e.data[0])) {
												if(e.interpolated!=null && e.interpolated[0]) {
													interpolated_count++;
												} else {
													real_count++;
												}
											}
										}

										csv.writeString(plotID);
										csv.writeString(sensorName);
										csv.writeLong(year);
										csv.writeLong(whole_count);
										csv.writeLong(real_count);
										csv.writeLong(interpolated_count);
										csv.finishRow();
										log.info(plotID+"/"+sensorName+":"+year+"  "+whole_count+"  "+real_count+"  "+interpolated_count);
										summary_plot_count++;
										summary_whole_count += whole_count;
										summary_real_count += real_count;
										summary_interpolated_count += interpolated_count;
									}
								} catch(Exception e) {
									//e.printStackTrace();
									log.warn(plotID+"/"+sensorName+"  "+e);
								}
							}
						}
						csv.close();

						csvSummary.writeString(sensorName);
						csvSummary.writeLong(year);
						csvSummary.writeString(generalStationName);
						csvSummary.writeLong(summary_plot_count);
						csvSummary.writeLong(summary_whole_count);
						csvSummary.writeLong(summary_real_count);
						csvSummary.writeLong(summary_interpolated_count);
						csvSummary.finishRow();
					}					
				}
			}
			csvSummary.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		} finally {
			tsdb.close();
		}
	}

}
