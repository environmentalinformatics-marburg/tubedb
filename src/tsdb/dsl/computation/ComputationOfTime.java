package tsdb.dsl.computation;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import tsdb.util.Computation;
import tsdb.util.TimeUtil;

public class ComputationOfTime {
	
	public static final String[] NON_DATA_VARIABLES = new String[]{
			"year",
			"year_float",
			"year_fraction",
			"month", 
			"days_of_month", 
			"day_of_month", 
			"day_of_month_float", 
			"days_of_year", 
			"day_of_year", 
			"day_of_year_float", 
			"day_fraction",  
			"hour",
			"sun"
	};
	
	public static final HashSet<String> NON_DATA_VARIABLES_SET = new HashSet<String>(Arrays.asList(ComputationOfTime.NON_DATA_VARIABLES));
	
	public static final Computation YEAR = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			return TimeUtil.oleMinutesToLocalDateTime(timestamp).getYear();	
		}		
	};
	
	public static final Computation YEAR_FLOAT = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
			return d.getYear() + (d.getDayOfYear()-1+d.getHour()/24f) / d.toLocalDate().lengthOfYear();	
		}		
	};
	
	public static final Computation YEAR_FRACTION = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
			return (d.getDayOfYear()-1+d.getHour()/24f) / d.toLocalDate().lengthOfYear();
		}		
	};
	
	public static final Computation MONTH = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			return TimeUtil.oleMinutesToLocalDateTime(timestamp).getMonthValue();
		}		
	};
	
	public static final Computation DAY_OF_MONTH = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			return TimeUtil.oleMinutesToLocalDateTime(timestamp).getDayOfMonth();
		}		
	};
	
	public static final Computation DAYS_OF_MONTH = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			return TimeUtil.oleMinutesToLocalDateTime(timestamp).toLocalDate().lengthOfMonth();	
		}		
	};
	
	public static final Computation DAY_OF_MONTH_FLOAT = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
			return d.getDayOfMonth()+d.getHour()/24f;			
		}		
	};
	
	public static final Computation DAY_OF_YEAR = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			return TimeUtil.oleMinutesToLocalDateTime(timestamp).getDayOfYear();	
		}		
	};
	
	public static final Computation DAYS_OF_YEAR = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			return TimeUtil.oleMinutesToLocalDateTime(timestamp).toLocalDate().lengthOfYear();	
		}		
	};
	
	public static final Computation DAYS_OF_YEAR_FLOAT = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			LocalDateTime d = TimeUtil.oleMinutesToLocalDateTime(timestamp);
			return d.getDayOfYear()+d.getHour()/24f;		
		}		
	};
	
	public static final Computation DAY_FRACTION = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			return TimeUtil.oleMinutesToLocalDateTime(timestamp).getHour()/24f;			
		}		
	};
	
	public static final Computation HOUR = new Computation() {
		@Override
		public float eval(long timestamp, float[] data) {
			return TimeUtil.oleMinutesToLocalDateTime(timestamp).getHour();		
		}		
	};
	
	public static final Computation SUN = new Sun(51.079, 10.460);
	
	static class Sun extends Computation {
		private static final double[] SIN_DECLINATION_RAD = new double[367];
		private static final double DECLINATION_CONST_DEG = 23.45d;
		private static final double DECLINATION_CONST_RAD = Math.toRadians(DECLINATION_CONST_DEG);
		private static final double[] COS_DECLINATION_RAD = new double[367];
		private final double longitude_DEG;
		private final double sin_latitude;
		private final double cos_latitude;
		
		static {
			for (int day = 0; day < 367; day++) {
				double declination_rad = DECLINATION_CONST_RAD * Math.sin(2d * Math.PI * (day + 284d) / 365d);
				SIN_DECLINATION_RAD[day] = Math.sin(declination_rad);
				COS_DECLINATION_RAD[day] = Math.cos(declination_rad);
			}
		}
		
		public Sun(double latitude_DEG, double longitude_DEG) {
			this.longitude_DEG = longitude_DEG;
			double latitude_RAD = Math.toRadians(latitude_DEG);
			this.sin_latitude = Math.sin(latitude_RAD);
			this.cos_latitude = Math.cos(latitude_RAD);
		}	

		@Override
		public float eval(long timestamp, float[] data) {
			
			LocalDateTime dateTime = TimeUtil.oleMinutesToLocalDateTime(timestamp);
			int day = dateTime.getDayOfYear(); // 1-366 for leap year
			double hour = dateTime.getHour() + dateTime.getMinute() / 60d; // 0.0 - 23.9833
			
			double equation_of_time = 0d - 1d;
			double solar_hour_angle_RAD = Math.toRadians(15d * (hour + longitude_DEG / 15d + equation_of_time - 12d));
			double sin_solar_elevation_angle = sin_latitude * SIN_DECLINATION_RAD[day] + cos_latitude * COS_DECLINATION_RAD[day] * Math.cos(solar_hour_angle_RAD);
			
			float elev = (float) Math.toDegrees(Math.asin(sin_solar_elevation_angle));
			
			return elev < 0 ? 0 : elev;		
		}

	
	};


	public static Computation compileVar(String name, boolean positive) {		
		switch(name) {
		case "year":
			return ComputationNeg.wrap(YEAR, positive);
		case "year_float":
			return ComputationNeg.wrap(YEAR_FLOAT, positive);
		case "year_fraction":
			return ComputationNeg.wrap(YEAR_FRACTION, positive);			
		case "month":
			return ComputationNeg.wrap(MONTH, positive);
		case "day_of_month":
			return ComputationNeg.wrap(DAY_OF_MONTH, positive);
		case "days_of_month":
			return ComputationNeg.wrap(DAYS_OF_MONTH, positive);		
		case "day_of_month_float":
			return ComputationNeg.wrap(DAY_OF_MONTH_FLOAT, positive);		
		case "day_of_year":
			return ComputationNeg.wrap(DAY_OF_YEAR, positive);	
		case "days_of_year":
			return ComputationNeg.wrap(DAYS_OF_YEAR, positive);	
		case "day_of_year_float":
			return ComputationNeg.wrap(DAYS_OF_YEAR_FLOAT, positive);	
		case "day_fraction":
			return ComputationNeg.wrap(DAY_FRACTION, positive);
		case "hour":
			return ComputationNeg.wrap(HOUR, positive);		
		case "sun":
			return ComputationNeg.wrap(SUN, positive);				
		default:
			return null;
		}		
	}
}
