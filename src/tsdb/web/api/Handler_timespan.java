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
import org.json.JSONWriter;

import tsdb.remote.RemoteTsDB;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;

public class Handler_timespan extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_timespan(RemoteTsDB tsdb) {
		super(tsdb, "timespan");
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
		try {
			ArrayList<TimestampInterval<String>> tsl = null;
			if(generalstationName==null&&regionName==null) {
				tsl = tsdb.getPlotTimeSpans();
			} else if(generalstationName!=null&&regionName==null){
				tsl = tsdb.getPlotTimeSpansOfGeneralStation(generalstationName);
			} else if(generalstationName==null&&regionName!=null){
				tsl = tsdb.getPlotTimeSpansOfRegion(regionName);				
			}
			if(tsl==null) {
				log.error("tsl null");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}
			tsl.sort(TimestampInterval.END_COMPARATOR);
			PrintWriter writer = response.getWriter();
			JSONWriter json_output = new JSONWriter(writer);
			json_output.array();
			for(TimestampInterval<String> i:tsl) {
				json_output.object();
				json_output.key("plot");
				json_output.value(i.value);
				json_output.key("first_timestamp");
				json_output.value(i.start);
				json_output.key("last_timestamp");
				json_output.value(i.end);
				json_output.key("first_datetime");
				json_output.value(TimeUtil.oleMinutesToText(i.start));
				json_output.key("last_datetime");
				json_output.value(TimeUtil.oleMinutesToText(i.end));				
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
