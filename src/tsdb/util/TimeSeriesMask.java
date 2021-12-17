package tsdb.util;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;


import org.tinylog.Logger;
import org.mapdb.Serializer;

/**
 * Contains list of intervals of time that should be masked out.
 * <p>
 * A mask defines parts of time series data that is (manually) marked as invalid data.
 * @author woellauer
 *
 */
public class TimeSeriesMask implements Externalizable {
	

	private ArrayList<Interval> intervals;

	/**
	 * Creates empty mask.
	 */
	public TimeSeriesMask() {
		this(new ArrayList<Interval>());
	}

	/**
	 * Creates mask with list of intervals
	 * @param intervals
	 */
	private TimeSeriesMask(ArrayList<Interval> intervals) {
		throwNull(intervals);
		this.intervals = intervals;
	}

	/**
	 * Get list of intervals.
	 * @return
	 */
	public ArrayList<Interval> getIntervals() {
		return intervals;
	}

	/**
	 * Insert new interval to mask.
	 * <p>
	 * Interval is added to list of intervals possibly merged with existing intervals.
	 * @param interval
	 */
	public void addInterval(Interval interval) {
		Logger.trace("add "+interval);
		Logger.trace("in "+intervals);
		throwNull(interval);
		ArrayList<Interval> result = new ArrayList<Interval>(intervals.size()+1);
		Iterator<Interval> it = intervals.iterator();
		Interval current;
		if(it.hasNext()) {
			current = it.next();
		} else {
			current = null;
		}

		while(interval!=null||current!=null) {
			//Logger.info("now intverval "+interval+" current  "+current);

			if(interval==null) { // current!=null
				result.add(current);
				if(it.hasNext()) {
					current = it.next();
				} else {
					current = null;
				}
			} else if(current==null) { //interval!=null
				result.add(interval);
				interval = null;
			} else { // interval!=null && current!=null
				if(Interval.isAdjacentOrOverlap(interval, current)) {
					interval = Interval.getEnvelope(interval, current);
					//result.add(current);
					if(it.hasNext()) {
						current = it.next();
					} else {
						current = null;
					}
				} else if(interval.less(current)) {
					result.add(interval);
					interval = null;
				} else if(current.less(interval)) {
					result.add(current);
					if(it.hasNext()) {
						current = it.next();
					} else {
						current = null;
					}
				} else {
					throw new RuntimeException("algorithm error");
				}
			}
		}		
		intervals = result;
		Logger.trace("intervals "+intervals.size());
	}

	public Iterator<Interval> getIterator() {
		return intervals.iterator();
	}
	
	public boolean isEmpty() {
		return intervals.isEmpty();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SERIALIZER.serialize(out, this);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException {
		TimeSeriesMask tsm = SERIALIZER.deserialize(in, -1);
		this.intervals = tsm.intervals;
	}	

	private static class TimeSeriesMaskSerializer implements Serializer<TimeSeriesMask>, Serializable {
		private static final long serialVersionUID = -537733926693978436L;
		@Override
		public void serialize(DataOutput out, TimeSeriesMask value) throws IOException {
			final int size = value.intervals.size();
			out.writeInt(size);
			int written=0;
			for(Interval interval:value.intervals) {
				Interval.SERIALIZER.serialize(out, interval);
				written++;
			}
			if(written!=size) {
				throw new RuntimeException("write error");
			}
		}
		@Override
		public TimeSeriesMask deserialize(DataInput in, int available) throws IOException {
			final int size = in.readInt();
			ArrayList<Interval> intervals = new ArrayList<Interval>(size);
			for(int i=0;i<size;i++) {
				intervals.add(Interval.SERIALIZER.deserialize(in, 8)); //??
			}
			return new TimeSeriesMask(intervals);
		}
		@Override		
		public int fixedSize() {
			return -1;
		}
	};

	public static final Serializer<TimeSeriesMask> SERIALIZER = new TimeSeriesMaskSerializer();
	
	@Override
	public String toString() {
		return "TimeSeriesMask [intervals=" + intervals + "]";
	}
}
