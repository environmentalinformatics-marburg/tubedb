package tsdb.component;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.AggregationType;
import tsdb.util.yaml.YamlList;
import tsdb.util.yaml.YamlMap;

/**
 * This class contains metadata that is associated with a sensor.
 * @author woellauer
 *
 */
public class Sensor implements Serializable {
	private static final Logger log = LogManager.getLogger();

	private static final long serialVersionUID = -4139931796468207965L;

	/**
	 * parameter name of this sensor
	 */
	public final String name;

	/**
	 * physical minimum
	 */
	public float physicalMin;

	/**
	 * physical maximum
	 */
	public float physicalMax;

	/**
	 * minimal change within a base aggregation time interval
	 */
	public float stepMin;

	/**
	 * maximum change within a base aggregation time interval
	 */
	public float stepMax;

	/**
	 * Type of aggregation for base aggregation
	 */
	private AggregationType baseAggregationType;

	/**
	 * fill gaps in time series of this sensor
	 */
	public boolean useInterpolation;

	/**
	 * maximum difference of empirical value, may be null if not used.
	 */
	public Float empiricalDiff;

	public String description;

	public String unitDescription;

	public SensorCategory category;

	public boolean internal;

	public double maxInterpolationMSE;
	
	public String[] raw_source = null;
	public String[] dependency = null;
	public String post_hour_func = null;
	public String post_day_func = null;

	public Sensor(String name) {
		this.name = name;
		physicalMin = -Float.MAX_VALUE;
		physicalMax = Float.MAX_VALUE;
		stepMin = 0.0f;
		stepMax = Float.MAX_VALUE;
		baseAggregationType = AggregationType.NONE;
		useInterpolation = false;
		empiricalDiff = null;
		category = SensorCategory.OTHER;
		internal = false;
		maxInterpolationMSE = 1f;
	}

	/**
	 * checks if value is in physical range
	 * @param value  value == NaN  ==> false
	 * @return if false value should not be included in further processing
	 */
	public boolean checkPhysicalRange(float value) {
		//log.info("check physical "+physicalMin+"  "+physicalMax);
		if(Float.isNaN(value)) {
			return false;
		}
		return physicalMin<=value&&value<=physicalMax;		
	}

	/**
	 * precondition: prevValue and value are valid (no NaN values)
	 * @param prevValue
	 * @param value
	 * @return
	 */
	public boolean checkStepRange(float prevValue, float value) {
		//log.info("check step "+stepMin+"  "+stepMax);
		float step = Math.abs(value-prevValue);		
		return stepMin<=step&&step<=stepMax;
	}

	public String getName() {
		return name;
	}

	public float getPhysicalMin() {
		return physicalMin;
	}

	public float getPhysicalMax() {
		return physicalMax;
	}

	public float getStepMin() {
		return stepMin;
	}

	public float getStepMax() {
		return stepMax;
	}

	public Float getEmpiricalDiff() {
		return empiricalDiff;
	}

	public boolean isAggregable() {
		return getAggregationHour() != AggregationType.NONE;
	}

	public double getMaxInterpolationMSE() {
		return maxInterpolationMSE;
	}

	private static float[] parseYamlRange(Object o) {
		if(o == null) {
			return null;
		}
		float[] floats = new YamlList(o).asFloatArray();
		if(floats.length == 2) {
			return floats;
		} else {
			throw new RuntimeException("range need two elements: "+Arrays.toString(floats));
		}
	}

	public static Sensor ofYaml(String sensorName, YamlMap yamlMap) {
		String description = yamlMap.optString("description", "no description");
		String unit = yamlMap.optString("unit", "no unit");
		AggregationType agg = AggregationType.getAggregationType(yamlMap.optString("aggregation", "none"));
		AggregationType aggregation = agg == null ? AggregationType.NONE : agg;

		float physicalMin = -Float.MAX_VALUE;
		float physicalMax = Float.MAX_VALUE;

		try {
			float[] range = parseYamlRange(yamlMap.optObject("physical_range"));
			if(range != null) {
				physicalMin = range[0];
				physicalMax = range[1];
			}
		} catch(Exception e) {
			log.warn("could not read physical range of "+sensorName+"   "+e);
		}

		float stepMin = -Float.MAX_VALUE;
		float stepMax = Float.MAX_VALUE;

		try {
			float[] range = parseYamlRange(yamlMap.optObject("step_range"));
			if(range != null) {
				stepMin = range[0];
				stepMax = range[1];
			}
		} catch(Exception e) {
			log.warn("could not read step range of "+sensorName+"   "+e);
		}

		Float empiricalDiff = null;
		try {
			empiricalDiff = yamlMap.optFloat("empirical_diff", null);
		} catch(Exception e) {
			log.warn("could not read empirical_diff of "+sensorName+"   "+e);
		}

		boolean[] useInterpolation = new boolean[]{false};
		double[] interpolation_mse = new double[]{1d};

		yamlMap.funDouble("interpolation_mse", 
				mse -> {useInterpolation[0] = true; interpolation_mse[0] = mse;}, 
				e -> log.warn("could not read interpolation_mse of "+sensorName+"   "+e)
				);
		
		SensorCategory category = SensorCategory.parse(yamlMap.optString("category", "other"));
		boolean internal = yamlMap.optString("visibility", "public").equals("internal");
				
		String[] raw_source = yamlMap.optList("raw_source").asStringArray();
		String[] dependency = yamlMap.optList("dependency").asStringArray();
		
		String post_hour_func = yamlMap.optString("post_hour_func", null);
		String post_day_func = yamlMap.optString("post_day_func", null);

		Sensor sensor = new Sensor(sensorName);
		sensor.description = description;
		sensor.unitDescription = unit;
		sensor.setAggregation(aggregation);
		sensor.physicalMin = physicalMin;
		sensor.physicalMax = physicalMax;
		sensor.stepMin = stepMin;
		sensor.stepMax = stepMax;
		sensor.empiricalDiff = empiricalDiff;
		sensor.useInterpolation = useInterpolation[0];
		sensor.maxInterpolationMSE = interpolation_mse[0];
		sensor.category = category;
		sensor.internal = internal;
		sensor.raw_source = raw_source.length == 0 ? null : raw_source;
		sensor.dependency = dependency.length == 0 ? null : dependency;
		sensor.post_hour_func = post_hour_func;
		sensor.post_day_func = post_day_func;

		return sensor;
	}
	
	public AggregationType getAggregationHour() {
		return baseAggregationType;	
	}

	public AggregationType getAggregationDay() {
		return baseAggregationType;		
	}

	public AggregationType getAggregationWeek() {
		return baseAggregationType;	
	}

	public AggregationType getAggregationMonth() {
		return baseAggregationType;	
	}

	public AggregationType getAggregationYear() {
		return baseAggregationType;	
	}
	
	public void setAggregation(AggregationType agg) {
		baseAggregationType = agg;		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sensor other = (Sensor) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
