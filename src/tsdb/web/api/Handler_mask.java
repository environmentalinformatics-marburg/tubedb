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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.json.JSONArray;
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
import tsdb.util.Interval;
import tsdb.util.TimeSeriesMask;
import tsdb.util.TimeUtil;
import tsdb.util.yaml.YamlMap;
import tsdb.util.yaml.YamlTimestampSafeConstructor;
import tsdb.web.util.Web;

public class Handler_mask extends MethodHandler {

	public Handler_mask(RemoteTsDB tsdb) {
		super(tsdb, "mask");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String httpMethod = baseRequest.getMethod();
		switch(httpMethod) {
		case "GET":
			handleGET(target, baseRequest, request, response);
			break;

		default:
			throw new RuntimeException("unknown HTTP method " + httpMethod);
		}
	}

	public synchronized void handleGET(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("application/json;charset=utf-8");

		String stationName = request.getParameter("station");
		String sensorName = request.getParameter("sensor");

		if(stationName == null || stationName.isBlank() || sensorName == null || sensorName.isBlank()) {
			Logger.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		try {
			TimeSeriesMask mask = tsdb.getTimeSeriesMask(stationName, sensorName);
			TimeSeriesMask suspect_mask = tsdb.getTimeSeriesSuspectMask(stationName, sensorName);

			PrintWriter writer = response.getWriter();
			JSONWriter json = new JSONWriter(writer);


			json.object(); // Start Object wrapper
			
			json.key("station");
			json.value(stationName);
			
			json.key("sensor");
			json.value(sensorName);

			json.key("mask");

			json.array();
			if(mask != null) {
				for(Interval i : mask.getIntervals()) {
					json.array();
					json.value(i.start);
					json.value(i.end);
					json.endArray();
				}
			}
			json.endArray();
			
			json.key("suspect_mask");

			json.array();
			if(suspect_mask != null) {
				for(Interval i : suspect_mask.getIntervals()) {
					json.array();
					json.value(i.start);
					json.value(i.end);
					json.endArray();
				}
			}
			json.endArray();

			json.endObject(); // end wrapper object

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			Logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}