package tsdb.component.iterator;

import java.util.ArrayDeque;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

public abstract class TimeWindowIterator extends InputIterator {
	private static final Logger log = LogManager.getLogger();

	private int windowSizeMinutes;
	protected ArrayDeque<TsEntry> past; // all element are in time window at processElement call
	protected ArrayDeque<TsEntry> future; // all element are in time window at processElement call

	private boolean pastFilled = false;

	public TimeWindowIterator(TsIterator input_iterator, int windowSizeMinutes, TsSchema output_schema) {
		super(input_iterator, output_schema);
		this.windowSizeMinutes = windowSizeMinutes;
		past = new ArrayDeque<TsEntry>((windowSizeMinutes / 60) + 1); //hours
		future = new ArrayDeque<TsEntry>((windowSizeMinutes / 60) + 1); //hours		
		if(input_iterator.hasNext()) {
			future.addLast(input_iterator.next());
		}

	}

	@Override
	public boolean hasNext() {
		return !future.isEmpty();
	}

	@Override
	public final TsEntry next() {
		TsEntry current = future.pollFirst();
		//log.info("window pollFirst " + current.toString());
		long tmax = current.timestamp + windowSizeMinutes;
		TsEntry futureLastAdd = null;
		TsEntry futureLastPeeked = future.peekLast();
		if(futureLastPeeked == null || futureLastPeeked.timestamp < tmax) {
			while(input_iterator.hasNext()) {
				TsEntry e = input_iterator.next();
				if(e.timestamp > tmax) {
					futureLastAdd = e;
					break;
				}
				future.addLast(e);
			}
		}	

		long tmin = current.timestamp - windowSizeMinutes;
		TsEntry pastFirst = past.peekFirst();

		if(!pastFilled && pastFirst != null && pastFirst.timestamp < tmin) {
			pastFilled = true;
		}

		while(pastFirst != null && pastFirst.timestamp < tmin) {
			past.pollFirst();
			pastFirst = past.peekFirst();
		}

		TsEntry result = processElement(current);
		if(futureLastAdd != null) {
			future.addLast(futureLastAdd);
		}
		past.addLast(current);		
		return result;
	}

	public boolean isPastFilled() {
		return pastFilled;
	}

	protected abstract TsEntry processElement(TsEntry current);
}
