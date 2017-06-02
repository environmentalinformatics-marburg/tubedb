package tsdb.component.iterator;

import java.util.ArrayDeque;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

public class NocCheckIterator extends InputIterator {
	private static final Logger log = LogManager.getLogger();

	private static final float ABSENT = -9999999f;

	//private static final int MAX_ELEMENTS = 7*24;
	private static final int MAX_ELEMENTS = 24;
	private ArrayDeque<TsEntry> past = new ArrayDeque<TsEntry>(MAX_ELEMENTS);
	private ArrayDeque<TsEntry> future = new ArrayDeque<TsEntry>(MAX_ELEMENTS);

	private float[] min;
	private float[] max;
	private final int len;
	private float[] min_range;


	public NocCheckIterator(TsIterator input_iterator) {
		super(input_iterator, input_iterator.getSchema());
		len = this.schema.length;
		min = new float[len];
		max = new float[len];
		min_range = new float[len];
		String[] names = this.schema.names;
		for (int i = 0; i < len; i++) {
			float minRange = ABSENT;
			switch(names[i]) {
			case "rH_200":
				minRange = 5f;
				break;
			}
			min_range[i] = minRange;
		}
		while(future.size() < MAX_ELEMENTS && input_iterator.hasNext()) {
			future.addLast(input_iterator.next());
		}
	}

	@Override
	public boolean hasNext() {
		return !future.isEmpty();
	}

	@Override
	public TsEntry next() {
		TsEntry current = future.pollFirst(); // remove first timestamp
		if(input_iterator.hasNext()) {
			future.addLast(input_iterator.next());
		}
		TsEntry result = check(current);
		if(past.size() == MAX_ELEMENTS) {
			past.pollFirst();
		}
		past.addLast(current);
		return result;
	}

	private TsEntry check(TsEntry current) {
		if(past.isEmpty() && future.isEmpty()) {
			return current;
		}
		for (int i = 0; i < len; i++) {
			min[i] = Float.MAX_VALUE;
			max[i] = -Float.MAX_VALUE;			
		}
		for(TsEntry e:past) {
			for (int i = 0; i < len; i++) {
				float v = e.data[i];
				if(v<min[i]) {
					min[i] = v;				
				}
				if(max[i]<v) {
					max[i] = v;
				}
			}
		}
		for(TsEntry e:future) {
			for (int i = 0; i < len; i++) {
				float v = e.data[i];
				if(v<min[i]) {
					min[i] = v;				
				}
				if(max[i]<v) {
					max[i] = v;
				}
			}
		}
		float[] prevDayHour = past.size()==MAX_ELEMENTS ? past.peekFirst().data : null;
		float[] futureDayHour = future.size()==MAX_ELEMENTS ? future.peekLast().data : null;
		//log.info(TimeUtil.oleMinutesToText(prevDayHour==null?0:prevDayHour.timestamp)+" "+TimeUtil.oleMinutesToText(current.timestamp)+" "+TimeUtil.oleMinutesToText(futureDayHour==null?0:futureDayHour.timestamp));
		float[] result = Arrays.copyOf(current.data, current.data.length);
		for (int i = 0; i < len; i++) {
			float v = current.data[i];
			boolean flag = false;
			if(min_range[i] != ABSENT) {
				float range = max[i] - min[i];
				//log.info("NOC "+min+" "+max+"    "+range);
				if(range <= min_range[i]) {
					flag = true;
				}
			}
			if(prevDayHour!=null && futureDayHour!=null) {
				int maxDiff = 10;
				/*if( (v+maxDiff<prevDayHour[i] && v+maxDiff<futureDayHour[i]) || (prevDayHour[i]<v-maxDiff && futureDayHour[i]<v-maxDiff) ) {
					flag = true;
				}*/
				if(v+maxDiff<prevDayHour[i] || prevDayHour[i]<v-maxDiff) {
					flag = true;
				}
				if(v+maxDiff<futureDayHour[i] || futureDayHour[i]<v-maxDiff) {
					flag = true;
				}
			}
			
			if(flag) {
				result[i] = Float.NaN;
			}
		}
		return new TsEntry(current.timestamp, result, current.qualityFlag);		
	}
}
