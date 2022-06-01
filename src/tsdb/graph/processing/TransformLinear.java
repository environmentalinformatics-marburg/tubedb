package tsdb.graph.processing;

import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuous;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;


public class TransformLinear extends DelegateContinuous {

	private final float a;
	private final float b;

	protected TransformLinear(Continuous source, float a, float b) {
		super(source);
		this.a = a;
		this.b = b;
	}

	public static TransformLinear of(Continuous source, float a, float b) {
		return new TransformLinear(source, a, b);
	}

	@Override
	public TsIterator getExactly(long start, long end) {
		return get(start, end);
	}

	@Override
	public TsIterator get(Long start, Long end) {		
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}		
		InputProcessingIterator it = new InputProcessingIterator(input_iterator,input_iterator.getSchema()){
			@Override
			protected TsEntry getNext() {
				if(!input_iterator.hasNext()) {
					return null;
				}
				TsEntry element = input_iterator.next();
				float[] data = new float[element.data.length];
				for(int i=0;i<data.length;i++) {
					data[i] = a*element.data[i]+b;
				}
				return new TsEntry(element.timestamp, data);
			}

		};		
		if(it==null||!it.hasNext()) {
			return null;
		}
		return it;
	}
}
