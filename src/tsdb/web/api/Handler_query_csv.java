package tsdb.web.api;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.iterator.ProjectionFillIterator;
import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.iterator.CSV;
import tsdb.util.iterator.CSVTimeType;
import tsdb.util.iterator.TimestampSeries;

/**
 * Get timeseries data as CSV-file.
 * <p>
 * parameters:
 * <br>
 * plot
 * <br>
 * (one or more times) sensor
 * <br>
 * (optional defaults to hour) aggregation
 * <br>
 * (optional defaults to step) quality
 * <br>
 * (optional defaults to false) interpolated
 * <br>
 * (optional defaults to all years) year
 * <br>
 * (optional defaults to full year) month 
 * <p>
 * returns: 
 * <br>
 * first row with header "datetime","sensorname1","sensorname2",...
 * <br>
 * following rows with data: e.g. 2012-01-01T00:00,10.2,85.4
 * @author woellauer
 *
 */
public class Handler_query_csv extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_query_csv(RemoteTsDB tsdb) {
		super(tsdb, "query_csv");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");

		if(request.getParameter("plot") == null) {
			log.warn("wrong call no plot parameter");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String[] plots = request.getParameterValues("plot");
		
		String col_plot_text = request.getParameter("col_plot");
		boolean col_plot = false;
		if(col_plot_text != null) {
			switch(col_plot_text) {
			case "true":
				col_plot = true;
				break;
			case "false":
				col_plot = false;
				break;
			default:
				log.warn("unknown input");
				col_plot = false;				
			}
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
						String timeDay = request.getParameter("day");
						if(timeDay == null) {
							LocalDateTime dateMonth = LocalDateTime.of(year, month, 1, 0, 0);
							startTime = TimeUtil.dateTimeToOleMinutes(dateMonth);
							endTime = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, dateMonth.toLocalDate().lengthOfMonth(), 23, 0));
						} else {
							try {
								int day = Integer.parseInt(timeDay);
								if(day < 1 || day > 31) {
									log.error("day out of range "+day);
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									return;
								}
								LocalDateTime dateDay = LocalDateTime.of(year, month, day, 0, 0);
								startTime = TimeUtil.dateTimeToOleMinutes(dateDay);
								endTime = TimeUtil.dateTimeToOleMinutes(dateDay.plusDays(1));
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
			} catch (Exception e) {
				log.error(e);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}

		String nanText = request.getParameter("nan_text") == null ? "NA" : request.getParameter("nan_text");


		try {
			LinkedHashSet<String> processingSensorNameCollector = new LinkedHashSet<>();
			for(String plot:plots) {
				String[] schema = tsdb.getValidSchemaWithVirtualSensors(plot, sensorNames);
				processingSensorNameCollector.addAll(Arrays.asList(schema));
			}
			String[] processingSensorNames = processingSensorNameCollector.toArray(new String[0]);

			ServletOutputStream out = response.getOutputStream();
			boolean firstPlot = true;
			for(String plot:plots) {
				sensorNames = tsdb.supplementSchema(sensorNames, tsdb.getSensorNamesOfPlotWithVirtual(plot));			
				String[] validSchema =  tsdb.getValidSchemaWithVirtualSensors(plot, sensorNames);
				/*if(sensorNames.length!=validSchema.length) {
					String error = "some sensors not in plot: "+plot+"  "+Arrays.toString(sensorNames);
					log.info(error);
					response.getWriter().println(error);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);				
					return;
				}*/
				try {
				TimestampSeries ts = tsdb.plot(null, plot, validSchema, agg, dataQuality, isInterpolated, startTime, endTime);
				if(ts != null) {					
					ProjectionFillIterator it = new ProjectionFillIterator(ts.tsIterator(), processingSensorNames);
					String plot_text = col_plot ? plot : null;
					CSV.write(it, firstPlot, out, ",", nanText, CSVTimeType.DATETIME, false, false, agg, plot_text);
					firstPlot = false;
				}
				} catch (Exception e) {
					e.printStackTrace();
					log.error(e);
				}
			}

			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			response.getWriter().println(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
