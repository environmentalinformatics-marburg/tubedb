package tsdb.web.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.json.JSONException;
import org.json.JSONWriter;

import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.remote.GeneralStationInfo;
import tsdb.remote.PlotInfo;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.StationInfo;
import tsdb.util.TimeUtil;
import tsdb.web.util.Web;

/**
 * get meta data of region 
 * @author woellauer
 *
 */
public class Handler_metadata extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_metadata(RemoteTsDB tsdb) {
		super(tsdb, "metadata.json");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("application/json;charset=utf-8");
		String regionName = request.getParameter("region");
		UserIdentity userIdentity = Web.getUserIdentity(baseRequest);
		if(regionName==null) {
			log.warn("missing region parameter");
			response.getWriter().write("missing region parameter");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			if(Web.isAllowed(userIdentity, regionName)) {
				Region region = tsdb.getRegionByName(regionName);
				if(region==null) {
					log.warn("region not found");
					response.getWriter().write("region not found");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				} else {
					JSONWriter json_output = new JSONWriter(response.getWriter());
					writeRegion(json_output, region);
					response.setStatus(HttpServletResponse.SC_OK);
				}
			} else {
				log.warn("no access to region "+regionName);
				response.getWriter().write("no access to region "+regionName);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}		
	}

	private void writeRegion(JSONWriter json_output, Region region) throws JSONException, IOException {
		json_output.object();

		json_output.key("region");		
		json_output.object();
		json_output.key("id");
		json_output.value(region.name);
		json_output.key("name");
		json_output.value(region.longName);
		json_output.key("view_year_range");
		json_output.object();
		json_output.key("start");
		json_output.value(String.valueOf(TimeUtil.fastDateWriteYears(TimeUtil.oleMinutesToLocalDateTime(region.viewTimeRange.start).toLocalDate())));
		json_output.key("end");
		json_output.value(String.valueOf(TimeUtil.fastDateWriteYears(TimeUtil.oleMinutesToLocalDateTime(region.viewTimeRange.end).toLocalDate())));
		json_output.endObject();
		if(region.defaultGeneralStation != null) {
			json_output.key("default_general_station");
			json_output.value(region.defaultGeneralStation);
		}
		json_output.endObject();

		GeneralStationInfo[] generalStationInfos = tsdb.getGeneralStationsOfRegion(region.name);		
		json_output.key("general_stations");
		json_output.array();
		for(GeneralStationInfo generalStationInfo:generalStationInfos) {
			json_output.object();
			json_output.key("id");
			json_output.value(generalStationInfo.name);
			json_output.key("name");
			json_output.value(generalStationInfo.longName);
			if(generalStationInfo.viewTimeRange != null) {
				json_output.key("view_year_range");
				json_output.object();
				json_output.key("start");
				json_output.value(String.valueOf(TimeUtil.fastDateWriteYears(TimeUtil.oleMinutesToLocalDateTime(generalStationInfo.viewTimeRange.start).toLocalDate())));
				json_output.key("end");
				json_output.value(String.valueOf(TimeUtil.fastDateWriteYears(TimeUtil.oleMinutesToLocalDateTime(generalStationInfo.viewTimeRange.end).toLocalDate())));
				json_output.endObject();
			}
			json_output.endObject();
		}
		json_output.endArray();

		Set<String> sensorNameSet = new HashSet<String>();
		Set<String> stationNameSet = new HashSet<String>();

		PlotInfo[] plotInfos = tsdb.getPlots();
		json_output.key("plots");
		json_output.array();
		for(PlotInfo plotInfo:plotInfos) {
			if(region.name.equals(plotInfo.generalStationInfo.region.name)) {
				String[] sensorNames = tsdb.getSensorNamesOfPlotWithVirtual(plotInfo.name);
				Arrays.sort(sensorNames, String.CASE_INSENSITIVE_ORDER);
				json_output.object();
				json_output.key("id");
				json_output.value(plotInfo.name);
				json_output.key("general_station");
				json_output.value(plotInfo.generalStationInfo.name);
				json_output.key("sensor_names");
				json_output.array();
				for(String sensorName:sensorNames) {
					sensorNameSet.add(sensorName);
					json_output.value(sensorName);
				}
				json_output.endArray();
				String[] stations = tsdb.getPlotStations(plotInfo.name);
				if(stations != null) {
					json_output.key("plot_stations");
					json_output.array();
					for(String station:stations) {
						stationNameSet.add(station);
						json_output.value(station);
					}
					json_output.endArray();
				}
				if(plotInfo.isStation) {
					json_output.key("logger_type");
					json_output.value(plotInfo.loggerTypeName);
				}
				json_output.key("vip");
				json_output.value(plotInfo.isVIP);
				if(Double.isFinite(plotInfo.geoPosLatitude)) {
					json_output.key("latitude");
					json_output.value(plotInfo.geoPosLatitude);
				}
				if(Double.isFinite(plotInfo.geoPosLongitude)) {
					json_output.key("longitude");
					json_output.value(plotInfo.geoPosLongitude);
				}
				if(Double.isFinite(plotInfo.elevation)) {
					json_output.key("elevation");
					json_output.value(plotInfo.elevation);
				}
				json_output.endObject();
			}
		}
		json_output.endArray();

		Sensor[] sensors = tsdb.getSensors();		
		Map<String, Sensor> sensorMap = Arrays.asList(sensors).stream().collect(Collectors.toMap(Sensor::getName, Function.identity()));
		ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
		for(String sensorName:sensorNameSet) {
			Sensor sensor = sensorMap.get(sensorName);
			if(sensor == null) {
				if(!TsDBFactory.HIDE_INTENAL_SENSORS) {
					sensor = new Sensor(sensorName);
					sensor.internal = true; // sensors that do not exist in config are marked as internal
					sensorList.add(sensor);
				}
			} else {
				if(!sensor.internal || !TsDBFactory.HIDE_INTENAL_SENSORS) {
					sensorList.add(sensor);
				}
			}
		}
		sensorList.sort((a,b)->String.CASE_INSENSITIVE_ORDER.compare(a.name, b.name));

		json_output.key("sensors");
		json_output.array();
		for(Sensor sensor:sensorList) {
			if(sensorNameSet.contains(sensor.name)) {
				json_output.object();
				json_output.key("id");
				json_output.value(sensor.name);
				json_output.key("description");
				json_output.value(sensor.description);
				json_output.key("unit_description");
				json_output.value(sensor.unitDescription);
				json_output.key("raw");
				json_output.value(!sensor.isAggregable());
				json_output.key("derived");
				json_output.value(sensor.isDerived());
				json_output.key("internal");
				json_output.value(sensor.internal);
				json_output.endObject();
			}
		}
		json_output.endArray();

		StationInfo[] stations = tsdb.getStations();
		Arrays.sort(stations,(a,b)->String.CASE_INSENSITIVE_ORDER.compare(a.stationID, b.stationID));

		json_output.key("stations");
		json_output.array();
		for(StationInfo station:stations) {
			if(stationNameSet.contains(station.stationID)) {
				json_output.object();
				json_output.key("id");
				json_output.value(station.stationID);
				json_output.key("logger_type");
				json_output.value(station.loggerType.typeName);
				String[] sensorNames = tsdb.getSensorNamesOfPlotWithVirtual(station.stationID);
				Arrays.sort(sensorNames, String.CASE_INSENSITIVE_ORDER);
				json_output.key("sensor_names");
				json_output.array();
				for(String sensorName:sensorNames) {
					sensorNameSet.add(sensorName);
					json_output.value(sensorName);
				}
				json_output.endArray();
				json_output.endObject();
			}
		}
		json_output.endArray();


		json_output.endObject();
	}
}
