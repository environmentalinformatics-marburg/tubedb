package tsdb.component.labeledproperty;

import java.util.Map;
import java.util.Set;

import tsdb.util.Util;

public abstract class BooleanFormula {
	public abstract BooleanComputation compile(Map<String, Integer> sensorMap);
	public abstract String compileToString(Map<String, Integer> sensorMap);
	public BooleanComputation compile(String[] sensorNames) {
		return compile(Util.stringArrayToMap(sensorNames, true));
	}
	public String compileToString(String[] sensorNames) {
		return compileToString(Util.stringArrayToMap(sensorNames, true));
	}
	public abstract BooleanFormula not();
	public abstract void collectVariables(Set<String> collector);
}
