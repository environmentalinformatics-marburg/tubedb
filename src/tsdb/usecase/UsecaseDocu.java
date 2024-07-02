package tsdb.usecase;

import java.rmi.RemoteException;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.Node;
import tsdb.graph.processing.Aggregated;
import tsdb.graph.source.StationBase;
import tsdb.graph.source.StationRawSource;
import tsdb.remote.GeneralStationInfo;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.ServerTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

public class UsecaseDocu {

	public static void main(String[] args) throws RemoteException {
		try(TsDB tsdbInternal = TsDBFactory.createDefault()) { // AutoCloseable instance created by TsDBFactory with default configuration file location
			RemoteTsDB tsdb = new ServerTsDB(tsdbInternal); // create RemoteTsDB interface instance to local TsDB
			String queryType = null;
			String plotID = "HEG01";
			String[] sensorNames = new String[]{"Ta_200", "rH_200"};
			AggregationInterval aggregationInterval = AggregationInterval.MONTH;
			DataQuality dataQuality = DataQuality.EMPIRICAL;
			boolean interpolated = true;
			Long start = TimeUtil.ofDateStartHour(2010);
			Long end = TimeUtil.ofDateEndHour(2015);
			TimestampSeries ts = tsdb.plot(queryType, plotID, sensorNames, aggregationInterval, dataQuality, interpolated, start, end); // query time series
			System.out.println(ts);
			for(GeneralStationInfo info:tsdb.getGeneralStations()) { // query meta data
				System.out.println(info.longName);
			}
		}
		
		
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			String stationName = "HEG01";
			String[] sensorNames = new String[]{"Ta_200", "rH_200"};
			AggregationInterval aggregationInterval = AggregationInterval.MONTH;
			Long start = TimeUtil.ofDateStartHour(2010);
			Long end = TimeUtil.ofDateEndHour(2015);
			Station station = tsdb.getStation(stationName); // get station
			Node rawNode = StationRawSource.of(tsdb, station, sensorNames); // create raw source node
			StationBase baseNode = StationBase.of(tsdb, rawNode); // create base aggregated (hourly value) node 
			Continuous continuousNode = Continuous.of(baseNode); // fill gaps in time with NA entries
			Aggregated aggregatedNode = Aggregated.of(tsdb, continuousNode, aggregationInterval, null, null, null, null); // aggregate data to months
			TsIterator it = aggregatedNode.get(start, end); // create iterator of processing graph
			System.out.println(it.getProcessingChain().getText()); // print processing graph
			while(it.hasNext()) { // on demand process and print time series
				System.out.println(it.next());
			}
			aggregatedNode.get(start, end).writeCSV(TsDBFactory.get_CSV_output_directory()+"data_month.csv"); // reuse processing graph and write time series directly to CSV-file
			rawNode.get(start, end).writeCSV(TsDBFactory.get_CSV_output_directory()+"data_raw.csv"); // reuse processing graph and write raw time series directly to CSV-file
		}
		
	}

}
