package tsdb.web.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.component.Region;
import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.gui.ImageRGBA;
import tsdb.util.gui.TimeSeriesHeatMap;
import tsdb.util.gui.TimeSeriesPainterGraphics2D;
import tsdb.util.iterator.TimestampSeries;

/**
 * Get heatmap as image.
 * <p>
 * timeseries data of all years or one year or one month is aggregated to hour.
 * <p>
 * parameters:
 * <br>
 * plot
 * <br>
 * sensor
 * <br>
 * (optional defaults to step) quality
 * <br>
 * (optional defaults to false) interpolated
 * <br>
 * (optional defaults to all years) year
 * <br>
 * (optional defaults to full year) month 
 * <p>
 * 
 * @author woellauer
 *
 */
public class Handler_query_heatmap extends MethodHandler {	
	

	public Handler_query_heatmap(RemoteTsDB tsdb) {
		super(tsdb, "query_heatmap");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("image/png");

		String plot = request.getParameter("plot");
		if(plot==null) {
			Logger.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String sensorName = request.getParameter("sensor");

		if(sensorName==null) {
			Logger.warn("wrong call");
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
								endTime = TimeUtil.dateTimeToOleMinutes(dateDay.plusHours(23));
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
		} else {
			Region region = tsdb.getRegionByPlot(plot);
			if(region!=null) {
				startTime = (long) region.viewTimeRange.start;
				endTime = (long) region.viewTimeRange.end;
			} else {			
				startTime = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2008, 1, 1, 0, 0)); ////TODO !!!!!!!!!!!! fixed start and end time
				endTime = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2015, 12, 31, 23, 0)); ///TODO !!!!!!!!!!!!!!!
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
			endTime = (long) TimeUtil.parseEndTimestamp(endText);
		}

		boolean timeScale = request.getParameter("time_scale") == null ? true : !request.getParameter("time_scale").equals("false");
		boolean byYear = request.getParameter("by_year") == null ? false : request.getParameter("by_year").equals("true");
		
		try {
			String[] sensorNames = tsdb.supplementSchema(new String[]{sensorName}, tsdb.getSensorNamesOfPlotWithVirtual(plot));
			String[] validSchema =  tsdb.getValidSchemaWithVirtualSensors(plot, sensorNames);
			if(sensorNames.length!=validSchema.length) {
				Logger.info("sensorName not in plot: "+plot+"  "+sensorName);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);				
				return;
			}

			TimestampSeries ts = tsdb.plot(null, plot, sensorNames, agg, dataQuality, isInterpolated, startTime, endTime);
			if(ts==null) {
				Logger.error("TimestampSeries null: "+plot);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}

			

			float xDiagramMin = 24;
			
			

			if(byYear) {
				
				float yDiagramMin = 12;

				int imageWidth = ((TimeUtil.roundNextYear((int)ts.getFirstTimestamp())-TimeUtil.roundLowerYear((int)ts.getFirstTimestamp()))/(60*24)) + (int)xDiagramMin + (int)xDiagramMin;

				int imageHeight = (TimeUtil.oleMinutesToLocalDateTime(TimeUtil.roundNextYear((int)ts.getLastTimestamp())).getYear() - TimeUtil.oleMinutesToLocalDateTime(TimeUtil.roundLowerYear((int)ts.getFirstTimestamp())).getYear()) * 24;
				imageHeight += timeScale ? 12 : 0;
				imageHeight += yDiagramMin;
				BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);

				TimeSeriesPainterGraphics2D tsp = new TimeSeriesPainterGraphics2D(bufferedImage);		

				TimeSeriesHeatMap tshm = new TimeSeriesHeatMap(ts);


				tshm.drawByYear(tsp,sensorName, xDiagramMin, yDiagramMin, agg);
				
				tshm.leftFieldByYear(tsp,0,yDiagramMin,xDiagramMin-1,imageHeight-1);
				tshm.rightFieldByYear(tsp,imageWidth-(int)xDiagramMin,yDiagramMin,imageWidth-1,imageHeight-1);

				if(timeScale) {
					tsp.setColor(255, 255, 255);
					tsp.fillRect(0, 0, xDiagramMin-1, yDiagramMin-1);
					tshm.drawTimescale(tsp, xDiagramMin, 0, imageWidth+1, 11, false);
					tshm.drawTimescale(tsp, xDiagramMin, imageHeight-12, imageWidth+1, imageHeight-1, false);
				}
				
				tsp.setColor(255, 255, 255);
				tsp.fillRect(imageWidth-xDiagramMin+1, 0, imageWidth-1, yDiagramMin-1);
				tsp.fillRect(imageWidth-xDiagramMin+1, imageHeight-yDiagramMin+1, imageWidth-1, imageHeight-1);
				tsp.setColor(150, 150, 150);
				tsp.drawLine(imageWidth-xDiagramMin+1, imageHeight-yDiagramMin, imageWidth-1, imageHeight-yDiagramMin);
				
				try {
					ImageRGBA.ofBufferedImage(bufferedImage).writePngCompressed(response.getOutputStream());
					response.setStatus(HttpServletResponse.SC_OK);
				} catch (IOException e) {
					Logger.error(e);
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}
			} else {

				int imageWidth = (int) ((ts.getLastTimestamp()-ts.getFirstTimestamp())/(60*24)) + (int)xDiagramMin;

				int imageHeight = 24;
				imageHeight += timeScale ? 12 : 0;
				BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);

				TimeSeriesPainterGraphics2D tsp = new TimeSeriesPainterGraphics2D(bufferedImage);		

				TimeSeriesHeatMap tshm = new TimeSeriesHeatMap(ts);

				tshm.draw(tsp,sensorName, xDiagramMin, agg);
				if(timeScale) {
					tshm.drawTimescale(tsp, xDiagramMin, 24, imageWidth+1, imageHeight-1, true);
				}
				tshm.leftField(tsp,0,0,xDiagramMin-1,imageHeight-1);
				
				try {
					ImageRGBA.ofBufferedImage(bufferedImage).writePngCompressed(response.getOutputStream());
					response.setStatus(HttpServletResponse.SC_OK);
				} catch (IOException e) {
					Logger.error(e);
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				}

			}			
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
