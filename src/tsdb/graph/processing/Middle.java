package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.iterator.MiddleIterator;
import tsdb.util.iterator.TsIterator;

public class Middle extends Continuous.Abstract {
	private static final Logger log = LogManager.getLogger();
	
	private final List<Continuous> sources; //not null
	private final String[] schema; //not null
	private final boolean _constant_timestep;
	
	private static final int SOURCE_COUNT = 3;

	public Middle(TsDB tsdb, List<Continuous> sources, String[] schema) {
		super(tsdb);
		throwNulls(sources,schema);
		if(sources.isEmpty()) {
			throw new RuntimeException("no sources");	
		}
		if(sources.size()!=SOURCE_COUNT) {
			log.warn("insufficient sources != 3");
		}
		this._constant_timestep = sources.get(0).isConstantTimestep();
		for(Continuous source:sources) {
			if(!source.isContinuous() || source.isConstantTimestep()!=_constant_timestep) {
				throw new RuntimeException("different source types");
			}
		}
		this.sources = sources;
		this.schema = schema;
	}

	public static Middle of(TsDB tsdb, List<Continuous> sources) {		
		Set<String> schemaSet = new LinkedHashSet<String>();
		for(Continuous continuous:sources) {
			String[] schema = continuous.getSchema();
			if(schema!=null&&schema.length>0) {
				schemaSet.addAll(Arrays.asList(schema));
			}
		}
		return new Middle(tsdb, sources, schemaSet.toArray(new String[0]));
	}

	@Override
	public TsIterator get(Long start, Long end) {
		if(start==null||end==null) {
			long[] interval = new long[]{Long.MAX_VALUE,Long.MIN_VALUE};
			sources.stream().map(s->tsdb.getBaseTimeInterval(s.getSourceStation().stationID)).forEach(i->{
				if(i[0]<interval[0]) {
					interval[0] = i[0];
				}
				if(i[1]>interval[1]) {
					interval[1] = i[1];
				}
			});
			/*if(interval==null) {
				return null;
			}*/
			if(start==null) {
				start = interval[0];
			}
			if(end==null) {
				end = interval[1];
			}
		}
		return getExactly(start,end);
	}

	public TsIterator getExactly(long start, long end) {		
		List<TsIterator> iteratorList = new ArrayList<>();		
		for(Continuous source:sources) {
			TsIterator it = source.get(start, end);
			if(it!=null&&it.hasNext()) {
				iteratorList.add(it);				
			}
		}
		if(iteratorList.size()<SOURCE_COUNT) {
			return null;
		}
		return new MiddleIterator(schema, iteratorList.toArray(new TsIterator[0]));		
	}

	@Override
	public Station getSourceStation() { // multiple source stations
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
	public VirtualPlot getSourceVirtualPlot() {
		return null;
	}
	
	@Override
	public long[] getTimestampInterval() {//maximum interval
		long[] interval = new long[]{Long.MAX_VALUE,Long.MIN_VALUE};
		sources.stream().map(s->s.getTimestampInterval()).forEach(i->{
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
