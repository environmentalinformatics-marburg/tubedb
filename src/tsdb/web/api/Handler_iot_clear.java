package tsdb.web.api;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.remote.RemoteTsDB;

/**
 * get meta data of region 
 * @author woellauer
 *
 */
public class Handler_iot_clear extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();
	
	public Handler_iot_clear(RemoteTsDB tsdb) {
		super(tsdb, "clear");
	}

	@Override
	public void handle(String target, Request request, HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {		
		request.setHandled(true);
		log.info("start clear");
		tsdb.clearData();
		response.setContentType("text/plain;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		log.info("removed all time series data");	
		response.getWriter().println("removed all time series data");			
	}
}
