package tsdb.util;

import java.io.Serializable;
import java.util.Comparator;

import static tsdb.util.AssumptionCheck.*;

/**
 * (open) interval of time with value object.
 * immutable if value is immutable
 * @param <T> Type of value 
 * @author woellauer
 */
public class TimestampInterval<T extends Serializable> implements Serializable {

	private static final long serialVersionUID = 1733091691391404002L;
	public final T value; //not null
	public final Long start; //nullable
	public final Long end; //nullable

	public TimestampInterval(T value, Long start, Long end) {
		throwNull(value);
		throwGreater(start,end);
		this.value = value;
		this.start = start;
		this.end = end;
	}

	public boolean contains(long start, long end) {
		throwGreater(start,end);
		return (this.start==null?true:this.start<=start) && (this.end==null?true:end<=this.end);
	}
	
	public TimestampInterval<T> filterByInterval(Long includedStart, Long includedEnd) {
		throwGreater(includedStart,includedEnd);		
		
		Long filterStart = start;
		if(includedStart!=null) {
			if(filterStart==null) {
				filterStart = includedStart;
			} else if(filterStart<includedStart){
				filterStart = includedStart;
			}
		}
		Long filterEnd = end;
		if(includedEnd!=null) {
			if(filterEnd==null) {
				filterEnd = includedEnd;
			} else if(includedEnd<filterEnd) {
				filterEnd = includedEnd;
			}
		}
		
		if(filterStart!=null&&filterEnd!=null&&filterEnd<filterStart) {
			return null;
		}
		
		return new TimestampInterval<T>(value, filterStart,filterEnd);
		
	}

	@Override
	public String toString() {
		return value.toString()+" ["+start+" - "+end+"]";
	}

	public static final Comparator<TimestampInterval<?>> END_COMPARATOR = (t1,t2)->{
		if(t1.end==null) {
			if(t2.end==null) {
				return 0;
			} else {
				return 1;
			}
		} else {
			if(t2.end==null) {
				return -1;
			} else {
				return Long.compare(t1.end,t2.end);
			}
		}
	};

	public static final Comparator<TimestampInterval<?>> START_END_COMPARATOR = new Comparator<TimestampInterval<?>>() {			
		@Override
		public int compare(TimestampInterval<?> a, TimestampInterval<?> b) {
			if(a.start==null) {
				if(b.start!=null) {
					return -1;
				}
			} else {
				if(b.start==null) {
					return 1;
				} else {
					int c = Long.compare(a.start, b.start);
					if(c!=0) {
						return c;
					}
				}
			}			
			if(a.end==null) {
				if(b.end==null) {
					return 0;
				} else {
					return 1;
				}
			} else {
				if(b.end==null) {
					return -1;
				} else {
					return Long.compare(a.end, b.end);
				}
			}				
		}
	};	
}
