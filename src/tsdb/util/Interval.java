package tsdb.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.Serializer;

/**
 * Interval of time with start and end timestamps.
 * immutable
 * @author woellauer
 *
 */
public class Interval implements Serializable {
	private static final long serialVersionUID = -3129013387844118531L;
	private static final Logger log = LogManager.getLogger("tsdb");

	public final int start; // start <= end
	public final int end;

	protected Interval(int start, int end) {			
		this.start = start;
		this.end = end;
	}

	public static Interval of(int start, int end) {
		if(start>end) {
			throw new RuntimeException();
		}
		return new Interval(start, end);
	}

	public static boolean isAdjacentOrOverlap(Interval a, Interval b) {
		if(a.end+1==b.start || b.end+1==a.start) {
			return true;
		}
		return a.end >= b.start && a.start <= b.end;
	}

	public static Interval getEnvelope(Interval a, Interval b) {
		return Interval.of(Math.min(a.start, b.start),Math.max(a.end, b.end));
	}

	public boolean less(Interval other) {
		return this.end<other.start;
	}

	/**
	 * format: [ x,   y ]
	 * example [2008,2015]
	 * @return
	 */
	public static Interval parse(String text) {
		try {
			text = text.trim();
			if(text.charAt(0)!='[') {
				log.error("no range: "+text);
				return null;
			}
			if(text.charAt(text.length()-1)!=']') {
				log.error("no range: "+text);
				return null;
			}
			int sepIndex = text.indexOf(',');
			if(sepIndex<0) {
				log.error("no range: "+text);
				return null;
			}
			String minText = text.substring(1, sepIndex).trim();
			String maxText = text.substring(sepIndex+1, text.length()-1).trim();
			int min= Integer.parseInt(minText);
			int max = Integer.parseInt(maxText);
			return Interval.of(min, max);
		} catch (Exception e) {
			log.error("parse Interval: "+e);
			return null;
		}
	}

	@Override
	public String toString() {
		return "["+TimeUtil.oleMinutesToText((long) start)+" .. "+TimeUtil.oleMinutesToText((long) end)+"]";
	}

	private static class IntervalSerializer implements Serializer<Interval>, Serializable {
		private static final long serialVersionUID = 5604613139076007495L;
		@Override
		public void serialize(DataOutput out, Interval value) throws IOException {
			out.writeInt(value.start);
			out.writeInt(value.end);
		}
		@Override
		public Interval deserialize(DataInput in, int available) throws IOException {
			int start = in.readInt();
			int end = in.readInt();
			return new Interval(start,end);
		}
		@Override
		public int fixedSize() {
			return 4+4;
		}
	};

	public static final Serializer<Interval> SERIALIZER = new IntervalSerializer();
}