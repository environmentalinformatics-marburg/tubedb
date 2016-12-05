package tsdb.run;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.NavigableSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;
import tsdb.util.Timer;

public class MonetDbDataWriter {
	private static final Logger log = LogManager.getLogger();
	private final TsDB tsdb;
	
	//public static String url = "jdbc:monetdb://localhost:50000/mydb?so_timeout=10000";
	public static String url = "jdbc:monetdb://192.168.191.183:8082/mydb?so_timeout=10000";
	
	public static String tablePrefix = "ts10_";

	public MonetDbDataWriter(TsDB tsdb) {
		this.tsdb = tsdb;
	}

	public static void main(String[] args) throws SQLException {
		TsDB tsdb = TsDBFactory.createDefault();
		MonetDbDataWriter writer = new MonetDbDataWriter(tsdb);			
		writer.writeAllStationsToDB();		
		tsdb.close();
	}





	public void writeAllStationsToDB() {
		
		try(Connection connection = DriverManager.getConnection(url, "monetdb", "monetdb")) {

			/*try(Statement statement = connection.createStatement()) { // clear database
				statement.execute("DROP ALL OBJECTS");
			} catch(SQLException sqle) {
				log.error("not dropping "+sqle);
			}*/

			NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();
			
			connection.setAutoCommit(false);

			Timer.start("MonetDB full write");
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
									statement.execute("CREATE TABLE "+tablePrefix+tsName+" (timestamp INT PRIMARY KEY, m REAL);");
								} catch (Exception e) {
									log.warn("at create table "+e);
								}
								PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO "+tablePrefix+tsName+" VALUES (?,?)");
								while(it.hasNext()) {
									DataEntry e = it.next();
									preparedStatement.setInt(1, e.timestamp);
									preparedStatement.setFloat(2, e.value);
									preparedStatement.addBatch();						
								}
								preparedStatement.executeBatch();
								//connection.commit();
								preparedStatement.close();
							}
						}
						connection.commit();
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
			log.info(Timer.stop("MonetDB full write"));
		} catch(Exception e) {
			log.error(e);
		} 

	}








}
