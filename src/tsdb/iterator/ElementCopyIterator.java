package tsdb.iterator;

import java.util.Arrays;

import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

public class ElementCopyIterator extends InputIterator {

	public static class Action {
		public final int sourceIndex;
		public final int targetIndex;
		private Action(int sourceIndex, int targetIndex) {
			this.sourceIndex = sourceIndex;
			this.targetIndex = targetIndex;
		}
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
	}

	private final Action[] actions;	

	public ElementCopyIterator(TsIterator input_iterator, Action[] actions) {
		super(input_iterator, input_iterator.getSchema());
		this.actions = actions;	
	}

	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
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
		return new TsEntry(entry.timestamp, data, qf);
	}
}