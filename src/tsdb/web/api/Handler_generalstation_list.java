package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.remote.GeneralStationInfo;
import tsdb.remote.RemoteTsDB;
import tsdb.web.util.Web;

/**
 * Get general stations.
 * <p>
 * parameter: region
 * <p>
 * returns: list of general stations with name and long name.
 * @author woellauer
 *
 */
public class Handler_generalstation_list extends MethodHandler {	
	

	public Handler_generalstation_list(RemoteTsDB tsdb) {
		super(tsdb, "generalstation_list");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");
		String regionName = request.getParameter("region");
		if(regionName==null) {
			Logger.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		if(Web.isAllowed(baseRequest, regionName)) {
		try {
			GeneralStationInfo[] generalStationInfos = tsdb.getGeneralStationsOfRegion(regionName);
			if(generalStationInfos==null) {
				Logger.error("generalStationInfos null: "+regionName);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			String[] webList = Arrays.stream(generalStationInfos).map(g->g.name+";"+g.longName).toArray(String[]::new);
			PrintWriter writer = response.getWriter();
			writeStringArray(writer, webList);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			Logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		} else {
			Logger.warn("no access to region "+regionName);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
	}
}
