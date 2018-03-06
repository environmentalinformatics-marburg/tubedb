package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import tsdb.TsDBFactory;
import tsdb.remote.RemoteTsDB;
import tsdb.web.util.Web;

/**
 * Central web API class, that dispatches requests to method handlers.
 * @author woellauer
 *
 */
public class IotAPIHandler extends AbstractHandler {

	private static final Logger log = LogManager.getLogger();

	private final RemoteTsDB tsdb;

	private HashMap<String,Handler> handlerMap;

	public IotAPIHandler(RemoteTsDB tsdb) {
		this.tsdb=tsdb;
		handlerMap = new HashMap<String,Handler>();
		addMethodHandler(new Handler_iot_sensor(tsdb));
		addMethodHandler(new Handler_iot_clear(tsdb));
	}

	private void addMethodHandler(MethodHandler methodHandler) {
		handlerMap.put(methodHandler.handlerMethodName, methodHandler);
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {	
		log.info(Web.requestMarker,Web.getRequestLogString("IoT", target, baseRequest));
		baseRequest.setHandled(true);
		if(!TsDBFactory.IOT_API_KEY.isEmpty()) {
			String iot_api_key = baseRequest.getHeader("x-api-key");
			if(iot_api_key == null) {
				response.setContentType("text/plain;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.warn("IoT API error: missing IoT API key (HTTP header with 'x-api-key')");
				response.getWriter().println("IoT API error: missing IoT API key (HTTP header with 'x-api-key')");				
				return;
			}
			if(!TsDBFactory.IOT_API_KEY.equals(iot_api_key)) {
				response.setContentType("text/plain;charset=utf-8");
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				log.warn("IoT API error: API key does not match (HTTP header with 'x-api-key')");
				response.getWriter().println("IoT API error: API key does not match (HTTP header with 'x-api-key')");				
				return;
			}
		}
		String method = target.substring(1);
		int i = method.indexOf("/");
		String subTarget = "";
		if(i>=0) {
			subTarget = method.substring(i + 1);
			method = method.substring(0, i);
		}
		log.info("method: " + method);
		log.info("subTarget: " + subTarget);
		Handler handler = handlerMap.get(method);
		if(handler!=null) {
			handler.handle(subTarget, baseRequest, request, response);
			return;
		}
		response.setContentType("text/plain;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		handle_error(response.getWriter(), baseRequest.getRequestURI());

	}

	private boolean handle_error(PrintWriter writer, String target) {
		writer.println("IoT API error: unknown request: "+target);
		return false;
	}	
}
