package tsdb.graph.processing;

import java.util.Arrays;

import tsdb.TsDB;
import tsdb.graph.node.Node;
import tsdb.graph.source.DelegateNode;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;
import tsdb.util.iterator.Virtual_P_RT_NRT_Iterator;

/**
 * Node: calculates corrected precipitation values from source (container)
 * @author woellauer
 *
 */
public class Virtual_P_RT_NRT extends DelegateNode {
	
	private final int pos_P_container_RT;
	private final int pos_P_RT_NRT;
	
	protected Virtual_P_RT_NRT(TsDB tsdb, Node source, int pos_P_container_RT, int pos_P_RT_NRT) {
		super(tsdb, source);
		this.pos_P_container_RT = pos_P_container_RT;
		this.pos_P_RT_NRT = pos_P_RT_NRT;
	}
	
	public static Virtual_P_RT_NRT of(TsDB tsdb, Node source) {
		String[] schema = source.getSchema();
		final int pos_P_container_RT = Util.getIndexInArray("P_container_RT", schema);
		if(pos_P_container_RT<0) {
			throw new RuntimeException("no P_container_RT in "+Arrays.toString(schema));
		}
		final int pos_P_RT_NRT = Util.getIndexInArray("P_RT_NRT", schema);
		if(pos_P_RT_NRT<0) {
			throw new RuntimeException("no P_RT_NRT in "+Arrays.toString(schema));
		}
		return new Virtual_P_RT_NRT(tsdb, source, pos_P_container_RT, pos_P_RT_NRT);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		
		TsIterator virtual_it = new Virtual_P_RT_NRT_Iterator(input_iterator, pos_P_container_RT, pos_P_RT_NRT);
		if(!virtual_it.hasNext()) {
			return null;
		}
			
		return virtual_it;
	}	
}
