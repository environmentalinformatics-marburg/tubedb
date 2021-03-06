package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Node: adds a value to first entry of source
 * @author woellauer
 *
 */
public class AdditionFirst implements Continuous {
	
	private final Continuous source;
	private final float value;
	
	protected AdditionFirst(Continuous source, float value) {
		throwNull(source);
		this.source = source;
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

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
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
	public TsIterator getExactly(long start, long end) {
		return get(start,end);
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
