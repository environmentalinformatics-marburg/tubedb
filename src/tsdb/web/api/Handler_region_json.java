package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.json.JSONException;
import org.json.JSONString;
import org.json.JSONWriter;

import tsdb.component.Region;
import tsdb.remote.PlotStatus;
import tsdb.remote.RemoteTsDB;
import tsdb.util.TimeUtil;
import tsdb.web.util.Web;

/**
 * get meta data of region 
 * @author woellauer
 *
 */
public class Handler_region_json extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_region_json(RemoteTsDB tsdb) {
		super(tsdb, "region.json");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("application/json;charset=utf-8");
		String regionName = request.getParameter("region");
		UserIdentity userIdentity = Web.getUserIdentity(baseRequest);
		if(regionName==null) {
			JSONWriter json_output = new JSONWriter(response.getWriter());
			json_output.array();
			for(Region region:tsdb.getRegions()) {
				if(Web.isAllowed(userIdentity, region.name)) {
					writeRegion(json_output, region);	
				}
			}
			json_output.endArray();
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			if(Web.isAllowed(userIdentity, regionName)) {
				Region region = tsdb.getRegionByName(regionName);
				if(region==null) {
					log.warn("region not found");
					response.getWriter().write("region not found");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				} else {
					JSONWriter json_output = new JSONWriter(response.getWriter());
					writeRegion(json_output, region);
					response.setStatus(HttpServletResponse.SC_OK);
				}
			} else {
				log.warn("no access to region "+regionName);
				response.getWriter().write("no access to region "+regionName);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}		
	}

	private void writeRegion(JSONWriter json_output, Region region) throws JSONException, IOException {
		json_output.object();
		json_output.key("id");
		json_output.value(region.name);
		json_output.key("name");
		json_output.value(region.longName);
		json_output.key("view_year_range");
		json_output.object();
		json_output.key("start");
		json_output.value(String.valueOf(TimeUtil.fastDateWriteYears(TimeUtil.oleMinutesToLocalDateTime(region.viewTimeRange.start).toLocalDate())));
		json_output.key("end");
		json_output.value(String.valueOf(TimeUtil.fastDateWriteYears(TimeUtil.oleMinutesToLocalDateTime(region.viewTimeRange.end).toLocalDate())));
		json_output.endObject();
		json_output.endObject();
	}
}
