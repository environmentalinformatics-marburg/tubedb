package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

public class Handler_query extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_query(RemoteTsDB tsdb) {
		super(tsdb, "query");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");

		String plot = request.getParameter("plot");
		if(plot==null) {
			log.warn("wrong call no plot");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String sensorName = request.getParameter("sensor");

		if(sensorName==null) {
			log.warn("wrong call no sensor");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		String aggregation = request.getParameter("aggregation");
		AggregationInterval agg = AggregationInterval.HOUR;
		if(aggregation!=null) {
			try {
				agg = AggregationInterval.parse(aggregation);
				if(agg==null) {
					agg = AggregationInterval.HOUR;
				}
			} catch (Exception e) {
				log.warn(e);
			}
		}

		String quality = request.getParameter("quality");
		DataQuality dataQuality = DataQuality.STEP;
		if(quality!=null) {
			try {
				dataQuality = DataQuality.parse(quality);
				if(dataQuality==null) {
					dataQuality = DataQuality.STEP;
				}
			} catch (Exception e) {
				log.warn(e);
			}
		}

		String interpolated = request.getParameter("interpolated");
		boolean isInterpolated = false;
		if(interpolated!=null) {
			switch(interpolated) {
			case "true":
				isInterpolated = true;
				break;
			case "false":
				isInterpolated = false;
				break;
			default:
				log.warn("unknown input");
				isInterpolated = false;				
			}
		}
		
		String timeYear = request.getParameter("year");
		Long startTime = null;
		Long endTime = null;
		if(timeYear!=null) {
			try {
				int year = Integer.parseInt(timeYear);
				if(year<Handler_query_image.MIN_YEAR||year>Handler_query_image.MAX_YEAR) {
					log.error("year out of range "+year);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
				String timeMonth = request.getParameter("month");
				if(timeMonth==null) {
					startTime = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 1, 1, 0, 0));
					endTime = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 12, 31, 23, 0));
				} else {
					try {
						int month = Integer.parseInt(timeMonth);
						if(month<1||month>12) {
							log.error("month out of range "+month);
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							return;
						}
						LocalDateTime dateMonth = LocalDateTime.of(year, month, 1, 0, 0);
						startTime = TimeUtil.dateTimeToOleMinutes(dateMonth);
						endTime = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, dateMonth.toLocalDate().lengthOfMonth(), 23, 0));
					} catch (Exception e) {
						log.error(e);
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						return;
					}	
				}				
			} catch (Exception e) {
				log.error(e);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}		

		try {
			String[] sensorNames;
			if(sensorName.equals("WD")) {
				sensorNames = new String[]{sensorName,"WV"};
			} else {
				sensorNames = new String[]{sensorName};
			}
			String[] validSchema =  tsdb.getValidSchema(plot, sensorNames);
			if(sensorNames.length!=validSchema.length) {
				log.info("sensorName not in plot: "+plot+"  "+sensorName);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);				
				return;
			}
			TimestampSeries ts = tsdb.plot(null, plot, sensorNames, agg, dataQuality, isInterpolated, startTime, endTime);
			if(ts==null) {
				log.error("TimestampSeries null: "+plot);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}
			
			PrintWriter writer = response.getWriter();
			TsIterator it = ts.tsIterator();
			while(it.hasNext()) {
				TsEntry e = it.next(); 
				writer.print(TimeUtil.oleMinutesToText(e.timestamp)+";");
				float value = e.data[0];
				if(Float.isNaN(value)) {
					writer.print("NA");
				} else {
					writer.format(Locale.ENGLISH,"%.2f", value);
				}						
				if(it.hasNext()) {
					writer.print('\n');
				}
			}
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			log.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
