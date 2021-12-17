package tsdb.experiment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


import org.tinylog.Logger;
import org.h2.jdbcx.JdbcDataSource;

import tsdb.util.DataRow;

public class Experiment_H2 extends Experiment {
	

	private final Connection connection;

	public Experiment_H2() throws SQLException {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:./h2_storage/data");
		connection = ds.getConnection();
	}

	@Override
	protected void clear() {
		try(Statement statement = connection.createStatement()) { // clear database
			statement.execute("DROP ALL OBJECTS");
		} catch(SQLException sqle) {
			Logger.error("not dropping "+sqle);
		}		
	}



	@Override
	protected void insertTimeseries(String stationName, String[] sensorNames, ArrayList<DataRow> dataRows) {
		createStation(stationName, sensorNames);

		String[] sn = Arrays.stream(sensorNames).map(s -> "?").toArray(String[]::new);
		String vars = String.join(",", sn);
		String fields = String.join(",", sensorNames);
		String q = "INSERT INTO " + stationName + " (timestamp," + fields + ")" +  " VALUES (?," + vars + ")";
		Logger.info(q);
		try(PreparedStatement preparedStatement = connection.prepareStatement(q)) {
			int sensorNamesLen = sensorNames.length;
			for(DataRow dataRow:dataRows) {
				preparedStatement.setInt(1, (int) dataRow.timestamp);
				for(int i=0; i<sensorNamesLen; i++) {
					preparedStatement.setFloat(i + 2, dataRow.data[i]);
				}
				preparedStatement.addBatch();						
			}
			preparedStatement.executeBatch();
		} catch (SQLException e) {
			Logger.error(e);
		}

	}

	private void createStation(String stationName, String[] sensorNames) {
		boolean createStation = true;
		try {
			ResultSet resultSet = connection.getMetaData().getTables(null, null, stationName, null);
			if (resultSet.next()) {
				createStation = false;
			}
		} catch (Exception e) {
			Logger.warn("get table " + e);
		}

		if(createStation) {
			try(Statement statement = connection.createStatement()) {
				String[] sn = Arrays.stream(sensorNames).map(s -> " " + s + " FLOAT4").toArray(String[]::new);
				String fields = String.join(",", sn);
				String q = "CREATE TABLE " +stationName + " (timestamp INT4 PRIMARY KEY," + fields + ")";
				Logger.info(q);
				statement.execute(q);
			} catch (Exception e) {
				Logger.warn("at create table "+e);
			}
		} else {
			HashSet<String> tableSensors = new HashSet<String>();
			try(Statement statement = connection.createStatement()) {
				String q = "SHOW COLUMNS FROM " + stationName;
				//Logger.info(q);
				ResultSet resultSet = statement.executeQuery(q);
				while(resultSet.next()) {
					tableSensors.add(resultSet.getString(1));
				}
			} catch (Exception e) {
				Logger.warn("at create table "+e);
			}
			Logger.info("existing: "+ tableSensors);
			for(String sensorName:sensorNames) {
				if(!tableSensors.contains(sensorName.toUpperCase())) {
					//Logger.info("ADD " + sensorName);
					try(Statement statement = connection.createStatement()) {
						String q = "ALTER TABLE " +stationName + " ADD " + sensorName + " FLOAT4";
						Logger.info(q);
						statement.execute(q);
					} catch (Exception e) {
						Logger.warn("at create table "+e);
					}
				} else {
					//Logger.info("EXISTING " + sensorName);
				}
			}
		}
	}

	@Override
	public void close() throws Exception {
		connection.close();
	}

	@Override
	protected long full_read() {
		long rowCount = 0;
		try(Statement statement = connection.createStatement()) {
			String q = "SHOW TABLES";
			//Logger.info(q);
			ResultSet resultSet = statement.executeQuery(q);
			while(resultSet.next()) {
				rowCount += full_read_station(resultSet.getString(1));
			}
		} catch (Exception e) {
			Logger.warn(e);
		}

		return rowCount;
	}

	private long full_read_station(String stationName) {
		ArrayList<String> tableSensors = new ArrayList<String>();
		try(Statement statement = connection.createStatement()) {
			String q = "SHOW COLUMNS FROM " + stationName;
			//Logger.info(q);
			ResultSet resultSet = statement.executeQuery(q);
			while(resultSet.next()) {
				tableSensors.add(resultSet.getString(1));
			}
		} catch (Exception e) {
			Logger.warn(e);
		}
		if(!tableSensors.isEmpty()) {
			return full_read_station_data(stationName, tableSensors);
		} else {
			return 0;
		}
	}

	private long full_read_station_data(String stationName, ArrayList<String> fieldNames) {
		long rowCount = 0;
		try(Statement statement = connection.createStatement()) {
			String fields = String.join(",", fieldNames);
			String q = "SELECT " + fields + " FROM " + stationName;
			Logger.info(q);
			ResultSet resultSet = statement.executeQuery(q);
			while(resultSet.next()) {
				rowCount++;
			}
		} catch (Exception e) {
			Logger.warn(e);
		}
		return rowCount;		
	}
}
