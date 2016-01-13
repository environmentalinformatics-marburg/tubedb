package tsdb.util.gui;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;






import tsdb.util.TimeUtil;
import tsdb.util.gui.TimeSeriesPainter.PosHorizontal;
import tsdb.util.gui.TimeSeriesPainter.PosVerical;

public class TimeScale {

	public static enum TimeGranularity {
		MAX,
		HOUR,
		DAY,
		MONTH,
		YEAR
	}

	//private final static int minGap = 25;
	private final static int minGap = 10;
	private final static int minGapText = 25;

	private final double minTimestamp;
	private final double maxTimestamp;
	private final int minYear;
	private final int maxYear;

	private double diagramMinX;
	private double diagramMaxX;
	private double diagramTimestampFactor;

	private float lineY0;
	private float lineY1;
	private float posY;

	private int yearStep;

	private final TimeGranularity lowestGranularity;

	public TimeScale(double minTimestamp, double maxTimestamp, TimeGranularity lowestGranularity) {
		this.minTimestamp = minTimestamp;
		this.maxTimestamp = maxTimestamp;
		this.lowestGranularity = lowestGranularity;
		this.minYear = TimeUtil.oleMinutesToLocalDateTime((long)minTimestamp).getYear();
		this.maxYear = TimeUtil.oleMinutesToLocalDateTime((long)maxTimestamp).getYear();		
	}

	public int calcDiagramX(double timestamp) {
		return (int) (diagramMinX+((timestamp-minTimestamp)*diagramTimestampFactor));
	}

	public int calcTimeMark(int year,int month,int day,int hour, int minute) {
		long timestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, day, hour, minute));
		return calcDiagramX(timestamp);
	}	

	public void draw(TimeSeriesPainter tsp, double diagramMinX, double diagramMaxX, float posY, double diagramTimestampFactor, float lineY0, float lineY1) {
		this.diagramMinX = diagramMinX;
		this.diagramMaxX = diagramMaxX;
		this.diagramTimestampFactor = (diagramMaxX-diagramMinX)/(maxTimestamp-minTimestamp);
		this.lineY0 = lineY0;
		this.lineY1 = lineY1;
		this.posY = posY;
		yearStep = calcDiagramX(TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2001, 1, 1, 0, 0)))-calcDiagramX(TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2000, 1, 1, 0, 0)));
		/*if(yearStep/2>=minGap) {
			drawHalfYearScale(tsp);
		}*/
		if(lowestGranularity==TimeGranularity.MAX||lowestGranularity==TimeGranularity.HOUR) {
			drawHourScale(tsp);
		}
		if(lowestGranularity==TimeGranularity.MAX||lowestGranularity==TimeGranularity.HOUR||lowestGranularity==TimeGranularity.DAY) {
			drawDayScale(tsp);
		}
		if(lowestGranularity==TimeGranularity.MAX||lowestGranularity==TimeGranularity.HOUR||lowestGranularity==TimeGranularity.DAY||lowestGranularity==TimeGranularity.MONTH) {
			drawMonthScale(tsp);
		}
		if(lowestGranularity==TimeGranularity.MAX||lowestGranularity==TimeGranularity.HOUR||lowestGranularity==TimeGranularity.DAY||lowestGranularity==TimeGranularity.MONTH||lowestGranularity==TimeGranularity.YEAR) {
			drawYearScale(tsp);
		}
	}

	public void drawYearScale(TimeSeriesPainter tsp) {
		int year = minYear-1;
		while(year<=maxYear) {
			int mark = calcTimeMark(year,1,1,0,0);
			if(isInRange(mark)) {
				tsp.setColorXScaleYearLine();
				tsp.drawLine(mark, lineY0, mark, lineY1);
				tsp.setColorXScaleYearText();
				tsp.drawText(""+year, mark, posY, PosHorizontal.CENTER, PosVerical.TOP);
			}
			year++;
		}		
	}

	private boolean isInRange(int x) {
		return diagramMinX<=x&&x<=diagramMaxX;
	}

	private void drawMonthScale(TimeSeriesPainter tsp) {
		int year = minYear-1;
		while(year<=maxYear) {			
			for(int month=2;month<=12;month++) {
				drawMonth(tsp,year,month);
			}
			year++;
		}				
	}

	private void drawMonth(TimeSeriesPainter tsp,int year, int month) {
		int step;
		switch(month) {
		case 7:
			step = yearStep/2;
			break;
		case 4:
		case 10:
			step = yearStep/4;
			break;
		default:
			step = yearStep/12;
		}
		if(step>=minGap) {
			int mark = calcTimeMark(year,month,1,0,0);
			if(isInRange(mark)) {
				switch(month) {
				default:
					tsp.setColorXScaleMonthLine();
				}
				tsp.drawLine(mark, lineY0, mark, lineY1);
				if(step>=minGapText) {
					switch(month) {
					default:
						tsp.setColorXScaleMonthText();
					}
					tsp.drawText(getMonthText(month), mark, posY, PosHorizontal.CENTER, PosVerical.TOP);
				}
			}
		}

	}

	public static String getMonthText(int month) {
		switch(month) {
		case 1: return "jan";
		case 2: return "feb";
		case 3: return "mar";
		case 4: return "apr";
		case 5: return "may";
		case 6: return "jun";
		case 7: return "jul";
		case 8: return "aug";
		case 9: return "sep";
		case 10: return "oct";
		case 11: return "nov";
		case 12: return "dec";
		default: return "???";
		}
	}

	private void drawDayScale(TimeSeriesPainter tsp) {
		int year = minYear-1;
		while(year<=maxYear) {			
			for(int month=1;month<=12;month++) {
				//drawMonth(tsp,year,month);
				int maxDay = new GregorianCalendar(year, month-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH);
				for(int day=2;day<=maxDay;day++) {
					drawDay(tsp,year,month,day,maxDay);
				}
			}
			year++;
		}		
	}

	private void drawDay(TimeSeriesPainter tsp, int year, int month, int day, int maxDay) {
		int step;
		switch(day) {
		default:
			//step = yearStep/12/maxDay;
			step = yearStep/12/31;
		}		
		if(step>=minGap) {
			int mark = calcTimeMark(year,month,day,0,0);
			if(isInRange(mark)) {
				tsp.setColorXScaleDayLine();
				tsp.drawLine(mark, lineY0, mark, lineY1);
				if(step>=minGapText) {
					tsp.setColorXScaleDayText();
					tsp.drawText(""+day, mark, posY, PosHorizontal.CENTER, PosVerical.TOP);
				}				
			}
		}
	}

	private void drawHourScale(TimeSeriesPainter tsp) {
		int year = minYear-1;
		while(year<=maxYear) {			
			for(int month=1;month<=12;month++) {
				int maxDay = new GregorianCalendar(year, month-1, 1).getActualMaximum(Calendar.DAY_OF_MONTH);
				for(int day=1;day<=maxDay;day++) {
					for(int hour=1;hour<24;hour++) {
						//drawDay(tsp,year,month,day,maxDay);
						drawHour(tsp,year,month,day,hour);						
					}
				}
			}
			year++;
		}		
	}

	private void drawHour(TimeSeriesPainter tsp, int year, int month, int day, int hour) {
		int step;
		switch(hour) {		
		case 12:
			step = yearStep/12/31/2;
			break;
		case 6:
		case 18:
			step = yearStep/12/31/4;
			break;
		case 3:
		case 9:
		case 15:
		case 21:
			step = yearStep/12/31/8;
			break;			
		default:
			step = yearStep/12/31/24;
		}

		if(step>=minGap) {
			int mark = calcTimeMark(year,month,day,hour,0);
			if(isInRange(mark)) {				
				/*for(int minute=5;minute<60;minute+=5) {
					int mark2 = calcTimeMark(year,month,day,hour,minute);
					tsp.drawLine(mark2, lineY0, mark2, lineY1);
				}*/				
				tsp.setColorXScaleHourLine();
				tsp.drawLine(mark, lineY0, mark, lineY1);
				if(step>=minGapText) {
					tsp.setColorXScaleHourText();
					tsp.drawText(hour+"h", mark, posY, PosHorizontal.CENTER, PosVerical.TOP);
				}				
			}
		}
	}

}
