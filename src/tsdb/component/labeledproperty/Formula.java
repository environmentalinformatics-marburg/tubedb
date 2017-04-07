package tsdb.component.labeledproperty;

import java.util.Map;

import tsdb.util.Util;

public abstract class Formula {
	public abstract Computation compile(Map<String, Integer> sensorMap);
	public Computation compile(String[] sensorNames) {
		return compile(Util.stringArrayToMap(sensorNames, true));
	}
}
