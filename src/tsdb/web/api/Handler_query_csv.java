package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.iterator.ProjectionFillIterator;
import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.iterator.CSV;
import tsdb.util.iterator.CSVTimeType;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

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


	public Handler_query_csv(RemoteTsDB tsdb) {
		super(tsdb, "query_csv");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");

		CSVTimeType csvTimeType = CSVTimeType.DATETIME;
		{
			String datetime_fomat = request.getParameter("datetime_format");
			//Logger.info("dt " + datetime_fomat);
			if(datetime_fomat != null) {
				switch(datetime_fomat) {
				case "timestamp":
					csvTimeType = CSVTimeType.TIMESTAMP;
					break;
				case "custom":
					csvTimeType = CSVTimeType.CUSTOM;
					break;
				default:
					Logger.warn("unknown datetime_fomat");
				}
			}
		}

		if(request.getParameter("plot") == null) {
			Logger.warn("wrong call no plot parameter");
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
				Logger.warn("unknown input");
				col_plot = false;				
			}
		}

		//String sensorName = request.getParameter("sensor");
		String[] sensorNames = request.getParameterValues("sensor");

		if(sensorNames==null) {
			Logger.warn("wrong call no sensor");
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
				Logger.warn(e);
			}
		}

		String casted_text = request.getParameter("casted");
		boolean casted = false;
		if(casted_text != null) {
			switch(casted_text) {
			case "true":
				casted = true;
				break;
			case "false":
				casted = false;
				break;
			default:
				Logger.warn("unknown input");
				casted = false;				
			}
		}

		String spatial_aggregated_text = request.getParameter("spatial_aggregated");
		boolean spatial_aggregated = false;
		if(spatial_aggregated_text != null) {
			switch(spatial_aggregated_text) {
			case "true":
				spatial_aggregated = true;
				break;
			case "false":
				spatial_aggregated = false;
				break;
			default:
				Logger.warn("unknown input");
				spatial_aggregated = false;				
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
				Logger.warn(e);
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
				Logger.warn("unknown input");
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
					Logger.error("year out of range "+year);
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
							Logger.error("month out of range "+month);
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
									Logger.error("day out of range "+day);
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									return;
								}
								LocalDateTime dateDay = LocalDateTime.of(year, month, day, 0, 0);
								startTime = TimeUtil.dateTimeToOleMinutes(dateDay);
								endTime = TimeUtil.dateTimeToOleMinutes(dateDay.plusDays(1));
							} catch (Exception e) {
								Logger.error(e);
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								return;
							}	
						}
					} catch (Exception e) {
						Logger.error(e);
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						return;
					}	
				}				
			} catch (Exception e) {
				Logger.error(e);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}

		String timeEndYear = request.getParameter("end_year");
		if(timeEndYear != null) {
			try {
				int year = Integer.parseInt(timeEndYear);
				if(year < Handler_query_image.MIN_YEAR || year > Handler_query_image.MAX_YEAR) {
					Logger.error("end_year out of range "+year);
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
				String timeEndMonth = request.getParameter("end_month");
				if(timeEndMonth == null) {
					endTime = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 12, 31, 23, 0));
				} else {
					try {
						int month = Integer.parseInt(timeEndMonth);
						if(month<1||month>12) {
							Logger.error("end_month out of range "+month);
							response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							return;
						}
						String timeEndDay = request.getParameter("end_day");
						if(timeEndDay == null) {
							LocalDateTime dateMonth = LocalDateTime.of(year, month, 1, 0, 0);
							endTime = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, dateMonth.toLocalDate().lengthOfMonth(), 23, 0));
						} else {
							try {
								int day = Integer.parseInt(timeEndDay);
								if(day < 1 || day > 31) {
									Logger.error("end_day out of range "+day);
									response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
									return;
								}
								LocalDateTime dateDay = LocalDateTime.of(year, month, day, 0, 0);
								endTime = TimeUtil.dateTimeToOleMinutes(dateDay.plusDays(1));
							} catch (Exception e) {
								Logger.error(e);
								response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								return;
							}	
						}
					} catch (Exception e) {
						Logger.error(e);
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						return;
					}	
				}
			} catch (Exception e) {
				Logger.error(e);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}

		String startText = request.getParameter("start");
		if(startText != null) {
			startTime = (long) TimeUtil.parseStartTimestamp(startText);
		}

		String endText = request.getParameter("end");
		if(endText != null) {
			endTime = (long) TimeUtil.parseEndTimestamp(endText, agg);
		}

		String nanText = request.getParameter("nan_text") == null ? "NA" : request.getParameter("nan_text");


		try {
			LinkedHashSet<String> processingSensorNameCollector = new LinkedHashSet<>();
			for(String plot:plots) {
				String[] schema = tsdb.getValidSchemaWithVirtualSensors(plot, sensorNames);
				//schema = tsdb.supplementSchema(schema, tsdb.getSensorNamesOfPlotWithVirtual(plot));
				processingSensorNameCollector.addAll(Arrays.asList(schema));
			}
			String[] processingSensorNames = processingSensorNameCollector.toArray(new String[0]);

			try(PrintWriter out = new PrintWriter(response.getOutputStream(), false, StandardCharsets.UTF_8)) {

				if(spatial_aggregated) {
					TimestampSeries ts = tsdb.plots_aggregate(plots, processingSensorNames, agg, dataQuality, isInterpolated, startTime, endTime);
					if(ts != null) {
						TsIterator it = ts.tsIterator();
						CSV.write(it, true, out, ",", nanText, csvTimeType, false, false, agg, null);
					}
				} else {
					if(casted) {
						TimestampSeries ts = tsdb.plots_casted(plots, processingSensorNames, agg, dataQuality, isInterpolated, startTime, endTime);
						if(ts != null) {
							TsIterator it = ts.tsIterator();
							CSV.write(it, true, out, ",", nanText, csvTimeType, false, false, agg, null);
						}
					} else {
						boolean firstPlot = true;			
						for(String plot:plots) {
							sensorNames = tsdb.supplementSchema(sensorNames, tsdb.getSensorNamesOfPlotWithVirtual(plot));			
							String[] validSchema =  tsdb.getValidSchemaWithVirtualSensors(plot, sensorNames);
							try {
								Logger.info("load of "+Arrays.toString(validSchema));
								TimestampSeries ts = tsdb.plot(null, plot, validSchema, agg, dataQuality, isInterpolated, startTime, endTime);
								if(ts != null) {					
									ProjectionFillIterator it = new ProjectionFillIterator(ts.tsIterator(), processingSensorNames);
									String plot_text = col_plot ? plot : null;
									CSV.write(it, firstPlot, out, ",", nanText, csvTimeType, false, false, agg, plot_text);
									firstPlot = false;
								}
							} catch (Exception e) {
								e.printStackTrace();
								Logger.error(e);
							}
						}
					}
				}
			}
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
			response.getWriter().println(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
