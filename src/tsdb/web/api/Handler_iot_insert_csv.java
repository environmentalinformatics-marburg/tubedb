package tsdb.web.api;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;

import org.eclipse.jetty.server.Request;

import tsdb.component.SourceEntry;
import tsdb.remote.RemoteTsDB;
import tsdb.util.DataRow;
import tsdb.util.Table;
import tsdb.util.TimeUtil;
import tsdb.util.iterator.CSVTimeType;

/**
 * get meta data of region 
 * @author woellauer
 *
 */
public class Handler_iot_insert_csv extends MethodHandler {	


	public Handler_iot_insert_csv(RemoteTsDB tsdb) {
		super(tsdb, "insert_csv");
	}



	@Override
	public void handle(String target, Request request, HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {		
		request.setHandled(true);
		Logger.info("target " + target);

		if(request.getMethod() != "POST") {
			throw new RuntimeException("insert_csv needs HTTP method POST");
		}

		// needs to read before parameters !!!!
		Table table = null;		
		try {
			table = Table.readCSV(request.getReader(), ',');
		} catch (Exception e) {
			throw new RuntimeException("could not read data");
		}

		/*String request_data = Web.requestContentToString(request);  		
		Logger.info("stream");
		Logger.info(request_data);*/

		String stationName = request.getParameter("station");
		if(stationName == null) {
			throw new RuntimeException("missing url parameter 'station'");
		}

		CSVTimeType csvTimeType = CSVTimeType.DATETIME;
		{
			String datetime_fomat = request.getParameter("datetime_format");
			Logger.info("dt " + datetime_fomat);
			if(datetime_fomat != null) {
				switch(datetime_fomat) {
				case "custom":
					csvTimeType = CSVTimeType.CUSTOM;
					break;
				default:
					Logger.warn("unknown datetime_fomat");
				}
			}
		}

		if(table == null) {
			throw new RuntimeException("could not read data");
		}

		int datetimeIndex = table.getColumnIndex("datetime");
		if(datetimeIndex != 0) {
			throw new RuntimeException("missing 'datetime' column in CSV header");
		}
		final int sensors = table.names.length-1;
		if(sensors <= 0) {
			throw new RuntimeException("no sensors in CSV header");
		}

		ArrayList<DataRow> dataRows = new ArrayList<>(table.rows.length);

		int prevTimestamp = -1;
		for(String[] row:table.rows) {
			int timestamp;
			if(csvTimeType == CSVTimeType.DATETIME) {
				timestamp = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.parse(row[0]));
			} else {
				String[] dt = row[0].split(" ");
				LocalDate date = LocalDate.parse(dt[0]);
				LocalTime time = LocalTime.parse(dt[1]);
				LocalDateTime datetime = LocalDateTime.of(date, time);
				timestamp = (int) TimeUtil.dateTimeToOleMinutes(datetime);
			}

			if(timestamp==prevTimestamp) {
				Logger.warn("skip duplicate timestamp "+row[0]);
				continue;
			}

			float[] data = new float[sensors];
			for(int i=0;i<sensors;i++) {
				String text = row[i+1];
				if(text.isEmpty() || text.equals("NA")) {
					data[i] = Float.NaN;
				} else {
					try {
						//float value = Float.parseFloat(text);
						float value = (float) FastDoubleParser.parseDouble(text);
						if(Float.isFinite(value)) {
							data[i] = value;
						} else {
							data[i] = Float.NaN;
						}
					} catch(Exception e) {
						data[i] = Float.NaN;
					}
				}
			}
			dataRows.add(new DataRow(data, timestamp));
			prevTimestamp = timestamp;
		}

		if(!dataRows.isEmpty()) {
			String[] sensorNames = Arrays.copyOfRange(table.names, 1, sensors + 1);
			tsdb.insertDataRows(stationName, sensorNames, dataRows);
			tsdb.insertSourceCatalogEntry(SourceEntry.of(stationName, sensorNames, dataRows, Paths.get("web", "insert_csv")));
		}
	}
}
