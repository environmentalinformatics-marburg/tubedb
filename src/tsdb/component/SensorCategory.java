package tsdb.component;


import org.tinylog.Logger;

/**
 * Type of sensor: used for visualization purposes
 * @author woellauer
 *
 */
public enum SensorCategory {	
	TEMPERATURE,
	WATER,
	OTHER;
	
	

	public static SensorCategory parse(String text) {
		switch(text.toLowerCase().trim()) {
		case "temperature":
			return TEMPERATURE;
		case "water":
			return WATER;
		case "other":
			return OTHER;
		default:
			Logger.warn("SensorCategory unknown: "+text);
			return OTHER;
		}
	}
	
	@Override
	public String toString() {
		switch(this) {
		case TEMPERATURE: 
			return "temperature";
		case WATER: 
			return "water";
		case OTHER: 
			return "other";		
		default:
			return "unknown";
		}
	}
}
