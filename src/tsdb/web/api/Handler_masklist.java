package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.jetty.server.Request;
import org.json.JSONWriter;
import org.tinylog.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tsdb.remote.MaskListEntry;
import tsdb.remote.RemoteTsDB;
import tsdb.run.command.LoadMasks.MaskType;

public class Handler_masklist extends MethodHandler {

	public Handler_masklist(RemoteTsDB tsdb) {
		super(tsdb, "masklist");
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

			PrintWriter writer = response.getWriter();
			JSONWriter json = new JSONWriter(writer);


			json.object(); // Start Object wrapper

			json.key("station");
			json.value(stationName);

			json.key("sensor");
			json.value(sensorName);

			json.key("mask");

			json.array();
			
			for(MaskListEntry e : tsdb.getTimeSeriesMaskList(stationName, sensorName, MaskType.INVALID)) {
				e.writeJSON(json);
			}

			json.endArray();

			json.key("suspect_mask");

			json.array();
			
			for(MaskListEntry e : tsdb.getTimeSeriesMaskList(stationName, sensorName, MaskType.SUSPECT)) {
				e.writeJSON(json);
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