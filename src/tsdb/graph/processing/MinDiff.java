package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;
import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.iterator.MinDiffIterator;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

public class MinDiff implements Continuous {
	private static final Logger log = LogManager.getLogger();
	
	private final Continuous target;
	private final List<Continuous> sources; //not null
	private final String[] schema; //not null
	private final boolean _constant_timestep;
	
	private final static int MIN_COMPARE_ELEMENTS = 1;
	
	public MinDiff(Continuous target, List<Continuous> sources, String[] schema) {
		throwNull(target);
		throwNulls(sources,schema);
		if(sources.isEmpty()) {
			throw new RuntimeException("no sources");	
		}
		if(sources.isEmpty()) {
			log.warn("insufficient sources");
		}
		this._constant_timestep = sources.get(0).isConstantTimestep();
		for(Continuous source:sources) {
			if(!source.isContinuous() || source.isConstantTimestep()!=_constant_timestep) {
				throw new RuntimeException("different source types");
			}
		}
		this.target = target;
		this.sources = sources;
		this.schema = schema;
	}

	public static MinDiff of(Continuous target, List<Continuous> sources) {		
		return new MinDiff(target, sources, target.getSchema());
	}

	public TsIterator getExactly(long start, long end) {		
		List<TsIterator> iteratorList = new ArrayList<>();		
		for(Continuous source:sources) {
			TsIterator it = source.getExactly(start, end);
			if(it!=null&&it.hasNext()) {
				iteratorList.add(it);				
			}
		}
		if(iteratorList.isEmpty()) {
			return null;
		}
		return new MinDiffIterator(schema, target.getExactly(start, end), iteratorList.toArray(new TsIterator[0]),MIN_COMPARE_ELEMENTS);		
	}

	@Override
	public Station getSourceStation() { // multiple sources
		return null;
	}

	@Override
	public String[] getSchema() {
		return schema;
	}

	@Override
	public boolean isConstantTimestep() {
		return _constant_timestep;
	}
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {  // multiple sources
		return null;
	}
	
	@Override
	public long[] getTimestampInterval() {//maximum interval
		long[] interval = new long[]{Long.MAX_VALUE,Long.MIN_VALUE};
		sources.stream().map(s->s.getTimestampInterval()).filter(Util::notNull).forEach(i->{
			if(i[0]<interval[0]) {
				interval[0] = i[0];
			}
			if(i[1]>interval[1]) {
				interval[1] = i[1];
			}
		});		
		if(interval[0]==Long.MAX_VALUE || interval[1]==Long.MIN_VALUE) {
			return null;
		}
		return interval;
	}

}
