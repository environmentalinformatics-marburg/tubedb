package tsdb.web.api;

import java.io.IOException;

import org.eclipse.jetty.server.Request;
import org.tinylog.Logger;

import com.opencsv.CSVWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tsdb.remote.RemoteTsDB;
import tsdb.util.TimeUtil;
import tsdb.util.iterator.TimestampSeriesCSVwriter;
import tsdb.web.util.Web;

public class Handler_db_cache_content extends MethodHandler {	


	public Handler_db_cache_content(RemoteTsDB tsdb) {
		super(tsdb, "db_cache_content.csv");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");

		if(!Web.isAllowed(baseRequest, Web.ROLE_ADMIN)) {
			Logger.warn("no admin access");
			response.getWriter().write("no admin access");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		CSVWriter writer = new CSVWriter(response.getWriter(), ',', '\u0000', '\u0000', TimestampSeriesCSVwriter.LINE_SEPARATOR);

		String[] headerLine = new String[]{"stream", "start", "end", "sensors"};
		writer.writeNext(headerLine);	
		
		String[] streamNames = tsdb.cacheStorageGetStreamNames();
		for(String streamName : streamNames) {
			int[] range = tsdb.cacheStorageGetSensorTimeRange(streamName);
			String[] streamNameSensorNames = tsdb.cacheStorageGetSensorNames(streamName);
			String[] line = new String[]{streamName, TimeUtil.oleMinutesToText(range[0]), TimeUtil.oleMinutesToText(range[1]), String.join(" ", streamNameSensorNames)};
			writer.writeNext(line);
		}

     	writer.close();
	}
}
