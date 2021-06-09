package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Arrays;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.remote.RemoteTsDB;
import tsdb.util.DataEntry;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TimestampSeries;
import tsdb.web.util.Web;

/**
 * get meta data of region 
 * @author woellauer
 *
 */
public class Handler_iot_sensor extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_iot_sensor(RemoteTsDB tsdb) {
		super(tsdb, "sensor");
	}

	private static final int lat_lon_add_factor = 100_000;

	@Override
	public void handle(String target, Request request, HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {		
		request.setHandled(true);
		log.info("target " + target);
		int i = target.indexOf("/");
		String sensorID = target;
		String stationID = "";
		if(i>=0) {
			stationID = target.substring(i + 1);
			sensorID = target.substring(0, i);
		}
		log.info("sensorID: " + sensorID);
		log.info("stationID: " + stationID);
		String httpMethod = request.getMethod();
		log.info("Method: " + httpMethod);

		switch(httpMethod) {
		case "POST":
			switch(sensorID) {
			case "location": {
				response.setContentType("text/plain;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				PrintWriter writer = response.getWriter();
				String req_data = Web.requestContentToString(request);
				String[] lines = req_data.split("\n");
				log.info("req " + req_data);
				for(String rawLine:lines) {
					String line = rawLine.trim();
					if(!line.isEmpty()) {
						String[] data = line.split(",");
						log.info(Arrays.toString(data));
						if(data.length != 3 && data.length != 4) {
							response.setContentType("text/plain;charset=utf-8");
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							log.warn("unknown sensor_type: " + sensorID);
							writer.println("expected three or four columns: " + Arrays.toString(data));
							return;
						}
						long unixTime = Long.parseLong(data[0]);
						LocalDateTime datetime = TimeUtil.unixTimeToLocalDateTime(unixTime);
						int timestamp = (int) TimeUtil.dateTimeToOleMinutes(datetime);
						float value_lon = Float.parseFloat(data[1]);
						float value_lat = Float.parseFloat(data[2]);
						log.info("insert " + timestamp + "   " + value_lon + " " + value_lat);
						tsdb.insertOneValue(stationID, "location_lon", timestamp, value_lon * lat_lon_add_factor);
						tsdb.insertOneValue(stationID, "location_lat", timestamp, value_lat * lat_lon_add_factor);
						writer.println("inserted " + stationID + "/" + "location_lon" + "@" + TimeUtil.oleMinutesToText(timestamp) + "  " + value_lon);
						writer.println("inserted " + stationID + "/" + "location_lat" + "@" + TimeUtil.oleMinutesToText(timestamp) + "  " + value_lat);
						if(data.length == 4) {
							float value_sat = Float.parseFloat(data[3]);
							tsdb.insertOneValue(stationID, "location_sat", timestamp, value_sat);
							writer.println("inserted " + stationID + "/" + "location_sat" + "@" + TimeUtil.oleMinutesToText(timestamp) + "  " + value_sat);
						}
					}
				}
				break;
			}
			default: {
				response.setContentType("text/plain;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				PrintWriter writer = response.getWriter();
				String[] lines = Web.requestContentToString(request).split("\n");
				for(String rawLine:lines) {
					String line = rawLine.trim();
					if(!line.isEmpty()) {
						String[] data = line.split(",");
						log.info(Arrays.toString(data));
						if(data.length != 2) {
							response.setContentType("text/plain;charset=utf-8");
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							log.warn("unknown sensor_type: " + sensorID);
							response.getWriter().println("expected two columns: " + Arrays.toString(data));
							return;
						}
						long unixTime = Long.parseLong(data[0]);
						LocalDateTime datetime = TimeUtil.unixTimeToLocalDateTime(unixTime);
						int timestamp = (int) TimeUtil.dateTimeToOleMinutes(datetime);
						float value = Float.parseFloat(data[1]);
						log.info("insert " + timestamp + "   " + value);
						tsdb.insertOneValue(stationID, sensorID, timestamp, value);
						writer.println("inserted " + stationID + "/" + sensorID + "@" + TimeUtil.oleMinutesToText(timestamp) + "  " + value);
					}
				}
				break;
			}
			/*default:			
				response.setContentType("text/plain;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.warn("unknown sensor_type: " + sensorID);
				response.getWriter().println("unknown sensor_type: " + sensorID);
				return;*/
			}
			break;
		case "GET":
			switch(sensorID) {
			case "temperature": 
			case "light":
			case "pressure": {				
				response.setContentType("text/plain;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				PrintWriter writer = response.getWriter();
				String[] stationNames = tsdb.getInternalStoredStationNames();
				for(String stationName:stationNames) {
					DataEntry[] data = tsdb.readRawData(stationName, sensorID);
					if(data != null) {
						for(DataEntry entry:data) {
							String timestamp = TimeUtil.oleMinutesToLocalDateTime(entry.timestamp).format(TimeUtil.DATE_TIME_FORMATER_SPACE_SECONDS);
							writer.println(timestamp + "," + stationName + "," + entry.value);
						}
					}
				}
				break;
			}
			case "location": {
				response.setContentType("text/plain;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_OK);
				PrintWriter writer = response.getWriter();
				String[] stationNames = tsdb.getInternalStoredStationNames();
				String[] sensorNames = new String[] {"location_lon", "location_lat", "location_sat"};
				for(String stationName:stationNames) {
					TimestampSeries data = tsdb.readRawData(stationName, sensorNames);
					if(data != null) {
						for(TsEntry entry:data) {
							String timestamp = TimeUtil.oleMinutesToLocalDateTime(entry.timestamp).format(TimeUtil.DATE_TIME_FORMATER_SPACE_SECONDS);
							writer.println(timestamp + ","  + stationName +  "," + (entry.data[0] / lat_lon_add_factor) + "," + (entry.data[1] / lat_lon_add_factor)+  "," + entry.data[2]);
						}
					}
				}
				break;
			}
			default:			
				response.setContentType("text/plain;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.warn("unknown sensor_type: " + sensorID);
				response.getWriter().println("unknown sensor_type: " + sensorID);
				return;
			}
			break;
		default:
			response.setContentType("text/plain;charset=utf-8");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			log.warn("unknown HTTP method: " + httpMethod);
			response.getWriter().println("unknown HTTP method: " + httpMethod);
			return;
		}
	}
}
