package tsdb.graph.processing;

import tsdb.TsDB;
import tsdb.graph.node.Node;
import tsdb.graph.source.DelegateNode;
import tsdb.iterator.SunshineIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Virtual sensor Sunshine calculates sunshine duration from radiation source.
 * @author woellauer
 *
 */
public class Sunshine extends DelegateNode {
	
	private final Node source;
	
	protected Sunshine(TsDB tsdb, Node source) {
		super(tsdb, source);
		this.source = source;
	}
	
	public static Sunshine of(TsDB tsdb, Node source) {
		return new Sunshine(tsdb, source);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}			
		return new SunshineIterator(input_iterator);
	}	
}
