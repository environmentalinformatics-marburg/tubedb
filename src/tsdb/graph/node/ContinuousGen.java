package tsdb.graph.node;

/**
 * Interface of generator functions of continuous nodes
 * @author woellauer
 *
 */
@FunctionalInterface
public interface ContinuousGen {
	
	public Continuous get(String stationName, String[] schema);
	
	public default Continuous getWithSensorNames(String stationName, String... sensorNames) {
		return get(stationName,sensorNames);
	}
}
