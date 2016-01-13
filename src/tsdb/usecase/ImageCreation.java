package tsdb.usecase;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import tsdb.TsDBFactory;
import tsdb.component.SensorCategory;
import tsdb.remote.RemoteTsDBFactory;
import tsdb.remote.ServerTsDB;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.gui.TimeSeriesDiagram;
import tsdb.util.gui.TimeSeriesPainterGraphics2D;
import tsdb.util.iterator.TimestampSeries;

public class ImageCreation {

	public static void main(String[] args) throws IOException {
		
		ServerTsDB tsdb = RemoteTsDBFactory.createDefaultServer();
		
		String queryType = null;
		String plotID = "HEG01";
		String[] columnNames = new String[]{"Ta_200"};
		AggregationInterval aggregationInterval = AggregationInterval.HOUR;
		DataQuality dataQuality = DataQuality.STEP;
		boolean interpolated = false;
		Long start = null;
		Long end = null;	
		
		TimestampSeries timestampseries = tsdb.plot(queryType, plotID, columnNames, aggregationInterval, dataQuality, interpolated, start, end);
		
		System.out.println("size: "+timestampseries.entryList.size());
		
		SensorCategory diagramType = SensorCategory.TEMPERATURE;
		TimeSeriesDiagram tsd = new TimeSeriesDiagram(timestampseries, aggregationInterval, diagramType, false);
		
		
		//BufferedImage bufferedImage = new BufferedImage(6000,600,java.awt.image.BufferedImage.TYPE_INT_RGB);
		BufferedImage bufferedImage = new BufferedImage(1280,400,java.awt.image.BufferedImage.TYPE_INT_RGB);
		Graphics2D gc = bufferedImage.createGraphics();
		gc.setBackground(new java.awt.Color(255, 255, 255));
		gc.setColor(new java.awt.Color(0, 0, 0));
		gc.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
		gc.dispose();
		TimeSeriesPainterGraphics2D tsp = new TimeSeriesPainterGraphics2D(bufferedImage);
		tsd.draw(tsp);
		
		String formatName = "png";
		ImageIO.write(bufferedImage, formatName, new File(TsDBFactory.OUTPUT_PATH+"/diagram.png"));
	
		
	}
	
}
