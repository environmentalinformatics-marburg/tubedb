package tsdb.web.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import org.json.JSONWriter;

import tsdb.component.Sensor;
import tsdb.remote.GeneralStationInfo;
import tsdb.remote.PlotInfo;
import tsdb.remote.RemoteTsDB;
import tsdb.util.Pair;
import tsdb.web.util.WebUtil;

public class TsDBAPIHandler extends AbstractHandler {

	private static final Logger log = LogManager.getLogger();

	private final RemoteTsDB tsdb;

	private HashMap<String,Handler> handlerMap;

	public TsDBAPIHandler(RemoteTsDB tsdb) {
		this.tsdb=tsdb;
		handlerMap = new HashMap<String,Handler>();
		addMethodHandler(new Handler_region_list(tsdb));
		addMethodHandler(new Handler_generalstation_list(tsdb));
		addMethodHandler(new Handler_plot_list(tsdb));
		addMethodHandler(new Handler_sensor_list(tsdb));
		addMethodHandler(new Handler_query(tsdb));
		addMethodHandler(new Handler_query_image(tsdb));
		addMethodHandler(new Handler_query_heatmap(tsdb));
		addMethodHandler(new Handler_query_csv(tsdb));
		addMethodHandler(new Handler_timespan(tsdb));
		addMethodHandler(new Handler_heatmap_scale(tsdb));
		addMethodHandler(new Handler_plotstation_list(tsdb));
		addMethodHandler(new Handler_status(tsdb));
	}

	private void addMethodHandler(MethodHandler methodHandler) {
		handlerMap.put("/"+methodHandler.handlerMethodName, methodHandler);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {	
		log.info(WebUtil.requestMarker,WebUtil.getRequestLogString("tsdb", target, baseRequest));

		/*log.info("auth   "+request.getAuthType());
		UserAuthentication userAuthentication = (UserAuthentication) baseRequest.getAuthentication();
		if(userAuthentication!=null&&userAuthentication.isUserInRole(null, "admin")) {
			log.info("is admin");
		}*/




		//response.setHeader("Server", "");
		//response.setHeader("Date", null);

		Handler handler = handlerMap.get(target);
		if(handler!=null) {
			handler.handle(target, baseRequest, request, response);
			return;
		}

		log.info("*********************************** old request handlers: "+target);


		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");




		boolean ret = false;

		switch(target) {
		case "/plots":
			ret = handle_plots(response.getWriter());
			break;
		case "/sensors":
			ret = handle_sensors(response.getWriter());
			break;
		case "/region_plot_list": {
			String region = request.getParameter("region");
			if(region!=null) {
				ret = handle_region_plot_list(response.getWriter(),region);
			}			
			break;
		}
		case "/region_sensor_list": {
			String region = request.getParameter("region");
			if(region!=null) {
				ret = handle_region_sensor_list(response.getWriter(),region);
			} else {
				log.warn("wrong call");
			}
			break;
		}		
		case "/execute_console_command": {
			response.setContentType("application/json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			ret = handle_execute_console_command(reader, response.getWriter());
			break;
		}
		case "/console_comand_get_output": {
			response.setContentType("application/json");
			String commandThreadIdText = request.getParameter("commandThreadId");
			if(commandThreadIdText!=null) {
				Long commandThreadId = null;
				try {
					commandThreadId = Long.parseLong(commandThreadIdText);
				} catch(Exception e) {
					log.warn(e);
				}
				if(commandThreadId!=null) {
					ret = handle_console_comand_get_output(commandThreadId, response.getWriter());
				}
			} else {
				log.warn("wrong call");
			}
			break;
		}
		case "/plot_info": {
			String region = request.getParameter("region");
			response.setContentType("application/json");
			ret = handle_plot_info(response.getWriter(),region);
			break;
		}
		default:
			ret = handle_error(response.getWriter(), baseRequest.getRequestURI());
		}

		if(ret) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private boolean handle_console_comand_get_output(Long commandThreadId, PrintWriter writer) {
		try {
			Pair<Boolean, String[]> pair = tsdb.console_comand_get_output(commandThreadId);
			JSONWriter json_output = new JSONWriter(writer);
			json_output.object();			
			json_output.key("running");
			json_output.value(pair.a);
			json_output.key("output_lines");
			json_output.array();
			for(String line:pair.b) {
				json_output.value(line);
			}
			json_output.endArray();
			json_output.endObject();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}	
	}

	private boolean handle_execute_console_command(BufferedReader reader, PrintWriter writer) {
		try {
			String jsonline = reader.readLine();
			JSONObject json_input = new JSONObject(jsonline);
			String input_line = json_input.getString("input_line");
			System.out.println("input_line: "+input_line);

			long commandThreadId = tsdb.execute_console_command(input_line);			

			JSONWriter json_output = new JSONWriter(writer);
			json_output.object();
			json_output.key("commandThreadId");
			json_output.value(commandThreadId);			
			json_output.endObject();		

			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}		
	}

	private boolean handle_plots(PrintWriter writer) {
		try {
			PlotInfo[] plotInfos = tsdb.getPlots();
			for(PlotInfo plotInfo:plotInfos) {
				writer.println(plotInfo.toString());
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean handle_plot_info(PrintWriter writer, String region) {
		try {
			PlotInfo[] plotInfos = tsdb.getPlots();
			JSONWriter json_output = new JSONWriter(writer);
			json_output.array();
			for(PlotInfo plotInfo:plotInfos) {
				
				if(region!=null && !region.equals(plotInfo.generalStationInfo.region.name)) {
					continue;
				}

				json_output.object();
				json_output.key("name");
				json_output.value(plotInfo.name);
				json_output.key("general");
				json_output.value(plotInfo.generalStationInfo.longName);

				if(Double.isFinite(plotInfo.geoPosLatitude)) {
					json_output.key("lat");
					json_output.value(plotInfo.geoPosLatitude);
				}
				if(Double.isFinite(plotInfo.geoPosLongitude)) {
					json_output.key("lon");
					json_output.value(plotInfo.geoPosLongitude);
				}
				if(Float.isFinite(plotInfo.elevation)) {
					json_output.key("elevation");
					json_output.value(plotInfo.elevation);
				}
				json_output.endObject();
			}
			json_output.endArray();
			return true;
		} catch (Exception e) {
			log.error(e);
			return false;
		}
	}	

	private boolean handle_sensors(PrintWriter writer) {
		try {
			Sensor[] sensors = tsdb.getSensors();
			for(Sensor sensor:sensors) {
				writer.println(sensor.name+"  "+sensor.description+"  "+sensor.unitDescription);
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean handle_error(PrintWriter writer, String target) {
		writer.println("tsdb API error: unknown query: "+target);
		return false;
	}

	private boolean handle_region_plot_list(PrintWriter writer, String regionName) {
		try {
			PlotInfo[] plotInfos = tsdb.getPlots();
			if(plotInfos!=null) {
				String[] webList = Arrays.stream(plotInfos)
						.filter(p->p.generalStationInfo.region.name.equals(regionName))
						.map(p->p.name)
						.toArray(String[]::new);
				//String[] webList = Arrays.stream(generalStationInfos).map(g->g.name+","+g.longName).toArray(String[]::new);
				writeStringArray(writer, webList);
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	private boolean handle_region_sensor_list(PrintWriter writer, String region) {
		try {			
			Set<String> sensorNameSet = new TreeSet<String>();
			for(GeneralStationInfo generalStationInfo:tsdb.getGeneralStations()) {
				System.out.println(generalStationInfo.name);
				if(generalStationInfo.region.name.equals(region)) {
					for(String sensorName:tsdb.getSensorNamesOfGeneralStation(generalStationInfo.name)) {
						sensorNameSet.add(sensorName);
					}
				}
			}
			String[] sensorNames = tsdb.getBaseSchema(sensorNameSet.toArray(new String[0]));			
			if(sensorNames!=null) {
				writeStringArray(writer, sensorNames);
				return true;
			} else {
				log.warn("null");
				System.out.println("null");
				return false;
			}			
		} catch (Exception e) {
			log.warn(e);
			System.out.println(e);
			return false;
		}
	}

	private static void writeStringArray(PrintWriter writer, String[] array) {
		if(array==null) {
			return;
		}
		boolean notFirst = false;
		for(String s:array) {
			if(notFirst) {
				writer.print('\n');
			}
			writer.print(s);
			notFirst = true;
		}
	}
}
