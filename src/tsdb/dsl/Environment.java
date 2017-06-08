package tsdb.dsl;

import java.util.Map;

import tsdb.util.Util;

public class Environment {
	public final Map<String, Integer> sensorMap;

	public Environment(Map<String, Integer> sensorMap) {
		this.sensorMap = sensorMap;
	}

	public Environment(String[] sensorNames) {
		this(Util.stringArrayToMap(sensorNames, true));
	}

	public boolean containsSensor(String name) {
		return sensorMap.containsKey(name);
	}

	public int getSensorIndex(String name) {
		return sensorMap.get(name).intValue();		
	}

	public boolean containsResolver(String name) {
		return false;
	}

	public Formula resolve(String name) {
		throw new RuntimeException("unknown variable "+name);
	}
}
