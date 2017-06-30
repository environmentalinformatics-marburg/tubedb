package tsdb.util.gui;

import java.time.LocalDate;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.AggregationInterval;
import tsdb.util.Pair;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.gui.TimeSeriesPainter.PosHorizontal;
import tsdb.util.gui.TimeSeriesPainter.PosVerical;
import tsdb.util.iterator.TimestampSeries;

public class TimeSeriesHeatMap {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private final TimestampSeries ts;

	public TimeSeriesHeatMap(TimestampSeries ts) {
		this.ts = ts;
		/*if(ts.timeinterval!=60) {
			throw new RuntimeException("TimeSeriesHeatMap needs one hour time steps: "+ts.timeinterval);
		}*/
	}
	
	public void draw(TimeSeriesPainter tsp, String sensorName, float xMin, AggregationInterval agg) {
		switch(agg) {
		case RAW:
		case HOUR:
			drawHours(tsp, sensorName, xMin);
			break;
		case DAY:
			drawDays(tsp, sensorName, xMin);
			break;
		case WEEK:
			drawWeeks(tsp, sensorName, xMin);
			break;
		case MONTH:
			drawMonths(tsp, sensorName, xMin);
			break;
		case YEAR:
			drawYears(tsp, sensorName, xMin);
			break;
		default:
			throw new RuntimeException("aggregation not drawable for heatmap: "+agg);
		}
	}
	
	public void drawByYear(TimeSeriesPainter tsp, String sensorName, float xMin, float yMin, AggregationInterval agg) {
		switch(agg) {
		case RAW:
		case HOUR:
			drawHoursByYear(tsp, sensorName, xMin, yMin);
			break;
		case DAY:
			drawDaysByYear(tsp, sensorName, xMin, yMin);
			break;
		case WEEK:
			drawWeeksByYear(tsp, sensorName, xMin, yMin);
			break;
		case MONTH:
			drawMonthsByYear(tsp, sensorName, xMin, yMin);
			break;
		case YEAR:
			drawYearsByYear(tsp, sensorName, xMin, yMin);
			break;
		default:
			throw new RuntimeException("aggregation not drawable for heatmap: "+agg);
		}
	}

	public void drawHours(TimeSeriesPainter tsp, String sensorName, float xMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();
		long start = ts.entryList.get(0).timestamp-ts.entryList.get(0).timestamp%(60*24);
		for(TsEntry entry:ts.entryList) {
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x = (((entry.timestamp-start)/60)/24)*1+xMin;
				float y = (((entry.timestamp-start)/60)%24)*1;
				tsp.drawLine(x, y, x, y);
			}
		}
	}
	
	public void drawDays(TimeSeriesPainter tsp, String sensorName, float xMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();
		long start = ts.entryList.get(0).timestamp-ts.entryList.get(0).timestamp%(60*24);
		for(TsEntry entry:ts.entryList) {
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x = (entry.timestamp - start)/1440 + xMin;
				tsp.drawLine(x, 0, x, 23);
			}
		}
	}
	
	public void drawWeeks(TimeSeriesPainter tsp, String sensorName, float xMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();
		long start = ts.entryList.get(0).timestamp-ts.entryList.get(0).timestamp%(60*24);
		for(TsEntry entry:ts.entryList) {
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x1 = (entry.timestamp - start)/1440 + xMin;
				float x2 = (entry.timestamp - start)/1440 + 6 + xMin;
				//tsp.drawLine(x1, 0, x1, 23);
			    //tsp.drawLine(x2, 0, x2, 23);
				tsp.fillRect(x1, 0, x2, 23);
			}
		}
	}
	
	public void drawMonths(TimeSeriesPainter tsp, String sensorName, float xMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();
		long start = ts.entryList.get(0).timestamp-ts.entryList.get(0).timestamp%(60*24);
		for(TsEntry entry:ts.entryList) {
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x1 = (entry.timestamp - start)/1440 + xMin;
				float x2 = (TimeUtil.roundNextMonth((int) entry.timestamp) - 1 - start)/1440 + xMin;
				//tsp.drawLine(x1, 0, x1, 23);
			    //tsp.drawLine(x2, 0, x2, 23);
				tsp.fillRect(x1, 0, x2, 23);
			}
		}
	}
	
	public void drawYears(TimeSeriesPainter tsp, String sensorName, float xMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();
		long start = ts.entryList.get(0).timestamp-ts.entryList.get(0).timestamp%(60*24);
		for(TsEntry entry:ts.entryList) {
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x1 = (entry.timestamp - start)/1440 + xMin;
				float x2 = (TimeUtil.roundNextYear((int) entry.timestamp) - 1 - start)/1440 + xMin;
				//tsp.drawLine(x1, 0, x1, 23);
				//tsp.drawLine(x2, 0, x2, 23);
				tsp.fillRect(x1, 0, x2, 23);
			}
		}
	}

	public void drawHoursByYear(TimeSeriesPainter tsp, String sensorName, float xMin, float yMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();		
		int yearBaseTimestamp = TimeUtil.roundLowerYear((int)ts.entryList.get(0).timestamp);
		int yearNextTimestamp = TimeUtil.roundNextYear((int)ts.entryList.get(0).timestamp);
		int yBase = (int) yMin;
		for(TsEntry entry:ts.entryList) {
			int t = (int) entry.timestamp;
			if(t>=yearNextTimestamp) {
				yearBaseTimestamp = TimeUtil.roundLowerYear(t);
				yearNextTimestamp = TimeUtil.roundNextYear(t);
				yBase += 24;
			}
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x = (((t-yearBaseTimestamp)/60)/24)*1+xMin;
				float y = yBase + (((t-yearBaseTimestamp)/60)%24)*1;
				tsp.drawLine(x, y, x, y);
			}
		}
	}
	
	public void drawDaysByYear(TimeSeriesPainter tsp, String sensorName, float xMin, float yMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();		
		int yearBaseTimestamp = TimeUtil.roundLowerYear((int)ts.entryList.get(0).timestamp);
		int yearNextTimestamp = TimeUtil.roundNextYear((int)ts.entryList.get(0).timestamp);
		int yBase = (int) yMin;
		for(TsEntry entry:ts.entryList) {
			int t = (int) entry.timestamp;
			if(t>=yearNextTimestamp) {
				yearBaseTimestamp = TimeUtil.roundLowerYear(t);
				yearNextTimestamp = TimeUtil.roundNextYear(t);
				yBase += 24;
			}
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x = (t - yearBaseTimestamp)/1440 + xMin;
				tsp.drawLine(x, yBase, x, yBase + 23);
			}
		}
	}
	
	public void drawWeeksByYear(TimeSeriesPainter tsp, String sensorName, float xMin, float yMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();		
		int yearBaseTimestamp = TimeUtil.roundLowerYear((int)ts.entryList.get(0).timestamp);
		int yearNextTimestamp = TimeUtil.roundNextYear((int)ts.entryList.get(0).timestamp);
		int yBase = (int) yMin;
		for(TsEntry entry:ts.entryList) {
			int t = (int) entry.timestamp;
			if(t>=yearNextTimestamp) {
				yearBaseTimestamp = TimeUtil.roundLowerYear(t);
				yearNextTimestamp = TimeUtil.roundNextYear(t);
				yBase += 24;
			}
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x1 = (t - yearBaseTimestamp)/1440 + xMin;
				float x2 = (t - yearBaseTimestamp)/1440 + 6 + xMin;
				//tsp.drawLine(x1, yBase, x1, yBase + 23);
				//tsp.drawLine(x2, yBase, x2, yBase + 23);
				tsp.fillRect(x1, yBase, x2, yBase + 23);
			}
		}
	}
	
	public void drawMonthsByYear(TimeSeriesPainter tsp, String sensorName, float xMin, float yMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();		
		int yearBaseTimestamp = TimeUtil.roundLowerYear((int)ts.entryList.get(0).timestamp);
		int yearNextTimestamp = TimeUtil.roundNextYear((int)ts.entryList.get(0).timestamp);
		int yBase = (int) yMin;
		for(TsEntry entry:ts.entryList) {
			int t = (int) entry.timestamp;
			if(t>=yearNextTimestamp) {
				yearBaseTimestamp = TimeUtil.roundLowerYear(t);
				yearNextTimestamp = TimeUtil.roundNextYear(t);
				yBase += 24;
			}
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x1 = (t - yearBaseTimestamp)/1440 + xMin;
				float x2 = (TimeUtil.roundNextMonth(t) - 1 - yearBaseTimestamp)/1440 + xMin;
				//tsp.drawLine(x1, yBase, x1, yBase + 23);
				//tsp.drawLine(x2, yBase, x2, yBase + 23);
				tsp.fillRect(x1, yBase, x2, yBase + 23);
			}
		}
	}
	
	public void drawYearsByYear(TimeSeriesPainter tsp, String sensorName, float xMin, float yMin) {
		setRange(tsp,sensorName);
		tsp.setColorRectWater();		
		int yearBaseTimestamp = TimeUtil.roundLowerYear((int)ts.entryList.get(0).timestamp);
		int yearNextTimestamp = TimeUtil.roundNextYear((int)ts.entryList.get(0).timestamp);
		int yBase = (int) yMin;
		for(TsEntry entry:ts.entryList) {
			int t = (int) entry.timestamp;
			if(t>=yearNextTimestamp) {
				yearBaseTimestamp = TimeUtil.roundLowerYear(t);
				yearNextTimestamp = TimeUtil.roundNextYear(t);
				yBase += 24;
			}
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				tsp.setIndexedColor(value);
				float x1 = (t - yearBaseTimestamp)/1440 + xMin;
				float x2 = (yearNextTimestamp - 1 - yearBaseTimestamp)/1440 + xMin;
				//tsp.drawLine(x1, yBase, x1, yBase + 23);
				//tsp.drawLine(x2, yBase, x2, yBase + 23);
				tsp.fillRect(x1, yBase, x2, yBase + 23);
			}
		}
	}

	/**
	 * 
	 * @param tsp
	 * @param xMin
	 * @param yMin
	 * @param xMax
	 * @param yMax
	 * @param yearMark  write year instead of January
	 */
	public void drawTimescale(TimeSeriesPainterGraphics2D tsp, float xMin, float yMin, float xMax, float yMax, boolean yearMark) {
		tsp.setColor(255, 255, 255);
		tsp.fillRect(xMin, yMin, xMax, yMax);

		long start = ts.entryList.get(0).timestamp-ts.entryList.get(0).timestamp%(60*24);
		long end = ts.entryList.get(ts.entryList.size()-1).timestamp-ts.entryList.get(ts.entryList.size()-1).timestamp%(60*24);
		//long start = TimeUtil.roundLowerYear((int) ts.entryList.get(0).timestamp);
		//long end = TimeUtil.roundNextYear((int) ts.entryList.get(ts.entryList.size()-1).timestamp); //- 60*24;

		tsp.setColor(150, 150, 150);
		//tsp.setColor(0, 0, 0);
		//int start_year = TimeConverter.oleMinutesToLocalDateTime(start).getYear();
		//tsp.drawText(""+start_year+"", xMin, yMax, PosHorizontal.LEFT, PosVerical.BOTTOM);


		LocalDate startDate = TimeUtil.oleMinutesToLocalDateTime(start).toLocalDate();
		//log.info(startDate);

		try {
			tsp.setFontSmall();

			long prevDayDiff = -1;
			LocalDate prevDate = null;
			long startDay = start/(60*24);
			long endDay = end/(60*24) + 1;
			boolean first = true;
			for(long day=startDay; day<=endDay; day++) {
				long dayDiff = day-startDay;
				LocalDate curr = startDate.plusDays(dayDiff);
				//log.info(curr);
				if(curr.getDayOfMonth()==1) {				
					tsp.drawLine(dayDiff+xMin, yMin, dayDiff+xMin, yMax);
					if(prevDayDiff>-1) {
						int month = prevDate.getMonthValue();
						if(yearMark&&month==1) {
							if(first) {
								tsp.drawText(TimeScale.getMonthText(month), (prevDayDiff+dayDiff)/2+xMin, yMin, PosHorizontal.CENTER, PosVerical.TOP);
							} else {
								//tsp.setColor(95, 81, 3);
								tsp.setColor(0, 0, 0);
								//tsp.drawLine(prevDayDiff+xMin, yMin+2, prevDayDiff+xMin, yMax);
								tsp.drawLine(prevDayDiff+1+xMin, yMin+1, prevDayDiff+1+xMin, yMax);
								tsp.drawText(""+prevDate.getYear(), (prevDayDiff+dayDiff)/2+xMin, yMin, PosHorizontal.CENTER, PosVerical.TOP);
								/*tsp.setColor(255, 255, 255);
								tsp.drawLine(prevDayDiff+xMin, yMin, prevDayDiff+1+xMin, yMin);
								tsp.drawLine(prevDayDiff+xMin, yMin+1, prevDayDiff+1+xMin, yMin+1);
								tsp.drawLine(prevDayDiff+xMin, yMin+2, prevDayDiff+1+xMin, yMin+2);*/	
								tsp.setColor(150, 150, 150);
							}
						} else {
							tsp.drawText(TimeScale.getMonthText(month), (prevDayDiff+dayDiff)/2+xMin, yMin, PosHorizontal.CENTER, PosVerical.TOP);
						}
						first = false;
					}
					prevDayDiff = dayDiff;
					prevDate = curr;
				}
			}



		} finally {
			tsp.setFontDefault();
		}

	}

	public void leftField(TimeSeriesPainterGraphics2D tsp, float xMin, float yMin, float xMax, float yMax) {
		try {

			tsp.setColor(255, 255, 255);
			tsp.fillRect(xMin, yMin, xMax, yMax);

			/*tsp.drawLine(xMin, yMin, xMin, yMax);
			tsp.drawLine(xMin, yMin, xMax, yMin);
			tsp.drawLine(xMax, yMin, xMax, yMax);
			tsp.drawLine(xMin, yMax, xMax, yMax);*/

			tsp.setColor(150, 150, 150);

			tsp.drawLine(xMax-4, 0, xMax, 0);
			tsp.drawLine(xMax-2, 6, xMax, 6);
			tsp.drawLine(xMax-4, 12, xMax, 12);
			tsp.drawLine(xMax-2, 18, xMax, 18);
			tsp.drawLine(xMax-4, 24, xMax, 24);


			tsp.setFontSmall();
			tsp.setColor(170, 170, 170);
			tsp.drawText(""+TimeUtil.oleMinutesToLocalDateTime(ts.entryList.get(0).timestamp).toLocalDate().getYear(), 0, 26, PosHorizontal.LEFT, PosVerical.TOP);

		} finally {
			tsp.setFontDefault();
		}
	}
	
	public void leftFieldByYear(TimeSeriesPainterGraphics2D tsp, float xMin, float yMin, float xMax, float yMax) {
		try {

			tsp.setColor(255, 255, 255);
			tsp.fillRect(xMin, yMin, xMax, yMax);

			/*tsp.drawLine(xMin, yMin, xMin, yMax);
			tsp.drawLine(xMin, yMin, xMax, yMin);
			tsp.drawLine(xMax, yMin, xMax, yMax);
			tsp.drawLine(xMin, yMax, xMax, yMax);*/

			int startYear = TimeUtil.oleMinutesToLocalDateTime(TimeUtil.roundLowerYear((int)ts.getFirstTimestamp())).getYear();
			int endYear = TimeUtil.oleMinutesToLocalDateTime(TimeUtil.roundLowerYear((int)ts.getLastTimestamp())).getYear();

			for(int year = startYear; year <= endYear; year++) {
				
				int base = (int) (yMin + (year-startYear) * 24);

				tsp.setColor(150, 150, 150);

				tsp.drawLine(xMin, base + 0, xMax, base + 0);
				tsp.drawLine(xMax-1, base + 6, xMax, base + 6);
				tsp.drawLine(xMax-2, base + 12, xMax, base + 12);
				tsp.drawLine(xMax-1, base + 18, xMax, base + 18);
				tsp.drawLine(xMin, base + 24, xMax, base + 24);


				tsp.setFontSmall();
				tsp.setColor(170, 170, 170);
				tsp.drawText(""+year, xMin, base + 13, PosHorizontal.LEFT, PosVerical.CENTER);

			}

		} finally {
			tsp.setFontDefault();
		}
	}
	
	public void rightFieldByYear(TimeSeriesPainterGraphics2D tsp, float xMin, float yMin, float xMax, float yMax) {
		try {

			tsp.setColor(255, 255, 255);
			tsp.fillRect(xMin, yMin, xMax, yMax);

			/*tsp.drawLine(xMin, yMin, xMin, yMax);
			tsp.drawLine(xMin, yMin, xMax, yMin);
			tsp.drawLine(xMax, yMin, xMax, yMax);
			tsp.drawLine(xMin, yMax, xMax, yMax);*/

			int startYear = TimeUtil.oleMinutesToLocalDateTime(TimeUtil.roundLowerYear((int)ts.getFirstTimestamp())).getYear();
			int endYear = TimeUtil.oleMinutesToLocalDateTime(TimeUtil.roundLowerYear((int)ts.getLastTimestamp())).getYear();

			for(int year = startYear; year <= endYear; year++) {
				
				int base = (int) (yMin + (year-startYear) * 24);

				tsp.setColor(150, 150, 150);

				tsp.drawLine(xMin, base + 0, xMax, base + 0);
				tsp.drawLine(xMin, base + 6, xMin+1, base + 6);
				tsp.drawLine(xMin, base + 12, xMin+2, base + 12);
				tsp.drawLine(xMin, base + 18, xMin+1, base + 18);
				tsp.drawLine(xMin, base + 24, xMax, base + 24);


				tsp.setFontSmall();
				tsp.setColor(170, 170, 170);
				tsp.drawText(""+year, xMin+4, base + 13, PosHorizontal.LEFT, PosVerical.CENTER);

			}

		} finally {
			tsp.setFontDefault();
		}
	}

	public static void drawScale(TimeSeriesPainter tsp, String sensorName) {
		setRange(tsp,sensorName);
		tsp.setColor(255, 255, 255);
		tsp.fillRect(tsp.getMinX(), tsp.getMinY(), tsp.getMaxX(), tsp.getMaxY());

		float[] r = tsp.getIndexColorRange();
		double min = r[0];
		double max = r[1];
		double range = max-min;

		double imageMin = tsp.getMinX();
		double imageMax = tsp.getMaxX();

		double scaleMin = imageMin+20;
		double scaleMax = imageMax-20;
		double scaleRange = scaleMax-scaleMin;

		ArrayList<double[]> scaleList = new ArrayList<double[]>(5);

		scaleList.add(new double[]{min,scaleMin});
		scaleList.add(new double[]{(3*min+max)/4,(3*scaleMin+scaleMax)/4});
		scaleList.add(new double[]{(min+max)/2,(scaleMin+scaleMax)/2});
		scaleList.add(new double[]{(min+3*max)/4,(scaleMin+3*scaleMax)/4});
		scaleList.add(new double[]{max, scaleMax});



		//log.info(min+"  "+max+"  "+(min+max)+"   "+(min+max)/2);

		tsp.setColor(0, 0, 0);

		for(double[] value:scaleList) {
			tsp.drawLine((float)value[1], 0, (float)value[1], 12);
		}

		for(double[] value:scaleList) {
			tsp.drawText(""+(float)value[0],(float) value[1], 10, PosHorizontal.CENTER, PosVerical.TOP);
		}


		for(int i=(int) scaleMin;i<=scaleMax;i++) {			
			double value = min + ((i-imageMin)*range)/scaleRange;
			tsp.setIndexedColor((float) value);
			for(int y=2;y<10;y++) {
				tsp.drawLine(i, y, i, y);
			}
		}
	}

	public static void drawRoundScale(TimeSeriesPainter tsp, String sensorName) {
		setRange(tsp,sensorName);
		tsp.setColor(255, 255, 255);
		//tsp.setColorTransparent();
		tsp.fillRect(tsp.getMinX(), tsp.getMinY(), tsp.getMaxX(), tsp.getMaxY());

		float prevX = 0;
		float prevY = 0;

		for(float d=60;d<90;d+=0.5f) {

			for(int i=0;i<720;i++) {

				tsp.setIndexedColor((float) i/2);

				float x = (float) (Math.sin((i*2*Math.PI)/720)*d)+100;
				float y = (float) (-Math.cos((i*2*Math.PI)/720)*d)+100;

				if(i==0) {
					prevX = x;
					prevY = y;
				}

				tsp.drawLine(prevX, prevY, x, y);

				prevX = x;
				prevY = y;
			}

		}

		@SuppressWarnings("unchecked")
		Pair<String, Float>[] wds = new Pair[]{
				Pair.of("N",0f),
				Pair.of("NE",45f),
				Pair.of("E",90f),
				Pair.of("SE",135f),
				Pair.of("S",180f),
				Pair.of("SW",225f),
				Pair.of("W",270f),
				Pair.of("NW",315f),
		}; 

		tsp.setColor(0, 0, 0);

		for(Pair<String, Float> wd:wds) {
			tsp.drawText(wd.a,(float) (float) (Math.sin((wd.b*2*Math.PI)/360)*75)+100, (float) (-Math.cos((wd.b*2*Math.PI)/360)*75)+100, PosHorizontal.CENTER, PosVerical.CENTER);

			tsp.drawLine(100, 100, (float) (Math.sin((wd.b*2*Math.PI)/360)*60)+100, (float) (-Math.cos((wd.b*2*Math.PI)/360)*60)+100);
		}

		tsp.fillCircle(100, 100, 30);


		/*tsp.drawText("N",(float) (float) (Math.sin((0*2*Math.PI)/360)*75)+100, (float) (-Math.cos((0*2*Math.PI)/360)*75)+100, PosHorizontal.CENTER, PosVerical.CENTER);
		tsp.drawText("NE",(float) (float) (Math.sin((45*2*Math.PI)/360)*75)+100, (float) (-Math.cos((45*2*Math.PI)/360)*75)+100, PosHorizontal.CENTER, PosVerical.CENTER);
		tsp.drawText("E",(float) (float) (Math.sin((90*2*Math.PI)/360)*75)+100, (float) (-Math.cos((90*2*Math.PI)/360)*75)+100, PosHorizontal.CENTER, PosVerical.CENTER);
		tsp.drawText("SE",(float) (float) (Math.sin((135*2*Math.PI)/360)*75)+100, (float) (-Math.cos((135*2*Math.PI)/360)*75)+100, PosHorizontal.CENTER, PosVerical.CENTER);
		tsp.drawText("S",(float) (float) (Math.sin((180*2*Math.PI)/360)*75)+100, (float) (-Math.cos((180*2*Math.PI)/360)*75)+100, PosHorizontal.CENTER, PosVerical.CENTER);
		tsp.drawText("SW",(float) (float) (Math.sin((225*2*Math.PI)/360)*75)+100, (float) (-Math.cos((225*2*Math.PI)/360)*75)+100, PosHorizontal.CENTER, PosVerical.CENTER);
		tsp.drawText("W",(float) (float) (Math.sin((270*2*Math.PI)/360)*75)+100, (float) (-Math.cos((270*2*Math.PI)/360)*75)+100, PosHorizontal.CENTER, PosVerical.CENTER);
		tsp.drawText("NW",(float) (float) (Math.sin((315*2*Math.PI)/360)*75)+100, (float) (-Math.cos((315*2*Math.PI)/360)*75)+100, PosHorizontal.CENTER, PosVerical.CENTER);*/

	}

	private static void setRange(TimeSeriesPainter tsp,String sensorName) {
		tsp.setColorScale("rainbow");
		switch(sensorName) {
		case "Ta_200":
		case "Ta_200_min":
		case "Ta_200_max":
		case "Ta_10":
		case "Tgnd":
		case "Trad":
			//tsp.setIndexedColorRange(-10, 30);
			tsp.setIndexedColorRange(-20, 45);
			break;
		case "Tsky":			
			tsp.setIndexedColorRange(-25, 25);
			break;
		case "Ts_5":
		case "Ts_10":
		case "Ts_20":
			tsp.setIndexedColorRange(-5, 25);
			break;			
		case "Ts_50":
			tsp.setIndexedColorRange(0, 20);
			break;
		case "Albedo":
			tsp.setIndexedColorRange(0.1f, 0.3f);
			break;
		case "rH_200":
		case "rH_200_min":
		case "rH_200_max":
		case "rh_1000":
		case "rh_2000":
		case "rh_3000":
		case "rh_3700":
		case "rh_4400":
			tsp.setIndexedColorRange(0, 100);
			break;
		case "SM_10":
		case "SM_10_2":
		case "SM_15":
		case "SM_20":
		case "SM_20_2":
		case "SM_30":
		case "SM_35":
		case "SM_40":
		case "SM_50":
			tsp.setIndexedColorRange(5, 60);
			break;
		case "B_01":
		case "B_02":
		case "B_03":
		case "B_04":
		case "B_05":
		case "B_06":
		case "B_07":
		case "B_08":
		case "B_09":
		case "B_10":
		case "B_11":
		case "B_12":
		case "B_13":
		case "B_14":
		case "B_15":
		case "B_16":
		case "B_17":
		case "B_18":
		case "B_19":
		case "B_20":
		case "B_21":
		case "B_22":
		case "B_23":
		case "B_24":
		case "B_25":
		case "B_26":
		case "B_27":
		case "B_28":
		case "B_29":
		case "B_30":
		case "Rainfall":
			tsp.setIndexedColorRange(0, 3);
			break;
		case "Fog":
			tsp.setIndexedColorRange(0, 0.3f);
			break;
		case "SWDR_300":
			tsp.setIndexedColorRange(0, 1000);
			break;
		case "SWUR_300":
			tsp.setIndexedColorRange(0, 200);
			break;
		case "LWDR_300":
			tsp.setIndexedColorRange(250, 450);
			break;
		case "LWUR_300":
			tsp.setIndexedColorRange(300, 520);
			break;			
		case "LWDR_3700":
		case "LWDR_4400":
			tsp.setIndexedColorRange(200, 450);
			break;
		case "LWUR_3700":
		case "LWUR_4400":			
			tsp.setIndexedColorRange(280, 500);
			break;
		case "PAR_200": //no data
		case "PAR_300":
			tsp.setIndexedColorRange(0, 2000);
			break;
		case "PAR_1000":
		case "PAR_2000":
			tsp.setIndexedColorRange(0, 500);
			break;			
		case "P_RT_NRT":
			//tsp.setIndexedColorRange(0, 0.2f);
			//tsp.setIndexedColorRange(0, 1);
			tsp.setIndexedColorRange(0, 3);
			break;
		case "P_container_RT":
		case "P_container_NRT":
			tsp.setIndexedColorRange(0, 600);
			break;
		case "Rn_300":
			tsp.setIndexedColorRange(-70, 700);
			break;
		case "WD":
			tsp.setIndexedColorRange(0, 360);
			tsp.setColorScale("round_rainbow");
			break;
		case "WV":
			tsp.setIndexedColorRange(0, 9);
			break;
		case "WV_gust":
			tsp.setIndexedColorRange(0, 20);
			break;						
		case "p_QNH":
			//tsp.setIndexedColorRange(980, 1040);
			tsp.setIndexedColorRange(800, 1040);
			break;
		case "P_RT_NRT_01": //few data?
		case "P_RT_NRT_02": //few data?
		case "F_RT_NRT_01": //few data?
		case "F_RT_NRT_02": //few data?
		case "T_RT_NRT_01": //few data?
		case "T_RT_NRT_02": //few data?
			tsp.setIndexedColorRange(0, 2);
			break;
		case "swdr_01": //range?
			tsp.setIndexedColorRange(0, 1200);
			break;
		case "swdr_02": //range?
			tsp.setIndexedColorRange(0, 30);
			break;
		case "par_01": //few data?
		case "par_02": //few data?			
			tsp.setIndexedColorRange(0, 2000);
			break;
		case "T_CNR":
			tsp.setIndexedColorRange(0, 35);
			break;
		case "p_200":
			tsp.setIndexedColorRange(820, 920);
			break;
		case "DecagonECH2O":
			tsp.setIndexedColorRange(300, 1000);
			break;
		case "sunshine":
			tsp.setIndexedColorRange(0, 1);
			break;			
		default:
			tsp.setIndexedColorRange(-10, 30);
		}		
	}
}
