package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;
import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

public class TransformLinear implements Continuous {
	
	private Continuous source;
	private final float a;
	private final float b;

	protected TransformLinear(Continuous source,float a,float b) {
		throwNull(source);
		this.source = source;
		this.a = a;
		this.b = b;
	}
	
	public static TransformLinear of(Continuous source,float a,float b) {
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

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public boolean isContinuous() {
		return source.isContinuous();
	}

	@Override
	public boolean isConstantTimestep() {
		return source.isConstantTimestep();
	}

	@Override
	public String[] getSchema() {
		return source.getSchema();
	}
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return source.getSourceVirtualPlot();
	}
	
	@Override
	public long[] getTimestampInterval() {
		return source.getTimestampInterval();
	}

}
