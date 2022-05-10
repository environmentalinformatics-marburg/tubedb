package tsdb.web.api;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jetty.server.Request;
import org.json.JSONWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tsdb.remote.RemoteTsDB;
import tsdb.util.DataEntry;
import tsdb.util.Measurement;
import tsdb.util.TimeUtil;

public class Handler_monitoring extends MethodHandler {	

	public Handler_monitoring(RemoteTsDB tsdb) {
		super(tsdb, "monitoring");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("application/json;charset=utf-8");	

		String[] plotIDs = request.getParameterValues("plot");
		String[] sensorNames = request.getParameterValues("sensor");

		ArrayList<Measurement> result = tsdb.getMonitoring(plotIDs, sensorNames);

		JSONWriter json = new JSONWriter(response.getWriter());
		json.object();
		json.key("sensors");
		json.array();
		for(String sensorName:sensorNames) {
			json.value(sensorName);
		}
		json.endArray();
		json.key("measurements");
		json.array();
		for(Measurement r:result) {
			json.object();
			json.key("plot");
			json.value(r.name);
			json.key("timestamp");
			json.array();
			for(DataEntry e:r.values) {
				json.value(e.timestamp);
			}
			json.endArray();
			json.key("datetime");
			json.array();
			for(DataEntry e:r.values) {
				json.value(TimeUtil.toDateSpaceTime(e.timestamp));
			}
			json.endArray();
			json.key("value");
			json.array();
			for(DataEntry e:r.values) {
				json.value(e.value);
			}
			json.endArray();
			json.endObject();
		}
		json.endArray();
		json.endObject();
	}
}
