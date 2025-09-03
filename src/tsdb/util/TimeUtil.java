package tsdb.util;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.Locale;


import org.tinylog.Logger;

/**
 * This class converts between date and time systems and keeps timestamp info from a UDBF-File.
 * @author woellauer
 *
 */
public final class TimeUtil implements Serializable {

	private TimeUtil(){}

	private static final long serialVersionUID = 4232805611076305334L;
		

	public static final LocalDateTime OLE_AUTOMATION_TIME_START = LocalDateTime.of(1899,12,30,0,0);

	public static LocalDateTime oleAutomatonTimeToDateTime(double oleAutomatonTimestampDays) {
		/*long oleAutomatonTimeSeconds = (long) Math.round(oleAutomatonTimestampDays*24*60*60);
		return TimeUtil.OLE_AUTOMATION_TIME_START.plus(Duration.ofSeconds(oleAutomatonTimeSeconds));*/
		return TimeUtil.OLE_AUTOMATION_TIME_START.plusSeconds((long) (oleAutomatonTimestampDays*24*60*60));
	}

	public static LocalDateTime oleMinutesToLocalDateTime(long oleTimeMinutes) {
		//return OLE_AUTOMATION_TIME_START.plus(Duration.ofMinutes(oleTimeMinutes));
		return OLE_AUTOMATION_TIME_START.plusMinutes(oleTimeMinutes);
	}

	public static Duration minutesToDuration(long minutes) {
		return Duration.ofMinutes(minutes);
	}

	public static long dateTimeToOleMinutes(LocalDateTime datetime) {
		return Duration.between(OLE_AUTOMATION_TIME_START, datetime).toMinutes();
	}

	public static String oleMinutesToText(Long oleTimeMinutes) {
		if(oleTimeMinutes == null || oleTimeMinutes < 0 || oleTimeMinutes > Integer.MAX_VALUE) {
			return "---";
		}
		return oleMinutesToLocalDateTime(oleTimeMinutes).toString();
	}

	public static String oleMinutesToText(Integer oleTimeMinutes) {
		return oleMinutesToText((Long.valueOf(oleTimeMinutes)));
	}

	public static String oleMinutesToDateTimeFileText(long oleTimeMinutes) {
		return oleMinutesToDateTimeFileText(Long.valueOf(oleTimeMinutes));
	}

	/**
	 * convertes timestamp to text that is usable as part of a filename
	 * @param oleTimeMinutes invalid timestamps (null or < 0 ) are handled by placeholder text
	 * @return
	 */
	public static String oleMinutesToDateTimeFileText(Long oleTimeMinutes) {
		if(oleTimeMinutes==null||oleTimeMinutes==-1) {
			return "xxxx_xx_xx";
		}
		LocalDate date = oleMinutesToLocalDateTime(oleTimeMinutes).toLocalDate();

		String s = "";
		s += date.getYear();
		s += "_";
		if(date.getMonthValue()<10) {
			s += "0";
		}
		s += date.getMonthValue();
		s += "_";
		if(date.getDayOfMonth()<10) {
			s += "0";
		}
		s += date.getDayOfMonth();
		s += "__";		
		LocalTime time = oleMinutesToLocalDateTime(oleTimeMinutes).toLocalTime();
		if(time.getHour()<10) {
			s += "0";
		}
		s += time.getHour();
		s += "_";
		if(time.getMinute()<10) {
			s += "0";
		}
		s += time.getMinute();

		return s;
	}

	public static String oleMinutesToText(long oleTimeMinutesStart, long oleTimeMinutesEnd) {
		return oleMinutesToText(oleTimeMinutesStart)+" - "+oleMinutesToText(oleTimeMinutesEnd);
	}

	public static String oleMinutesToText(Long oleTimeMinutesStart, Long oleTimeMinutesEnd) {
		return oleMinutesToText(oleTimeMinutesStart)+" - "+oleMinutesToText(oleTimeMinutesEnd);
	}

	private static final DateTimeFormatter DATE_TIME_FORMATER_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
	private static final DateTimeFormatter DATE_TIME_FORMATER_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	public static final DateTimeFormatter DATE_TIME_FORMATER_SPACE_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter DATE_TIME_FORMATER_MOF = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmmss");

	/**
	 * format: yyyy/MM/dd HH:mm
	 * example: 2010/08/25 00:05
	 * @param dateTimeText
	 * @return timestamp
	 */
	public static long parseTimestampSlashFormat(String dateTimeText) {		
		LocalDateTime dt = LocalDateTime.parse(dateTimeText, DATE_TIME_FORMATER_SLASH);
		return TimeUtil.dateTimeToOleMinutes(dt);
	}

	/**
	 * format: yyyy-MM-dd HH:mm:ss
	 * example: 2010-08-25 00:05:23
	 * example: 2010-08-25 00:05:23.1234
	 * @param dateTimeText
	 * @return timestamp
	 */
	public static long parseTimestampSpaceFormat(String dateTimeText) {
		String datetimeminutes = dateTimeText.substring(0, dateTimeText.lastIndexOf(':'));
		LocalDateTime dt = LocalDateTime.parse(datetimeminutes, DATE_TIME_FORMATER_SPACE);
		return TimeUtil.dateTimeToOleMinutes(dt);
	}
	
	public static String toDateSpaceTime(int t) {
		LocalDateTime datetime = oleMinutesToLocalDateTime(t);
		return DATE_TIME_FORMATER_SPACE.format(datetime);
	}

	/**
	 * example: 05.21.16 10:10:00 AM 
	 * @param dateTimeText
	 * @return
	 */
	public static long parseTimestampMonthFirstAmPmFormat(String dateTimeText) {
		LocalDateTime dt = LocalDateTime.parse(dateTimeText, DATE_TIME_FORMATER_MONTH_FIRST_AM_PM);
		return TimeUtil.dateTimeToOleMinutes(dt);
	}

	/**
	 * example: 07.12.2016 22:10
	 * @param dateTimeText
	 * @return
	 */
	public static long parseTimestampMonthFirstFormat(String dateTimeText) {
		LocalDateTime dt = LocalDateTime.parse(dateTimeText, DATE_TIME_FORMATER_MONTH_FIRST);
		return TimeUtil.dateTimeToOleMinutes(dt);
	}
	
	/**
	 * example: 12.07.2016 22:10
	 * @param dateTimeText
	 * @return
	 */
	public static int parseTimestampDayFirstFormat(String dateTimeText) {
		LocalDateTime dt = LocalDateTime.parse(dateTimeText, DATE_TIME_FORMATER_DAY_FIRST);
		return (int) TimeUtil.dateTimeToOleMinutes(dt);
	}

	private static final DateTimeFormatter DATE_TIME_FORMATER_MONTH_FIRST_AM_PM = DateTimeFormatter.ofPattern("MM.dd.yy hh:mm:ss a");
	private static final DateTimeFormatter DATE_TIME_FORMATER_MONTH_FIRST = DateTimeFormatter.ofPattern("MM.dd.yyyy HH:mm");
	
	private static final DateTimeFormatter DATE_TIME_FORMATER_DAY_FIRST = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");

	private static final DateTimeFormatter DATE_TIME_FORMATER_MONTH_NAME_ONE_HOUR_DIGIT =  DateTimeFormatter.ofPattern("dd-MMM-yyyy   H:mm").withLocale(Locale.ENGLISH);
	private static final DateTimeFormatter DATE_TIME_FORMATER_MONTH_NAME_TWO_HOUR_DIGITS = DateTimeFormatter.ofPattern("dd-MMM-yyyy  HH:mm").withLocale(Locale.ENGLISH);
	
	/**
	 * example: 01-Jul-2010   3:25
	 * example: 16-Dec-2010  14:55
	 * example: 12-Oct-2012  11:30
	 * @param dateTimeText
	 * @return
	 */
	public static long parseTimestampMonthNameFormat(String dateTimeText) {		
		DateTimeFormatter dtf = DATE_TIME_FORMATER_MONTH_NAME_TWO_HOUR_DIGITS;
		if(dateTimeText.charAt(13)==' ') {
			dtf = DATE_TIME_FORMATER_MONTH_NAME_ONE_HOUR_DIGIT;
		}		
		LocalDateTime dt = LocalDateTime.parse(dateTimeText, dtf);
		return TimeUtil.dateTimeToOleMinutes(dt);
	}


	/**
	 * 
	 * 
	 * 
	 * example: 2010-10-07,24  ==> 2010-10-07T23:00
	 * example: 2010-10-08,1   ==> 2010-10-08T00:00
	 * example: 2010-10-08,0   ==> 2010-10-07,23:00 (special case)
	 * 
	 * @param dateTimeText
	 * @return
	 */
	public static long parseTimestampDateFullHourFormat(String dateText, int fullHour) {
		LocalDate date = LocalDate.parse(dateText, DateTimeFormatter.ISO_DATE);
		if(fullHour==0) {
			LocalDateTime dt = LocalDateTime.of(date, LocalTime.of(23, 0));
			return TimeUtil.dateTimeToOleMinutes(dt.minusDays(1));
		} else {
			LocalDateTime dt = LocalDateTime.of(date, LocalTime.of(fullHour-1, 0));
			return TimeUtil.dateTimeToOleMinutes(dt);
		}		
	}

	public static long parseTimestamp(String dateText, String timeText, boolean isISOdate) {		
		int dayOfMonth;
		int month;
		int year;

		if(isISOdate) {
			// 2012-06-15
			// 0123456789
			year = 1000*(dateText.charAt(0)-'0')+100*(dateText.charAt(1)-'0')+10*(dateText.charAt(2)-'0')+(dateText.charAt(3)-'0');
			month = 10*(dateText.charAt(5)-'0')+(dateText.charAt(6)-'0');
			dayOfMonth = 10*(dateText.charAt(8)-'0')+(dateText.charAt(9)-'0');
		} else {
			// 01.07.13
			dayOfMonth = 10*(dateText.charAt(0)-'0')+(dateText.charAt(1)-'0');
			month = 10*(dateText.charAt(3)-'0')+(dateText.charAt(4)-'0');
			year = 2000 + 10*(dateText.charAt(6)-'0')+(dateText.charAt(7)-'0');
		}	

		int hour;
		int minute;
		int second;

		if( !(timeText.isEmpty() || timeText.equals("NA"))) {
			// 09:30:00
			hour = 10*(timeText.charAt(0)-'0')+(timeText.charAt(1)-'0');
			minute = 10*(timeText.charAt(3)-'0')+(timeText.charAt(4)-'0');
			second = 10*(timeText.charAt(6)-'0')+(timeText.charAt(7)-'0');
		} else {
			Logger.trace("no time at "+dateText+"  -> set time to 12:00:00");
			hour = 12;
			minute = 00;
			second = 00;
		}

		try {
			LocalDateTime datetime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
			return TimeUtil.dateTimeToOleMinutes(datetime);
		} catch (Exception e) {
			throw new RuntimeException("could not parse date: "+dateText+" "+timeText,e);
		}
	}

	/**
	 * Get timestamp of start of the year at timestamp.
	 * @param timestamp
	 * @return
	 */
	public static int roundLowerYear(int timestamp) {
		LocalDateTime datetime = oleMinutesToLocalDateTime(timestamp);
		return (int) dateTimeToOleMinutes(LocalDateTime.of(datetime.getYear(),1,1,0,0));
	}

	public static int roundLowerMonth(int timestamp) {
		LocalDateTime datetime = oleMinutesToLocalDateTime(timestamp);
		return (int) dateTimeToOleMinutes(LocalDateTime.of(datetime.getYear(),datetime.getMonthValue(),1,0,0));
	}

	/**
	 * Get timestamp of start of next year following year of timestamp.
	 * @param timestamp
	 * @return
	 */
	public static int roundNextYear(int timestamp) {
		LocalDateTime datetime = oleMinutesToLocalDateTime(timestamp);
		return (int) dateTimeToOleMinutes(LocalDateTime.of(datetime.getYear()+1,1,1,0,0));
	}

	public static int roundNextMonth(int timestamp) {
		LocalDateTime datetime = oleMinutesToLocalDateTime(timestamp);
		int m = datetime.getMonthValue();
		if(m < 12) {
			return (int) dateTimeToOleMinutes(LocalDateTime.of(datetime.getYear(),m + 1,1,0,0));
		} else {
			return (int) dateTimeToOleMinutes(LocalDateTime.of(datetime.getYear()+1,1,1,0,0));	
		}		
	}

	public static long ofDateStartHour(int year) { // at hour
		return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 1, 1, 0, 0));
	}

	public static long ofDateEndHour(int year) { // at hour
		return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 12, 31, 23, 0));
	}

	public static long ofDateStartMinute(int year) {
		return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 1, 1, 0, 0));
	}

	public static long ofDateStartMinute(int year, int month) {
		return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 1, 0, 0));
	}

	public static long ofDateEndMinute(int year) {
		return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 12, 31, 23, 59));
	}

	public static long ofDateEndMinute(int year, int month) { // TODO remove exceptions
		try {
			return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 31, 23, 59));
		} catch (DateTimeException e31) {
			try {
				return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 30, 23, 59));
			} catch (DateTimeException e30) {
				try {
					return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 29, 23, 59));
				} catch (DateTimeException e29) {
					return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 28, 23, 59));
				}
			}
		}
	}

	public static long ofDateStartHour(int year,int month) { // at hour
		return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 1, 0, 0));
	}

	public static long ofDateEndHour(int year,int month) { // at hour  TODO remove exceptions
		try {
			return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 31, 23, 0));
		} catch (DateTimeException e31) {
			try {
				return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 30, 23, 0));
			} catch (DateTimeException e30) {
				try {
					return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 29, 23, 0));
				} catch (DateTimeException e29) {
					return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 28, 23, 0));
				}
			}
		}
	}

	/*public static int ofDateStart(int year) {
		return (int) TimeConverter.DateTimeToOleMinutes(LocalDateTime.of(year, 1, 1, 0, 0));
	}

	public static int ofDateEnd(int year) {
		return (int) TimeConverter.DateTimeToOleMinutes(LocalDateTime.of(year, 1, 1, 0, 0));
	}*/

	public final static Comparator<Long> TIMESTAMP_START_ASC_COMPARATOR = (a,b) -> {
		if(a==null) {
			if(b==null) {
				return 0;
			} else {
				return -1; // start1==null start2!=null
			}
		} else {
			if(b==null) {
				return 1; // start1!=null start2==null
			} else {
				return (a < b) ? -1 : ((a == b) ? 0 : 1);
			}
		}
	};

	public final static Comparator<Long> TIMESTAMP_END_ASC_COMPARATOR = (a,b) -> {
		if(a==null) {
			if(b==null) {
				return 0;
			} else {
				return 1; // start1==null start2!=null
			}
		} else {
			if(b==null) {
				return -1; // start1!=null start2==null
			} else {
				return (a < b) ? -1 : ((a == b) ? 0 : 1);
			}
		}
	};

	/**
	 * parses an ISO 8601 date.
	 * full format:  YYYY-MM-DDThh:mm eg. 2009-12-31T14:09
	 * completes shortened dates to start of period
	 * eg. 2009 leads to 2009-01-01T00:00
	 * text "*" as unspecified date returns Integer.MIN_VALUE
	 * @param text
	 * @return timestamp in minutes
	 */
	public static int parseStartTimestamp(String text) {
		text = text.trim();

		switch(text.length()) {
		case 1: {
			if(text.charAt(0)=='*') {
				return Integer.MIN_VALUE;
			} else {
				throw new RuntimeException("unknown timestamp "+text);
			}
		}
		case 4: {
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-01-01T00:00"));
		}
		case 7: {
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-01T00:00"));
		}
		case 10: {
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"T00:00"));
		}
		case 13: {
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+":00"));
		}
		case 16: {
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text));
		}
		case 19: { // Format with seconds: 2009-01-01T00:00:00   (ignore seconds)
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text));
		}
		default:
			throw new RuntimeException("unknown timestamp "+text);
		}
	}

	/**
	 * parses an ISO 8601 date.
	 * full format:  YYYY-MM-DDThh:mm eg. 2009-12-31T14:09
	 * completes shortened dates to end of period
	 * eg. 2009 leads to 2009-12-31T23:59
	 * text "*" as unspecified date returns Integer.MAX_VALUE
	 * @param text
	 * @return timestamp in minutes
	 */
	public static int parseEndTimestamp(String text) {
		text = text.trim();

		switch(text.length()) {
		case 1: {
			if(text.charAt(0)=='*') {
				return Integer.MAX_VALUE;
			} else {
				throw new RuntimeException("unknown timestamp "+text);
			}
		}
		case 4: {
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-12-31T23:59"));
		}
		case 7: {
			try {
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-31T23:59"));
			} catch (DateTimeParseException e) {
				try {
					return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-30T23:59"));
				} catch (DateTimeParseException e1) {
					try {
						return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-29T23:59"));
					} catch (DateTimeParseException e2) {
						return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-28T23:59"));
					}
				}
			}
		}
		case 10: {
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"T23:59"));
		}
		case 13: {
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+":59"));
		}
		case 16: {
			return (int) dateTimeToOleMinutes(LocalDateTime.parse(text));
		}
		default:
			throw new RuntimeException("unknown timestamp "+text);
		}
	}

	/**
	 * parses an ISO 8601 date.
	 * full format:  YYYY-MM-DDThh:mm eg. 2009-12-31T14:09
	 * completes shortened dates to end of period
	 * eg. 2009 leads to 2009-12-31T23:59 for aggregation 'raw'
	 * eg. 2009 leads to 2009-12-31T23:00 for aggregation 'hour'
	 * eg. 2009 leads to 2009-12-01T00:00 for aggregation 'month'
	 * text "*" as unspecified date returns Integer.MAX_VALUE
	 * @param text
	 * @param agg
	 * @return timestamp in minutes
	 */
	public static long parseEndTimestamp(String text, AggregationInterval agg) {
		text = text.trim();

		switch(text.length()) {
		case 1: {
			if(text.charAt(0)=='*') {
				return Integer.MAX_VALUE;
			} else {
				throw new RuntimeException("unknown timestamp "+text);
			}
		}
		case 4: { // YYYY
			switch(agg) {
			case RAW:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-12-31T23:59"));
			case HOUR:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-12-31T23:00"));
			case DAY:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-12-31T23:00"));
			case WEEK:
				throw new RuntimeException("week aggregation can not be applied to shortened end date");
			case MONTH:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-12-31T23:00"));
			case YEAR:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-12-31T23:00"));
			default:
				throw new RuntimeException("unknown aggregation");
			}			
		}
		case 7: { // YYYY-MM			
			switch(agg) {
			case RAW:
				try {
					return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-31T23:59"));
				} catch (DateTimeParseException e) {
					try {
						return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-30T23:59"));
					} catch (DateTimeParseException e1) {
						try {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-29T23:59"));
						} catch (DateTimeParseException e2) {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-28T23:59"));
						}
					}
				}
			case HOUR:
				try {
					return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-31T23:00"));
				} catch (DateTimeParseException e) {
					try {
						return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-30T23:00"));
					} catch (DateTimeParseException e1) {
						try {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-29T23:00"));
						} catch (DateTimeParseException e2) {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-28T23:00"));
						}
					}
				}
			case DAY:
				try {
					return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-31T23:00"));
				} catch (DateTimeParseException e) {
					try {
						return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-30T23:00"));
					} catch (DateTimeParseException e1) {
						try {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-29T23:00"));
						} catch (DateTimeParseException e2) {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-28T23:00"));
						}
					}
				}
			case WEEK:
				throw new RuntimeException("week aggregation can not be applied to shortened end date");
			case MONTH:
				try {
					return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-31T23:00"));
				} catch (DateTimeParseException e) {
					try {
						return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-30T23:00"));
					} catch (DateTimeParseException e1) {
						try {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-29T23:00"));
						} catch (DateTimeParseException e2) {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-28T23:00"));
						}
					}
				}
			case YEAR:
				Logger.warn("correct month for end date not checked");
				try {
					return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-31T23:00"));
				} catch (DateTimeParseException e) {
					try {
						return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-30T23:00"));
					} catch (DateTimeParseException e1) {
						try {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-29T23:00"));
						} catch (DateTimeParseException e2) {
							return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"-28T23:00"));
						}
					}
				}
			default:
				throw new RuntimeException("unknown aggregation");
			}
		}
		case 10: { // YYYY-MM-DD
			switch(agg) {
			case RAW:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"T23:59"));
			case HOUR:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"T23:00"));
			case DAY:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"T23:00"));
			case WEEK:
				//throw new RuntimeException("week aggregation can not be applied to shortened end date");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"T23:00"));
			case MONTH:
				Logger.warn("correct day for end date not checked");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"T23:00"));
			case YEAR:
				Logger.warn("correct month and day for end date not checked");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+"T23:00"));
			default:
				throw new RuntimeException("unknown aggregation");
			}			
		}
		case 13: { // YYYY-MM-DDThh
			switch(agg) {
			case RAW:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+":59"));
			case HOUR:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+":00"));
			case DAY:
				Logger.warn("correct hour for end date not checked");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+":00"));
			case WEEK:
				throw new RuntimeException("week aggregation can not be applied to shortened end date");
			case MONTH:
				Logger.warn("correct day and hour for end date not checked");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+":00"));
			case YEAR:
				Logger.warn("correct month, day and hour for end date not checked");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text+":00"));
			default:
				throw new RuntimeException("unknown aggregation");
			}			
		}
		case 16: { // YYYY-MM-DDThh:mm			
			switch(agg) {
			case RAW:
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text));
			case HOUR:
				Logger.warn("correct minute for end date not checked");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text));
			case DAY:
				Logger.warn("correct hour and minute for end date not checked");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text));
			case WEEK:
				throw new RuntimeException("week aggregation can not be applied to shortened end date");
			case MONTH:
				Logger.warn("correct day, hour and minute for end date not checked");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text));
			case YEAR:
				Logger.warn("correct month, day, hour and minute for end date not checked");
				return (int) dateTimeToOleMinutes(LocalDateTime.parse(text));
			default:
				throw new RuntimeException("unknown aggregation");
			}		
		}
		default:
			throw new RuntimeException("unknown timestamp "+text);
		}
	}

	public static char[] fastTimestampWrite(long timestamp, AggregationInterval datetimeFormat) {
		return fastTimestampWrite(TimeUtil.oleMinutesToLocalDateTime(timestamp), datetimeFormat);		
	}

	public static char[] fastTimestampWrite_custom(long timestamp, AggregationInterval datetimeFormat) {
		return fastTimestampWrite_custom(TimeUtil.oleMinutesToLocalDateTime(timestamp), datetimeFormat);		
	}

	public static char[] fastTimestampWrite(LocalDateTime datetime, AggregationInterval datetimeFormat) {
		switch(datetimeFormat) {
		case YEAR:
			return TimeUtil.fastDateWriteYears(datetime.toLocalDate());
		case MONTH:
			return TimeUtil.fastDateWriteMonths(datetime.toLocalDate());
		case WEEK:
			return TimeUtil.fastDateWriteWeeks(datetime.toLocalDate());
		case DAY:
			return TimeUtil.fastDateWrite(datetime.toLocalDate());
		case HOUR:
			return TimeUtil.fastDateTimeWriteHours(datetime);
		default:
			return TimeUtil.fastDateTimeWrite(datetime);	
		}				
	}

	public static char[] fastTimestampWrite_custom(LocalDateTime datetime, AggregationInterval datetimeFormat) {
		switch(datetimeFormat) {
		case YEAR:
			return TimeUtil.fastDateWriteYears(datetime.toLocalDate());
		case MONTH:
			return TimeUtil.fastDateWriteMonths(datetime.toLocalDate());
		case WEEK:
			return TimeUtil.fastDateWriteWeeks(datetime.toLocalDate());
		case DAY:
			return TimeUtil.fastDateWrite(datetime.toLocalDate());
		case HOUR:
			return TimeUtil.fastDateTimeWriteHours_custom(datetime);
		default:
			return TimeUtil.fastDateTimeWrite_custom(datetime);	
		}				
	}

	public static char[] fastTimeWrite(LocalTime localTime) {
		char[] c = new char[5];
		int h = localTime.getHour();
		c[0] = (char) ('0'+(h/10));
		c[1] = (char) ('0'+(h%10));
		c[2] = (char) (':');
		int m = localTime.getMinute();
		c[3] = (char) ('0'+(m/10));
		c[4] = (char) ('0'+(m%10));
		return c;
	}

	public static char[] fastDateWrite(LocalDate localDate) {
		char[] c = new char[10];
		int y = localDate.getYear();
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		c[4] = (char) ('-');
		int m = localDate.getMonthValue();
		c[5] = (char) ('0'+(m/10));
		c[6] = (char) ('0'+(m%10));
		c[7] = (char) ('-');
		int d = localDate.getDayOfMonth();
		c[8] = (char) ('0'+(d/10));
		c[9] = (char) ('0'+(d%10));
		return c;
	}

	private static final TemporalField weekOfYear = WeekFields.of(Locale.GERMANY).weekOfWeekBasedYear();
	private static final TemporalField yearOfWeek = WeekFields.of(Locale.GERMANY).weekBasedYear();

	public static char[] fastDateWriteWeeks(LocalDate localDate) {		
		char[] c = new char[8];
		int y = localDate.get(yearOfWeek);
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		c[4] = (char) ('-');
		c[5] = (char) ('W');
		int w = localDate.get(weekOfYear);
		c[6] = (char) ('0'+(w/10));
		c[7] = (char) ('0'+(w%10));
		return c;
	}

	public static char[] fastDateWriteMonths(LocalDate localDate) {
		char[] c = new char[7];
		int y = localDate.getYear();
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		c[4] = (char) ('-');
		int m = localDate.getMonthValue();
		c[5] = (char) ('0'+(m/10));
		c[6] = (char) ('0'+(m%10));
		return c;
	}

	public static char[] fastDateWriteYears(LocalDate localDate) {
		char[] c = new char[4];
		int y = localDate.getYear();
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		return c;
	}

	public static char[] fastDateTimeWrite(LocalDateTime localDateTime) {
		char[] c = new char[16];

		LocalDate localDate = localDateTime.toLocalDate();
		int y = localDate.getYear();
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		c[4] = (char) ('-');
		int m = localDate.getMonthValue();
		c[5] = (char) ('0'+(m/10));
		c[6] = (char) ('0'+(m%10));
		c[7] = (char) ('-');
		int d = localDate.getDayOfMonth();
		c[8] = (char) ('0'+(d/10));
		c[9] = (char) ('0'+(d%10));
		c[10] = (char) ('T');
		LocalTime localTime = localDateTime.toLocalTime();
		int h = localTime.getHour();
		c[11] = (char) ('0'+(h/10));
		c[12] = (char) ('0'+(h%10));
		c[13] = (char) (':');
		int mo = localTime.getMinute();
		c[14] = (char) ('0'+(mo/10));
		c[15] = (char) ('0'+(mo%10));

		return c;		
	}

	public static char[] fastDateTimeWrite_custom(LocalDateTime localDateTime) {
		char[] c = new char[16];

		LocalDate localDate = localDateTime.toLocalDate();
		int y = localDate.getYear();
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		c[4] = (char) ('-');
		int m = localDate.getMonthValue();
		c[5] = (char) ('0'+(m/10));
		c[6] = (char) ('0'+(m%10));
		c[7] = (char) ('-');
		int d = localDate.getDayOfMonth();
		c[8] = (char) ('0'+(d/10));
		c[9] = (char) ('0'+(d%10));
		c[10] = (char) (' ');
		LocalTime localTime = localDateTime.toLocalTime();
		int h = localTime.getHour();
		c[11] = (char) ('0'+(h/10));
		c[12] = (char) ('0'+(h%10));
		c[13] = (char) (':');
		int mo = localTime.getMinute();
		c[14] = (char) ('0'+(mo/10));
		c[15] = (char) ('0'+(mo%10));

		return c;		
	}

	public static char[] fastDateTimeWriteHours(LocalDateTime localDateTime) {
		char[] c = new char[13];

		LocalDate localDate = localDateTime.toLocalDate();
		int y = localDate.getYear();
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		c[4] = (char) ('-');
		int m = localDate.getMonthValue();
		c[5] = (char) ('0'+(m/10));
		c[6] = (char) ('0'+(m%10));
		c[7] = (char) ('-');
		int d = localDate.getDayOfMonth();
		c[8] = (char) ('0'+(d/10));
		c[9] = (char) ('0'+(d%10));
		c[10] = (char) ('T');
		LocalTime localTime = localDateTime.toLocalTime();
		int h = localTime.getHour();
		c[11] = (char) ('0'+(h/10));
		c[12] = (char) ('0'+(h%10));

		return c;		
	}

	public static char[] fastDateTimeWriteHours_custom(LocalDateTime localDateTime) {
		char[] c = new char[13];

		LocalDate localDate = localDateTime.toLocalDate();
		int y = localDate.getYear();
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		c[4] = (char) ('-');
		int m = localDate.getMonthValue();
		c[5] = (char) ('0'+(m/10));
		c[6] = (char) ('0'+(m%10));
		c[7] = (char) ('-');
		int d = localDate.getDayOfMonth();
		c[8] = (char) ('0'+(d/10));
		c[9] = (char) ('0'+(d%10));
		c[10] = (char) (' ');
		LocalTime localTime = localDateTime.toLocalTime();
		int h = localTime.getHour();
		c[11] = (char) ('0'+(h/10));
		c[12] = (char) ('0'+(h%10));

		return c;		
	}

	public static LocalDateTime unixTimeToLocalDateTime(long unixTime) {
		Instant instant = Instant.ofEpochSecond(unixTime);
		LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		return datetime;
	}
	
	public static int parseNormalDatetime(String s) {
		s = s.trim();
		switch(s.length()) {
		case 1:
			if(s.charAt(0)=='*') {
				return Integer.MIN_VALUE;
			} else {
				throw new RuntimeException("unknown timestamp "+s);
			}
		case 4: // years 
			return parseNormalDateTimeFormat4(s);
		case 7: // months
			return parseNormalDateTimeFormat7(s);
		case 10: // days
			return parseNormalDateTimeFormat10(s);
		case 13: // hours
			return parseNormalDateTimeFormat13(s);
		case 16: // minutes
		case 19: // Format with seconds: 2009-01-01T00:00:00   (ignore seconds)
		case 20: // Format with seconds: 2009-01-01T00:00:00Z   (ignore seconds and Z)
			return parseNormalDateTimeFormat16(s);
		default:
			throw new RuntimeException("unknown timestamp "+s);
		}
	}

	public static int parseNormalDatetimeEnd(String s) {
		s = s.trim();
		switch(s.length()) {
		case 1:
			if(s.charAt(0)=='*') {
				return Integer.MAX_VALUE;
			} else {
				throw new RuntimeException("unknown timestamp "+s);
			}
		case 4: // years 
			return parseNormalDateTimeFormat4End(s);
		case 7: // months
			return parseNormalDateTimeFormat7End(s);
		case 10: // days
			return parseNormalDateTimeFormat10(s) + 23 * 60 + 59;
		case 13: // hours
			return parseNormalDateTimeFormat13(s) + 59;
		case 16: // minutes
		case 19: // Format with seconds: 2009-01-01T00:00:00   (ignore seconds)
			return parseNormalDateTimeFormat16(s);
		default:
			throw new RuntimeException("unknown timestamp "+s);
		}
	}

	private static int parseNormalDateTimeFormat16(String s) {
		int y1000 = (s.charAt(0)-'0');
		int y100 = (s.charAt(1)-'0');
		int y10 = (s.charAt(2)-'0');
		int y1 = (s.charAt(3)-'0');
		int m10 = (s.charAt(5)-'0');
		int m1 = (s.charAt(6)-'0');
		int d10 = (s.charAt(8)-'0');
		int d1 = (s.charAt(9)-'0');
		int h10 = (s.charAt(11)-'0');
		int h1 = (s.charAt(12)-'0');
		int n10 = (s.charAt(14)-'0');
		int n1 = (s.charAt(15)-'0');
		if(        y1000 < 0 || y1000 > 9
				|| y100 < 0 || y100 > 9
				|| y10 < 0 || y10 > 9
				|| y1 < 0 || y1 > 9
				|| m10 < 0 || m10 > 1
				|| m1 < 0 || m1 > 9
				|| d10 < 0 || d10 > 3
				|| d1 < 0 || d1 > 9
				|| h10 < 0 || h10 > 6
				|| h1 < 0 || h1 > 9
				|| n10 < 0 || n10 > 6
				|| n1 < 0 || n1 > 9				
				) {
			throw new RuntimeException("timestamp error");
		}
		int year =  1000*y1000+100*y100+10*y10+y1;
		int month = 10*m10+m1;
		int day = 10*d10+d1;
		int hour = 10*h10+h1;
		int minute = 10*n10+n1;
		//return (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, day, hour, minute));
		return datetimeToOleMinutes(year, month, day, hour, minute);
	}

	private static int parseNormalDateTimeFormat13(String s) {
		int y1000 = (s.charAt(0)-'0');
		int y100 = (s.charAt(1)-'0');
		int y10 = (s.charAt(2)-'0');
		int y1 = (s.charAt(3)-'0');
		int m10 = (s.charAt(5)-'0');
		int m1 = (s.charAt(6)-'0');
		int d10 = (s.charAt(8)-'0');
		int d1 = (s.charAt(9)-'0');
		int h10 = (s.charAt(11)-'0');
		int h1 = (s.charAt(12)-'0');
		if(        y1000 < 0 || y1000 > 9
				|| y100 < 0 || y100 > 9
				|| y10 < 0 || y10 > 9
				|| y1 < 0 || y1 > 9
				|| m10 < 0 || m10 > 1
				|| m1 < 0 || m1 > 9
				|| d10 < 0 || d10 > 3
				|| d1 < 0 || d1 > 9
				|| h10 < 0 || h10 > 6
				|| h1 < 0 || h1 > 9			
				) {
			throw new RuntimeException("timestamp error");
		}
		int year =  1000*y1000+100*y100+10*y10+y1;
		int month = 10*m10+m1;
		int day = 10*d10+d1;
		int hour = 10*h10+h1;
		//return (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, day, hour, 0));
		return datetimeToOleMinutes(year, month, day, hour, 0);
	}

	private static int parseNormalDateTimeFormat10(String s) {
		int y1000 = (s.charAt(0)-'0');
		int y100 = (s.charAt(1)-'0');
		int y10 = (s.charAt(2)-'0');
		int y1 = (s.charAt(3)-'0');
		int m10 = (s.charAt(5)-'0');
		int m1 = (s.charAt(6)-'0');
		int d10 = (s.charAt(8)-'0');
		int d1 = (s.charAt(9)-'0');
		if(        y1000 < 0 || y1000 > 9
				|| y100 < 0 || y100 > 9
				|| y10 < 0 || y10 > 9
				|| y1 < 0 || y1 > 9
				|| m10 < 0 || m10 > 1
				|| m1 < 0 || m1 > 9
				|| d10 < 0 || d10 > 3
				|| d1 < 0 || d1 > 9		
				) {
			throw new RuntimeException("timestamp error");
		}
		int year =  1000*y1000+100*y100+10*y10+y1;
		int month = 10*m10+m1;
		int day = 10*d10+d1;
		//return (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, day, 0, 0));
		return datetimeToOleMinutes(year, month, day, 0, 0);
	}

	private static int parseNormalDateTimeFormat7(String s) {
		int y1000 = (s.charAt(0)-'0');
		int y100 = (s.charAt(1)-'0');
		int y10 = (s.charAt(2)-'0');
		int y1 = (s.charAt(3)-'0');
		int m10 = (s.charAt(5)-'0');
		int m1 = (s.charAt(6)-'0');
		if(        y1000 < 0 || y1000 > 9
				|| y100 < 0 || y100 > 9
				|| y10 < 0 || y10 > 9
				|| y1 < 0 || y1 > 9
				|| m10 < 0 || m10 > 1
				|| m1 < 0 || m1 > 9	
				) {
			throw new RuntimeException("timestamp error");
		}
		int year =  1000*y1000+100*y100+10*y10+y1;
		int month = 10*m10+m1;
		//return (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 1, 0, 0));
		return datetimeToOleMinutes(year, month, 1, 0, 0);
	}

	private static int parseNormalDateTimeFormat7End(String s) {
		int y1000 = (s.charAt(0)-'0');
		int y100 = (s.charAt(1)-'0');
		int y10 = (s.charAt(2)-'0');
		int y1 = (s.charAt(3)-'0');
		int m10 = (s.charAt(5)-'0');
		int m1 = (s.charAt(6)-'0');
		if(        y1000 < 0 || y1000 > 9
				|| y100 < 0 || y100 > 9
				|| y10 < 0 || y10 > 9
				|| y1 < 0 || y1 > 9
				|| m10 < 0 || m10 > 1
				|| m1 < 0 || m1 > 9	
				) {
			throw new RuntimeException("timestamp error");
		}
		int year =  1000*y1000+100*y100+10*y10+y1;
		int month = 10*m10+m1;
		//return (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, month, 1, 0, 0));
		return yearMonthEndtoOleMinutes(year, month);
	}

	private static int parseNormalDateTimeFormat4(String s) {
		int y1000 = (s.charAt(0)-'0');
		int y100 = (s.charAt(1)-'0');
		int y10 = (s.charAt(2)-'0');
		int y1 = (s.charAt(3)-'0');
		if(        y1000 < 0 || y1000 > 9
				|| y100 < 0 || y100 > 9
				|| y10 < 0 || y10 > 9
				|| y1 < 0 || y1 > 9
				) {
			throw new RuntimeException("timestamp error");
		}
		int year =  1000*y1000+100*y100+10*y10+y1;
		//return (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 1, 1, 0, 0));
		return datetimeToOleMinutes(year, 1, 1, 0, 0);
	}

	private static int parseNormalDateTimeFormat4End(String s) {
		int y1000 = (s.charAt(0)-'0');
		int y100 = (s.charAt(1)-'0');
		int y10 = (s.charAt(2)-'0');
		int y1 = (s.charAt(3)-'0');
		if(        y1000 < 0 || y1000 > 9
				|| y100 < 0 || y100 > 9
				|| y10 < 0 || y10 > 9
				|| y1 < 0 || y1 > 9
				) {
			throw new RuntimeException("timestamp error");
		}
		int year =  1000*y1000+100*y100+10*y10+y1;
		//return (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(year, 1, 1, 0, 0));
		return yearEndtoOleMinutes(year);
	}

	private static int toLookupMonth(int y, int m) {
		return (y-1) * 12 + (m-1);
	}

	private static final int LOOKUP_YEAR_START = 1990;
	private static final int LOOKUP_YEAR_END = 2030;

	private static final LocalDateTime DATETIME_LOOKUP_START_DAY = LocalDateTime.of(LOOKUP_YEAR_START,01,01,0,0);
	private static final int DATETIME_LOOKUP_START_DAY_OLE_MINUTES = (int) TimeUtil.dateTimeToOleMinutes(DATETIME_LOOKUP_START_DAY);
	private static final int TIMELINE_MONTH_START = toLookupMonth(LOOKUP_YEAR_START, 1);
	private static final int TIMELINE_MONTH_END = toLookupMonth(LOOKUP_YEAR_END, 12);
	private static final int LOOKUP_MONTH_COUNT = TIMELINE_MONTH_END - TIMELINE_MONTH_START + 1;

	private static final byte[] LOOKUP_TABLE_MONTH_DAYS = new byte[LOOKUP_MONTH_COUNT];
	private static final int[] LOOKUP_TABLE_START_DAY = new int[LOOKUP_MONTH_COUNT];	

	static {
		for(int y = LOOKUP_YEAR_START; y <= LOOKUP_YEAR_END; y++) {
			for(int m = 1; m <= 12; m++) {			
				int i = toLookupMonth(y, m) - TIMELINE_MONTH_START;
				int maxD = YearMonth.of(y, m).lengthOfMonth();
				LOOKUP_TABLE_MONTH_DAYS[i] = (byte) maxD;
				long dayStart = Duration.between(DATETIME_LOOKUP_START_DAY, LocalDateTime.of(y, m, 1, 0, 0)).toDays();				
				LOOKUP_TABLE_START_DAY[i] = (int) dayStart;
			}
		}
	}

	private static int datetimeToOleMinutes(int y, int m, int d, int h, int mm) {
		if(y < LOOKUP_YEAR_START || LOOKUP_YEAR_END < y) {
			int t = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(y, m, d, h, mm));
			return t;
		} else {
			if(m < 1 || m > 12) {
				throw new RuntimeException("timestamp error");
			}
			int i = toLookupMonth(y, m) - TIMELINE_MONTH_START;
			if(d < 1 || d > LOOKUP_TABLE_MONTH_DAYS[i]) {
				throw new RuntimeException("timestamp error");
			}
			if(h < 0 || h > 23 || mm < 0 || mm > 59) {
				throw new RuntimeException("timestamp error");
			}
			int t = ((LOOKUP_TABLE_START_DAY[i] + (d - 1)) * 24 + h) * 60 + mm + DATETIME_LOOKUP_START_DAY_OLE_MINUTES;
			/*int tS = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(y, m, d, h, mm));
			if(t != tS) {
				throw new RuntimeException("timestamp error + " + t + "  " + tS);
			}*/
			return t;
		}
	}

	private static LocalDateTime yearMonthToDateTime(int y, int m) {
		int d = YearMonth.of(y, m).lengthOfMonth();
		return LocalDateTime.of(y, m, d, 23, 59);		
	}

	private static int yearMonthEndtoOleMinutes(int y, int m) {
		if(y < LOOKUP_YEAR_START || LOOKUP_YEAR_END < y) {
			int t = (int) TimeUtil.dateTimeToOleMinutes(yearMonthToDateTime(y, m));
			return t;
		} else {
			if(m < 1 || m > 12) {
				throw new RuntimeException("timestamp error");
			}
			int i = toLookupMonth(y, m) - TIMELINE_MONTH_START;
			int t = ((LOOKUP_TABLE_START_DAY[i] + (LOOKUP_TABLE_MONTH_DAYS[i] - 1)) * 24 + 23) * 60 + 59 + DATETIME_LOOKUP_START_DAY_OLE_MINUTES;
			/*int tS = (int) TimeUtil.dateTimeToOleMinutes(toDateTimeEndSlow(y, m));
			if(t != tS) {
				throw new RuntimeException("timestamp error + " + t + "  " + tS);
			}*/
			return t;
		}
	}

	private static int yearEndtoOleMinutes(int y) {
		if(y < LOOKUP_YEAR_START || LOOKUP_YEAR_END < y) {
			int t = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(y, 12, 31, 23, 59));
			return t;
		} else {
			int i = toLookupMonth(y, 12) - TIMELINE_MONTH_START;
			int t = ((LOOKUP_TABLE_START_DAY[i] + (31 - 1)) * 24 + 23) * 60 + 59 + DATETIME_LOOKUP_START_DAY_OLE_MINUTES;
			/*int tS = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(y, m, d, h, mm));
			if(t != tS) {
				throw new RuntimeException("timestamp error + " + t + "  " + tS);
			}*/
			return t;
		}
	}
}
