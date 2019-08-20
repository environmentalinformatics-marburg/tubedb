package tsdb.experiment;

import java.util.ArrayList;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.util.DataRow;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

public class Experiment_TubeDB extends Experiment{
	private final TsDB tsdb;

	public Experiment_TubeDB() {
		this.tsdb = TsDBFactory.createDefault();
	}

	@Override
	protected void clear() {
		tsdb.clear();
	}

	@Override
	protected void insertTimeseries(String stationName, String[] sensorNames, ArrayList<DataRow> dataRows) {
		tsdb.streamStorage.insertDataRows(stationName, sensorNames, dataRows);
	}

	@Override
	public void close() throws Exception {
		tsdb.close();
	}

	@Override
	protected long full_read() {
		long rowCount = 0;
		for(String stationName : tsdb.streamStorage.getStationNames()) {
			String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
			if(sensorNames != null && sensorNames.length != 0) {
				TsIterator it = tsdb.streamStorage.getRawIterator(stationName, sensorNames , null, null);
				if(it != null) {
					while(it.hasNext()) {
						TsEntry e = it.next();
						rowCount++;
					}
				}
			}
		}
		return rowCount;
	}
}
