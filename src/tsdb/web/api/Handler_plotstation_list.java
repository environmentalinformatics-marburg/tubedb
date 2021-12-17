package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.remote.RemoteTsDB;

/**
 * Get stations of virtual plot.
 * <p>
 * parameter: plot
 * <p>
 * returns: list of station names and logger types
 * @author woellauer
 *
 */
public class Handler_plotstation_list extends MethodHandler {
	

	public Handler_plotstation_list(RemoteTsDB tsdb) {
		super(tsdb, "plotstation_list");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");
		String plotID = request.getParameter("plot");
		if(plotID==null) {
			Logger.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try {
			String[] webList = tsdb.getPlotStations(plotID);
			if(webList==null) {
				webList = new String[0];
			}

			for(int i=0;i<webList.length;i++) {
				String loggerTypeName = tsdb.getStationLoggerTypeName(webList[i]);
				if(loggerTypeName!=null) {
					webList[i] += ";"+loggerTypeName;
				} else {
					webList[i] += ";"+"unknown";
				}
			}


			PrintWriter writer = response.getWriter();
			writeStringArray(writer, webList);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			Logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
