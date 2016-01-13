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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.Serializer;

public class TimeSeriesMask implements Externalizable {
	private static final Logger log = LogManager.getLogger();

	private ArrayList<Interval> intervals;

	public TimeSeriesMask() {
		this(new ArrayList<Interval>());
	}

	private TimeSeriesMask(ArrayList<Interval> intervals) {
		throwNull(intervals);
		this.intervals = intervals;
	}

	public ArrayList<Interval> getIntervals() {
		return intervals;
	}

	public void addInterval(Interval interval) {
		log.info("add "+interval);
		log.info("in "+intervals);
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
			//log.info("now intverval "+interval+" current  "+current);

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
		log.info("intervals "+intervals.size());
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
