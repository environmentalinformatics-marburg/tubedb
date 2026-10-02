package tsdb.component.labeledproperty;

import java.util.Arrays;
import java.util.Collection;

import tsdb.util.DataRow;
import tsdb.util.Util;
import tsdb.util.yaml.YamlMap;

/**
 * Write source to target in order if source value is not NA.
 *
 */
public class PropertyOverwrite {

	private final String[] source;
	private final String[] target;

	public PropertyOverwrite(String[] source, String[] target) {
		this.source = source;
		this.target = target;
	}

	public static PropertyOverwrite parse(YamlMap map) {		
		String[] source = map.optList("source").asStringArray();
		String[] target = map.optList("target").asStringArray();
		if(source.length != target.length) {
			throw new RuntimeException("source and target need to be same length: "+map);
		}
		if(source.length == 0) {
			throw new RuntimeException("source is empty: "+map);
		}
		return new PropertyOverwrite(source, target);
	}

	public void calculate(Collection<DataRow> rows, String[] sensorNames) {
		if(Util.containsString(source, "NA")) { // one NA source
			throw new RuntimeException("NA sensor");
		} else { //no NA source			
			int[] sourceIndex = Util.stringArrayToPositionIndexArray(source, sensorNames, false, true);
			int[] targetIndex = Util.stringArrayToPositionIndexArray(target, sensorNames, false, true);
			int sensorNamesLen = sensorNames.length;
			int indexLen = sourceIndex.length;
			for(DataRow row:rows) {
				float[] data = row.data;
				float[] temp = Arrays.copyOf(data, sensorNamesLen).clone();
				for (int i = 0; i < indexLen; i++) {
					float v = temp[sourceIndex[i]];
					if(Float.isFinite(v)) {
						data[targetIndex[i]] = v;
					}
				}
			}
		}
	}
}
