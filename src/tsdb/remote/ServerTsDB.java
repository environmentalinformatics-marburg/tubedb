package tsdb.remote;

import static tsdb.util.AssumptionCheck.throwNull;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.GeneralStation;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.component.LoggerType;
import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.component.SourceEntry;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.iterator.CollectingAggregator;
import tsdb.iterator.DayCollectingAggregator;
import tsdb.iterator.EvaluatingAggregationIterator;
import tsdb.iterator.MonthCollectingAggregator;
import tsdb.iterator.WeekCollectingAggregator;
import tsdb.iterator.YearCollectingAggregator;
import tsdb.run.ConsoleRunner;
import tsdb.streamdb.StreamIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.DataEntry;
import tsdb.util.DataQuality;
import tsdb.util.Pair;
import tsdb.util.TimeSeriesMask;
import tsdb.util.TimestampInterval;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

/**
 * Local implementation of RemoteTsDB
 * @author woellauer
 *
 */
public class ServerTsDB implements RemoteTsDB {

	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb; //not null

	public ServerTsDB(TsDB tsdb) throws RemoteException { // !!
		throwNull(tsdb);
		this.tsdb = tsdb;
	}

	//----------------------- sensor

	@Override
	public String[] getSensorNamesOfPlot(String plotID) {
		if(plotID==null) {
			log.warn("plotID null");
			return null;
		}
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return tsdb.includeVirtualSensorNames(virtualPlot.getSchema());
		}
		Station station = tsdb.getStation(plotID);
		if(station!=null) {
			return tsdb.includeVirtualSensorNames(station.getSchema());

		}
		log.warn("plotID not found "+plotID);
		return null;
	}

	@Override
	public String[] getSensorNamesOfGeneralStation(String generalStationName) {
		if(generalStationName==null) {
			log.warn("generalStationName null");
			return null;
		}		
		GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
		if(generalStation==null) {
			log.warn("generalStation not found");
			return null;
		}

		TreeSet<String> sensorNameSet = new TreeSet<String>();

		for(Station station:generalStation.stationList) {
			sensorNameSet.addAll(Arrays.asList(station.getSchema()));
		}

		for(VirtualPlot virtualPlot:generalStation.virtualPlots) {
			sensorNameSet.addAll(Arrays.asList(virtualPlot.getSchema()));
		}		

		return tsdb.includeVirtualSensorNames(sensorNameSet.toArray(new String[sensorNameSet.size()])); 
	}

	@Override
	public Sensor[] getSensors() {
		return tsdb.getSensors().toArray(new Sensor[0]);
	}

	@Override
	public Sensor getSensor(String sensorName) {
		return tsdb.getSensor(sensorName);
	}

	@Override
	public String[] getBaseSchema(String[] rawSchema) {
		return tsdb.getBaseSchema(rawSchema);
	}

	@Override
	public String[] getCacheSchemaNames(String streamName) {//TODO remove
		return null;
	}

	@Override
	public String[] getValidSchema(String plotID, String[] sensorNames) {
		return tsdb.getValidSchema(plotID, sensorNames);
	}
	
	@Override
	public String[] getValidSchemaWithVirtualSensors(String plotID, String[] sensorNames) {
		return tsdb.getValidSchemaWithVirtualSensors(plotID, sensorNames);
	}
	
	@Override
	public String[] supplementSchema(String... schema) {
		return tsdb.supplementSchema(schema);
	}

	// ----------------------------------- region
	@Override
	public Region[] getRegions() {
		Collection<Region> regions = tsdb.getRegions();
		return regions.toArray(new Region[regions.size()]);
	}

	@Override
	public String[] getRegionLongNames() {
		return tsdb.getRegionLongNames().toArray(String[]::new);
	}

	@Override
	public Region getRegionByLongName(String longName) {
		return tsdb.getRegionByLongName(longName);
	}

	@Override
	public Region getRegionByPlot(String plotID) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot==null) {
			try {
				int index = plotID.indexOf(':');
				if(index>0) {
					String mainPlotID = plotID.substring(0, index);
					virtualPlot = tsdb.getVirtualPlot(mainPlotID);
				}
			} catch (Exception e) {
				log.warn(e);
			}
		}
		if(virtualPlot!=null) {
			GeneralStation general = virtualPlot.generalStation;
			if(general!=null) {
				return general.region;
			} else {
				return null;
			}
		}
		Station station = tsdb.getStation(plotID); 
		if(station!=null) {
			GeneralStation general = station.generalStation;
			if(general!=null) {
				return general.region;
			} else {
				return null;
			}
		}
		return null;
	}

	// ---------------------------- general station
	@Override
	public GeneralStationInfo[] getGeneralStations() {
		return tsdb.getGeneralStations().stream().map(g->new GeneralStationInfo(g)).toArray(GeneralStationInfo[]::new);
	}

	@Override
	public GeneralStationInfo[] getGeneralStationsOfRegion(String regionName) {
		return tsdb.getGeneralStations(regionName).map(g->new GeneralStationInfo(g)).toArray(GeneralStationInfo[]::new);
	}

	@Override
	public String[] getGeneralStationLongNames(String regionName) {
		return tsdb.getGeneralStationLongNames(regionName);
	}

	// ----------------------------------- plot station virtualPlot
	@Override
	public PlotInfo[] getPlots() {
		return Stream.concat(
				tsdb.getStations().stream().filter(s->s.isPlot).map(s->new PlotInfo(s)), 
				tsdb.getVirtualPlots().stream().map(v->new PlotInfo(v))
				).toArray(PlotInfo[]::new);
	}

	@Override
	public StationInfo[] getStations() {
		return tsdb.getStations().stream().map(s->new StationInfo(s)).toArray(StationInfo[]::new);
	}

	@Override
	public String getStationLoggerTypeName(String stationName) throws RemoteException {
		if(stationName==null) {
			return null;
		}
		Station station = tsdb.getStation(stationName);
		if(station==null) {
			return null;
		}
		if(station.loggerType==null) {
			return null;
		}
		return station.loggerType.typeName;
	}

	@Override
	public String[] getPlotStations(String plotID) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot==null) {
			return null;
		}
		return virtualPlot.getStationIDs();
	}

	@Override
	public VirtualPlotInfo[] getVirtualPlots() {
		return tsdb.getVirtualPlots().stream().map(v->new VirtualPlotInfo(v)).toArray(VirtualPlotInfo[]::new);
	}

	@Override
	public VirtualPlotInfo getVirtualPlot(String plotID) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return new VirtualPlotInfo(virtualPlot);
		} else {
			return null;
		}
	}

	@Override
	public String[] getStationNames() {
		return tsdb.getStationNames().toArray(new String[0]);
	}

	@Override
	public String[] cacheStorageGetStreamNames() { // remove
		return null;
	}

	@Override
	public String[] getPlotIDsByGeneralStationByLongName(String longName) {		
		GeneralStation generalStation = tsdb.getGeneralStationByLongName(longName);
		if(generalStation==null) {
			return null;
		}
		ArrayList<String> plotIDList = new ArrayList<String>();
		generalStation.stationList.stream().forEach(station->plotIDList.add(station.stationID));
		generalStation.virtualPlots.stream().forEach(virtualPlot->plotIDList.add(virtualPlot.plotID));
		if(plotIDList.isEmpty()) {
			return null;
		}
		return plotIDList.toArray(new String[0]);
	}

	@Override 
	public ArrayList<TimestampInterval<String>> getPlotTimeSpans() {
		ArrayList<TimestampInterval<String>> result = new ArrayList<TimestampInterval<String>>();

		tsdb.getPlotNames().forEach(plotID->{
			long[] interval = tsdb.getTimeInterval(plotID);
			if(interval!=null) {
				result.add(new TimestampInterval<String>(plotID, interval[0], interval[1]));
			}
		});		

		return result;
	}

	@Override
	public ArrayList<TimestampInterval<String>> getPlotTimeSpansOfGeneralStation(String generalStationName) throws RemoteException {
		System.out.println("*********************************************  getTimeSpanListByGeneralStation   "+generalStationName);
		ArrayList<TimestampInterval<String>> result = new ArrayList<TimestampInterval<String>>();
		GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
		if(generalStation==null) {
			log.warn("generalStationName not found: "+generalStationName);
			return null;
		}
		generalStation.getStationAndVirtualPlotNames().forEach(plotID->{
			long[] interval = tsdb.getTimeInterval(plotID);
			if(interval!=null) {
				result.add(new TimestampInterval<String>(plotID, interval[0], interval[1]));
			}
		});

		return result;
	}

	@Override
	public ArrayList<TimestampInterval<String>> getPlotTimeSpansOfRegion(String regionName) throws RemoteException {
		ArrayList<TimestampInterval<String>> result = new ArrayList<TimestampInterval<String>>();
		tsdb.getGeneralStations(regionName).forEach(generalStation->{
			generalStation.getStationAndVirtualPlotNames().forEach(plotID->{
				long[] interval = tsdb.getTimeInterval(plotID);
				if(interval!=null) {
					result.add(new TimestampInterval<String>(plotID, interval[0], interval[1]));
				}
			});	
		});
		return result;
	}
	
	
	@Override
	public ArrayList<PlotStatus> getPlotStatuses() {
		return collectPlotStatuses(tsdb.getPlotNames());
	}
	
	@Override
	public ArrayList<PlotStatus> getPlotStatusesOfGeneralStation(String generalStationName) {		
		GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
		if(generalStation==null) {
			log.warn("generalStationName not found: "+generalStationName);
			return null;
		}		
		return collectPlotStatuses(generalStation.getStationAndVirtualPlotNames());
	}
	
	@Override
	public ArrayList<PlotStatus> getPlotStatusesOfRegion(String regionName) {
		return collectPlotStatuses(tsdb.getGeneralStations(regionName).flatMap(g->g.getStationAndVirtualPlotNames()));
	}
	
	private ArrayList<PlotStatus> collectPlotStatuses(Stream<String> plotIDstream) {
		return collectPlotStatuses(plotIDstream, new ArrayList<PlotStatus>());
	}
	
	private ArrayList<PlotStatus> collectPlotStatuses(Stream<String> plotIDstream, ArrayList<PlotStatus> result) {
		plotIDstream.forEach(plotID->{
			long[] interval = tsdb.getTimeInterval(plotID);
			if(interval!=null) {
				float voltage = Float.NaN;
				StreamIterator it = null;
				//tsdb.streamStorage.getStationTimeInterval(plotID);
				int[] uottInterval = tsdb.streamStorage.getSensorTimeInterval(plotID, "UOtt");
				if(uottInterval!=null&&uottInterval[1]+(60*24)>=interval[1]) {
					it = tsdb.streamStorage.getRawSensorIterator(plotID, "UOtt", (long)uottInterval[1], (long)uottInterval[1]);
				}
				if(it==null) {
					int[] ubInterval = tsdb.streamStorage.getSensorTimeInterval(plotID, "UB");
					if(ubInterval!=null&&ubInterval[1]+(60*24)>=interval[1]) {
						it = tsdb.streamStorage.getRawSensorIterator(plotID, "UB", (long)ubInterval[1], (long)ubInterval[1]);
					}
				}
				if(it!=null&&it.hasNext()) {
					DataEntry e = it.next();
					//if(e.timestamp==ub[1]) {
						voltage = e.value;
					//} else {
					//	log.warn("timestamp error");
					//}
				}				
				result.add(new PlotStatus(plotID, (int)interval[0], (int)interval[1], voltage));
			}
		});		
		return result;
	}

	// ------------------------------- logger

	@Override
	public LoggerType[] getLoggerTypes() {
		return tsdb.getLoggerTypes().toArray(new LoggerType[0]);
	}

	@Override
	public LoggerType getLoggerType(String loggerTypeName) {
		return tsdb.getLoggerType(loggerTypeName);
	}

	// ------------------------------------ source catalog

	@Override
	public SourceEntry[] getSourceCatalogEntries() {
		return tsdb.sourceCatalog.getEntries().toArray(new SourceEntry[0]);
	}

	// ------------------------------------ console

	private final Object command_counter_sync_object = new Object();
	long command_counter=0L;

	Map<Long,Pair<Thread,ConsoleRunner>> commandThreadMap = new ConcurrentHashMap<Long,Pair<Thread,ConsoleRunner>>();

	private long createCommandThreadId() {
		synchronized (command_counter_sync_object) {
			final long commandThreadId = command_counter;
			command_counter++;
			return commandThreadId;
		}
	}

	@Override
	public long execute_console_command(String input_line) throws RemoteException {
		final long commandThreadId = createCommandThreadId();
		ConsoleRunner consolerunner = new ConsoleRunner(tsdb, input_line);
		Thread commandThread = new Thread(consolerunner);		
		commandThread.start();
		System.out.println("execute_console_command: "+input_line+"     "+command_counter);
		commandThreadMap.put(commandThreadId, new Pair<Thread,ConsoleRunner>(commandThread,consolerunner));
		return commandThreadId;
	}

	@Override
	public Pair<Boolean,String[]> console_comand_get_output(long commandThreadId) throws RemoteException {
		Pair<Thread,ConsoleRunner> pair = commandThreadMap.get(commandThreadId);
		if(pair==null) {
			return null;
		}
		System.out.println("console_comand_get_output: "+commandThreadId);
		Thread commandThread = pair.a;
		ConsoleRunner consolerunner = pair.b;
		boolean running = commandThread.isAlive(); //first
		String[] output_lines = consolerunner.getOutputLines();  //and then
		if(!running) {
			commandThreadMap.remove(commandThreadId);
		}

		return new Pair<Boolean,String[]>(running,output_lines);
	}


	//-------------------------------------- query

	@Override
	public TimestampSeries plot(String queryType, String plotID, String[] columnNames, AggregationInterval aggregationInterval, DataQuality dataQuality, boolean interpolated, Long start, Long end) {
		Node node = null;
		if(queryType==null||queryType.equals("standard")) {		
			node = QueryPlan.plot(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);
		} else if(queryType.equals("difference")) {
			return null; //TODO remove
			//node = QueryPlan.plotDifference(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);
		} else {
			log.error("queryType unknown");
		}
		if(node==null) {
			return null;
		}
		TsIterator it = node.get(start, end);
		if(it==null||!it.hasNext()) {
			return null;
		}
		log.info(it.getProcessingChain().getText());
		return it.toTimestampSeries(plotID);
	}

	@Override
	public TimestampSeries plotQuartile(String plotID, String[] columnNames, AggregationInterval aggregationInterval, DataQuality dataQuality, boolean interpolated, Long start, Long end) {
		
		Node node = QueryPlan.plot(tsdb, plotID, columnNames, AggregationInterval.HOUR, dataQuality, interpolated);		
		if(node==null) {
			return null;
		}
		TsIterator hour_it = node.get(start, end);
		if(hour_it==null||!hour_it.hasNext()) {
			return null;
		}

		CollectingAggregator collectingAggregator;
		switch(aggregationInterval) {
		case RAW:
			throw new RuntimeException("no boxplot for "+aggregationInterval);
		case HOUR:
			throw new RuntimeException("no boxplot for "+aggregationInterval);
		case DAY:
			collectingAggregator = new DayCollectingAggregator(tsdb, hour_it);
			break;
		case WEEK:
			collectingAggregator = new WeekCollectingAggregator(new DayCollectingAggregator(tsdb, hour_it));
			break;
		case MONTH:
			collectingAggregator = new MonthCollectingAggregator(new DayCollectingAggregator(tsdb, hour_it));
			break;
		case YEAR:
			collectingAggregator = new YearCollectingAggregator(new MonthCollectingAggregator(new DayCollectingAggregator(tsdb, hour_it)));
			break;
		default:
			throw new RuntimeException("no boxplot for "+aggregationInterval);
		}

		EvaluatingAggregationIterator eai = new EvaluatingAggregationIterator(hour_it.getSchema(),collectingAggregator);
		
		if(eai==null||!eai.hasNext()) {
			return null;
		}
		log.info(eai.getProcessingChain().getText());
		return eai.toTimestampSeries(plotID);
	}	

	@Override
	public TimestampSeries cache(String streamName, String[] columnNames, AggregationInterval aggregationInterval) {
		throw new RuntimeException("not implemted");
		/*Node node =  QueryPlan.cache(tsdb, streamName, columnNames, aggregationInterval);
		if(node==null) {
			return null;
		}
		TsIterator it = node.get(null, null);
		if(it==null||!it.hasNext()) {
			return null;
		}
		return it.toTimestampSeries(streamName);*/		
	}

	@Override
	public TimeSeriesMask getTimeSeriesMask(String stationName, String sensorName) {
		return tsdb.streamStorage.getTimeSeriesMask(stationName, sensorName);
	}

	@Override
	public void setTimeSeriesMask(String stationName, String sensorName, TimeSeriesMask timeSeriesMask) {
		tsdb.streamStorage.setTimeSeriesMask(stationName, sensorName, timeSeriesMask);
	}
}
