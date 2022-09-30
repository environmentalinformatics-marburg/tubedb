package tsdb.web.api;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;

import org.eclipse.jetty.server.Request;

import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.component.SensorCategory;
import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.gui.ImageRGBA;
import tsdb.util.gui.TimeSeriesDiagram;
import tsdb.util.gui.TimeSeriesDiagram.AggregatedConnectionType;
import tsdb.util.gui.TimeSeriesDiagram.AggregatedValueType;
import tsdb.util.gui.TimeSeriesDiagram.RawConnectionType;
import tsdb.util.gui.TimeSeriesDiagram.RawValueType;
import tsdb.util.gui.TimeSeriesPainterGraphics2D;
import tsdb.util.iterator.TimestampSeries;

/**
 * Get timeseries data as image with diagram or boxplot.
 * <p>
 * parameters:
 * <br>
 * plot
 * <br>
 * sensor
 * <br>
 * (optional defaults to hour) aggregation
 * <br>
 * (optional defaults to false) boxplot
 * <br>
 * (optional defaults to step) quality
 * <br>
 * (optional defaults to false) interpolated
 * <br>
 * (optional defaults to all years) year
 * <br>
 * (optional defaults to full year) month 
 * @author woellauer
 *
 */
public class Handler_query_image extends MethodHandler {	
	

	public static final int MIN_YEAR = 1900;
	public static final int MAX_YEAR = 2100;

	private static final boolean USE_COMPARE_TIMESERIES = false;

	public Handler_query_image(RemoteTsDB tsdb) {
		super(tsdb, "query_image");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("image/png");
		//response.setHeader("Cache-Control", "max-age=300");
		response.setHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
		response.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
		response.setHeader("Pragma", "no-cache");

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
		
		String connectionText = request.getParameter("connection");
		AggregatedConnectionType aggregatedConnection = AggregatedConnectionType.STEP;
		if(connectionText != null) {
			try {
				AggregatedConnectionType ac = AggregatedConnectionType.parse(connectionText);
				if(ac != null) {
					aggregatedConnection = ac;
				}
			} catch (Exception e) {
				Logger.warn(e);
			}
		}
		
		String rawConnectionText = request.getParameter("raw_connection");
		RawConnectionType rawConnection = RawConnectionType.CURVE;
		if(rawConnectionText != null) {
			try {
				RawConnectionType rc = RawConnectionType.parse(rawConnectionText);
				if(rc != null) {
					rawConnection = rc;
				}
			} catch (Exception e) {
				Logger.warn(e);
			}
		}
		
		String aggregatedValueText = request.getParameter("value");
		AggregatedValueType aggregatedValue = AggregatedValueType.LINE;
		if(aggregatedValueText != null) {
			try {
				AggregatedValueType av = AggregatedValueType.parse(aggregatedValueText);
				if(av != null) {
					aggregatedValue = av;
				}
			} catch (Exception e) {
				Logger.warn(e);
			}
		}
		
		String rawValueText = request.getParameter("raw_value");
		RawValueType rawValue = RawValueType.POINT;
		if(rawValueText != null) {
			try {
				RawValueType rv = RawValueType.parse(rawValueText);
				if(rv != null) {
					rawValue = rv;
				}
			} catch (Exception e) {
				Logger.warn(e);
			}
		}

		boolean boxplot = false;		

		String boxplotText = request.getParameter("boxplot");
		if(boxplotText!=null) {
			switch(boxplotText) {
			case "true":
				if(agg==AggregationInterval.DAY||agg==AggregationInterval.WEEK||agg==AggregationInterval.MONTH||agg==AggregationInterval.YEAR) {
					boxplot = true;
				} else {
					Logger.warn("no boxplot for aggregate "+agg);
				}
				break;
			case "false":
				boxplot = false;
				break;
			default:
				Logger.warn("unknown input for boxplot");
				boxplot = false;				
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
				Logger.warn("unknown value for parameter interpolated: "+interpolated);
				isInterpolated = false;				
			}
		}

		String timeYear = request.getParameter("year");
		Long startTime = null;
		Long endTime = null;
		if(timeYear!=null) {
			try {
				int year = Integer.parseInt(timeYear);
				if(year<MIN_YEAR||year>MAX_YEAR) {
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
				if(year < MIN_YEAR || year > MAX_YEAR) {
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

		try {
			String[] sensorNames = tsdb.supplementSchema(new String[]{sensorName}, tsdb.getSensorNamesOfPlotWithVirtual(plot));
			String[] validSchema =  tsdb.getValidSchemaWithVirtualSensors(plot, sensorNames);
			if(sensorNames.length!=validSchema.length) {
				Logger.info("sensorName not in plot: "+plot+"  "+sensorName+"    "+Arrays.toString(sensorNames)+"   "+Arrays.toString(validSchema));
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);				
				return;
			}

			TimestampSeries ts;

			if(boxplot) {
				ts = tsdb.plotQuartile(plot, sensorNames, agg, dataQuality, isInterpolated, startTime, endTime);
			} else {
				ts = tsdb.plot(null, plot, sensorNames, agg, dataQuality, isInterpolated, startTime, endTime);
			}

			if(ts==null) {
				Logger.error("TimestampSeries null: "+plot);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}
			TimestampSeries compareTs = null;
			if(USE_COMPARE_TIMESERIES) {
				try {
					compareTs = tsdb.plot(null, plot, new String[]{sensorName}, agg, DataQuality.NO, false, startTime, endTime);
				} catch(Exception e) {
					e.printStackTrace();
					Logger.warn(e);
				}
			}

			int imageWidth = 1500;
			//int imageHeight = 400;
			int imageHeight = 200;

			String imageWidthText = request.getParameter("width");
			String imageHeightText = request.getParameter("height");
			if(imageWidthText!=null&&imageHeightText!=null) {
				try {
					int w = Integer.parseInt(imageWidthText);
					int h = Integer.parseInt(imageHeightText);
					if(w >= 32 && h >= 32 && w < 100000 && h < 100000) {
						imageWidth = w;
						imageHeight = h;
					}
				} catch(Exception e) {
					Logger.warn(e);
				}
			}

			BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);
			Graphics2D gc = bufferedImage.createGraphics();
			gc.setBackground(new Color(255, 255, 255));
			gc.setColor(new Color(0, 0, 0));
			gc.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
			gc.dispose();

			SensorCategory diagramType = SensorCategory.OTHER;
			try {
				Sensor sensor = tsdb.getSensor(sensorName);
				if(sensor!=null) {
					diagramType = sensor.category;
					if(diagramType==null) {
						diagramType = SensorCategory.OTHER;
					}
				}
			} catch(Exception e) {
				Logger.warn(e);
			}
			
			float[] valueRange = null;
			String valueMinText = request.getParameter("value_min");
			String valueMaxText = request.getParameter("value_max");
			if(valueMinText != null && valueMaxText != null) {
				//float valueMin = Float.parseFloat(valueMinText);
				//float valueMax = Float.parseFloat(valueMaxText);
				float valueMin = (float) FastDoubleParser.parseDouble(valueMinText);
				float valueMax = (float) FastDoubleParser.parseDouble(valueMaxText);
				valueRange = new float[] {valueMin, valueMax};
			}

			TimeSeriesDiagram tsd = new TimeSeriesDiagram(ts, agg, diagramType, boxplot, aggregatedConnection, rawConnection, aggregatedValue, rawValue, valueRange);

			if(agg != null && startTime != null && endTime !=null && agg == AggregationInterval.RAW) {
				tsd.setDiagramTimestampRange(startTime, endTime);
			}
			tsd.draw(new TimeSeriesPainterGraphics2D(bufferedImage),compareTs);

			try {
				response.setStatus(HttpServletResponse.SC_OK);
				//ImageIO.write(bufferedImage, "png", response.getOutputStream());
				//ImageIO.write(bufferedImage, "jpg", response.getOutputStream());
				/*{
					ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
					ImageWriteParam iwp = writer.getDefaultWriteParam();
					iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
					iwp.setCompressionQuality(1f);
					writer.setOutput(ImageIO.createImageOutputStream(response.getOutputStream()));
					IIOImage image = new IIOImage(bufferedImage, null, null);
					writer.write(null, image, iwp);
					writer.dispose();
				}*/
				//ImageRGBA.ofBufferedImage(bufferedImage).writePngUncompressed(response.getOutputStream());
				ImageRGBA.ofBufferedImage(bufferedImage).writePngCompressed(response.getOutputStream());
			} catch (IOException e) {
				Logger.error(e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}	
}
