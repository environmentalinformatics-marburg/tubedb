package tsdb.component.labeledproperty;

import java.util.Arrays;
import java.util.Collection;

import tsdb.util.DataRow;
import tsdb.util.Util;
import tsdb.util.yaml.YamlMap;

public class PropertyReassign {

	private final String[] source;
	private final String[] target;

	public PropertyReassign(String[] source, String[] target) {
		this.source = source;
		this.target = target;
	}


	public static PropertyReassign parse(YamlMap map) {		
		String[] source = map.optList("source").asStringArray();
		String[] target = map.optList("target").asStringArray();
		if(source.length != target.length) {
			throw new RuntimeException("source and target need to be same length: "+map);
		}
		if(source.length == 0) {
			throw new RuntimeException("source is empty: "+map);
		}
		return new PropertyReassign(source, target);
	}

	public void calculate(Collection<DataRow> rows, String[] sensorNames) {
		int sensorNamesLen_1 = sensorNames.length;
		int naPos = sensorNamesLen_1 - 1;
		sensorNames = Arrays.copyOf(sensorNames, sensorNamesLen_1);
		sensorNames[naPos] = "NA";
		int[] sourceIndex = Util.stringArrayToPositionIndexArray(source, sensorNames, false, true);
		int[] targetIndex = Util.stringArrayToPositionIndexArray(target, sensorNames, false, true);
		int indexLen = sourceIndex.length;
		for(DataRow row:rows) {
			float[] data = row.data;
			float[] temp = Arrays.copyOf(data, sensorNamesLen_1);
			temp[naPos] = Float.NaN;
			for (int i = 0; i < indexLen; i++) {
				data[targetIndex[i]] = temp[sourceIndex[i]];
			}
		}
	}
}
