package tsdb.component.labeledproperty;

import java.util.Collection;

import tsdb.util.DataRow;
import tsdb.util.Util;
import tsdb.util.yaml.YamlMap;

public class PropertyClear {

	private final String[] target;

	public PropertyClear(String[] target) {
		this.target = target;
	}


	public static PropertyClear parse(YamlMap map) {		
		String[] target = map.optList("target").asStringArray();
		if(target.length == 0) {
			throw new RuntimeException("target is empty: "+map);
		}
		return new PropertyClear(target);
	}

	public void calculate(Collection<DataRow> rows, String[] sensorNames) {
		int[] targetIndex = Util.stringArrayToPositionIndexArray(target, sensorNames, false, true);
		int indexLen = targetIndex.length;
		for(DataRow row:rows) {
			float[] data = row.data;
			for (int i = 0; i < indexLen; i++) {
				data[targetIndex[i]] = Float.NaN;
			}
		}
	}
}
