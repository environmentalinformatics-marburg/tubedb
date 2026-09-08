package tsdb.web.api;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONWriter;
import org.tinylog.Logger;

import com.opencsv.CSVWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tsdb.remote.RemoteTsDB;
import tsdb.run.command.LoadMasks.MaskType;
import tsdb.util.Interval;
import tsdb.util.TimeSeriesMask;
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
		case "POST":
			handlePOST(target, baseRequest, request, response);
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

	public synchronized void handlePOST(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);

		Logger.info("handlePOST ");

		response.setContentType("application/json;charset=utf-8");
		JSONObject jsonReq = new JSONObject(new JSONTokener(request.getReader()));
		String action = jsonReq.getString("action");
		Logger.info(action);
		switch(action) {
		case "add": {
			JSONObject content = jsonReq.getJSONObject("content");
			handleActionAdd(content, baseRequest, response);
			break;
		}
		default:
			Logger.error("unknown action");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	public synchronized void handleActionAdd(JSONObject json, Request request, HttpServletResponse response) throws IOException, ServletException {

		String userName = "anonymous";
		UserIdentity identity = Web.getUserIdentity(request);
		if(identity != null) {
			String user = identity.getUserPrincipal().getName();
			if(user != null && !user.isBlank()) {
				userName = user;
			}
		}
		
		String typeText = json.optString("type");
		MaskType maskType = MaskType.fromText(typeText);

		String station = json.optString("station");
		if(station == null || station.isBlank()) {
			throw new RuntimeException("missing station");
		}
		station = station.strip();
		Logger.info(station);

		String sensor = json.optString("sensor");
		if(sensor == null || sensor.isBlank()) {
			throw new RuntimeException("missing sensor");
		}
		sensor = sensor.strip();

		String start = json.optString("start");
		if(start == null || start.isBlank()) {
			throw new RuntimeException("missing start");
		}
		start = start.strip();

		String end = json.optString("end");
		if(end == null || end.isBlank()) {
			throw new RuntimeException("missing end");
		}
		end = end.strip();

		String comment = json.optString("comment");
		if(comment == null) {
			comment = "";
		}
		comment = comment.strip();

		try {
			tsdb.addTimeSeriesMaskInterval(station, sensor, start, end, userName, String.valueOf(System.currentTimeMillis()), comment, maskType);

			response.setStatus(HttpServletResponse.SC_OK);
			response.getWriter().write("{\"status\":\"success\"}");

		} catch (Exception e) {
			Logger.error(e, "Failed to write mask entry to CSV");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
		}
	}
}