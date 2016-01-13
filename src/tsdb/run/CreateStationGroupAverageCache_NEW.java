package tsdb.run;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.processing.Addition;
import tsdb.graph.processing.Averaged;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

/**
 * create cache of average values from groups of stations
 * @author woellauer
 *
 */
public class CreateStationGroupAverageCache_NEW {

	private static final Logger log = LogManager.getLogger();

	public interface CbPrint {
		public void println(String text);
	}

	private final TsDB tsdb;
	private CbPrint cbPrint;

	public static void main(String[] args) {
		System.out.println("start...");
		TsDB tsdb = TsDBFactory.createDefault();
		new CreateStationGroupAverageCache_NEW(tsdb).run();
		tsdb.close();
		System.out.println("...end");
	}

	public CreateStationGroupAverageCache_NEW(TsDB tsdb) {
		this(tsdb,text->System.out.println(text));
	}

	public CreateStationGroupAverageCache_NEW(TsDB tsdb, CbPrint cbPrint) {
		throwNulls(tsdb,cbPrint);
		this.tsdb = tsdb;
		this.cbPrint = cbPrint;
	}

	public void run() {

		long startRunTime = System.currentTimeMillis();

		ContinuousGen continuousGen = QueryPlanGenerators.getContinuousGen(tsdb, DataQuality.STEP);

		for(String group:tsdb.getGeneralStationGroups()) {
			List<String> list = tsdb.getStationAndVirtualPlotNames(group).collect(Collectors.toList());

			TreeSet<String> sensorNameSet = new TreeSet<String>(); 
			for(String plotID:list) {
				String[] sensorNames = tsdb.getSensorNamesOfPlot(plotID);
				if(sensorNames==null||sensorNames.length==0) {
					continue;
				}				
				sensorNames = tsdb.getBaseSchema(sensorNames);
				if(sensorNames==null||sensorNames.length==0) {
					continue;
				}				
				sensorNameSet.addAll(Arrays.asList(sensorNames));
			}
			log.info(sensorNameSet);

			for(String sensorName:sensorNameSet) {

				long groupMinTimestamp = Long.MAX_VALUE;
				long groupMaxTimestamp = Long.MIN_VALUE;
				for(String plotID:list) {

					String[] sensorNames = tsdb.getSensorNamesOfPlot(plotID);
					if(sensorNames==null) {
						continue;
					}
					boolean sensorContained = false;
					for(String s:sensorNames) {
						if(s.equals(sensorName)) {
							sensorContained = true;
							break;
						}						
					}
					if(!sensorContained)  {
						continue;
					}
					if(sensorName.equals("WD")) {
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

				cbPrint.println(group+"  "+sensorName+" ********************************* "+TimeUtil.oleMinutesToLocalDateTime(groupMinTimestamp)+"\t - \t"+TimeUtil.oleMinutesToLocalDateTime(groupMaxTimestamp)+" **************************************************************** "+groupMinTimestamp+"\t-\t"+groupMaxTimestamp);
				List<Continuous> sources = new ArrayList<Continuous>();
				List<Continuous> additions = new ArrayList<Continuous>();
				for(String plotID:list) {
					try {
						String[] sensorNames = tsdb.getSensorNamesOfPlot(plotID);
						if(sensorNames==null) {
							continue;
						}
						boolean sensorContained = false;
						for(String s:sensorNames) {
							if(s.equals(sensorName)) {
								sensorContained = true;
								break;
							}						
						}
						if(!sensorContained)  {
							continue;
						}


						Continuous continuous = continuousGen.get(plotID,new String[]{sensorName});
						if(continuous!=null) {
							Addition addition = Addition.createWithElevationTemperature(tsdb,continuous,plotID);
							if(addition!=null) {
								additions.add(addition);
							}
							sources.add(continuous);
						} else {
							//log.warn("no stream of "+plotID);
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
					} else {
						log.warn("averages: "+group);
					}
				} else {
					log.warn("not enough sources for average: "+group);
				}
			}
			cbPrint.println("create average: "+group+" -> "+list);
		}

		long endRunTime = System.currentTimeMillis();
		cbPrint.println("CreateStationGroupAverageCache run time: "+(endRunTime-startRunTime)/1000+" s");

	}

}
