package tsdb.iterator;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.AssumptionCheck;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

public class SunshineOlivieriIterator extends InputIterator {
	private static final Logger log = LogManager.getLogger();
	
	private static final double MIN_SOLAR_ELEVATION_ANGLE_DEG = 3d;
	private static final double MIN_SOLAR_ELEVATION_ANGLE_RAD = Math.toRadians(MIN_SOLAR_ELEVATION_ANGLE_DEG);
	private static final double MIN_SIN_SOLAR_ELEVATION_ANGLE = Math.sin(MIN_SOLAR_ELEVATION_ANGLE_RAD);
	private static final double DECLINATION_CONST_DEG = 23.45d;
	private static final double DECLINATION_CONST_RAD = Math.toRadians(DECLINATION_CONST_DEG);
	private static final double[] DECLINATION_RAD = new double[367];
	private static final double[] SIN_DECLINATION_RAD = new double[367];
	private static final double[] COS_DECLINATION_RAD = new double[367];
	
	private static final double A = 0.73;
	private static final double B = 0.06;	
	private static final double[] F = new double[367];
	
	static {
		for (int day = 0; day < 367; day++) {
			DECLINATION_RAD[day] = DECLINATION_CONST_RAD * Math.sin(2d * Math.PI * (day + 284d) / 365d);
			SIN_DECLINATION_RAD[day] = Math.sin(DECLINATION_RAD[day]);
			COS_DECLINATION_RAD[day] = Math.cos(DECLINATION_RAD[day]);
			F[day] = A + B * Math.cos(2d * Math.PI * day / 365d);
		}
	}

	public static final String RADIATION_SENSOR_NAME = "SWDR";
	public static final String SUNSHINE_SENSOR_NAME = "SD_Olivieri";
	private int sensor_pos = -1;
	private final double latitude_DEG;
	private final double longitude_DEG;
	private final double latitude_RAD;
	//private final double longitude_RAD;
	private final double sin_latitude;
	private final double cos_latitude;
	
	

	public SunshineOlivieriIterator(TsIterator input_iterator, double latitude_DEG, double longitude_DEG) {
		super(input_iterator, input_iterator.getSchema());
		
		this.latitude_DEG = 51.079;
		this.longitude_DEG = 10.460;
		this.latitude_RAD = Math.toRadians(this.latitude_DEG);
		//this.longitude_RAD = Math.toRadians(this.longitude_DEG);
		this.sin_latitude = Math.sin(this.latitude_RAD);
		this.cos_latitude = Math.cos(this.latitude_RAD);
		
		String[] names = this.getNames();
		for(int i=0;i<names.length;i++) {
			if(names[i].equals(SUNSHINE_SENSOR_NAME)) {
				sensor_pos = i;
				break;
			}
		}
		AssumptionCheck.throwTrue(sensor_pos<0,"sensor not found for SunshineOlivieriIterator");		
	}

	@Override
	public TsEntry next() {
		TsEntry entry = input_iterator.next();
		float[] data = Arrays.copyOf(entry.data, entry.data.length);
		float value = entry.data[sensor_pos];
		data[sensor_pos] = Float.isNaN(value)?Float.NaN:calc((int) entry.timestamp, value);
		DataQuality[] qf;
		if(entry.qualityFlag!=null) {
			qf = Arrays.copyOf(entry.qualityFlag, entry.qualityFlag.length);
		} else {
			qf = null;
		}
		return new TsEntry(entry.timestamp, data, qf);
	}

	public float calc(long timestamp, float value) {		
		LocalDateTime dateTime = TimeUtil.oleMinutesToLocalDateTime(timestamp);
		int day = dateTime.getDayOfYear(); // 1-366 for leap year
		double hour = dateTime.getHour() + dateTime.getMinute() / 60d; // 0.0 - 23.9833
		
		double equation_of_time = 0d - 1d;
		double solar_hour_angle_RAD = Math.toRadians(15d * (hour + longitude_DEG / 15d + equation_of_time - 12d));
		double sin_solar_elevation_angle = sin_latitude * SIN_DECLINATION_RAD[day] + cos_latitude * COS_DECLINATION_RAD[day] * Math.cos(solar_hour_angle_RAD);
		if(sin_solar_elevation_angle < MIN_SIN_SOLAR_ELEVATION_ANGLE) {
			return 0f;
		}
		double G0 = 1080d * Math.pow(sin_solar_elevation_angle, 1.25d);
		double ref = G0 * F[day];
		float res = value < ref ? 0f : 1f;
		//log.info(ref+"   v "+value+"  ->  "+res+"    "+Double.isNaN(G0));
		
		//return (float) F; // OK
		//return (float) Math.toDegrees(solar_declination_RAD); // OK
		//return (float) Math.toDegrees(solar_hour_angle_RAD); // OK (no EQ)
		//return (float) Math.toDegrees(Math.asin(sin_solar_elevation_angle)); //  (no EQ)
		//return (float) G0; // OK (no EQ)
		return res;
	}

}
