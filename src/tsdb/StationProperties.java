package tsdb;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;

/**
 * Properties of stations
 * @author woellauer
 *
 */
public class StationProperties implements Serializable{
	private static final long serialVersionUID = -4558930650676952510L;
	private static final Logger log = LogManager.getLogger();


	public final static String PROPERTY_START = "DATE_START";
	public final static String PROPERTY_END = "DATE_END";
	public final static String PROPERTY_LOGGER = "LOGGER";
	public final static String PROPERTY_PLOTID = "PLOTID";
	public final static String PROPERTY_SERIAL = "SERIAL";
	public final static String PROPERTY_TYPE = "TYPE"; //type: EP or VIP

	private final static String TYPE_VIP = "VIP";

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
				log.warn("error in read int: "+e+"  for propery  "+key+" and value  "+text);
				return null;
			}
		} else {
			log.warn("error in read int: not found for property "+key);
			return null;
		}
	}

	public float getFloatProperty(String key) {
		String text = propertyMap.get(key);
		if(text!=null) {
			try {
				return Float.parseFloat(text);
			} catch(Exception e) {
				log.warn("error in read float: "+e+"  for propery  "+key+" and value  "+text);
				return Float.NaN;
			}
		} else {
			log.warn("error in read float: not found for property "+key);
			return Float.NaN;
		}
	}

	private static Long parseConfigDateStart(String startText) {
		if(startText==null || startText.equals("*") || startText.equals("1999-01-01")) {
			return null;
		}
		LocalDate startDate = LocalDate.parse(startText,DateTimeFormatter.ISO_DATE);
		LocalDateTime startDateTime = LocalDateTime.of(startDate, LocalTime.of(00, 00));
		return TimeUtil.dateTimeToOleMinutes(startDateTime);
	}

	private static Long parseConfigDateEnd(String endText) {
		if(endText==null || endText.equals("*") || endText.equals("2099-12-31")) {
			return null;
		}
		LocalDate endDate = LocalDate.parse(endText,DateTimeFormatter.ISO_DATE);
		LocalDateTime endDateTime = LocalDateTime.of(endDate, LocalTime.of(23, 59));
		return TimeUtil.dateTimeToOleMinutes(endDateTime);
	}

	public Long get_date_start() {
		return parseConfigDateStart(propertyMap.get(PROPERTY_START));
	}

	public Long get_date_end() {
		return parseConfigDateEnd(propertyMap.get(PROPERTY_END));
	}

	public String get_logger_type_name() {
		return ConfigLoader.loggerPropertyKiLiToLoggerName(propertyMap.get(PROPERTY_LOGGER));
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
				log.warn(e);
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

	@Override
	public String toString() {
		return propertyMap.toString();
	}
}
