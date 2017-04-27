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
	@SuppressWarnings("unused")
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
	public AggregationType baseAggregationType;

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
		return baseAggregationType!= AggregationType.NONE;
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
			log.warn("could not read physicalRange of "+sensorName+"   "+e);
		}


		Sensor sensor = new Sensor(sensorName);
		sensor.description = description;
		sensor.unitDescription = unit;
		sensor.baseAggregationType = aggregation;
		sensor.physicalMin = physicalMin;
		sensor.physicalMax = physicalMax;

		return sensor;
	}


}
