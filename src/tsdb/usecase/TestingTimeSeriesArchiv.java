package tsdb.usecase;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TimeSeriesArchivReader;
import tsdb.TimeSeriesArchivReader.TimeSeriesArchivVisitor;
import tsdb.TimeSeriesArchivWriter;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.DataEntry;
import tsdb.util.DataQuality;
import tsdb.util.iterator.TimestampSeries;

public class TestingTimeSeriesArchiv {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private static final String FILENAME = TsDBFactory.OUTPUT_PATH+"/"+"testing.tsa";

	public static void main(String[] args) throws IOException  {
		write();
		read();
	}

	public static void write() throws IOException {
		TsDB tsdb = TsDBFactory.createDefault();

		String plotID = "HEG01";
		String[] columnNames = new String[]{"Ta_200"};
		AggregationInterval aggregationInterval = AggregationInterval.HOUR;
		DataQuality dataQuality = DataQuality.STEP;
		boolean interpolated = false;
		Node node = QueryPlan.plot(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);

		TimestampSeries timestampSeries = node.get(null, null).toTimestampSeries("HEG01");

		System.out.println(timestampSeries);

		TimeSeriesArchivWriter tsaWriter = new TimeSeriesArchivWriter(FILENAME);
		tsaWriter.open();
		tsaWriter.writeTimestampSeries(timestampSeries);
		tsaWriter.close();
	}

	public static void read() throws IOException {
		TimeSeriesArchivReader tsaReader = new TimeSeriesArchivReader(FILENAME);		
		tsaReader.readFully(new TimeSeriesArchivVisitor() {
			@Override
			public void readDataEntries(String stationName, String sensorName, DataEntry[] dataEntries) {
			}
			@Override
			public void readTimestampSeries(TimestampSeries timestampSeries) {
				System.out.println(timestampSeries);
			}
		});
	}

}
