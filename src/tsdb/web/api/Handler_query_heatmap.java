package tsdb.web.api;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.component.Region;
import tsdb.remote.RemoteTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.gui.TimeSeriesHeatMap;
import tsdb.util.gui.TimeSeriesPainterGraphics2D;
import tsdb.util.iterator.TimestampSeries;

public class Handler_query_heatmap extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_query_heatmap(RemoteTsDB tsdb) {
		super(tsdb, "query_heatmap");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("image/png");

		String plot = request.getParameter("plot");
		if(plot==null) {
			log.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		String sensorName = request.getParameter("sensor");

		if(sensorName==null) {
			log.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
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

		try {
			String[] sensorNames = tsdb.supplementSchema(sensorName);
			/*String[] sensorNames;
			if(sensorName.equals("WD")) {
				sensorNames = new String[]{sensorName,"WV"};
			} else if(sensorName.equals("sunshine")) {
				sensorNames = new String[]{sensorName,"Rn_300"};
			} else {
				sensorNames = new String[]{sensorName};
			}*/
			String[] validSchema =  tsdb.getValidSchemaWithVirtualSensors(plot, sensorNames);
			if(sensorNames.length!=validSchema.length) {
				log.info("sensorName not in plot: "+plot+"  "+sensorName);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);				
				return;
			}
			TimestampSeries ts = tsdb.plot(null, plot, sensorNames, AggregationInterval.HOUR, dataQuality, isInterpolated, startTime, endTime);
			if(ts==null) {
				log.error("TimestampSeries null: "+plot);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}

			float xDiagramMin = 24;

			int imageWidth = (int) ((ts.getLastTimestamp()-ts.getFirstTimestamp())/(60*24))+(int)xDiagramMin;
			int imageHeight = (24+12);
			BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);

			TimeSeriesPainterGraphics2D tsp = new TimeSeriesPainterGraphics2D(bufferedImage);		

			TimeSeriesHeatMap tshm = new TimeSeriesHeatMap(ts);



			tshm.draw(tsp,sensorName, xDiagramMin);
			tshm.drawTimescale(tsp, xDiagramMin, 24, imageWidth+1, imageHeight-1, true);
			tshm.leftField(tsp,0,0,xDiagramMin-1,imageHeight-1);

			try {
				ImageIO.write(bufferedImage, "png", response.getOutputStream());
				response.setStatus(HttpServletResponse.SC_OK);
			} catch (IOException e) {
				log.error(e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
