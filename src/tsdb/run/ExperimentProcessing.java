package tsdb.run;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.ServerTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.Timer;

public class ExperimentProcessing {
	private static final Logger log = LogManager.getLogger();


	public static void main(String[] args) throws RemoteException {
		log.info("start...");
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			RemoteTsDB remoteTsdb = new ServerTsDB(tsdb);
			Region region = remoteTsdb.getRegionByName("BE");
			String[] sensorNames = new String[]{"Ta_200"};
			String[] plotIDs = remoteTsdb.getStationNames(); //new String[]{"HEG10"}; 
			boolean allinone = true;
			boolean desc_sensor = false;
			boolean desc_plot = false;
			boolean desc_settings = false;
			boolean col_plotid = true;
			boolean col_timestamp = false;
			boolean col_datetime = true;
			boolean write_header = true;
			Long startTimestamp = TimeUtil.ofDateStartHour(2014);
			Long endTimestamp = TimeUtil.ofDateEndHour(2015);
			boolean col_qualitycounter = false;

			String[] mode = new String[]{
					"raw",
					"hour",
					"day",
					"month",
					"na",
					"step", 
					"empirical", 
					"interpolated"
			};
			AggregationInterval[] aggregationInterval = new AggregationInterval[]{
					AggregationInterval.RAW,
					AggregationInterval.HOUR,
					AggregationInterval.DAY,
					AggregationInterval.MONTH,
					AggregationInterval.YEAR,
					AggregationInterval.YEAR,
					AggregationInterval.YEAR,
					AggregationInterval.YEAR
			};
			DataQuality[] dataQuality = new DataQuality[]{
					DataQuality.Na,
					DataQuality.Na,
					DataQuality.Na,
					DataQuality.Na,
					DataQuality.Na, 
					DataQuality.STEP, 
					DataQuality.EMPIRICAL, 
					DataQuality.EMPIRICAL
			};
			boolean[] interpolated = new boolean[]{
					false, 
					false, 
					false, 
					false, 
					false, 
					false, 
					false, 
					true
			};

			for(int r=1;r<=10;r++) {
				System.out.println("round "+r);
				for(int i=0;i<mode.length;i++) {
					System.gc();
					Timer.start(mode[i]);
					/*ZipExport zipExport = new ZipExport(remoteTsdb, region, sensorNames, plotIDs, aggregationInterval, dataQuality[i], interpolated[i], allinone, desc_sensor, desc_plot, desc_settings, col_plotid, col_timestamp, col_datetime, write_header, startTimestamp, endTimestamp, col_qualitycounter);
					zipExport.writeToStream(NULL_OUTPUT_STREAM);*/
					for(String plotID:plotIDs) {
						remoteTsdb.plot(null, plotID, sensorNames, aggregationInterval[i], dataQuality[i], interpolated[i], startTimestamp, endTimestamp);
					}
					System.out.println(Timer.stop(mode[i]));
				}
			}
		}

		log.info("end");
	}


	private static OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
		@Override
		public void write(int b) throws IOException {/*nothing*/}
		@Override
		public void write(byte[] b) throws IOException {/*nothing*/}
		@Override
		public void write(byte[] b, int off, int len) throws IOException {/*nothing*/}		
	};

}
