package tsdb.run;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NavigableSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.h2.jdbcx.JdbcDataSource;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;

public class H2DataWriter {
	private static final Logger log = LogManager.getLogger();
	private final TsDB tsdb;

	public H2DataWriter(TsDB tsdb) {
		this.tsdb = tsdb;
	}

	public static void main(String[] args) throws SQLException {

		TsDB tsdb = TsDBFactory.createDefault();
		H2DataWriter writer = new H2DataWriter(tsdb);			
		writer.writeAllStationsToDB();		
		tsdb.close();

		/*
		long timeStartImport = System.currentTimeMillis();

		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:c:/h2_storage/h2_storage");
		//ds.setUser("sa");
		//ds.setPassword("sa");
		Connection connection = ds.getConnection();

		try(Statement statement = connection.createStatement()) {
			statement.execute("DROP ALL OBJECTS");
		} catch(SQLException sqle) {
			log.error("not dropping "+sqle);
		}


		for(int tsi=0;tsi<100;tsi++) {
			String ts = "ts"+tsi;
			log.info("ts "+ts);
			Statement statement = connection.createStatement();
			statement.execute("CREATE TABLE "+ts+" (timestamp INT4 PRIMARY KEY, m FLOAT4)");
			statement.close();
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+ts+" VALUES (?,?)");
			for (int i=0;i<400000;i++)
			{
				preparedStatement.setInt(1, i);
				preparedStatement.setFloat(2, i);
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
			connection.commit();
			preparedStatement.close();

		}




		connection.commit();
		connection.close();

		long timeEndImport = System.currentTimeMillis();
		log.info((timeEndImport-timeStartImport)/1000+" s Export");*/
	}





	public void writeAllStationsToDB() {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:c:/h2_storage/h2_storage");

		try(Connection connection = ds.getConnection()) {

			/*try(Statement statement = connection.createStatement()) { // clear database
				statement.execute("DROP ALL OBJECTS");
			} catch(SQLException sqle) {
				log.error("not dropping "+sqle);
			}*/

			NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

			long timeStartImport = System.currentTimeMillis();
			try {
				for(String stationName:stationNames) {
					try {
						String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
						for(String sensorName:sensorNames) {
							StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, sensorName, null, null);
							if(it!=null&&it.hasNext()) {
								String tsName = stationName+"_"+sensorName;
								try(Statement statement = connection.createStatement()) {
									statement.execute("CREATE TABLE "+tsName+" (timestamp INT4 PRIMARY KEY, m FLOAT4)");
								} catch (Exception e) {
									log.warn("at create table "+e);
								}
								PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+tsName+" VALUES (?,?)");
								while(it.hasNext()) {
									DataEntry e = it.next();
									preparedStatement.setInt(1, e.timestamp);
									preparedStatement.setFloat(2, e.value);
									preparedStatement.addBatch();						
								}
								preparedStatement.executeBatch();
								connection.commit();
								preparedStatement.close();
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
						log.error(e);
					}
				}
			} catch (Exception e) {
				log.error(e);
			}
			long timeEndImport = System.currentTimeMillis();
			log.info((timeEndImport-timeStartImport)/1000+" s Export");
		} catch(Exception e) {
			log.error(e);
		} 

	}








}
