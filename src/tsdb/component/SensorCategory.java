package tsdb.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Type of sensor: used for visualization purposes
 * @author woellauer
 *
 */
public enum SensorCategory {	
	TEMPERATURE,
	WATER,
	OTHER;
	
	private static final Logger log = LogManager.getLogger();

	public static SensorCategory parse(String text) {
		switch(text.toLowerCase().trim()) {
		case "temperature":
			return TEMPERATURE;
		case "water":
			return WATER;
		case "other":
			return OTHER;
		default:
			log.warn("SensorCategory unknown: "+text);
			return OTHER;
		}
	}
}
