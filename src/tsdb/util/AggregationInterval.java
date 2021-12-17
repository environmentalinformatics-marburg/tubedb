package tsdb.util;


import org.tinylog.Logger;

import tsdb.util.TsSchema.Aggregation;

public enum AggregationInterval {
	RAW,
	HOUR, 
	DAY, 
	WEEK, 
	MONTH, 
	YEAR;
	
	
	
	public static AggregationInterval getBaseAggregation() {
		return HOUR;
	}
	
	public String getText() {
		switch(this) {
		case RAW:
			return "raw";
		case HOUR:
			return "hour";
		case DAY:
			return "day";
		case WEEK:
			return "week";
		case MONTH:
			return "month";
		case YEAR:
			return "year";
		default:
			Logger.warn("aggregation unknown");
			return "unknown";
		}		
	}
	
	public Aggregation toAggregation() {
		switch(this) {
		case RAW:
			return Aggregation.NO;
		case HOUR:
			return Aggregation.CONSTANT_STEP;
		case DAY:
			return Aggregation.CONSTANT_STEP;
		case WEEK:
			return Aggregation.CONSTANT_STEP;
		case MONTH:
			return Aggregation.MONTH;
		case YEAR:
			return Aggregation.YEAR;
		default:
			Logger.warn("aggregation unknown");
			return Aggregation.NO;
		}			
	}
	
	public int toTimeStep() {
		switch(this) {
		case RAW:
			return TsSchema.NO_CONSTANT_TIMESTEP;
		case HOUR:
			return 60;
		case DAY:
			return 24*60;
		case WEEK:
			return 7*24*60;
		case MONTH:
			return TsSchema.NO_CONSTANT_TIMESTEP;
		case YEAR:
			return TsSchema.NO_CONSTANT_TIMESTEP;
		default:
			Logger.warn("aggregation unknown");
			return TsSchema.NO_CONSTANT_TIMESTEP;
		}			
	}
	
	public static AggregationInterval parse(String text) {
		if(text==null) {
			Logger.warn("aggregation text null");
			return null;
		}
		switch(text.trim().toLowerCase()) {
		case "raw":
		case "none":
			return AggregationInterval.RAW;
		case "hour":
			return AggregationInterval.HOUR;
		case "day":
			return AggregationInterval.DAY;
		case "week":
			return AggregationInterval.WEEK;
		case "month":
			return AggregationInterval.MONTH;
		case "year":
			return AggregationInterval.YEAR;
		default:
			Logger.warn("aggregation unknown: "+text);
			return null;
		}		
	}
	
	public boolean isDay() {
		return this==AggregationInterval.DAY;
	}
	
	public boolean isWeek() {
		return this==AggregationInterval.WEEK;
	}
	
	public boolean isMonth() {
		return this==AggregationInterval.MONTH;
	}
	
	public boolean isYear() {
		return this==AggregationInterval.YEAR;
	}
	
	public boolean isDayOrHigher() {
		return isDay() || isWeek() || isMonth() || isYear();
	}
	
	
}