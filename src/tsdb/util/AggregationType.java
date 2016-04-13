package tsdb.util;

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
	SUM_RADIATION; // sum of values, special average for base aggregation - used for radiation sensors ( W/m^2 -> Wh/m^2)

	public static AggregationType getAggregationType(String aggregateTypeText) {
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
		case "sum_radiation":
			return SUM_RADIATION;						
		default:
			return null;
		}
	}
}