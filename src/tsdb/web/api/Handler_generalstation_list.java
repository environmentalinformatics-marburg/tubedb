package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.remote.GeneralStationInfo;
import tsdb.remote.RemoteTsDB;

public class Handler_generalstation_list extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_generalstation_list(RemoteTsDB tsdb) {
		super(tsdb, "generalstation_list");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");
		String regionName = request.getParameter("region");
		if(regionName==null) {
			log.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		try {
			GeneralStationInfo[] generalStationInfos = tsdb.getGeneralStationsOfRegion(regionName);
			if(generalStationInfos==null) {
				log.error("generalStationInfos null: "+regionName);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			String[] webList = Arrays.stream(generalStationInfos).map(g->g.name+";"+g.longName).toArray(String[]::new);
			PrintWriter writer = response.getWriter();
			writeStringArray(writer, webList);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			log.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
