package tsdb.dsl;

import java.util.Map;

import org.tinylog.Logger;

import tsdb.dsl.formula.Formula;
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
		try {
			return sensorMap.get(name).intValue();
		} catch (RuntimeException e) {
			Logger.warn("sensor " + name + "  in  " + sensorMap.keySet());
			throw e;
		}
	}

	public boolean containsResolver(String name) {
		return false;
	}

	public Formula resolve(String name) {
		throw new RuntimeException("unknown variable "+name);
	}
}
