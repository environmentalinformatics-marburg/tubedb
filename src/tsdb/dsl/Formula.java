package tsdb.dsl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import tsdb.util.Util;

public abstract class Formula {
	public abstract Computation compile(Map<String, Integer> sensorMap);
	public abstract String compileToString(Map<String, Integer> sensorMap);
	public final Computation compile(String[] sensorNames) {
		return compile(Util.stringArrayToMap(sensorNames, true));
	}
	public final String compileToString(String[] sensorNames) {
		return compileToString(Util.stringArrayToMap(sensorNames, true));
	}
	public abstract void collectDataVariables(Set<String> collector);
	public final Set<String> getDataVariables() {
		LinkedHashSet<String> collector = new LinkedHashSet<String>();
		collectDataVariables(collector);
		return collector;
	}
	public final int[] getDataVariableIndices(HashMap<String, Integer> sensorMap) {
		return getDataVariables().stream().mapToInt(name -> sensorMap.get(name)).toArray();
	}
}
