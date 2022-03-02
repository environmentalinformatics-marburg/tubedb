package tsdb.web.api;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.json.JSONWriter;

import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.dsl.FormulaBuilder;
import tsdb.dsl.FormulaJavaVisitor;
import tsdb.dsl.FormulaPrintFormulaVisistor;
import tsdb.dsl.FormulaToJsonTreeVisitor;
import tsdb.dsl.FormulaToStringVisitor;
import tsdb.dsl.FormulaUnifyVisitor;
import tsdb.dsl.formula.Formula;
import tsdb.dsl.printformula.PrintFormula;
import tsdb.dsl.printformula.PrintFormulaToJsonVisitor;
import tsdb.remote.GeneralStationInfo;
import tsdb.remote.PlotInfo;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.StationInfo;
import tsdb.web.util.Web;

public class Handler_model extends MethodHandler {	
	

	public Handler_model(RemoteTsDB tsdb) {
		super(tsdb, "model");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		UserIdentity userIdentity = Web.getUserIdentity(baseRequest);
		baseRequest.setHandled(true);
		response.setContentType("application/json;charset=utf-8");
		JSONWriter json = new JSONWriter(response.getWriter());
		writeModel(json, userIdentity);		
	}

	private void writeModel(JSONWriter json, UserIdentity userIdentity) throws RemoteException {
		json.object();
		json.key("model");
		json.object();
		json.key("projects");
		json.object();
		for(Region region : tsdb.getRegions()) {
			if(Web.isAllowed(userIdentity, region.name)) {
				json.key(region.name);
				json.object();
				json.key("id");
				json.value(region.name);
				json.key("title");
				json.value(region.longName);				
				json.key("groups");
				json.array();
				GeneralStationInfo[] generalStationInfos = tsdb.getGeneralStationsOfRegion(region.name);
				for(GeneralStationInfo generalStationInfo : generalStationInfos) {
					json.value(generalStationInfo.name);
				}
				json.endArray(); // end groups
				if(region.viewTimeRange != null) {
					json.key("view_timestamp_start");
					json.value(region.viewTimeRange.start);		
					json.key("view_timestamp_end");
					json.value(region.viewTimeRange.end);		
				}
				json.endObject(); // end project
			}
		}
		json.endObject(); // end projects
		json.key("groups");
		json.object();
		HashSet<String> allPlots = new HashSet<String>();
		for(GeneralStationInfo generalStationInfo : tsdb.getGeneralStations()) {
			if(Web.isAllowed(userIdentity, generalStationInfo.region.name)) {
				json.key(generalStationInfo.name);
				json.object();
				json.key("id");
				json.value(generalStationInfo.name);
				json.key("title");
				json.value(generalStationInfo.longName);				
				json.key("plots");
				json.array();
				HashSet<String> assignedPlots = new HashSet<String>();
				if(generalStationInfo.assigned_plots != null) {
					for(String assigned_plot : generalStationInfo.assigned_plots) {
						assignedPlots.add(assigned_plot);
					}
				}
				PlotInfo[] plotInfos = tsdb.getPlots();
				for(PlotInfo plotInfo : plotInfos) {
					if(assignedPlots.contains(plotInfo.name) || plotInfo.generalStationInfo.name.equals(generalStationInfo.name)) {
						json.value(plotInfo.name);
						allPlots.add(plotInfo.name);
					}
				}
				json.endArray(); // end plots
				if(generalStationInfo.viewTimeRange != null) {
					json.key("view_timestamp_start");
					json.value(generalStationInfo.viewTimeRange.start);		
					json.key("view_timestamp_end");
					json.value(generalStationInfo.viewTimeRange.end);		
				} else if(generalStationInfo.region != null && generalStationInfo.region.viewTimeRange != null){
					json.key("view_timestamp_start");
					json.value(generalStationInfo.region.viewTimeRange.start);		
					json.key("view_timestamp_end");
					json.value(generalStationInfo.region.viewTimeRange.end);	
				}
				json.endObject(); // end group
			}
		}
		json.endObject(); // end groups
		json.key("plots");
		json.object();
		HashSet<String> allStations = new HashSet<String>();
		HashSet<String> allSensors = new HashSet<String>();
		PlotInfo[] plotInfos = tsdb.getPlots();
		for(PlotInfo plotInfo : plotInfos) {
			if(allPlots.contains(plotInfo.name)) {
				json.key(plotInfo.name);
				json.object();
				json.key("id");
				json.value(plotInfo.name);				
				json.key("stations");
				json.array();
				String[] stationNames = tsdb.getPlotStations(plotInfo.name);
				if(stationNames != null)  {
					for(String stationName : stationNames) {
						json.value(stationName);
						allStations.add(stationName);
					}
				}
				json.endArray(); // end stations

				json.key("sensors");
				json.array();
				String[] sensorNames = tsdb.getSensorNamesOfPlotWithVirtual(plotInfo.name);
				for(String sensorName : sensorNames) {
					json.value(sensorName);
					allSensors.add(sensorName);
				}
				json.endArray(); // end sensors
				json.endObject(); // end plot
			}
		}
		json.endObject(); // end plots
		json.key("stations");
		json.object();
		StationInfo[] stations = tsdb.getStations();
		Arrays.sort(stations,(a,b)->String.CASE_INSENSITIVE_ORDER.compare(a.stationID, b.stationID));
		for(StationInfo station: stations) {
			if(allStations.contains(station.stationID)) {
				json.key(station.stationID);
				json.object();
				json.key("id");
				json.value(station.stationID);				
				json.key("sensors");
				json.array();
				String[] sensorNames = tsdb.getSensorNamesOfPlotWithVirtual(station.stationID);
				for(String sensorName : sensorNames) {
					json.value(sensorName);
					allSensors.add(sensorName);
				}
				json.endArray(); // end sensors
				json.endObject(); // end plot
			}
		}
		json.endObject(); // end stations
		json.key("sensors");
		json.object();		
		Sensor[] sensors = tsdb.getSensors();
		Map<String, Sensor> sensorMap = Arrays.asList(sensors).stream().collect(Collectors.toMap(Sensor::getName, Function.identity()));		
		ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
		for(String sensorName : allSensors) {
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
		for(Sensor sensor : sensorList) {
			json.key(sensor.name);
			json.object();
			json.key("id");
			json.value(sensor.name);
			if(sensor.description != null) {
				json.key("description");
				json.value(sensor.description);
			}
			if(sensor.unitDescription != null) {
				json.key("unit");
				json.value(sensor.unitDescription);
			}
			json.key("aggregation_hour");
			json.value(sensor.getAggregationHour().toString());
			json.key("aggregation_day");
			json.value(sensor.getAggregationDay().toString());
			json.key("aggregation_week");
			json.value(sensor.getAggregationWeek().toString());
			json.key("aggregation_month");
			json.value(sensor.getAggregationMonth().toString());
			json.key("aggregation_year");
			json.value(sensor.getAggregationYear().toString());
			if(-Float.MAX_VALUE < sensor.physicalMin || sensor.physicalMax < Float.MAX_VALUE) {
				json.key("physical_range");
				json.array();
				json.value(sensor.physicalMin);
				json.value(sensor.physicalMax);
				json.endArray();
			}
			if(0f < sensor.stepMin || sensor.stepMax < Float.MAX_VALUE) {
				json.key("step_range");
				json.array();
				json.value(sensor.stepMin);
				json.value(sensor.stepMax);
				json.endArray();
			}
			if(sensor.empiricalDiff != null) {
				json.key("empirical_diff");
				json.value(sensor.empiricalDiff);
			}
			if(sensor.useInterpolation) {
				json.key("interpolation_mse");
				json.value(sensor.maxInterpolationMSE);
			}
			json.key("category");
			json.value(sensor.category.toString());
			json.key("visibility");
			json.value(sensor.internal ? "internal" : "public");
			if(sensor.isDerived()) {
				json.key("derived");
				json.value(sensor.isDerived());
			}
			if(sensor.raw_source != null && sensor.raw_source.length > 0) {
				json.key("raw_source");
				json.array();
				for(String name:sensor.raw_source) {
					json.value(name);
				}
				json.endArray();
			}
			if(sensor.dependency != null && sensor.dependency.length > 0) {
				json.key("dependency");
				json.array();
				for(String name:sensor.dependency) {
					json.value(name);
				}
				json.endArray();
			}
			if(sensor.raw_func != null) {
				json.key("raw_func");
				json.value(sensor.raw_func);

				try {
					Formula formula_org = FormulaBuilder.parseFormula(sensor.raw_func);
					Formula formula_unified = formula_org.accept(FormulaUnifyVisitor.DEFAULT);
					String funcText = formula_unified.accept(FormulaToStringVisitor.DEFAULT);
					if(funcText != null && !funcText.isEmpty()) {
						json.key("raw_func_parsed");				
						json.value(funcText);
						json.key("raw_func_tree");				
						formula_unified.accept(new FormulaToJsonTreeVisitor(json));		
						json.key("raw_func_print");				
						PrintFormula printFormula = formula_unified.accept(FormulaPrintFormulaVisistor.DEFAULT);	
						printFormula.accept(new PrintFormulaToJsonVisitor(json));
					}
				} catch(Exception e) {
					Logger.warn(e);
				}
			}
			if(sensor.post_hour_func != null) {
				json.key("post_hour_func");
				json.value(sensor.post_hour_func);

				try {
					Formula formula_org = FormulaBuilder.parseFormula(sensor.post_hour_func);
					Formula formula_unified = formula_org.accept(FormulaUnifyVisitor.DEFAULT);
					String funcText = formula_unified.accept(FormulaToStringVisitor.DEFAULT);
					if(funcText != null && !funcText.isEmpty()) {
						json.key("post_hour_func_parsed");				
						json.value(funcText);	
						json.key("post_hour_func_tree");				
						formula_unified.accept(new FormulaToJsonTreeVisitor(json));		
						json.key("post_hour_func_print");				
						PrintFormula printFormula = formula_unified.accept(FormulaPrintFormulaVisistor.DEFAULT);	
						printFormula.accept(new PrintFormulaToJsonVisitor(json));
					}
				} catch(Exception e) {
					e.printStackTrace();
					Logger.warn(e + " at " + sensor.post_hour_func);
				}
			}
			if(sensor.post_day_func != null) {
				json.key("post_day_func");
				json.value(sensor.post_day_func);

				try {
					Formula formula_org = FormulaBuilder.parseFormula(sensor.post_day_func);
					Formula formula_unified = formula_org.accept(FormulaUnifyVisitor.DEFAULT);
					String funcText = formula_unified.accept(FormulaToStringVisitor.DEFAULT);
					if(funcText != null && !funcText.isEmpty()) {
						json.key("post_day_func_parsed");				
						json.value(funcText);
						json.key("post_day_func_tree");				
						formula_unified.accept(new FormulaToJsonTreeVisitor(json));		
						json.key("post_day_func_print");				
						PrintFormula printFormula = formula_unified.accept(FormulaPrintFormulaVisistor.DEFAULT);	
						printFormula.accept(new PrintFormulaToJsonVisitor(json));
					}
				} catch(Exception e) {
					Logger.warn(e);
				}				
			}
			json.endObject(); // end sensor
		}
		json.endObject();  // end sensors		
		json.endObject(); // end model
		json.endObject(); // end
	}




}
