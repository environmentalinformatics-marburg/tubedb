package tsdb.iterator;

import java.util.Collections;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.DataQuality;
import tsdb.util.Interval;
import tsdb.util.TimeSeriesMask;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

public class MaskIterator extends InputIterator {
		@SuppressWarnings("unused")
		private static final Logger log = LogManager.getLogger();

		private Iterator<Interval>[] maskIterators;
		private Interval[] currIntervals;

		/**
		 * 
		 * @param input_iterator
		 * @param dataQuality lowest acceptable quality
		 */
		@SuppressWarnings("unchecked")
		public MaskIterator(TsIterator input_iterator, TimeSeriesMask[] masks) {
			super(input_iterator, input_iterator.getSchema().copy());
			if(masks.length!=input_iterator.getSchema().length) {
				throw new RuntimeException("wrong number of masks");
			}
			this.maskIterators = new Iterator[masks.length];
			this.currIntervals = new Interval[masks.length];
			for (int i = 0; i < masks.length; i++) {
				if(masks[i]!=null) {
					maskIterators[i] = masks[i].getIterator();
				} else {
					maskIterators[i] = Collections.emptyIterator();
				}
				nextInterval(i);
			}
		}
		
		private Interval nextInterval(int i) {
			if(maskIterators[i].hasNext()) {
				currIntervals[i] = maskIterators[i].next();
			} else {
				currIntervals[i] = null;
			}
			return currIntervals[i];
		}

		@Override
		public boolean hasNext() {
			return input_iterator.hasNext();
		}

		@Override
		public TsEntry next() {
			TsEntry next = input_iterator.next();
			long timestamp = next.timestamp;
			float[] data = next.data;
			DataQuality[] qualityFlag = next.qualityFlag;
			float[] resultData = new float[schema.length];
			for(int i=0;i<data.length;i++) {
				
				while(currIntervals[i]!=null && timestamp>currIntervals[i].end) {
					nextInterval(i);
				}
				
				if(currIntervals[i]!=null && currIntervals[i].start<=timestamp) {
					resultData[i] = Float.NaN;
				} else {
					resultData[i] = data[i];
				}
			}
			return new TsEntry(next.timestamp,resultData,qualityFlag);
		}
	}
