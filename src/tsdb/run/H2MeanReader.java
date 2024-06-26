package tsdb.run;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NavigableSet;


import org.tinylog.Logger;
import org.h2.jdbcx.JdbcDataSource;

import tsdb.TsDB;
import tsdb.TsDBFactory;

public class H2MeanReader {
	

	/*public static void main(String[] args) {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:c:/h2_storage/h2_storage");

		try(Connection connection = ds.getConnection()) {
			Logger.info("connected");
			Statement statement = connection.createStatement();
			String tsName = "AEG01"+"_"+"Ta_200";
			ResultSet rs = statement.executeQuery("SELECT * FROM "+tsName);
			while(rs.next()) {
				//Logger.info(rs.getInt(1));
			}

		} catch (SQLException e) {
			e.printStackTrace();
			Logger.error(e);
		}
		Logger.info("closed");
	}*/

	private final TsDB tsdb;

	private long total_count = 0;

	public static void main(String[] args) {		
		TsDB tsdb = TsDBFactory.createDefault();
		H2MeanReader h2DateReader = new H2MeanReader(tsdb);
		h2DateReader.readAll();
		tsdb.close();		
	}

	public H2MeanReader(TsDB tsdb) {
		this.tsdb = tsdb;
	}

	public void readAll() {

		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:c:/h2_storage/h2_storage");

		try(Connection connection = ds.getConnection()) {


			NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

			long timeStartImport = System.currentTimeMillis();
			try {
				for(String stationName:stationNames) {

					if(stationName.equals("HET38") || stationName.equals("HET44") ) { //very slow reads  reason: unknown
						continue;
					}
					try {
						try(Statement statement = connection.createStatement()) {
							readSeries(statement, stationName, "Ta_200");
						}
					} catch(Exception e) {
						e.printStackTrace();
						Logger.error(e);
					}
				}
			} catch (Exception e) {
				Logger.error(e);
			}
			long timeEndImport = System.currentTimeMillis();
			Logger.info((timeEndImport-timeStartImport)/1000+" s Export "+total_count+" count");

		} catch(Exception e) {
			Logger.error(e);
		} 
	}

	private void readSeries(Statement statement, String stationName, String sensorName) throws SQLException {		
		String tsName = stationName+"_"+sensorName;
		Logger.info("read "+tsName);
		try(ResultSet rs = statement.executeQuery("SELECT AVG(m) FROM "+tsName)) {
			while(rs.next()) {
				float v = rs.getFloat(1);
				Logger.info(v);
				total_count++;
				//Logger.info(rs.getInt(1));
			}
		}

	}

}
