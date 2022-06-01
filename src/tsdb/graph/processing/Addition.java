package tsdb.graph.processing;

import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuous;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Node: adds a value to all entries of source
 * @author woellauer
 *
 */
public class Addition extends DelegateContinuous {
	
	private final float value;
	
	protected Addition(Continuous source, float value) {
		super(source);
		this.value = value;
	}
	
	public static Addition of(Continuous source, float value) {
		return new Addition(source, value);
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
				for(int i = 0; i < data.length; i++) {
					result[i] = data[i] + value;
				}
				return new TsEntry(element.timestamp, result);
			}			
		};
	}	
}
