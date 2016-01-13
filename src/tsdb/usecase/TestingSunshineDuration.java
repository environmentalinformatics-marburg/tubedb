package tsdb.usecase;

import java.util.Iterator;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.BaseAggregationTimeUtil;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.MoveIterator;
import tsdb.util.iterator.TsIterator;

public class TestingSunshineDuration {

	private static interface Enumerator<T> {
		public boolean moveNext();
		public T current();
	}

	private static class IteratorEnumerator<T> implements Enumerator<T> {
		private final Iterator<T> iterator;
		private T current;
		public IteratorEnumerator(Iterator<T> iterator) {
			this.iterator = iterator;
			this.current = null;
		}
		@Override
		public boolean moveNext() {
			if(iterator.hasNext()) {
				current = iterator.next();
				return true;
			} else {
				current = null;
				return false;
			}
		}
		@Override
		public T current() {
			return current;
		}
	}

	private static abstract class InputEnumerationIterator extends MoveIterator {		
		protected final Enumerator<TsEntry> input_enumerator;		
		public InputEnumerationIterator(TsIterator input_iterator) {
			super(input_iterator.getSchema());
			this.input_enumerator = new IteratorEnumerator<TsEntry>(input_iterator);
		}		
	}

	@SuppressWarnings("unused")
	private static class SunshineDurationIterator extends InputEnumerationIterator {
		private long aggregationTimestamp;
		private boolean end=false;



		public SunshineDurationIterator(TsIterator input_iterator) {
			super(input_iterator);
			if(input_enumerator.moveNext()) {
				aggregationTimestamp = BaseAggregationTimeUtil.calcBaseAggregationTimestamp(input_enumerator.current().timestamp);
			} else {
				close();				
			}
		}

		@Override
		protected TsEntry getNext() {
			if(end) {
				return null;
			}
			int tick=0;
			long inputAggregationTimestamp = BaseAggregationTimeUtil.calcBaseAggregationTimestamp(input_enumerator.current().timestamp);
			while(aggregationTimestamp==inputAggregationTimestamp) {
				if(input_enumerator.current().data[0]>=120) {
					tick++;
				}
				if(input_enumerator.moveNext()) {
					inputAggregationTimestamp = BaseAggregationTimeUtil.calcBaseAggregationTimestamp(input_enumerator.current().timestamp);
				} else {
					end = true;
					break;
				}
			}

			TsEntry result = new TsEntry(aggregationTimestamp, new float[]{tick/6f});
			aggregationTimestamp = inputAggregationTimestamp;
			return result;
		}

	}


	/*private static class SunshineDurationAggregator extends InputProcessingIterator {
		long aggregationTimestamp;
		int ticks = 0;

		public SunshineDurationAggregator(TsIterator input_iterator) {
			super(input_iterator, input_iterator.getSchema());
			if(!input_iterator.hasNext()) {
				this.close();
			}
			processNext();
		}

		private void processNext() {
			TsEntry currentEntry = input_iterator.next();
			long currentAggregationTimestamp = BaseAggregationTimeUtil.calcBaseAggregationTimestamp(currentEntry.timestamp);
		}



		@Override
		protected TsEntry getNext() {
			while(input_iterator.hasNext()) {
				TsEntry nextEntry = input_iterator.next();
				long nextAggregationTimestamp = BaseAggregationTimeUtil.calcBaseAggregationTimestamp(currentEntry.timestamp);

			}





			if(!input_iterator.hasNext()) {
				return null;
			}
			TsEntry entry = input_iterator.next();
			long elementAggregationTimestamp = BaseAggregationTimeUtil.calcBaseAggregationTimestamp(entry.timestamp); 			
			do {

			} while (1==1);





				// TODO Auto-generated method stub
				return null;
		}
	};*/



	public static void main(String[] args) {

		//	logger.error("_...__----_-----");


		TsDB tsdb = TsDBFactory.createDefault();
		String plotID = "HEG03";
		String[] columnNames = new String[]{"sunshine","Rn_300"};
		AggregationInterval aggregationInterval = AggregationInterval.DAY;
		DataQuality dataQuality = DataQuality.NO;
		boolean interpolated = false;
		Node ts = QueryPlan.plot(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);
		//TsIterator it = new SunshineDurationIterator(ts.get(null, null));
		TsIterator it = ts.get(null, null);




		int counter=0;
		int no_counter=0;
		while(it.hasNext()) {			
			TsEntry e = it.next();
			System.out.println(e);
			/*float v = e.data[0];
			if(v>=120) {
				System.out.println(1f/6);
				counter++;
			} else {
				System.out.println(0f/6);
				no_counter++;
			}*/


		}

		System.out.println("counter "+counter);
		System.out.println("no_counter "+no_counter);	

		System.out.println(it.getSchema());



	}	

}
