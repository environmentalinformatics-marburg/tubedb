package tsdb.run;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Plot;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.Sensor;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.processing.Addition;
import tsdb.graph.processing.AdditionFirst;
import tsdb.graph.processing.Averaged;
import tsdb.graph.processing.Subtraction;
import tsdb.util.DataQuality;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

/**
 * create cache of average values from groups of stations
 * @author woellauer
 *
 */
public class CreateStationGroupAverageCache {

	private static final Logger log = LogManager.getLogger();

	public interface CbPrint {
		public void println(String text);
	}

	private final TsDB tsdb;
	private CbPrint cbPrint;

	public static void main(String[] args) {
		log.info("create averages...");
		TsDB tsdb = TsDBFactory.createDefault();
		new CreateStationGroupAverageCache(tsdb).run();
		tsdb.close();
		log.info("...end");
	}

	public CreateStationGroupAverageCache(TsDB tsdb) {
		this(tsdb,text->log.info(text));
	}

	public CreateStationGroupAverageCache(TsDB tsdb, CbPrint cbPrint) {
		throwNulls(tsdb,cbPrint);
		this.tsdb = tsdb;
		this.cbPrint = cbPrint;
	}

	public void run() {

		long startRunTime = System.currentTimeMillis();

		ContinuousGen continuousGen = QueryPlanGenerators.getContinuousGen(tsdb, DataQuality.STEP);

		for(String group:tsdb.getGeneralStationGroups()) {
			log.info("create average of group "+group);
			List<String> plotList = tsdb.getStationAndVirtualPlotNames(group).collect(Collectors.toList());

			TreeSet<String> sensorNameSet = new TreeSet<String>(); 
			for(String plotID:plotList) {
				String[] sensorNames = tsdb.getSensorNamesOfPlot(plotID);
				sensorNames = tsdb.includeVirtualSensorNames(sensorNames);
				if(sensorNames == null || sensorNames.length == 0) {
					continue;
				}
				sensorNames = tsdb.getBaseSchema(sensorNames);
				if(sensorNames == null || sensorNames.length == 0) {
					continue;
				}
				sensorNameSet.addAll(Arrays.asList(sensorNames));
			}			
			//log.info(sensorNameSet);

			for(String processingSensorName:sensorNameSet) {
				Sensor sensor = tsdb.getSensor(processingSensorName);
				if(sensor == null || sensor.getEmpiricalDiff() == null || !Float.isFinite(sensor.getEmpiricalDiff())) {
					continue;
				}
				
				//log.info(processingSensorName);

				long groupMinTimestamp = Long.MAX_VALUE;
				long groupMaxTimestamp = Long.MIN_VALUE;
				for(String plotID:plotList) {
					String[] sensorNames = tsdb.getSensorNamesOfPlot(plotID);
					sensorNames = tsdb.includeVirtualSensorNames(sensorNames);
					if(sensorNames==null) {
						continue;
					}
					boolean sensorContained = false;
					for(String s:sensorNames) {
						if(s.equals(processingSensorName)) {
							sensorContained = true;
							break;
						}						
					}
					if(!sensorContained)  {
						continue;
					}
					if(processingSensorName.equals("WD")) {
						continue;
					}

					long[] interval = tsdb.getBaseTimeInterval(plotID);				
					if(interval!=null) {
						if(interval[0]<groupMinTimestamp) {
							groupMinTimestamp = interval[0];
						}
						if(groupMaxTimestamp<interval[1]) {
							groupMaxTimestamp = interval[1];
						}
					}
				}

				if(groupMinTimestamp==Long.MAX_VALUE || groupMaxTimestamp==Long.MIN_VALUE) {
					continue;
				}

				//log.info(group+"  "+processingSensorName+" ********************************* "+TimeUtil.oleMinutesToLocalDateTime(groupMinTimestamp)+"\t - \t"+TimeUtil.oleMinutesToLocalDateTime(groupMaxTimestamp)+" **************************************************************** "+groupMinTimestamp+"\t-\t"+groupMaxTimestamp);
				List<Continuous> sources = new ArrayList<Continuous>();
				List<Continuous> additions = new ArrayList<Continuous>();
				for(String plotID:plotList) {
					try {
						String[] sensorNames = tsdb.getSensorNamesOfPlot(plotID);
						sensorNames = tsdb.includeVirtualSensorNames(sensorNames);
						if(sensorNames==null) {
							continue;
						}
						boolean sensorContained = false;
						for(String s:sensorNames) {
							if(s.equals(processingSensorName)) {
								sensorContained = true;
								break;
							}						
						}
						if(!sensorContained)  {
							continue;
						}


						Plot plot = tsdb.getPlot(plotID);
						if(plot!=null && plot.existData()) {
							String[] schema = new String[]{processingSensorName};
							schema = tsdb.supplementSchema(schema, tsdb.includeVirtualSensorNames(plot.getSensorNames()));
							//log.info(Arrays.toString(schema)+"of "+Arrays.toString(plot.getSensorNames()));
							Continuous continuous = continuousGen.get(plotID, schema);
							if(continuous!=null) {
								Subtraction subtraction = Subtraction.createWithElevationTemperature(tsdb,continuous,plotID);
								if(subtraction!=null) {
									//log.info("with ref " + plotID + " " + processingSensorName);
									additions.add(subtraction);
								}
								sources.add(continuous);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.warn(e);
					}
				}

				final int MIN_AVERAGE = 3;

				Averaged averaged = null;

				if(averaged==null && additions.size()>=MIN_AVERAGE) {
					averaged = Averaged.of(tsdb, additions, MIN_AVERAGE);
				}

				if(averaged==null && sources.size()>=MIN_AVERAGE) {
					averaged = Averaged.of(tsdb, sources, MIN_AVERAGE);
				}

				if(averaged!=null) {
					TsIterator it = averaged.get(groupMinTimestamp, groupMaxTimestamp);
					if(it!=null&&it.hasNext()) {
						//tsdb.cacheStorage.writeNew(group, averaged.get(groupMinTimestamp, groupMaxTimestamp));
						TimestampSeries timestampSeries = it.toTimestampSeries(group);
						tsdb.streamCache.insertTimestampSeries(timestampSeries);
					    log.info(group+"/"+processingSensorName+" <- "+averaged.getSourceText());
					} else {
						log.warn("averages: "+group);
					}
				} else {
					log.trace(group+"/"+processingSensorName+" not enough sources for average");
				}
			}
			//cbPrint.println(group+" -> "+list);
		}

		long endRunTime = System.currentTimeMillis();
		cbPrint.println("CreateStationGroupAverageCache run time: "+(endRunTime-startRunTime)/1000+" s");

	}

}
