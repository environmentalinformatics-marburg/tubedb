package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;
import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Remove all values from time series within a time interval
 * @author woellauer
 *
 */
public class IntervalRemove implements Continuous {
	
	private final Continuous source;
	private final long removeStart;
	private final long removeEnd;
	
	protected IntervalRemove(Continuous source, long removeStart, long removeEnd) {
		throwNull(source);
		this.source = source;
		this.removeStart = removeStart;
		this.removeEnd = removeEnd;		
	}
	
	public static IntervalRemove of(Continuous source, long removeStart, long removeEnd) {
		return new IntervalRemove(source, removeStart, removeEnd);
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
				if(element.timestamp<removeStart || element.timestamp>removeEnd) {
					return element;
				} else {
					return new TsEntry(element.timestamp, TsEntry.createNanData(element.data.length));
				}
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