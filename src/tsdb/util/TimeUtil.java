package tsdb.util;

import java.io.IOException;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Comparator;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class converts between date and time systems and keeps timestamp info from a UDBF-File.
 * @author woellauer
 *
 */
public final class TimeUtil implements Serializable {

	private TimeUtil(){}

	private static final long serialVersionUID = 4232805611076305334L;
	private static final Logger log = LogManager.getLogger();	

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
		if(oleTimeMinutes==null||oleTimeMinutes==-1) {
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
	private static final DateTimeFormatter DATE_TIME_FORMATER_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
	 * example: 2010/08/25 00:05
	 * @param dateTimeText
	 * @return timestamp
	 */
	public static long parseTimestampSpaceFormat(String dateTimeText) {		
		LocalDateTime dt = LocalDateTime.parse(dateTimeText, DATE_TIME_FORMATER_SPACE);
		return TimeUtil.dateTimeToOleMinutes(dt);
	}

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
			log.trace("no time at "+dateText+"  -> set time to 12:00:00");
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

	/**
	 * Get timestamp of start of next year following year of timestamp.
	 * @param timestamp
	 * @return
	 */
	public static int roundNextYear(int timestamp) {
		LocalDateTime datetime = oleMinutesToLocalDateTime(timestamp);
		return (int) dateTimeToOleMinutes(LocalDateTime.of(datetime.getYear()+1,1,1,0,0));
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
	
	public static char[] fastTimeWrite(LocalTime localTime) throws IOException {
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
	
	public static char[] fastDateWrite(LocalDate localDate) throws IOException {
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
	
	public static char[] fastDateWriteWeeks(LocalDate localDate) throws IOException {		
		char[] c = new char[7];
		int y = localDate.get(yearOfWeek);
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		c[4] = (char) ('W');
		int w = localDate.get(weekOfYear);
		c[5] = (char) ('0'+(w/10));
		c[6] = (char) ('0'+(w%10));
		return c;
	}
	
	public static char[] fastDateWriteMonths(LocalDate localDate) throws IOException {
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
	
	public static char[] fastDateWriteYears(LocalDate localDate) throws IOException {
		char[] c = new char[4];
		int y = localDate.getYear();
		c[0] = (char) ('0'+  y/1000);
		c[1] = (char) ('0'+ ((y%1000)/100)  );
		c[2] = (char) ('0'+ ((y%100)/10)  );
		c[3] = (char) ('0'+ (y%10)  );
		return c;
	}
	
	public static char[] fastDateTimeWrite(LocalDateTime localDateTime) throws IOException {
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
	
	public static char[] fastDateTimeWriteHours(LocalDateTime localDateTime) throws IOException {
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
	
	
}
