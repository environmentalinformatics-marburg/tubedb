package tsdb.web.api;

import java.awt.image.BufferedImage;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.remote.RemoteTsDB;
import tsdb.util.gui.ImageRGBA;
import tsdb.util.gui.TimeSeriesHeatMap;
import tsdb.util.gui.TimeSeriesPainterGraphics2D;

/**
 * Get color scale of heatmap as image.
 * <p>
 * parameter: sensor
 * @author woellauer
 *
 */
public class Handler_heatmap_scale extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_heatmap_scale(RemoteTsDB tsdb) {
		super(tsdb, "heatmap_scale");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("image/png");

		String sensorName = request.getParameter("sensor");

		if(sensorName==null) {
			log.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}	

		try {
			BufferedImage bufferedImage;

			if(sensorName.equals("WD")) {
				
				int imageWidth = 200;
				bufferedImage = new BufferedImage(imageWidth, 200, java.awt.image.BufferedImage.TYPE_INT_RGB);
				TimeSeriesPainterGraphics2D tsp = new TimeSeriesPainterGraphics2D(bufferedImage);			
				TimeSeriesHeatMap.drawRoundScale(tsp, sensorName);

			} else {

				int imageWidth = 800;
				bufferedImage = new BufferedImage(imageWidth, 24, java.awt.image.BufferedImage.TYPE_INT_RGB);
				TimeSeriesPainterGraphics2D tsp = new TimeSeriesPainterGraphics2D(bufferedImage);			
				TimeSeriesHeatMap.drawScale(tsp, sensorName);
			}

			try {
				//ImageIO.write(bufferedImage, "png", response.getOutputStream());
				ImageRGBA.ofBufferedImage(bufferedImage).writePngCompressed(response.getOutputStream());
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
