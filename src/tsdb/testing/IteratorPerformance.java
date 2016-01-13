package tsdb.testing;

import java.util.function.Consumer;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.TsIterator;

public class IteratorPerformance {	
	private static class Source extends TsIterator {
		private final int limit;
		private int count = 0;
		public Source(int limit) {
			super(new TsSchema( new String[]{"source"}));
			this.limit = limit;
		}
		@Override
		public boolean hasNext() {
			return count<limit;
		}
		@Override
		public TsEntry next() {
			count++;
			return TsEntry.of(count, count);
		}		
	}
	
	private static class SimpleIterator extends TsIterator {
		private TsIterator input_iterator;
		public SimpleIterator(TsIterator input_iterator) {
			super(input_iterator.getSchema());
			this.input_iterator = input_iterator;
		}
		@Override
		public boolean hasNext() {
			return input_iterator.hasNext();
		}
		@Override
		public TsEntry next() {
			TsEntry e = input_iterator.next();
			//return TsEntry.of(e.timestamp,e.data[0]/2);
			return e;
		}		
	}

	static class Ac implements Consumer<Integer> {
		public int sum = 0;
		@Override
		public void accept(Integer t) {
			sum += t;				
		}			
	}

	public static void main(String[] args) {
		/*LinkedList<Integer> a = new LinkedList<Integer>();

		for(int i=0;i<10000000;i++) {
			a.add(i*10+i%99);
		}

		Ac action = new Ac(); 

		int sum = 0;*/

		for(int loop=0;loop<30;loop++) {
			System.gc();
			//final int ELEMENT_COUNT = 1000000000;
			final int ELEMENT_COUNT = 100000000;
			//Source it = new Source(ELEMENT_COUNT);
			//TsIterator it = new ApplyIterator(new Source(ELEMENT_COUNT),e->TsEntry.of(e.timestamp,e.data[0]));
			/*TsIterator it = new ApplyIterator(new Source(ELEMENT_COUNT),new ApplyIterator.ApplyFunc() {
				@Override
				public TsEntry apply(TsEntry e) {
					return TsEntry.of(e.timestamp,e.data[0]);
				}
			});*/
			//TsIterator it = new ApplyIterator(new Source(ELEMENT_COUNT),e->e);
			//TsIterator it = new SimpleIterator(new Source(ELEMENT_COUNT));
			TsIterator it = new SimpleIterator(new SimpleIterator(new SimpleIterator(new SimpleIterator(new Source(ELEMENT_COUNT)))));
			/*ApplyIterator.ApplyFunc func = e->TsEntry.of(e.timestamp,e.data[0]);
			TsIterator it = new ApplyIterator(new ApplyIterator(new ApplyIterator(new ApplyIterator(new ApplyIterator(new ApplyIterator(new Source(ELEMENT_COUNT),func),func),func),func),func),func);
			System.out.println(it.getProcessingChain().getText());*/
			
			long time_start = System.currentTimeMillis();
			/*for(int i=0;i<100;i++) {
			Spliterator<Integer> spit = a.spliterator();		
			spit.forEachRemaining(action);*/

			/*for(Integer v:a) {
				action.sum += v;
			}*/

			/*Iterator<Integer> it = a.iterator();
			while(it.hasNext()) {
				action.sum += it.next();
			}*/
			//}

			float sum = 0;
			while(it.hasNext()) {
				TsEntry e = it.next();
				sum += e.data[0];
			}




			long time_end = System.currentTimeMillis();




			System.out.println(sum);
			System.out.println((time_end-time_start)+" ms");
		}

	}

}
