package tsdb.util.iterator;

import tsdb.util.Mutator;
import tsdb.util.TsEntry;

public class MutatorIterator {
	
	public static TsIterator appendMutators(TsIterator it, Mutator[] mutators) {
		
		if(mutators == null || mutators.length == 0) {
			return it;
		}
		
		if(mutators.length == 1) {
			Mutator m = mutators[0];
			return new InputIterator(it, it.getSchema()) {
				@Override
				public TsEntry next() {
					TsEntry e = input_iterator.next();
					m.apply(e.timestamp, e.data);
					return e;
				}			
			};
			
		}
		
		return new InputIterator(it, it.getSchema()) {
			@Override
			public TsEntry next() {
				TsEntry e = input_iterator.next();
				for(Mutator m:mutators) {
					m.apply(e.timestamp, e.data);
				}
				return e;
			}			
		};		
	}
}
