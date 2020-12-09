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
import org.json.JSONString;
import org.json.JSONWriter;

import tsdb.remote.PlotStatus;
import tsdb.remote.RemoteTsDB;
import tsdb.util.TimeUtil;
import tsdb.web.util.Web;

/**
 * Get status information of plots.
 * <p>
 * parameters: (optional one of) region or generalstation
 * <p>
 * returns: list of plots as JSON array with keys:
 * <br>
 * plot, first_timestamp, last_timestamp, first_datetime, last_datetime, voltage, message_date, message
 * @author woellauer
 *
 */
public class Handler_status extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();
	
	private static class JSONFloat implements JSONString {		
		public final float value;		
		public JSONFloat(float value) {
			this.value = value;
		}
		@Override
		public String toJSONString() {
			return Float.toString(value);
		}		
	}

	public Handler_status(RemoteTsDB tsdb) {
		super(tsdb, "status");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");
		String generalstationName = request.getParameter("generalstation");
		String regionName = request.getParameter("region");
		if((generalstationName!=null&&regionName!=null)) {
			log.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		if(regionName!=null && !Web.isAllowed(baseRequest, regionName)) {
			log.warn("no access to region "+regionName);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		try {
			ArrayList<PlotStatus> statusList = null;
			if(generalstationName==null&&regionName==null) {
				statusList = tsdb.getPlotStatuses();
			} else if(generalstationName!=null&&regionName==null){
				statusList = tsdb.getPlotStatusesOfGeneralStation(generalstationName);
			} else if(generalstationName==null&&regionName!=null){
				statusList = tsdb.getPlotStatusesOfRegion(regionName);				
			}
			if(statusList==null) {
				log.error("tsl null");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}
			PrintWriter writer = response.getWriter();
			JSONWriter json_output = new JSONWriter(writer);
			json_output.array();
			for(PlotStatus status:statusList) {
				json_output.object();
				json_output.key("plot");
				json_output.value(status.plotID);
				json_output.key("first_timestamp");
				json_output.value(status.firstTimestamp);
				json_output.key("last_timestamp");
				json_output.value(status.lastTimestamp);
				json_output.key("first_datetime");
				json_output.value(TimeUtil.oleMinutesToText(status.firstTimestamp));
				json_output.key("last_datetime");
				json_output.value(TimeUtil.oleMinutesToText(status.lastTimestamp));
				if(Float.isFinite(status.voltage)) {
					json_output.key("voltage");
					json_output.value(new JSONFloat(status.voltage));	
					json_output.key("voltage_min_watch");
					json_output.value(new JSONFloat(status.voltage_min_watch));
					json_output.key("voltage_min_good");
					json_output.value(new JSONFloat(status.voltage_min_good));
					json_output.key("voltage_min_error");
					json_output.value(new JSONFloat(status.voltage_min_error));
				}
				if(status.plotMessage!=null) {
					try {
						json_output.key("message_date");
						json_output.value(status.plotMessage.dateTime.toString());
						json_output.key("message");
						json_output.value(status.plotMessage.message);			
					}catch(Exception e) {
						log.error(e);
					}
				}
				json_output.endObject();
			}
			json_output.endArray();
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			log.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}


}
