package tsdb.graph.processing;

import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuous;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Node: adds a value to first entry of source
 * @author woellauer
 *
 */
public class AdditionFirst extends DelegateContinuous {
	
	private final float value;
	
	protected AdditionFirst(Continuous source, float value) {
		super(source);
		this.value = value;
	}
	
	public static AdditionFirst of(Continuous source, float value) {
		return new AdditionFirst(source, value);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		
		return new InputProcessingIterator(input_iterator,input_iterator.getSchema()) {
			@Override
			protected TsEntry getNext() {
				if(!input_iterator.hasNext()) {
					return null;
				}
				TsEntry element = input_iterator.next();
				float[] data = element.data;
				float[] result = new float[data.length];
				result[0] = data[0] + value;
				for(int i = 1; i < data.length; i++) {
					result[i] = data[i];
				}
				return new TsEntry(element.timestamp, result);
			}			
		};
	}	
}
