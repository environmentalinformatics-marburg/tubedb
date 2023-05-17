package tsdb.component.labeledproperty;

import java.util.Arrays;
import java.util.Collection;

import tsdb.util.DataRow;
import tsdb.util.Util;
import tsdb.util.yaml.YamlMap;

/**
 * Copy source to target name.
 * Fill target with NA if source name is "NA".
 *
 */
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
		if(Util.containsString(source, "NA")) { // one NA source
			int sensorNamesLen_plus_one = sensorNames.length + 1;
			int naPos = sensorNamesLen_plus_one - 1;
			String[] sensorNamesWithNA = Arrays.copyOf(sensorNames, sensorNamesLen_plus_one);
			sensorNamesWithNA[naPos] = "NA";
			int[] sourceIndex = Util.stringArrayToPositionIndexArray(source, sensorNamesWithNA, false, true);
			int[] targetIndex = Util.stringArrayToPositionIndexArray(target, sensorNames, false, true);
			int indexLen = sourceIndex.length;
			for(DataRow row:rows) {
				float[] data = row.data;
				float[] temp = Arrays.copyOf(data, sensorNamesLen_plus_one);
				temp[naPos] = Float.NaN;
				for (int i = 0; i < indexLen; i++) {
					data[targetIndex[i]] = temp[sourceIndex[i]];
				}
			}
		} else { //no NA source			
			int[] sourceIndex = Util.stringArrayToPositionIndexArray(source, sensorNames, false, true);
			int[] targetIndex = Util.stringArrayToPositionIndexArray(target, sensorNames, false, true);
			int sensorNamesLen = sensorNames.length;
			int indexLen = sourceIndex.length;
			for(DataRow row:rows) {
				float[] data = row.data;
				float[] temp = Arrays.copyOf(data, sensorNamesLen).clone();
				for (int i = 0; i < indexLen; i++) {
					data[targetIndex[i]] = temp[sourceIndex[i]];
				}
			}
		}
	}
}
