package tsdb.graph.source;

import java.util.ArrayList;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Base;
import tsdb.graph.node.Node;
import tsdb.graph.node.NodeGen;
import tsdb.iterator.MergeIterator;
import tsdb.iterator.ProjectionFillIterator;
import tsdb.util.TimestampInterval;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates base aggregated data of one virtual plot that consist of multiple station sources
 * @author woellauer
 *
 */
public class VirtualBase extends Base.Abstract  {
	private static final Logger log = LogManager.getLogger();

	private final VirtualPlot virtualPlot; //not null	
	private final String[] schema; // not null
	private final NodeGen stationGen; // not null

	protected VirtualBase(TsDB tsdb, VirtualPlot virtualPlot, String[] schema, NodeGen stationGen) {
		super(tsdb);
		throwNulls(virtualPlot, schema, stationGen);
		if(schema.length==0) {
			throw new RuntimeException("no schema");			
		}
		
		String[] virtualPlotSchema = virtualPlot.getSchema();
		virtualPlotSchema = tsdb.includeVirtualSensorNames(virtualPlotSchema);
		if(virtualPlotSchema==null||virtualPlotSchema.length==0) {
			throw new RuntimeException("no sensors in virtualplot "+virtualPlot.plotID);
		}
		String[] virtualPlotBaseSchema = tsdb.getBaseSchema(virtualPlotSchema);
		if(virtualPlotBaseSchema==null||virtualPlotBaseSchema.length==0) {
			throw new RuntimeException("no base sensors in virtualplot "+virtualPlot.plotID);
		}
		
		if(!Util.isContained(schema, virtualPlotBaseSchema)) {
			throw new RuntimeException("schema not valid  "+Arrays.toString(schema)+"  in  "+virtualPlot.plotID+"   "+Arrays.toString(tsdb.getBaseSchema(virtualPlot.getSchema())));
		}
		this.virtualPlot = virtualPlot;
		this.schema = schema;
		this.stationGen = stationGen;
	}
	
	public static VirtualBase of(TsDB tsdb, VirtualPlot virtualPlot, String[] querySchema, NodeGen stationGen) {
		if(querySchema==null) {
			String[] schema = virtualPlot.getSchema();
			if(schema==null) {
				throw new RuntimeException("empty VirtualPlot: "+virtualPlot.plotID);
			}			
			querySchema = tsdb.getBaseSchema(schema);
			if(querySchema==null) {
				log.warn("empty base schema in VirtualPlot: "+virtualPlot.plotID);
				return null;
			}
		}
		return new VirtualBase(tsdb, virtualPlot, querySchema, stationGen);		
	}

	@Override
	public TsIterator get(Long start, Long end) {
		List<TimestampInterval<StationProperties>> intervalList = virtualPlot.getStationList(start, end, schema);			 
		List<TsIterator> processing_iteratorList = new ArrayList<TsIterator>();				
		for(TimestampInterval<StationProperties> interval:intervalList) {
			String stationID = interval.value.get_serial();
			String[] stationSchema = tsdb.getValidSchema(stationID, schema);
			if(stationSchema.length>0) {				
				TimestampInterval<StationProperties> filteredInterval = interval.filterByInterval(start, end);
				if(filteredInterval!=null) {
					Station station = tsdb.getStation(stationID);
					Node node = StationBase.of(tsdb, station, stationSchema, stationGen);			
					TsIterator it = node.get(filteredInterval.start, filteredInterval.end);
					if(it!=null&&it.hasNext()) {
						processing_iteratorList.add(it);
					}
				}				
			}
		}
		if(processing_iteratorList.isEmpty()) {
			return null;
		}
		if(processing_iteratorList.size()==1) {			
			TsIterator it = processing_iteratorList.get(0);
			if(Arrays.equals(it.getSchema().names,schema)) {
				//log.info("one iterator no projection");
				return it;
			} else {
				//log.info("one iterator with projection");
				return new ProjectionFillIterator(it, schema);
			}
		}
		MergeIterator virtual_iterator = new MergeIterator(schema, processing_iteratorList, virtualPlot.plotID);			
		if(virtual_iterator==null||!virtual_iterator.hasNext()) {
			return null;
		}
		return virtual_iterator;
	}
	
	@Override
	public Station getSourceStation() {
		return null; // source unknown
	}

	@Override
	public String[] getSchema() {
		return schema;
	}
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return virtualPlot;
	}
	
	@Override
	public long[] getTimestampInterval() {
		return virtualPlot.getTimestampInterval();
	}
}
