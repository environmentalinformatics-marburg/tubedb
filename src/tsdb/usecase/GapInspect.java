package tsdb.usecase;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlanGenerators;
import tsdb.util.DataQuality;
import tsdb.util.Interval;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;
import tsdb.util.TsEntry;
import tsdb.util.gui.TimeSeriesPainter.PosHorizontal;
import tsdb.util.gui.TimeSeriesPainter.PosVerical;
import tsdb.util.gui.TimeSeriesPainterGraphics2D;
import tsdb.util.iterator.TsIterator;

public class GapInspect {

	int min = Integer.MAX_VALUE;
	int max = Integer.MIN_VALUE;
	int offsetX = 100;


	private static final Logger log = LogManager.getLogger();

	private static class StationGapInfo {
		public final String stationID;
		public int first;
		public int last;
		public ArrayList<Interval> gapList;

		public StationGapInfo(String stationID) {
			this.stationID = stationID;
			first = -1;
			last = -1;
			gapList = new ArrayList<Interval>();
		}
	}

	public int toX(int timestamp) {
		return ((timestamp-min)/(24*60))+offsetX;
	}

	public static void main(String[] args) {
		new GapInspect().run();
	}

	public void run() {
		log.info("start");

		TsDB tsdb = TsDBFactory.createDefault();


		Map<String,StationGapInfo> gapMap = new TreeMap<String,StationGapInfo>();


		for(Station station:tsdb.getStations()) {
			if(!station.isPlot) {
				String[] sensorNames = tsdb.streamStorage.getSensorNames(station.stationID);
				if(sensorNames!=null&&sensorNames.length>0) {
					//StreamIterator it = tsdb.streamStorage.getRawSensorIterator(station.stationID, "Ta_200", null, null);
					//TsIterator it = tsdb.streamStorage.getRawIterator(station.stationID, sensorNames, null, null, null);
					TsIterator it = QueryPlanGenerators.getStationGen(tsdb, DataQuality.Na).get(station.stationID, sensorNames).get(null, null);
					StationGapInfo stationGapInfo = new StationGapInfo(station.stationID);
					if(it==null||!it.hasNext()) {

					} else {

						TsEntry e = it.next();
						int timestamp = (int) e.timestamp;
						stationGapInfo.first = timestamp;
						stationGapInfo.last = timestamp;
						int prev = timestamp;
						while(it.hasNext()) {
							e = it.next();
							timestamp = (int) e.timestamp;
							stationGapInfo.last = timestamp;
							int curr = timestamp;

							if(prev+(2*24*60)<curr) {
								int gap_start = prev;
								int gap_end = curr;							
								stationGapInfo.gapList.add(Interval.of(gap_start, gap_end));	
							}


							prev = curr;
						}
					}
					gapMap.put(station.stationID, stationGapInfo);
				}
			}
		}


		for(StationGapInfo stationGapInfo:gapMap.values()) {
			if(stationGapInfo.first!=-1&&stationGapInfo.first<min) {
				min = stationGapInfo.first;
			}
			if(stationGapInfo.last!=-1&&max<stationGapInfo.last) {
				max = stationGapInfo.last;
			}
		}

		System.out.println(min+"  "+max);

		int pos = 0;

		int blockheight = 20;
		int blockheight_small = 10;
		int block_start_small = 5;

		int blockheight_plot = 9;
		int block_start_plot = 25;

		int stationHeight = 40;


		int imageWidth = toX(max)+20;
		int imageHeight = gapMap.size()*stationHeight;
		BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);


		TimeSeriesPainterGraphics2D tsp = new TimeSeriesPainterGraphics2D(bufferedImage);
		tsp.setColor(255, 255, 255);
		tsp.fillRect(0, 0, imageWidth-1, imageHeight-1);

		System.out.println();

		LocalDateTime dt = TimeUtil.oleMinutesToLocalDateTime(min);
		int minMonthTimestamp = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(dt.getYear(), dt.getMonthValue(), 1, 0, 0));


		for(int t=minMonthTimestamp;t<=max;t+=(24*60)) {
			dt = TimeUtil.oleMinutesToLocalDateTime(t);
			if(dt.getDayOfMonth()==1) {
				if(dt.getMonthValue()==1) {
					tsp.setColor(90, 90, 90);
				} else if(dt.getMonthValue()==7) {
					tsp.setColor(170, 170, 170);
				} else if(dt.getMonthValue()%2==1) {
					tsp.setColor(210, 210, 210);
				}else {					
					tsp.setColor(210, 210, 210);
				}
				int monthX = toX(t);
				tsp.drawLine(monthX, 0, monthX, imageHeight-1);
			}
		}




		for(StationGapInfo stationGapInfo:gapMap.values()) {
			int posStartY = pos*stationHeight;
			int blockPosStartY = posStartY+1;
			int plotPosStartY = posStartY+block_start_plot; 

			tsp.setColor(200, 200, 200);
			tsp.drawLine(0, posStartY, imageWidth-1, posStartY);


			tsp.setColor(100, 100, 100);
			tsp.drawText(stationGapInfo.stationID, 10, posStartY, PosHorizontal.LEFT, PosVerical.TOP);



			tsp.setColor(200, 200, 255);
			int start = toX(stationGapInfo.first);
			int end = toX(stationGapInfo.last);
			//tsp.drawLine(start, pos*10, end, pos*10);
			tsp.fillRect(start, blockPosStartY, end, blockPosStartY+blockheight-1);

			System.out.println(stationGapInfo.stationID);
			for(Interval gap:stationGapInfo.gapList) {
				System.out.println(gap);
				tsp.setColor(255, 255, 255);
				start = toX(gap.start);
				end = toX(gap.end);
				//tsp.drawLine(start, pos*10, end, pos*10);
				tsp.fillRect(start, blockPosStartY, end, blockPosStartY+blockheight-1);
				tsp.setColor(255, 50, 50);
				tsp.fillRect(start, blockPosStartY+block_start_small, end, blockPosStartY+block_start_small+blockheight_small-1);
			}
			System.out.println();

			Station station = tsdb.getStation(stationGapInfo.stationID);



			@SuppressWarnings("unchecked")
			Consumer<Void>[] plotColors = new Consumer[]{x->tsp.setColor(190, 240, 240),x->tsp.setColor(240, 190, 240),x->tsp.setColor(240, 240, 190)};


			int plotOffsetY = 0;
			for(TimestampInterval<StationProperties> property:station.propertiesList) {
				int plotStart =  property.start==null?min:property.start.intValue();
				int plotEnd =  property.end==null?max:property.end.intValue();
				//tsp.setColor(170, 170, 170);
				plotColors[plotOffsetY%plotColors.length].accept(null);
				tsp.fillRect(toX(plotStart), plotPosStartY, toX(plotEnd), plotPosStartY+blockheight_plot-1);				
				plotOffsetY++;
			}

			for(TimestampInterval<StationProperties> property:station.propertiesList) {
				int plotStart =  property.start==null?min:property.start.intValue();
				int plotEnd =  property.end==null?max:property.end.intValue();				
				tsp.setColor(0, 0, 0);
				tsp.drawText(property.value.get_plotid(), (toX(plotStart)+toX(plotEnd))/2,plotPosStartY+3, PosHorizontal.CENTER, PosVerical.CENTER);				
			}


			pos++;
		}

		try {
			ImageIO.write(bufferedImage, "png", new File(TsDBFactory.OUTPUT_PATH+"/"+"GapInspect.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}


		log.info("gap count "+gapMap.size());

		log.info("end");
	}

}
