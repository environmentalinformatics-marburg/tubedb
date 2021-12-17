package tsdb.util.gui;

import static tsdb.util.AssumptionCheck.throwNull;
import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.List;


import org.tinylog.Logger;

import tsdb.component.SensorCategory;
import tsdb.util.AggregationInterval;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.Util;
import tsdb.util.gui.TimeScale.TimeGranularity;
import tsdb.util.gui.TimeSeriesPainter.PosHorizontal;
import tsdb.util.gui.TimeSeriesPainter.PosVerical;
import tsdb.util.iterator.TimestampSeries;

public class TimeSeriesDiagram {

	private static final int ELEMENT_INDEX_MIN = 0;
	private static final int ELEMENT_INDEX_MAX = 6;
	private static final int ELEMENT_INDEX_W_MIN = 1;
	private static final int ELEMENT_INDEX_W_MAX = 5;
	private static final int ELEMENT_INDEX_Q1 = 2;
	private static final int ELEMENT_INDEX_Q3 = 4;
	private static final int ELEMENT_INDEX_MEDIAN = 3;

	

	private static final float MIN_VALUE_RANGE = 0.01f;

	private final boolean boxplot;

	private final TimestampSeries timestampseries;
	private final AggregationInterval aggregationInterval;
	private final SensorCategory diagramType;

	private long aggregationTimeInterval;

	private float dataMinValue;
	private float dataMaxValue;
	private float dataValueRange;
	@SuppressWarnings("unused")
	private float dataCount;
	@SuppressWarnings("unused")
	private float dataSum;

	private long dataMinTimestamp;
	private long dataMaxTimestamp;
	@SuppressWarnings("unused")
	private long dataTimestampRange;

	private float borderTop = 5;
	private float borderBottom = 15;
	private float borderLeft = 50;
	private float borderRight = 5;

	private float diagramMinX;
	private float diagramMinY;
	private float diagramMaxX;
	private float diagramMaxY;
	private float diagramWidth;
	private float diagramHeigh;

	private double diagramMinTimestamp;
	private double diagramMaxTimestamp;
	private double diagramTimestampRange;
	private double diagramMinValue;
	private double diagramMaxValue;
	private double diagramValueRange;
	private double diagramTimestampFactor;
	private double diagramValueFactor;

	private boolean scale_right = true;

	private final AggregatedConnectionType aggregatedConnectionType;
	private final RawConnectionType rawConnectionType;
	private final AggregatedValueType aggregatedValue;
	private final RawValueType rawValue;

	/**
	 * 
	 * @param timestampseries
	 * @param aggregationInterval
	 * @param diagramType
	 * @param boxplot
	 * @param aggregatedConnectionType
	 * @param rawConnectionType
	 * @param aggregatedValue
	 * @param rawValue
	 * @param valueRange  null or min and max value for diagram y-axis
	 */
	public TimeSeriesDiagram(TimestampSeries timestampseries, AggregationInterval aggregationInterval, SensorCategory diagramType, boolean boxplot, AggregatedConnectionType aggregatedConnectionType, RawConnectionType rawConnectionType, AggregatedValueType aggregatedValue, RawValueType rawValue, float[] valueRange) {

		this.aggregatedConnectionType = aggregatedConnectionType;
		this.rawConnectionType = rawConnectionType;
		this.aggregatedValue = aggregatedValue;
		this.rawValue = rawValue;

		if(scale_right) {
			borderRight = 50;
		}

		throwNulls(timestampseries,aggregationInterval);
		this.boxplot = boxplot;
		this.timestampseries = timestampseries;
		this.aggregationInterval = aggregationInterval;
		this.diagramType = diagramType;

		aggregationTimeInterval = 60;
		switch(aggregationInterval) {
		case RAW:
			aggregationTimeInterval=365*24*60; //pre
			break;
		case HOUR:
			aggregationTimeInterval=60;
			break;
		case DAY:
			aggregationTimeInterval=1*24*60;
			break;
		case WEEK:
			aggregationTimeInterval=7*24*60;
			break;
		case MONTH:
			aggregationTimeInterval=30*24*60;//28*24*60;
			break;
		case YEAR:
			aggregationTimeInterval=365*24*60;
			break;
		default:
			Logger.warn("error in agg");
		}

		dataMinValue = Float.MAX_VALUE;
		dataMaxValue = -Float.MAX_VALUE;
		dataCount = 0f;
		dataSum = 0f;

		if(!boxplot) {
			long prev = 0;
			for(TsEntry entry:timestampseries) {
				float value = entry.data[0];
				if(!Float.isNaN(value)) {
					if(value<dataMinValue) {
						dataMinValue = value;						
					}
					if(value>dataMaxValue) {
						dataMaxValue = value;						
					}
					long diff = entry.timestamp-prev;
					if(aggregationInterval==AggregationInterval.RAW && diff>0&&diff<aggregationTimeInterval) {
						aggregationTimeInterval = diff;
					}
					prev = entry.timestamp;
					dataCount++;
					dataSum += value;
				}
			}
		} else {
			long prev = 0;
			for(TsEntry entry:timestampseries) {
				float valueMin = entry.data[ELEMENT_INDEX_MIN];
				if(!Float.isNaN(valueMin)) {
					if(valueMin < dataMinValue) {
						dataMinValue = valueMin;						
					}
					float valueMax = entry.data[ELEMENT_INDEX_MAX];
					if(valueMax > dataMaxValue) {
						dataMaxValue = valueMax;						
					}
					long diff = entry.timestamp-prev;
					if(aggregationInterval == AggregationInterval.RAW && diff > 0&& diff < aggregationTimeInterval) {
						aggregationTimeInterval = diff;
					}
					prev = entry.timestamp;
					dataCount++;
					dataSum += valueMin;
				}
			}
			/*long prev = 0;
			for(TsEntry entry:timestampseries) {
				float value = entry.data[2];
				if(!Float.isNaN(value)) {
					if(value<dataMinValue) {
						dataMinValue = value;						
					}
					if(value>dataMaxValue) {
						dataMaxValue = value;						
					}
					long diff = entry.timestamp-prev;
					if(aggregationInterval==AggregationInterval.RAW && diff>0&&diff<aggregationTimeInterval) {
						aggregationTimeInterval = diff;
					}
					prev = entry.timestamp;
					dataCount++;
					dataSum += value;
				}
			}*/
			/*long prev = 0;
			for(TsEntry entry:timestampseries) {
				float valueMin = entry.data[1];
				if(!Float.isNaN(valueMin)) {
					if(valueMin<dataMinValue) {
						dataMinValue = valueMin;						
					}
					float valueMax = entry.data[3];
					if(valueMax>dataMaxValue) {
						dataMaxValue = valueMax;						
					}
					long diff = entry.timestamp-prev;
					if(aggregationInterval==AggregationInterval.RAW && diff>0&&diff<aggregationTimeInterval) {
						aggregationTimeInterval = diff;
					}
					prev = entry.timestamp;
					dataCount++;
					dataSum += valueMin;
				}
			}*/
		}
		
		if(valueRange != null) {
			dataMinValue = Math.min(valueRange[0], valueRange[1]);
			dataMaxValue = Math.max(valueRange[0], valueRange[1]);
		}

		dataValueRange = dataMaxValue - dataMinValue;

		if(dataValueRange > MIN_VALUE_RANGE) {
			diagramMinValue = dataMinValue;
			diagramMaxValue = dataMaxValue;			
		} else {
			diagramMinValue = dataMinValue - (dataMinValue % MIN_VALUE_RANGE) - MIN_VALUE_RANGE;
			diagramMaxValue = diagramMinValue + 3 * MIN_VALUE_RANGE;
		}


		diagramValueRange = diagramMaxValue-diagramMinValue;

		dataMinTimestamp = timestampseries.getFirstTimestamp();
		dataMaxTimestamp = timestampseries.getLastTimestamp();
		dataTimestampRange = dataMaxTimestamp-dataMinTimestamp;


		diagramMinTimestamp = dataMinTimestamp;
		diagramMaxTimestamp = dataMaxTimestamp;
		if(aggregationTimeInterval>0) {
			diagramMaxTimestamp += aggregationTimeInterval-1;
		}
		Logger.trace(TimeUtil.oleMinutesToText(dataMaxTimestamp)+"  "+TimeUtil.oleMinutesToText((long) diagramMaxTimestamp));
		diagramTimestampRange = diagramMaxTimestamp-diagramMinTimestamp;
	}

	public TimestampSeries getTimeStampSeries() {
		return timestampseries;
	}

	public int calcDiagramX(double timestamp) {
		return (int) (diagramMinX+((timestamp-diagramMinTimestamp)*diagramTimestampFactor));
	}

	public long calcTimestamp(double posX) {
		return (long) (diagramMinTimestamp+((posX - diagramMinX)/diagramTimestampFactor));
	}

	public int calcDiagramY(double value) {
		return (int) (diagramMaxY-((value-diagramMinValue)*diagramValueFactor));
	}

	public double calcValue(double posY) {
		return ((diagramMaxY-posY)/diagramValueFactor)+diagramMinValue;
	}

	public float[] getDiagramXMinMax() {
		return new float[]{diagramMinX,diagramMaxX};
	}

	private static class ValueLine {
		public final float x0;
		public final float x1;
		public final float y;
		public ValueLine(float x0, float x1, float y) {
			this.x0 = x0;
			this.x1 = x1;
			this.y = y;
		}
	}

	private static class ConnectLine {
		public final float x;
		public final float y0;
		public final float y1;
		public ConnectLine(float x, float y0, float y1) {
			this.x = x;
			this.y0 = y0;
			this.y1 = y1;
		}
	}

	public static class RawPoint {
		public final float x;
		public final float y;
		public RawPoint(float x, float y) {
			this.x = x;
			this.y = y;
		}

		public RawPoint scale(float factor) {
			return new RawPoint(factor * x, factor * y);
		}

		public RawPoint plus(RawPoint point) {
			return new RawPoint(x + point.x, y + point.y);
		}

		public RawPoint plus(float factor, RawPoint point) {
			return new RawPoint(x + factor * point.x, y + factor * point.y);
		}

		public RawPoint minus(RawPoint point) {
			return new RawPoint(x - point.x, y - point.y);
		}

		public RawPoint minus(float factor, RawPoint point) {
			return new RawPoint(x - factor * point.x, y - factor * point.y);
		}
	}

	private static class RawConnect {
		public final float x0;
		public final float y0;
		public final float x1;
		public final float y1;
		public RawConnect(float x0, float y0, float x1, float y1) {
			this.x0 = x0;
			this.y0 = y0;
			this.x1 = x1;
			this.y1 = y1;
		}
	}

	public void draw(TimeSeriesPainter tsp) {
		draw(tsp, null);
	}

	public void draw(TimeSeriesPainter tsp, TimestampSeries compareTs) {
		throwNull(tsp);

		diagramMinX = tsp.getMinX()+borderLeft;
		diagramMinY = tsp.getMinY()+borderTop;
		diagramMaxX = tsp.getMaxX()-borderRight;
		diagramMaxY = tsp.getMaxY()-borderBottom;
		diagramWidth = diagramMaxX-diagramMinX;
		diagramHeigh = diagramMaxY-diagramMinY;



		diagramTimestampFactor = ((double)diagramWidth)/((double)diagramTimestampRange);
		diagramValueFactor = ((double)diagramHeigh)/((double)diagramValueRange);

		tsp.setLineStyleDotted();

		drawYScale(tsp);

		TimeGranularity lowestGranularity = TimeGranularity.MAX;
		switch(aggregationInterval) {
		case RAW:
			lowestGranularity = TimeGranularity.MAX;
			break;
		case HOUR:
			lowestGranularity = TimeGranularity.HOUR;
			break;
		case DAY:
			lowestGranularity = TimeGranularity.DAY;
			break;
		case WEEK:
			lowestGranularity = TimeGranularity.DAY;
			break;
		case MONTH:
			lowestGranularity = TimeGranularity.MONTH;
			break;
		case YEAR:
			lowestGranularity = TimeGranularity.YEAR;
			break;
		default:
			Logger.warn("error in agg");
		}


		TimeScale timescale = new TimeScale(diagramMinTimestamp,diagramMaxTimestamp, lowestGranularity);		
		timescale.draw(tsp, diagramMinX, diagramMaxX, diagramMaxY+1, diagramTimestampFactor,diagramMinY,diagramMaxY+3);
		tsp.setLineStyleSolid();
		drawAxis(tsp);



		if(boxplot) {
			drawBoxplot(tsp);
		} else {
			if(compareTs!=null) {
				drawGraph(tsp,compareTs,false, aggregatedConnectionType, rawConnectionType);
			}
			drawGraph(tsp,timestampseries,true, aggregatedConnectionType, rawConnectionType);
		}


		tsp.setColor(150, 150, 150);
		int start_year = TimeUtil.oleMinutesToLocalDateTime(timestampseries.getFirstTimestamp()).getYear();
		try {
			tsp.setFontSmall();
			tsp.drawText(""+start_year+"", tsp.getMinX(), tsp.getMaxY(), PosHorizontal.LEFT, PosVerical.BOTTOM);
		} finally {
			tsp.setFontDefault();
		}
	}

	public long getDataMinTimestamp() {
		return dataMinTimestamp;
	}

	public long getDataMaxTimestamp() {
		return dataMaxTimestamp;
	}

	public double getDiagramMinTimestamp() {
		return diagramMinTimestamp;
	}

	public double getDiagramMaxTimestamp() {
		return diagramMaxTimestamp;
	}

	public void setDiagramTimestampRange(double min, double max) {
		diagramMinTimestamp = min;
		diagramMaxTimestamp = max;
		diagramTimestampRange = max-min;
	}

	public float getDataMinValue() {
		return dataMinValue;
	}

	public float getDataMaxValue() {
		return dataMaxValue;
	}

	public double getDiagramMinValue() {
		return diagramMinValue;
	}

	public double getDiagramMaxValue() {
		return diagramMaxValue;
	}

	public void setDiagramValueRange(double min, double max) {
		diagramMinValue = min;
		diagramMaxValue = max;
		diagramValueRange = max-min;
	}

	public void fitDiagramValueRangeToDiagramTimestampRange() {

		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;		

		for(TsEntry entry:timestampseries) {
			if(entry.timestamp<diagramMinTimestamp) {
				continue;
			}
			if(entry.timestamp>diagramMaxTimestamp) {
				break;
			}
			float value = entry.data[0];
			if(!Float.isNaN(value)) {
				if(value<min) {
					min = value;						
				}
				if(value>max) {
					max = value;						
				}
			}
		}

		setDiagramValueRange(min,max);
	}

	private void drawBoxplot(TimeSeriesPainter tsp) { // 0:min 1:dmin 2:q1 3:median 4:q3 5:dmax 6:max

		/*switch(diagramType) {
		case TEMPERATURE:
			tsp.setColor(180,220,180);
			break;
		case WATER:
			tsp.setColor(180,220,180);
			break;
		case OTHER:
			tsp.setColor(180,220,180);
			break;
		default:
			Logger.error("unknown diagram type: "+diagramType);
		}*/

		tsp.setColor(100,220,100);

		for(TsEntry entry:timestampseries) {
			if(entry.timestamp<diagramMinTimestamp) {
				continue;
			}			

			long timestamp = entry.timestamp;
			float valueMin = entry.data[ELEMENT_INDEX_MIN];
			if(!Float.isNaN(valueMin)) {
				float valueMax = entry.data[ELEMENT_INDEX_MAX];
				int x0 = calcDiagramX(timestamp);
				int x1 = calcDiagramX(timestamp+aggregationTimeInterval);
				int y0 = calcDiagramY(valueMin);
				int y1 = calcDiagramY(valueMax);
				tsp.fillRect(x0, y1, x1, y0);
			}

			if(entry.timestamp>diagramMaxTimestamp) {
				break;
			}

		}



		switch(diagramType) {
		case TEMPERATURE:
			tsp.setColor(220,180,180);
			break;
		case WATER:
			tsp.setColor(180,180,220);
			break;
		case OTHER:
			tsp.setColor(220,220,220);
			break;
		default:
			Logger.error("unknown diagram type: "+diagramType);
		}

		for(TsEntry entry:timestampseries) {
			if(entry.timestamp<diagramMinTimestamp) {
				continue;
			}			

			long timestamp = entry.timestamp;
			float valueDMin = entry.data[ELEMENT_INDEX_W_MIN];
			if(!Float.isNaN(valueDMin)) {
				float valueDMax = entry.data[ELEMENT_INDEX_W_MAX];
				int x0 = calcDiagramX(timestamp);
				int x1 = calcDiagramX(timestamp+aggregationTimeInterval);
				int y0 = calcDiagramY(valueDMin);
				int y1 = calcDiagramY(valueDMax);
				tsp.fillRect(x0, y1, x1, y0);
			}

			if(entry.timestamp>diagramMaxTimestamp) {
				break;
			}

		}

		switch(diagramType) {
		case TEMPERATURE:
			tsp.setColor(220, 100, 100);
			break;
		case WATER:
			tsp.setColor(120, 120, 220);
			break;
		case OTHER:
			tsp.setColor(160, 160, 160);
			break;
		default:
			Logger.error("unknown diagram type: "+diagramType);
		}		

		//tsp.setColorValueLineTemperature();

		for(TsEntry entry:timestampseries) {
			if(entry.timestamp<diagramMinTimestamp) {
				continue;
			}			

			long timestamp = entry.timestamp;
			float valueMin = entry.data[ELEMENT_INDEX_Q1];
			if(!Float.isNaN(valueMin)) {
				float valueMax = entry.data[ELEMENT_INDEX_Q3];
				int x0 = calcDiagramX(timestamp);
				int x1 = calcDiagramX(timestamp+aggregationTimeInterval);
				int y0 = calcDiagramY(valueMin);
				int y1 = calcDiagramY(valueMax);
				tsp.fillRect(x0, y1, x1, y0);
			}

			if(entry.timestamp>diagramMaxTimestamp) {
				break;
			}

		}

		tsp.setColorValueLineUnknown();
		switch(diagramType) {
		case TEMPERATURE:
			tsp.setColor(100, 0, 0);
			break;
		case WATER:
			tsp.setColor(0, 0, 100);
			break;
		case OTHER:
			tsp.setColor(30, 30, 30);
			break;
		default:
			Logger.error("unknown diagram type: "+diagramType);
		}



		for(TsEntry entry:timestampseries) {
			if(entry.timestamp<diagramMinTimestamp) {
				continue;
			}			

			long timestamp = entry.timestamp;
			float valueMed = entry.data[ELEMENT_INDEX_MEDIAN];
			if(!Float.isNaN(valueMed)) {
				int x0 = calcDiagramX(timestamp);
				int x1 = calcDiagramX(timestamp+aggregationTimeInterval);
				int y = calcDiagramY(valueMed);
				tsp.drawLine(x0, y, x1, y);
			}

			if(entry.timestamp>diagramMaxTimestamp) {
				break;
			}

		}
	}


	private void drawGraph(TimeSeriesPainter tsp, TimestampSeries ts, boolean isPrimary, AggregatedConnectionType aggregatedConnectionType, RawConnectionType rawConnectionType) {

		if(aggregationInterval != AggregationInterval.RAW) { // aggregated
			boolean hasPrev = false;
			float prevY = 0;
			List<ValueLine> valueLineList = new ArrayList<ValueLine>(ts.entryList.size());
			List<ConnectLine> connectLineList = new ArrayList<ConnectLine>(ts.entryList.size());
			List<List<RawPoint>> curveList = new ArrayList<List<RawPoint>>();

			List<RawPoint> currentCurve = new ArrayList<RawPoint>();

			for(TsEntry entry:ts) {
				if(entry.timestamp<diagramMinTimestamp) {
					continue;
				}

				long timestamp = entry.timestamp;
				float value = entry.data[0];
				if(Float.isNaN(value)) {
					hasPrev = false;
					if(currentCurve.size() == 0) {
						// nothing
					} else if (currentCurve.size() == 1) {
						currentCurve.clear();
					} else {
						curveList.add(currentCurve);
						currentCurve = new ArrayList<RawPoint>();
					}
				} else {
					int x0 = calcDiagramX(timestamp);
					int x1 = calcDiagramX(timestamp + aggregationTimeInterval);
					int y = calcDiagramY(value);
					valueLineList.add(new ValueLine(x0, x1, y));
					if(hasPrev) {
						connectLineList.add(new ConnectLine(x0, prevY, y));
					}
					prevY = y;
					hasPrev = true;
					if(currentCurve == null) {
						currentCurve = new ArrayList<RawPoint>();
					}
					currentCurve.add(new RawPoint((x0 + x1) / 2, y));
				}

				if(entry.timestamp>diagramMaxTimestamp) {
					break;
				}

			}
			if(currentCurve.size() > 1) {
				curveList.add(currentCurve);
			}

			switch(diagramType) {
			case TEMPERATURE:
				drawDiagramTemperature(tsp, valueLineList, connectLineList, isPrimary, curveList, aggregatedConnectionType);
				break;
			case WATER:
				drawDiagramWater(tsp, valueLineList, connectLineList, isPrimary);
				break;
			case OTHER:
				//if(aggregationInterval==AggregationInterval.RAW) {

				//} else {
				drawDiagramUnknown(tsp, valueLineList, connectLineList, isPrimary, curveList, aggregatedConnectionType);
				//}
				break;
			default:
				Logger.error("unknown diagram type: "+diagramType);
			}
		} else { // raw

			ArrayList<RawPoint> pointList = new ArrayList<RawPoint>();
			ArrayList<RawConnect> connectList = new ArrayList<RawConnect>();

			boolean hasPrev = false;
			float prevX = 0;
			float prevY = 0;
			for(TsEntry entry:ts) {

				if(entry.timestamp<diagramMinTimestamp) {
					continue;
				}

				long timestamp = entry.timestamp;
				float value = entry.data[0];

				if(Float.isNaN(value)) {
					hasPrev = false;
				} else {

					long check = TimeUtil.dateTimeToOleMinutes(TimeUtil.oleMinutesToLocalDateTime(timestamp));
					if(check!=timestamp) {
						throw new RuntimeException();
					}

					int x = calcDiagramX(timestamp);
					int y = calcDiagramY(value);
					pointList.add(new RawPoint(x, y));
					if(hasPrev) {
						connectList.add(new RawConnect(prevX, prevY, x, y));
					}
					prevX = x;
					prevY = y;
					hasPrev = true;
				}

				if(entry.timestamp>diagramMaxTimestamp) {
					break;
				}
			}			
			drawDiagramRaw(tsp, pointList, connectList, isPrimary, rawConnectionType);
		}
	}

	public static enum AggregatedConnectionType {
		NONE,
		STEP,
		LINE,
		CURVE;

		public static AggregatedConnectionType parse(String text) {
			if(text==null) {
				Logger.warn("aggregation connection type text null");
				return null;
			}
			switch(text.trim().toLowerCase()) {
			case "none":
				return NONE;
			case "step":
				return STEP;
			case "line":
				return LINE;
			case "curve":
				return CURVE;
			default:
				Logger.warn("aggregation connection type unknown: "+text);
				return null;
			}		
		}
	}

	public static enum RawConnectionType {
		NONE,
		LINE,
		CURVE;

		public static RawConnectionType parse(String text) {
			if(text==null) {
				Logger.warn("raw connection type text null");
				return null;
			}
			switch(text.trim().toLowerCase()) {
			case "none":
				return NONE;
			case "line":
				return LINE;
			case "curve":
				return CURVE;
			default:
				Logger.warn("raw connection type unknown: "+text);
				return null;
			}		
		}
	}

	public static enum AggregatedValueType {
		NONE,
		POINT,
		LINE;

		public static AggregatedValueType parse(String text) {
			if(text==null) {
				Logger.warn("aggregation value type text null");
				return null;
			}
			switch(text.trim().toLowerCase()) {
			case "none":
				return NONE;
			case "point":
				return POINT;				
			case "line":
				return LINE;
			default:
				Logger.warn("aggregation value type unknown: "+text);
				return null;
			}		
		}
	}

	public static enum RawValueType {
		NONE,
		POINT;

		public static RawValueType parse(String text) {
			if(text==null) {
				Logger.warn("raw value type text null");
				return null;
			}
			switch(text.trim().toLowerCase()) {
			case "none":
				return NONE;
			case "point":
				return POINT;
			default:
				Logger.warn("raw value type unknown: "+text);
				return null;
			}		
		}
	}

	private void drawDiagramTemperature(TimeSeriesPainter tsp, List<ValueLine> valueLineList, List<ConnectLine> connectLineList, boolean isPrimary, List<List<RawPoint>> curveList, AggregatedConnectionType connectionType) {
		if(isPrimary) {
			tsp.setColorConnectLineTemperature();
		} else {
			tsp.setColorConnectLineTemperatureSecondary();	
		}
		switch(connectionType) {
		case NONE:
			// nothing
			break;
		case STEP:
			for(ConnectLine connectLine:connectLineList) {
				tsp.drawLine(connectLine.x, connectLine.y0, connectLine.x, connectLine.y1);
			}
			break;
		case LINE:
			for(List<RawPoint> curve : curveList) {
				tsp.drawPointsAsLineString(curve);
			}
			break;
		case CURVE:
			for(List<RawPoint> curve : curveList) {
				tsp.drawPointsAsCurve(curve);
			}
			break;
		default:
			throw new RuntimeException("unknown connection type: " + connectionType);
		}

		if(isPrimary) {
			tsp.setColorValueLineTemperature();
		} else {
			tsp.setColorValueLineTemperatureSecondary();	
		}

		switch(aggregatedValue) {
		case NONE:
			// nothing
			break;
		case POINT:
			for(ValueLine valueLine : valueLineList) {
				float x = (valueLine.x0 + valueLine.x1) / 2;
				tsp.drawLine(x, valueLine.y, x, valueLine.y);
			}
			break;			
		case LINE:
			for(ValueLine valueLine : valueLineList) {
				tsp.drawLine(valueLine.x0, valueLine.y, valueLine.x1, valueLine.y);
			}
			break;
		default:
			throw new RuntimeException("unknown aggregated value type: " + aggregatedValue);
		}
	}

	private void drawDiagramWater(TimeSeriesPainter tsp, List<ValueLine> valueLineList, List<ConnectLine> connectLineList, boolean isPrimary) {
		if(isPrimary) {
			tsp.setColorRectWater();
		} else {
			tsp.setColorRectWaterSecondary();	
		}
		for(ValueLine valueLine:valueLineList) {
			tsp.fillRect(valueLine.x0, valueLine.y, valueLine.x1, diagramMaxY);
		}
	}

	private void drawDiagramUnknown(TimeSeriesPainter tsp, List<ValueLine> valueLineList, List<ConnectLine> connectLineList, boolean isPrimary, List<List<RawPoint>> curveList, AggregatedConnectionType connectionType) {
		if(isPrimary) {
			tsp.setColorConnectLineUnknown();
		} else {
			tsp.setColorConnectLineUnknownSecondary();	
		}
		switch(connectionType) {
		case NONE:
			// nothing
			break;
		case STEP:
			for(ConnectLine connectLine:connectLineList) {
				tsp.drawLine(connectLine.x, connectLine.y0, connectLine.x, connectLine.y1);
			}
			break;
		case LINE:
			for(List<RawPoint> curve : curveList) {
				tsp.drawPointsAsLineString(curve);
			}
			break;
		case CURVE:
			for(List<RawPoint> curve : curveList) {
				tsp.drawPointsAsCurve(curve);
			}
			break;
		default:
			throw new RuntimeException("unknown connection type: " + connectionType);
		}

		if(isPrimary) {
			tsp.setColorValueLineUnknown();
		} else {
			tsp.setColorValueLineUnknownSecondary();	
		}

		switch(aggregatedValue) {
		case NONE:
			// nothing
			break;
		case POINT:
			for(ValueLine valueLine : valueLineList) {
				float x = (valueLine.x0 + valueLine.x1) / 2;
				tsp.drawLine(x, valueLine.y, x, valueLine.y);
			}
			break;			
		case LINE:
			for(ValueLine valueLine:valueLineList) {
				tsp.drawLine(valueLine.x0,valueLine.y,valueLine.x1,valueLine.y);
			}
			break;
		default:
			throw new RuntimeException("unknown aggregated value type: " + aggregatedValue);
		}
	}

	private void drawDiagramRaw(TimeSeriesPainter tsp, ArrayList<RawPoint> pointList, ArrayList<RawConnect> connectList, boolean isPrimary, RawConnectionType connectionType) {
		if(isPrimary) {
			tsp.setColorConnectLineUnknown();
		} else {
			tsp.setColorConnectLineUnknownSecondary();	
		}

		switch(connectionType) {
		case NONE:
			// nothing
			break;
		case LINE:
			for(RawConnect r:connectList) {
				tsp.drawLine(r.x0, r.y0, r.x1, r.y1);
			}
			break;
		case CURVE:
			tsp.drawPointsAsCurve(pointList);
			break;
		default:
			throw new RuntimeException("unknown connection type: " + connectionType);
		}

		if(isPrimary) {
			tsp.setColorValueLineUnknown();
		} else {
			tsp.setColorValueLineUnknownSecondary();	
		}

		switch(rawValue) {
		case NONE:
			// nothing
			break;
		case POINT:
			for(RawPoint p:pointList) {
				tsp.drawLine(p.x,p.y,p.x,p.y);
			}
			break;
		default:
			throw new RuntimeException("unknown raw value type: " + rawValue);
		}
	}

	private void drawAxis(TimeSeriesPainter tsp) {
		tsp.setColorAxisLine();
		tsp.drawLine(diagramMinX , diagramMinY, diagramMaxX, diagramMinY); //x-Aches
		tsp.drawLine(diagramMinX , diagramMaxY, diagramMaxX, diagramMaxY); //x-Grenze
		tsp.drawLine(diagramMaxX , diagramMinY, diagramMaxX, diagramMaxY); // y-Achse
		tsp.drawLine(diagramMinX , diagramMinY, diagramMinX, diagramMaxY); // y-Grenze

		int zeroY = calcDiagramY(0f);
		if(diagramMinY<=zeroY&&zeroY<=diagramMaxY) {
			tsp.setLineStyleDotted();
			tsp.setColorZeroLine();
			tsp.drawLine(diagramMinX , zeroY, diagramMaxX, zeroY);
			tsp.setLineStyleSolid();
		}		
	}

	private void drawYScale(TimeSeriesPainter tsp) {
		int maxLines = (int) (diagramHeigh/17);
		double minLineStep = diagramValueRange/maxLines;
		double logMinLineStep = Math.pow(10d, Math.ceil(Math.log10(minLineStep)));
		double lineStep = logMinLineStep;
		if((diagramValueRange/(lineStep/5))<=maxLines) {
			lineStep /= 5d;
		} else if((diagramValueRange/(lineStep/2))<=maxLines) {
			lineStep /= 2d;
		}
		double mod = diagramMinValue%lineStep;
		float lineStart = (float) (mod>0d?diagramMinValue+lineStep-mod:diagramMinValue-mod);
		drawYScale(tsp,lineStart,lineStep);		
	}

	private void drawYScale(TimeSeriesPainter tsp, float lineStart, double lineStep) {
		float line = lineStart;
		int debug_counter = 0; //TODO remove
		while(line<=diagramMaxValue && debug_counter<100) {			
			int y = calcDiagramY(line);
			tsp.setColorYScaleLine();
			tsp.drawLine(diagramMinX-1 , y, diagramMaxX, y);			
			String valueText;
			PosHorizontal posType = PosHorizontal.RIGHT;
			float pos = diagramMinX-3;
			float absLine = Math.abs(line);
			if(absLine>99999) {
				posType = PosHorizontal.LEFT;
				pos = diagramMinX-borderLeft;
				valueText = Util.doubleToString0(line);
			} else if(lineStep>=1d||absLine>9999) {
				valueText = Util.doubleToString0(line);
			} else if(lineStep>=0.1d||absLine>999) {
				valueText = Util.doubleToString1(line);
			} else if(lineStep>=0.01d||absLine>99) {
				valueText = Util.doubleToString2(line);
			} else if(lineStep>=0.001d||absLine>9) {
				valueText = Util.doubleToString3(line);
			} else if(lineStep>=0.0001d) {
				valueText = Util.doubleToString4(line);								
			} else {
				posType = PosHorizontal.LEFT;
				pos = diagramMinX-borderLeft;
				valueText = Util.doubleToStringFull(line);
			}			
			tsp.setColorYScaleText();
			tsp.drawText(valueText,pos,y,posType,PosVerical.CENTER);
			if(scale_right) {
				tsp.drawText(valueText,diagramMaxX+2,y,PosHorizontal.LEFT,PosVerical.CENTER); //big numbers get clipped
				//tsp.drawText(valueText,diagramMaxX+borderRight,y,PosHorizontal.RIGHT,PosVerical.CENTER); //no clipping
			}
			line+=lineStep;
			debug_counter++;
		}		
	}

}
