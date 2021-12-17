package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


import org.tinylog.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.iterator.AverageIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates average values per time step over all sources.
 * @author woellauer
 *
 */
public class Averaged extends Continuous.Abstract {
	

	private final List<Continuous> sources; //not null
	private final String[] schema; //not null
	private final int minCount;
	private final boolean withQualityMeasures;
	private final boolean _constant_timestep;

	public Averaged(TsDB tsdb, List<Continuous> sources, String[] schema, int minCount, boolean withQualityMeasures) {
		super(tsdb);
		throwNulls(sources,schema);
		if(sources.isEmpty()) {
			throw new RuntimeException("no sources");	
		}
		if(minCount<1) {
			Logger.warn("no senseful min count= "+minCount);
		}
		this.withQualityMeasures = withQualityMeasures;
		if(sources.size()<minCount) {
			Logger.warn("insufficient sources with min count= "+minCount+"  "+sources.size());
		}
		this.minCount = minCount;
		this._constant_timestep = sources.get(0).isConstantTimestep();
		for(Continuous source:sources) {
			if(!source.isContinuous() || source.isConstantTimestep()!=_constant_timestep) {
				throw new RuntimeException("different source types");
			}
		}
		this.sources = sources;
		this.schema = schema;
	}

	public static Averaged of(TsDB tsdb, List<Continuous> sources, int minCount, boolean withQualityMeasures) {		
		Set<String> schemaSet = new LinkedHashSet<String>();
		for(Continuous continuous:sources) {
			String[] schema = continuous.getSchema();
			if(schema!=null&&schema.length>0) {
				schemaSet.addAll(Arrays.asList(schema));
			}
		}
		return new Averaged(tsdb, sources, schemaSet.toArray(new String[0]), minCount, withQualityMeasures);
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
		if(iteratorList.size()<minCount) {
			return null;
		}
		return new AverageIterator(schema, iteratorList.toArray(new TsIterator[0]), minCount, withQualityMeasures);		
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
	
	public String getSourceText() {
		String s="";
		for(Continuous source:sources) {
			s+=source.getSourceName()+" ";
		}
		return s;
	}

}
