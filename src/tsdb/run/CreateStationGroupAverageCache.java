package tsdb.run;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;


import org.tinylog.Logger;

import tsdb.Plot;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.Sensor;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.processing.Averaged;
import tsdb.graph.processing.AveragedCounted;
import tsdb.graph.processing.MedianCounted;
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

	

	public interface CbPrint {
		public void println(String text);
	}

	private final TsDB tsdb;
	private CbPrint cbPrint;

	public static void main(String[] args) {
		Logger.info("create reference cache...");
		TsDB tsdb = TsDBFactory.createDefault();
		new CreateStationGroupAverageCache(tsdb).run();
		tsdb.close();
		Logger.info("...end");
	}

	public CreateStationGroupAverageCache(TsDB tsdb) {
		this(tsdb,text->Logger.info(text));
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
			Logger.info("create reference of group "+group);
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
			//Logger.info(sensorNameSet);

			for(String processingSensorName:sensorNameSet) {
				Sensor sensor = tsdb.getSensor(processingSensorName);
				if(sensor == null || sensor.getEmpiricalDiff() == null || !Float.isFinite(sensor.getEmpiricalDiff())) {
					continue;
				}
				
				//Logger.info(processingSensorName);

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

				//Logger.info(group+"  "+processingSensorName+" ********************************* "+TimeUtil.oleMinutesToLocalDateTime(groupMinTimestamp)+"\t - \t"+TimeUtil.oleMinutesToLocalDateTime(groupMaxTimestamp)+" **************************************************************** "+groupMinTimestamp+"\t-\t"+groupMaxTimestamp);
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
							//Logger.info(Arrays.toString(schema)+"of "+Arrays.toString(plot.getSensorNames()));
							Continuous continuous = continuousGen.get(plotID, schema);
							if(continuous!=null) {
								Subtraction subtraction = Subtraction.createWithElevationTemperature(tsdb,continuous,plotID);
								if(subtraction!=null) {
									//Logger.info("with ref " + plotID + " " + processingSensorName);
									additions.add(subtraction);
								}
								sources.add(continuous);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						Logger.warn(e);
					}
				}

				final int MIN_NODES = 3;
				

				/*Averaged refNode = null;
				if(refNode==null && additions.size() >= MIN_NODES) {
					refNode = Averaged.of(tsdb, additions, MIN_NODES, false);
				}
				if(refNode==null && sources.size() >= MIN_NODES) {
					refNode = Averaged.of(tsdb, sources, MIN_NODES, false);
				}*/
				
				/*AveragedCounted refNode = null;
				if(refNode==null && additions.size() >= MIN_NODES) {
					refNode = AveragedCounted.of(tsdb, additions, MIN_NODES, false);
				}
				if(refNode==null && sources.size() >= MIN_NODES) {
					refNode = AveragedCounted.of(tsdb, sources, MIN_NODES, false);
				}*/
				
				MedianCounted refNode = null;
				if(refNode==null && additions.size() >= MIN_NODES) {
					refNode = MedianCounted.of(tsdb, additions, MIN_NODES, false);
				}
				if(refNode==null && sources.size() >= MIN_NODES) {
					refNode = MedianCounted.of(tsdb, sources, MIN_NODES, false);
				}
				

				if(refNode != null) {
					TsIterator it = refNode.get(groupMinTimestamp, groupMaxTimestamp);
					if(it!=null&&it.hasNext()) {
						//tsdb.cacheStorage.writeNew(group, averaged.get(groupMinTimestamp, groupMaxTimestamp));
						TimestampSeries timestampSeries = it.toTimestampSeries(group);
						tsdb.streamCache.insertTimestampSeries(timestampSeries);
					    Logger.info(group+"/"+processingSensorName+" <- "+refNode.getSourceText());
					} else {
						Logger.warn("reference group: "+group);
					}
				} else {
					Logger.trace(group+"/"+processingSensorName+" not enough sources for average");
				}
			}
			//cbPrint.println(group+" -> "+list);
		}

		long endRunTime = System.currentTimeMillis();
		cbPrint.println(String.format("create reference cache run time: %d s %03d ms", (endRunTime - startRunTime) / 1000, (endRunTime - startRunTime) % 1000));
	}
}
