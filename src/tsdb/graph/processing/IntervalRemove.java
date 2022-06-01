package tsdb.graph.processing;

import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuous;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Remove all values from time series within a time interval
 * @author woellauer
 *
 */
public class IntervalRemove extends DelegateContinuous {
	
	private final long removeStart;
	private final long removeEnd;
	
	protected IntervalRemove(Continuous source, long removeStart, long removeEnd) {
		super(source);		
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
}