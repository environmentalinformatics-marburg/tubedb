package tsdb.web.api;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.json.JSONWriter;

import tsdb.component.Sensor;
import tsdb.remote.RemoteTsDB;

public class Handler_model extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_model(RemoteTsDB tsdb) {
		super(tsdb, "model");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("application/json;charset=utf-8");
		JSONWriter json = new JSONWriter(response.getWriter());
		writeModel(json);		
	}

	private void writeModel(JSONWriter json) throws RemoteException {
		json.object();
		json.key("model");
		json.object();
		json.key("sensors");
		json.object();
		for(Sensor sensor : tsdb.getSensors()) {
			json.key(sensor.name);
			json.object();
			json.key("desc");
			json.value(sensor.description);
			json.endObject();
		}
		json.endObject();  // end sensors
		json.endObject(); // end model
		json.endObject(); // end
		
	}

	
}
