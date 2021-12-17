package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.TsDBFactory;
import tsdb.component.Sensor;
import tsdb.remote.GeneralStationInfo;
import tsdb.remote.RemoteTsDB;

/**
 * Get sensors (parameters).
 * <p>
 * parameters:
 * <br>
 * (exactly one of) station or plot or general_station or region
 * <p>
 * returns: list of sensors with [name],[description],[unit],[aggregation type],[is internal]
 * @author woellauer
 *
 */
public class Handler_sensor_list extends MethodHandler {	
	

	public Handler_sensor_list(RemoteTsDB tsdb) {
		super(tsdb, "sensor_list");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");
		String plot = request.getParameter("plot");
		String general_station = request.getParameter("general_station");
		String region = request.getParameter("region");
		String station = request.getParameter("station");
		if(!(     (plot!=null&&general_station==null&&region==null&&station==null)
				||(plot==null&&general_station!=null&&region==null&&station==null)
				||(plot==null&&general_station==null&&region!=null&&station==null)
				||(plot==null&&general_station==null&&region==null&&station!=null)
				)) {
			Logger.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try {
			String[] sensorNames = null;
			if(plot!=null) {			
				sensorNames = tsdb.getSensorNamesOfPlotWithVirtual(plot);			
			} else if(general_station!=null) {
				sensorNames = tsdb.getSensorNamesOfGeneralStationWithVirtual(general_station);	
			} else if(region!=null){
				Set<String> sensorNameSet = new TreeSet<String>();
				for(GeneralStationInfo generalStationInfo:tsdb.getGeneralStations()) {
					if(generalStationInfo.region.name.equals(region)) {
						for(String sensorName:tsdb.getSensorNamesOfGeneralStationWithVirtual(generalStationInfo.name)) {
							sensorNameSet.add(sensorName);
						}
					}
				}
				sensorNames = sensorNameSet.toArray(new String[0]);
			} else if(station!=null) {
				sensorNames = tsdb.getSensorNamesOfPlotWithVirtual(station); //TODO change to just query stations
			}
			if(sensorNames==null) {
				Logger.error("sensorNames null: "+plot);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}
			//Arrays.sort(sensorNames,String.CASE_INSENSITIVE_ORDER);
			boolean isRaw = Boolean.parseBoolean(request.getParameter("raw"));
			//Map<String, Integer> sensorMap = Util.stringArrayToMap(sensorNames);
			Sensor[] tsdbSensors = tsdb.getSensors();
			if(tsdbSensors==null) {
				Logger.error("sensors null: "+plot);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}
			Map<String,Sensor> sensorMap = new HashMap<String, Sensor>();
			for(Sensor sensor:tsdbSensors) {
				sensorMap.put(sensor.name, sensor);
			}

			/*ArrayList<Sensor> sensorList = new ArrayList<>(sensorNames.)
			for(String sensorName:sensorNames) {

			}*/
			String[] webList = Arrays.stream(sensorNames)
					.map(sensorName->{
						Sensor sensor = sensorMap.get(sensorName);
						if(sensor == null) {
							sensor = new Sensor(sensorName);
							sensor.internal = true; // sensors that do not exist in config are marked as internal
						}
						return sensor;
					}).filter(s->(!s.internal || !TsDBFactory.HIDE_INTENAL_SENSORS) && (isRaw || s.isAggregable()))
					.sorted((s1,s2)->{
						if(s1.internal&&!s2.internal) {
							return +1;
						}
						if(!s1.internal&&s2.internal) {
							return -1;
						}
						if(s1.isAggregable()&&!s2.isAggregable()) {
							return -1;
						}
						if(!s1.isAggregable()&&s2.isAggregable()) {
							return +1;
						}
						return String.CASE_INSENSITIVE_ORDER.compare(s1.name, s2.name);
					}).map(s->{
						String desc = s.description==null?"---":s.description;
						String unit = s.unitDescription==null?"---":s.unitDescription;
						return s.name+";"+desc+";"+unit+";"+s.getAggregationHour()+";"+s.internal;
					}).toArray(String[]::new);


			/*String[] webList = Arrays.stream(sensors)
					.filter(s->sensorMap.containsKey(s.name)&&s.isAggregable())
					.map(s->s.name+";"+s.description+";"+s.unitDescription)
					.toArray(String[]::new);*/
			/*ArrayList<String> webList = new ArrayList<String>();
			for(String sensorName:sensorNames) {
				Sensor s = sensorMap.get(sensorName);
				if(TsDBFactory.HIDE_INTENAL_SENSORS&&s!=null&&s.internal) {
					continue; //internal sensor
				}
				//if(true/*s.isAggregable()*///) {
			//if(/*true*/s.isAggregable()) {
			/*if(isRaw||((s!=null)&&s.isAggregable())) {
					if(s!=null) {
						String desc = s.description==null?"---":s.description;
						String unit = s.unitDescription==null?"---":s.unitDescription;
						webList.add(s.name+";"+desc+";"+unit+";"+s.baseAggregationType+";"+s.internal);
					} else {
						webList.add(sensorName+";"+"unknown"+";"+"unknown"+";"+"NONE"+";"+"false");	
					}
				}
			}*/

			PrintWriter writer = response.getWriter();
			writeStringArray(writer, webList);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
