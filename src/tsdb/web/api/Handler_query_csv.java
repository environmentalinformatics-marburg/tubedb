package tsdb.web.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.iterator.CSV;
import tsdb.util.iterator.CSVTimeType;
import tsdb.util.iterator.TimestampSeries;

public class Handler_query_csv extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_query_csv(RemoteTsDB tsdb) {
		super(tsdb, "query_csv");
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
		//String sensorName = request.getParameter("sensor");
		String[] sensorNames = request.getParameterValues("sensor");

		if(sensorNames==null) {
			log.warn("wrong call no sensor");
			response.getWriter().println("wrong call no sensor");
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
			sensorNames = tsdb.supplementSchema(sensorNames);
			/*if(Util.containsString(sensorNames, "WD") && !Util.containsString(sensorNames, "WV")) {
				sensorNames = Util.concat(sensorNames, "WV");
			}
			
			if(Util.containsString(sensorNames, "sunshine") && !Util.containsString(sensorNames, "Rn_300")) {
				sensorNames = Util.concat(sensorNames, "Rn_300");
			}*/
			
			String[] validSchema =  tsdb.getValidSchemaWithVirtualSensors(plot, sensorNames);
			if(sensorNames.length!=validSchema.length) {
				String error = "some sensors not in plot: "+plot+"  "+Arrays.toString(sensorNames);
				log.info(error);
				response.getWriter().println(error);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);				
				return;
			}
			TimestampSeries ts = tsdb.plot(null, plot, sensorNames, agg, dataQuality, isInterpolated, startTime, endTime);
			if(ts==null) {
				String error = "TimestampSeries null: "+plot;
				log.info(error);
				response.getWriter().println(error);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}
			
			ServletOutputStream out = response.getOutputStream();
			
			CSV.write(ts.tsIterator(), true, out, ",", "NA", CSVTimeType.DATETIME, false, false);

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			response.getWriter().println(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
