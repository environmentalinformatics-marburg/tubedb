package tsdb;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;

/**
 * Properties of stations
 * @author woellauer
 *
 */
public class StationProperties implements Serializable{
	private static final long serialVersionUID = -4558930650676952510L;
	


	public final static String PROPERTY_START = "DATE_START";
	public final static String PROPERTY_END = "DATE_END";
	public final static String PROPERTY_LOGGER = "LOGGER";
	public final static String PROPERTY_PLOTID = "PLOTID";
	public final static String PROPERTY_SERIAL = "SERIAL";
	public final static String PROPERTY_TYPE = "TYPE"; //type: EP or VIP
	public final static String PROPERTY_ALIAS = "alias";

	public final static String TYPE_VIP = "VIP";

	private Map<String,String> propertyMap;

	public StationProperties(Map<String,String> propertyMap) {
		this.propertyMap = propertyMap;		
	}

	public String getProperty(String key) {
		return propertyMap.get(key);
	}

	public boolean isVIP() {
		String type = getProperty(PROPERTY_TYPE);
		if(type==null) {
			return false;
		}
		return type.equals(TYPE_VIP);
	}

	public Integer getIntProperty(String key) {
		String text = propertyMap.get(key);
		if(text!=null) {
			try {
				return Integer.parseInt(text);
			} catch(Exception e) {
				Logger.warn("error in read int: "+e+"  for propery  "+key+" and value  "+text);
				return null;
			}
		} else {
			Logger.warn("error in read int: not found for property "+key);
			return null;
		}
	}

	public float getFloatProperty(String key) {
		return getFloatProperty(key, "-");
	}

	public float getFloatProperty(String key, String traceText) {
		String text = propertyMap.get(key);
		if(text!=null) {
			try {
				//return Float.parseFloat(text);
				return (float) FastDoubleParser.parseDouble(text);
			} catch(Exception e) {
				Logger.warn("error in read float: "+e+"  for propery  "+key+" and value  "+text+"   at "+traceText);
				return Float.NaN;
			}
		} else {
			Logger.warn("error in read float: not found for property "+key+"   at "+traceText);
			return Float.NaN;
		}
	}

	private static Long parseConfigDateStart(String startText) {
		if(startText==null || startText.equals("*") || startText.equals("1999-01-01")) {
			return null;
		}
		try {
			LocalDate startDate = LocalDate.parse(startText,DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.of(00, 00));
			return TimeUtil.dateTimeToOleMinutes(startDateTime);
		} catch(Exception e) {
			LocalDateTime startDateTime = LocalDateTime.parse(startText,DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			return TimeUtil.dateTimeToOleMinutes(startDateTime);
		}
	}

	private static Long parseConfigDateEnd(String endText) {
		if(endText==null || endText.equals("*") || endText.equals("2099-12-31")) {
			return null;
		}
		try {
			LocalDate endDate = LocalDate.parse(endText,DateTimeFormatter.ISO_LOCAL_DATE);
			LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.of(23, 59));
			return TimeUtil.dateTimeToOleMinutes(endDateTime);
		} catch(Exception e) {
			LocalDateTime startDateTime = LocalDateTime.parse(endText,DateTimeFormatter.ISO_LOCAL_DATE_TIME);
			return TimeUtil.dateTimeToOleMinutes(startDateTime);
		}
	}

	public Long get_date_start() {
		return parseConfigDateStart(propertyMap.get(PROPERTY_START));
	}

	public Long get_date_end() {
		return parseConfigDateEnd(propertyMap.get(PROPERTY_END));
	}

	public String get_logger_type_name() {
		return propertyMap.get(PROPERTY_LOGGER);
	}

	public TimestampInterval<StationProperties> createTimestampInterval() {
		return new TimestampInterval<StationProperties>(this,get_date_start(),get_date_end());
	}

	public static List<TimestampInterval<StationProperties>> createIntervalList(List<StationProperties> list) {
		ArrayList<TimestampInterval<StationProperties>> resultList = new ArrayList<TimestampInterval<StationProperties>>(list.size());		
		for(StationProperties properties:list) {
			try {
				resultList.add(properties.createTimestampInterval());
			} catch(Exception e) {
				Logger.warn(e);
			}
		}		
		return resultList;
	}

	public String get_plotid() {
		return propertyMap.get(PROPERTY_PLOTID);
	}

	public String get_serial() {
		return propertyMap.get(PROPERTY_SERIAL);
	}

	private static final String[] NO_ALIASES = new String[0];

	public String[] get_aliases() {
		String aliasText = propertyMap.get(PROPERTY_ALIAS);
		if(aliasText == null) {
			return NO_ALIASES;
		}
		aliasText = aliasText.trim();
		if(aliasText.isEmpty()) {
			return NO_ALIASES;
		}
		return aliasText.split(";");
	}

	@Override
	public String toString() {
		return propertyMap.toString();
	}
}
