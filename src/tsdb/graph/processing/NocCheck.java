package tsdb.graph.processing;

import tsdb.TsDB;
import tsdb.component.iterator.NocCheckIterator;
import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuousAbstract;
import tsdb.util.iterator.TsIterator;

/**
 * This node filters values based on the difference to a reference source.
 * @author woellauer
 *
 */
public class NocCheck extends DelegateContinuousAbstract {	

	private NocCheck(TsDB tsdb, Continuous source) {
		super(tsdb, source);
	}

	public static Continuous of(TsDB tsdb, Continuous continuous) {
		return new NocCheck(tsdb,continuous);
	}

	@Override
	public TsIterator get(Long start, Long end) {		
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		NocCheckIterator noc_iterator = new NocCheckIterator(input_iterator);
		return noc_iterator;
	}
}
