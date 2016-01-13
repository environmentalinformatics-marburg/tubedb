package tsdb.util;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class convertes beween date and time systems and keeps timestamp info from a UDBF-File.
 * @author woellauer
 *
 */
public final class TimeUtil implements Serializable {

	private TimeUtil(){}

	private static final long serialVersionUID = 4232805611076305334L;
	private static final Logger log = LogManager.getLogger();	

	public static final LocalDateTime OLE_AUTOMATION_TIME_START = LocalDateTime.of(1899,12,30,0,0);

	public static LocalDateTime oleAutomatonTimeToDateTime(double oleAutomatonTimestamp) {
		long oleAutomatonTimeSeconds = (long) Math.round(oleAutomatonTimestamp*24*60*60);
		return TimeUtil.OLE_AUTOMATION_TIME_START.plus(Duration.ofSeconds(oleAutomatonTimeSeconds));
	}

	public static LocalDateTime oleMinutesToLocalDateTime(long oleTimeMinutes) {
		return OLE_AUTOMATION_TIME_START.plus(Duration.ofMinutes(oleTimeMinutes));
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
	 * example: 2010-10-07,24
	 * example: 2010-10-08,1
	 * 
	 * @param dateTimeText
	 * @return
	 */
	public static long parseTimestampDateFullHourFormat(String dateText, int fullHour) {
		LocalDate date = LocalDate.parse(dateText, DateTimeFormatter.ISO_DATE);
		LocalDateTime dt = LocalDateTime.of(date, LocalTime.of(fullHour-1, 0));
		return TimeUtil.dateTimeToOleMinutes(dt);
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

		if(!timeText.isEmpty())  {
			// 09:30:00
			hour = 10*(timeText.charAt(0)-'0')+(timeText.charAt(1)-'0');
			minute = 10*(timeText.charAt(3)-'0')+(timeText.charAt(4)-'0');
			second = 10*(timeText.charAt(6)-'0')+(timeText.charAt(7)-'0');
		} else {
			log.info("no time at "+dateText+"  -> set time to 12:00:00");
			hour = 12;
			minute = 00;
			second = 00;
		}

		LocalDateTime datetime = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
		return TimeUtil.dateTimeToOleMinutes(datetime);
	}

	public static int roundLowerYear(int timestamp) {
		LocalDateTime datetime = oleMinutesToLocalDateTime(timestamp);
		return (int) dateTimeToOleMinutes(LocalDateTime.of(datetime.getYear(),1,1,0,0));
	}

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
}
