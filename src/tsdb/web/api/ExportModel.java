package tsdb.web.api;

import java.time.LocalDateTime;


import org.tinylog.Logger;

import tsdb.component.Region;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.Pair;
import tsdb.util.TimeUtil;

public class ExportModel{

	public enum TimespanType{
		ALL,YEAR,YEARS,DATES;

		public static TimespanType parseText(String text) {
			if(text==null) {
				Logger.warn("unknown TimespanType null");
				return ALL;
			}
			switch(text.toLowerCase()) {
			case "all":
				return ALL;
			case "year":
				return YEAR;
			case "years":
				return YEARS;	
			case "dates":
				return DATES;					
			default:
				Logger.warn("unknown TimespanType: "+text);
				return ALL;
			}
		}
		public String toText() {
			switch(this) {
			case ALL:
				return "all";
			case YEAR:
				return "year";
			case YEARS:
				return "years";		
			case DATES:
				return "dates";							
			default:
				Logger.warn("unknown TimespanType: "+this);
				return "all";
			}
		}
	}
	
	public enum SpatialAggregation {
		SEPARATE, AGGREGATED, SEPARATE_AND_AGGREGATED;

		public static SpatialAggregation parseText(String text) {
			if(text==null) {
				Logger.warn("unknown SpatialAggregation null");
				return SEPARATE;
			}
			switch(text.toLowerCase()) {
			case "separate":
				return SEPARATE;
			case "aggregated":
				return AGGREGATED;
			case "separate_and_aggregated":
				return SEPARATE_AND_AGGREGATED;	
			default:
				Logger.warn("unknown SpatialAggregation: "+text);
				return SEPARATE;
			}
		}
		
		public String toText() {
			switch(this) {
			case SEPARATE:
				return "separate";
			case AGGREGATED:
				return "aggregated";
			case SEPARATE_AND_AGGREGATED:
				return "separate_and_aggregated";					
			default:
				Logger.warn("unknown SpatialAggregation: " + this);
				return "separate plots";
			}
		}

		boolean isSeparate() {
			return this == SEPARATE || this == SEPARATE_AND_AGGREGATED;
		}
		
		boolean isAggregated() {
			return this == AGGREGATED || this == SEPARATE_AND_AGGREGATED;
		}
	}

	public String[] plots;
	public String[] sensors;
	public boolean interpolate;
	public boolean desc_sensor;
	public boolean desc_plot;
	public boolean desc_settings;
	public boolean allinone;
	public AggregationInterval aggregationInterval;
	public DataQuality quality;
	public Region region;
	public boolean col_plotid;
	public boolean col_timestamp;
	public boolean col_datetime;
	public boolean col_year;
	public boolean col_month;
	public boolean col_day;
	public boolean col_hour;
	public boolean col_day_of_year;
	public boolean col_qualitycounter;
	public boolean write_header;
	public SpatialAggregation spatial_aggregation;
	public boolean casted;

	public TimespanType timespanType;	
	public int timespanYear;	
	public int timespanYearsFrom;
	public int timespanYearsTo;
	public String timespanDatesFrom;
	public String timespanDatesTo;

	public ExportModel() {
		reset();
	}
	
	public void reset() {
		this.plots = new String[]{"plot1","plot2","plot3"};
		this.sensors = new String[]{"sensor1","sensor2","sensor3","sensor4"};
		this.interpolate = false;
		this.desc_sensor = true;
		this.desc_plot = true;
		this.desc_settings = true;
		this.allinone = true;
		this.aggregationInterval = AggregationInterval.DAY;
		this.quality = DataQuality.STEP;
		this.region = null;
		this.col_plotid = true;
		this.col_timestamp = false;
		this.col_datetime = true;		
		this.col_year = false;
		this.col_month = false;
		this.col_day = false;
		this.col_hour = false;
		this.col_day_of_year = false;		
		this.col_qualitycounter = false;
		this.write_header = true;
		this.spatial_aggregation = SpatialAggregation.SEPARATE;
		this.casted = false;
		this.timespanType = TimespanType.ALL;
		this.timespanYear = 0;
		this.timespanYearsFrom = 0;
		this.timespanYearsTo = 0;
		this.timespanDatesFrom = null;
		this.timespanDatesTo = null;
	}

	public Pair<Long,Long> getTimespan() {
		Long startTimestamp = null;
		Long endTimestamp = null;

		switch(timespanType) {
		case ALL:
			break;
		case YEAR:
			startTimestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(timespanYear, 1, 1, 0, 0));
			endTimestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(timespanYear, 12, 31, 23, 0));
			break;
		case YEARS:
			startTimestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(timespanYearsFrom, 1, 1, 0, 0));
			endTimestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(timespanYearsTo, 12, 31, 23, 0));
			break;
		case DATES:
			startTimestamp = parseDateFrom(timespanDatesFrom);
			endTimestamp = parseDateTo(timespanDatesTo);
			break;
		default:
			Logger.error("unknown timespan");
		}
		return new Pair<Long, Long>(startTimestamp,endTimestamp);
	}

	public static Long parseDateFrom(String text) {
		if(text.equals("*")) {
			return null;
		}
		if(text.matches("\\d{4}")) {
			//0123456789012
			//2014
			int year = Integer.parseInt(text);
			return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 1, 1, 0, 0));
		}
		if(text.matches("\\d{4}-\\d{2}")) {
			//0123456
			//2014-01
			int year = Integer.parseInt(text.substring(0, 4));
			int month = Integer.parseInt(text.substring(5, 7));
			return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 1, 0, 0));
		}
		if(text.matches("\\d{4}-\\d{2}-\\d{2}")) {
			//0123456789
			//2014-01-01
			int year = Integer.parseInt(text.substring(0, 4));
			int month = Integer.parseInt(text.substring(5, 7));
			int day = Integer.parseInt(text.substring(8, 10));
			return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, day, 0, 0));
		}
		if(text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}")) {
			//0123456789012
			//2014-01-01T07
			int year = Integer.parseInt(text.substring(0, 4));
			int month = Integer.parseInt(text.substring(5, 7));
			int day = Integer.parseInt(text.substring(8, 10));
			int hour = Integer.parseInt(text.substring(11, 13));
			return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, day, hour, 0));
		}
		throw new RuntimeException("unknown from date");
	}

	public static Long parseDateTo(String text) {
		if(text.equals("*")) {
			return null;
		}
		if(text.matches("\\d{4}")) {
			//0123456789012
			//2014
			int year = Integer.parseInt(text);
			return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 12, 31, 23, 0));
		}
		if(text.matches("\\d{4}-\\d{2}")) {
			//0123456
			//2014-01
			int year = Integer.parseInt(text.substring(0, 4));
			int month = Integer.parseInt(text.substring(5, 7));
			try {
				return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 31, 23, 0));
			} catch(Exception e0) {
				try {
					return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 30, 23, 0));
				} catch(Exception e1) {
					try {
						return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 29, 23, 0));
					} catch(Exception e2) {
						return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 28, 23, 0));
					}
				}
			}
		}
		if(text.matches("\\d{4}-\\d{2}-\\d{2}")) {
			//0123456789
			//2014-01-01
			int year = Integer.parseInt(text.substring(0, 4));
			int month = Integer.parseInt(text.substring(5, 7));
			int day = Integer.parseInt(text.substring(8, 10));
			return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, day, 23, 0));
		}
		if(text.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}")) {
			//0123456789012
			//2014-01-01T07
			int year = Integer.parseInt(text.substring(0, 4));
			int month = Integer.parseInt(text.substring(5, 7));
			int day = Integer.parseInt(text.substring(8, 10));
			int hour = Integer.parseInt(text.substring(11, 13));
			return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, day, hour, 0));
		}
		throw new RuntimeException("unknown from date");
	}	
}