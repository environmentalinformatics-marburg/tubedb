package tsdb.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * aggregation type of sensor data
 * @author woellauer
 *
 */
public enum AggregationType {	
	NONE,	// no aggregation
	AVERAGE, // average of values
	SUM, // sum of values
	AVERAGE_WIND_DIRECTION, // special average aggregation for wind direction
	AVERAGE_WIND_VELOCITY,  // helper tag for calculation of AVERAGE_WIND_DIRECTION
	MINIMUM, // minimum of values
	MAXIMUM, // maximum of values
	AVERAGE_ZERO, // average of values, NaN values are interpreted as zero
	AVERAGE_ALBEDO, // average of values, special base aggregation for albedo
	SUM_SUNSHINE, // average of values, special base aggregation for sunshine (SD)
	SUM_OF_AVERAGE, // sum of values, average for base aggregation
	SUM_SECOND_TO_HOUR, // sum of values, unit second to hour conversion for base aggregation
	LAST; // most recent value of aggregation interval
	
	private static final Logger log = LogManager.getLogger();

	public static AggregationType parse(String aggregateTypeText) {
		switch(aggregateTypeText.toLowerCase()) {
		case "average":
			return AVERAGE;
		case "sum":
			return SUM;
		case "average_wind_direction":
			return AVERAGE_WIND_DIRECTION;
		case "average_wind_velocity":
			return AVERAGE_WIND_VELOCITY;
		case "minimum":
			return MINIMUM;
		case "maximum":
			return MAXIMUM;
		case "average_zero":
			return AVERAGE_ZERO;
		case "average_albedo":
			return AVERAGE_ALBEDO;
		case "none":
			return NONE;
		case "sum_sunshine":			
			return SUM_SUNSHINE;
		case "sum_of_average":
			return SUM_OF_AVERAGE;
		case "sum_second_to_hour":
			return SUM_SECOND_TO_HOUR;
		case "last":
			return LAST;				
		default:
			log.warn("unknown aggregation: "+aggregateTypeText);
			return null;
		}
	}
	
	@Override
	public String toString() {
		switch(this) {
		case NONE: 
			return "none";
		case AVERAGE: 
			return "average";
		case SUM: 
			return "sum";
		case AVERAGE_WIND_DIRECTION: 
			return "average_wind_direction";
		case AVERAGE_WIND_VELOCITY: 
			return "average_wind_velocity";
		case MINIMUM: 
			return "minimum";
		case MAXIMUM: 
			return "maximum";
		case AVERAGE_ZERO: 
			return "average_zero";
		case AVERAGE_ALBEDO: 
			return "average_albedo";
		case SUM_SUNSHINE: 
			return "sum_sunshine";
		case SUM_OF_AVERAGE: 
			return "sum_second_to_hour";
		case SUM_SECOND_TO_HOUR: 
			return "sum_second_to_hour";
		case LAST: 
			return "last";
		default:
			return "unknown";
		}
	}
}