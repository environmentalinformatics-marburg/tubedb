package tsdb.iterator;

import java.util.Arrays;

import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Copies value data to different columns.
 * <p>
 * Meta data (quality flags, quality counter, interpolation flag) is copied together with values if present.
 * @author woellauer
 *
 */
public class ElementCopyIterator extends InputIterator {

	/**
	 * Action defines one copy operation from one column to an other column for all entries in iterator.
	 * @author woellauer
	 *
	 */
	public static class Action {
		public final int sourceIndex;
		public final int targetIndex;

		/**
		 * Creates one copy operation with source and target-index.
		 * @param sourceIndex
		 * @param targetIndex
		 */
		private Action(int sourceIndex, int targetIndex) {
			this.sourceIndex = sourceIndex;
			this.targetIndex = targetIndex;
		}

		/**
		 * Creates one copy operation with source and target-column-name.
		 * @param schema
		 * @param sourceName
		 * @param targetName
		 * @return
		 */
		public static Action of(String[] schema, String sourceName, String targetName) {
			int source = -1;
			int target = -1;
			for (int i = 0; i < schema.length; i++) {
				if(schema[i].equals(sourceName)) {
					source = i;					
				}
				if(schema[i].equals(targetName)) {
					target = i;					
				}
			}
			if(source<0 || target<0) {
				throw new RuntimeException("names not found in schema");
			}
			return new Action(source, target);
		}

		/**
		 * Creates one copy operation with one of sourceNames and target-column-name.
		 * First in schema contained sourceName will be used.
		 * @param schema
		 * @param sourceName
		 * @param targetName
		 * @return
		 */
		public static Action of(String[] schema, String[] sourceNames, String targetName) {
			int source = -1;
			searchSourceLoop: for(String sourceName:sourceNames) {
				for (int i = 0; i < schema.length; i++) {
					if(schema[i].equals(sourceName)) {
						source = i;
						break searchSourceLoop;
					}
				}
			}
			int target = -1;
			for (int i = 0; i < schema.length; i++) {
				if(schema[i].equals(targetName)) {
					target = i;					
				}
			}
			if(source<0 || target<0) {
				throw new RuntimeException("names not found in schema: "+Arrays.toString(schema)+" sourceNames: "+Arrays.toString(sourceNames)+" targetName: "+targetName);
			}
			return new Action(source, target);
		}
	}

	private final Action[] actions;	

	/**
	 * Creates copy iterator with array of copy actions, that are applied to all entries in iterator.
	 * @param input_iterator
	 * @param actions
	 */
	public ElementCopyIterator(TsIterator input_iterator, Action[] actions) {
		super(input_iterator, input_iterator.getSchema());
		this.actions = actions;	
	}

	@Override
	public TsEntry next() {
		TsEntry entry = input_iterator.next();
		float[] data = Arrays.copyOf(entry.data, entry.data.length);
		for(Action action:actions) {
			data[action.targetIndex] = data[action.sourceIndex];
		}
		DataQuality[] qf;
		if(entry.qualityFlag!=null) {
			qf = Arrays.copyOf(entry.qualityFlag, entry.qualityFlag.length);
			for(Action action:actions) {
				qf[action.targetIndex] = qf[action.sourceIndex];
			}
		} else {
			qf = null;
		}
		int[][] qc;
		if(entry.qualityCounter!=null) {
			qc = Arrays.copyOf(entry.qualityCounter, entry.qualityCounter.length); //copy pointers to sub arrays (read only)
			for(Action action:actions) {
				qc[action.targetIndex] = qc[action.sourceIndex];
			}
		} else {
			qc = null;
		}
		boolean[] inter;
		if(entry.interpolated!=null) {
			inter = Arrays.copyOf(entry.interpolated, entry.interpolated.length);
			for(Action action:actions) {
				inter[action.targetIndex] = inter[action.sourceIndex];
			}
		} else {
			inter = null;
		}		
		return new TsEntry(entry.timestamp, data, qf, qc, inter);
	}
}