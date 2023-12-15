package tsdb.web.api; 

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.tinylog.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tsdb.TsDBFactory;
import tsdb.remote.PlotStatus;
import tsdb.remote.RemoteTsDB;
import tsdb.util.TimeUtil;
import tsdb.util.yaml.YamlMap;
import tsdb.util.yaml.YamlTimestampSafeConstructor;
import tsdb.web.util.Web;

/**
 * Get status information of plots.
 * <p>
 * parameters: (optional one of) region or generalstation
 * <p>
 * returns: list of plots as JSON array with keys:
 * <br>
 * plot, first_timestamp, last_timestamp, first_datetime, last_datetime, voltage, message_date, message
 * @author woellauer
 *
 */
public class Handler_status extends MethodHandler {

	private final String yamlFile;

	private static class JSONFloat implements JSONString {		
		public final float value;		
		public JSONFloat(float value) {
			this.value = value;
		}
		@Override
		public String toJSONString() {
			return Float.toString(value);
		}		
	}

	public Handler_status(RemoteTsDB tsdb) {
		super(tsdb, "status");
		//yamlFile =TsDBFactory.WEBFILES_PATH + "/supplement/" + "testingyamlfile.yaml";
		yamlFile =TsDBFactory.WEBFILES_PATH + "/testingyamlfile.yaml";
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String httpMethod = baseRequest.getMethod();
		switch(httpMethod) {
		case "GET":
			handleGET(target, baseRequest, request, response);
			break;
		case "POST":
			handlePOST(target, baseRequest, request, response);
			break;
		default:
			throw new RuntimeException("unknown HTTP method " + httpMethod);
		}
	}

	public synchronized void handleGET(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");
		String plotName = request.getParameter("plot");
		String generalstationName = request.getParameter("generalstation");
		String regionName = request.getParameter("region");
		boolean withPlotMessage = request.getParameter("plot_message") != null;
		boolean withPlotStatus = request.getParameter("plot_status") != null;
		boolean withHistory = request.getParameter("history") != null;


		if(
				(generalstationName != null && regionName != null) ||
				(generalstationName != null && plotName != null) ||
				(regionName != null && plotName != null)
				) {
			Logger.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		if(regionName != null && !Web.isAllowed(baseRequest, regionName)) {
			Logger.warn("no access to region "+regionName);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		try {
			ArrayList<PlotStatus> statusList = null;
			if(plotName == null && generalstationName == null && regionName == null) {
				statusList = tsdb.getPlotStatuses(withPlotMessage);
			} else if(plotName != null && generalstationName == null && regionName == null){
				statusList = tsdb.getPlotStatus(plotName, withPlotMessage);				
			} else if(plotName == null && generalstationName != null && regionName == null){
				statusList = tsdb.getPlotStatusesOfGeneralStation(generalstationName, withPlotMessage);
			} else if(plotName == null && generalstationName == null && regionName != null){
				statusList = tsdb.getPlotStatusesOfRegion(regionName, withPlotMessage);				
			}
			if(statusList==null) {
				Logger.error("tsl null");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}


			HashMap<String, YamlMap> statusMap = withPlotStatus ? readEntries() : null;

			PrintWriter writer = response.getWriter();
			JSONWriter json_output = new JSONWriter(writer);
			json_output.array();
			long now = TimeUtil.dateTimeToOleMinutes(LocalDateTime.now());
			for(PlotStatus status:statusList) {
				json_output.object();
				json_output.key("plot");
				json_output.value(status.plotID);
				json_output.key("first_timestamp");
				json_output.value(status.firstTimestamp);
				json_output.key("last_timestamp");
				json_output.value(status.lastTimestamp);
				json_output.key("first_datetime");
				json_output.value(TimeUtil.oleMinutesToText(status.firstTimestamp));
				json_output.key("last_datetime");
				json_output.value(TimeUtil.oleMinutesToText(status.lastTimestamp));
				json_output.key("elapsed_days");
				json_output.value((now - status.lastTimestamp) / 1440);
				if(Float.isFinite(status.voltage)) {
					json_output.key("voltage");
					json_output.value(new JSONFloat(status.voltage));	
					json_output.key("voltage_min_watch");
					json_output.value(new JSONFloat(status.voltage_min_watch));
					json_output.key("voltage_min_good");
					json_output.value(new JSONFloat(status.voltage_min_good));
					json_output.key("voltage_min_error");
					json_output.value(new JSONFloat(status.voltage_min_error));
				}
				if(withPlotMessage && status.plotMessage != null) {
					try {
						json_output.key("message_date");
						json_output.value(status.plotMessage.dateTime.toString());
						json_output.key("message");
						json_output.value(status.plotMessage.message);			
					}catch(Exception e) {
						Logger.error(e);
					}
				}
				if(withPlotStatus && statusMap != null) {
					YamlMap inf = statusMap.get(status.plotID);					
					if(inf != null) {
						write(inf, json_output);
					}	
				}
				if(withHistory) {
					json_output.key("history");
					json_output.array();
					forEachEntriesOfPlot(status.plotID, yamlMap -> {
						json_output.object();
						write(yamlMap, json_output);
						json_output.endObject();
					});
					json_output.endArray();
				}
				json_output.endObject();
			}
			json_output.endArray();
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			Logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private static void write(YamlMap inf, JSONWriter json_output) {
		for(String key : inf.keys()) {
			if(!DYNAMIC_PROPERTIES.contains(key)) {
				Object value = inf.getObject(key);
				json_output.key(key);
				json_output.value(value);
			}
		}
	}

	private final static Set<String> SYSTEM_PROPERTIES = Set.of(
			"plot",
			"first_timestamp",
			"last_timestamp",
			"first_datetime",
			"last_datetime",
			"first_time",
			"last_time",
			"first_date",
			"last_date",
			"elapsed_days",
			"voltage",
			"voltage_min_watch",
			"voltage_min_error",
			"voltage_min_good",
			"datetime",
			"author",
			"status",
			"tasks",
			"notes",
			"history"
			);

	private final static Set<String> DYNAMIC_PROPERTIES = Set.of(
			"plot",
			"first_timestamp",
			"last_timestamp",
			"first_datetime",
			"last_datetime",
			"first_time",
			"last_time",
			"first_date",
			"last_date",
			"elapsed_days",
			"voltage",
			"voltage_min_watch",
			"voltage_min_error",
			"voltage_min_good",
			"history"
			);

	private static void optPut(String key, JSONObject jsonReq, LinkedHashMap<String,Object> map) {
		Object obj = jsonReq.opt(key);
		if(obj != null) {
			String value = obj.toString();
			if(!value.isBlank()) {
				map.put(key, value);
			}
		}
	}

	public synchronized void handlePOST(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonReq = new JSONObject(new JSONTokener(request.getReader()));
		String plot = jsonReq.getString("plot");
		Logger.info(plot);

		DumperOptions options = new DumperOptions();
		options.setExplicitStart(true);
		//options.setExplicitEnd(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		Yaml yaml = new Yaml(options);

		String userName = "anonymous";
		UserIdentity identity = Web.getUserIdentity(baseRequest);
		if(identity != null) {
			String user = identity.getUserPrincipal().getName();
			if(user != null && !user.isBlank()) {
				userName = user;
			}
		}

		LinkedHashMap<String,Object> map = new LinkedHashMap<String,Object>();
		map.put("plot", plot);
		optPut("status", jsonReq, map);
		optPut("tasks", jsonReq, map);
		optPut("notes", jsonReq, map);

		for(String key : jsonReq.keySet()) {
			if(!SYSTEM_PROPERTIES.contains(key)) {				
				optPut(key, jsonReq, map);				
			}
		}

		map.put("author", userName);
		map.put("datetime", TimeUtil.oleMinutesToText(TimeUtil.dateTimeToOleMinutes(LocalDateTime.now())));		

		File file = new File(yamlFile);
		try(FileOutputStream out = new FileOutputStream(file, true)) {
			try(OutputStreamWriter writer = new OutputStreamWriter(out)) {
				//writer.append('\n');
				yaml.dump(map, writer);
				writer.flush();
			}
		}		
	}

	public synchronized HashMap<String, YamlMap> readEntries() throws FileNotFoundException, IOException {
		HashMap<String, YamlMap> map = new HashMap<String, YamlMap>();

		File file = new File(yamlFile);
		if(!file.exists()) {
			file.createNewFile();
		}
		Yaml yaml = new Yaml(new YamlTimestampSafeConstructor());
		try(InputStream in = new FileInputStream(file)) {
			Iterable<Object> it = yaml.loadAll(in);
			for(Object yamlObject : it) {
				//Logger.info(yamlObject);
				YamlMap yamlMap = YamlMap.ofObject(yamlObject);
				String plotID = yamlMap.getString("plot");
				//Logger.info(plotID);
				map.put(plotID, yamlMap);
			}
		}
		return map;
	}

	public synchronized void forEachEntriesOfPlot(String plot, Consumer<YamlMap> consumer) throws FileNotFoundException, IOException {
		File file = new File(yamlFile);
		Yaml yaml = new Yaml(new YamlTimestampSafeConstructor());
		try(InputStream in = new FileInputStream(file)) {
			Iterable<Object> it = yaml.loadAll(in);
			for(Object yamlObject : it) {
				YamlMap yamlMap = YamlMap.ofObject(yamlObject);
				String plotID = yamlMap.getString("plot");
				if(plot.equals(plotID)) {
					consumer.accept(yamlMap);
				}
			}
		}
	}
}
