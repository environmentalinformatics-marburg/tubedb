package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.iterator.BadInterpolatedRemoveIterator;
import tsdb.util.Util;
import tsdb.util.iterator.Interpolator;
import tsdb.util.iterator.TimeSeries;
import tsdb.util.iterator.TsIterator;

/**
 * This node tries to interpolate missing values.
 * For one-values gaps it uses linear interpolation.
 * For bigger gaps it uses multilinear interpolation.
 * @author woellauer
 *
 */
@Deprecated
public class Interpolated extends Continuous.Abstract {

	public final static int MIN_STATION_INTERPOLATION_COUNT = 2; //multilinear interpolation needs at least two sources
	public final static int STATION_INTERPOLATION_COUNT = 15;		
	public final static int TRAINING_TIME_INTERVAL = 60*24*7*4; // in minutes;  four weeks

	final Continuous source;  //not null
	final Continuous[] interpolationSources;  //not null
	final String[] interpolationSchema;  //not null

	/**
	 * internal constructor
	 * @param tsdb
	 * @param source
	 * @param interpolationSources
	 * @param interpolationSchema
	 */
	protected Interpolated(TsDB tsdb, Continuous source, Continuous[] interpolationSources, String[] interpolationSchema) {		
		super(tsdb);
		throwNulls(source,interpolationSources,interpolationSchema);
		if(!source.isContinuous()) {
			throw new RuntimeException("source not continuous");
		}
		this.source = source;
		for(Continuous interpolationSource:interpolationSources) {
			if(!interpolationSource.isContinuous()) {
				throw new RuntimeException("interpolation source not continuous");
			}
		}
		this.interpolationSources = interpolationSources;
		this.interpolationSchema = interpolationSchema;
	}
	
	/**
	 * Constructor for specific use cases (eg. testing)
	 * @param tsdb
	 * @param source
	 * @param interpolationSources
	 * @param interpolationSchema
	 * @return
	 */
	public static Interpolated of(TsDB tsdb, Continuous source, Continuous[] interpolationSources, String[] interpolationSchema){
		return new Interpolated(tsdb, source, interpolationSources, interpolationSchema);		
	}
	
	/**
	 * general constructor
	 * @param tsdb
	 * @param plotID
	 * @param querySchema
	 * @param sourceGen
	 * @return
	 */
	public static Continuous of(TsDB tsdb, String plotID, String[] querySchema, ContinuousGen sourceGen) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return createFromVirtual(tsdb, virtualPlot, querySchema, sourceGen);
		} 
		Station station = tsdb.getStation(plotID);
		if(station!=null) {
			return createFromStation(tsdb,station,querySchema, sourceGen);
		}
		throw new RuntimeException("station not found");
	}

	/**
	 * Constructor for stations only
	 * @param tsdb
	 * @param station
	 * @param querySchema
	 * @param sourceGen
	 * @return
	 */
	public static Continuous createFromStation(TsDB tsdb, Station station, String[] querySchema, ContinuousGen sourceGen) {
		if(querySchema==null) {
			querySchema = station.getSchema();
		} else {
			querySchema = station.getValidSchemaEntriesWithVirtualSensors(querySchema);
		}
		if(querySchema.length==0) {
			throw new RuntimeException("empty schema");
		}		
		Continuous source = sourceGen.get(station.stationID, querySchema);	

		String[] interpolationSchema = Arrays.asList(querySchema)
				.stream()
				.filter(sensorName -> tsdb.getSensor(sensorName).useInterpolation)
				.toArray(String[]::new);

		Continuous[] interpolationSources = station.nearestStations
				.stream()
				.limit(STATION_INTERPOLATION_COUNT)
				.filter(sourceStation -> sourceStation.getValidSchemaEntriesWithVirtualSensors(interpolationSchema).length>0)
				.map(sourceStation -> sourceGen.get(sourceStation.stationID, sourceStation.getValidSchemaEntriesWithVirtualSensors(interpolationSchema)))
				.toArray(Continuous[]::new);

		if(interpolationSources.length<MIN_STATION_INTERPOLATION_COUNT) {
			return source;
		} else {
			return new Interpolated(tsdb, source, interpolationSources, interpolationSchema);
		}		
	}
	
	/**
	 * Constructor for virtual plots only
	 * @param tsdb
	 * @param virtualPlot
	 * @param querySchema
	 * @param sourceGen
	 * @return
	 */
	public static Continuous createFromVirtual(TsDB tsdb, VirtualPlot virtualPlot, String[] querySchema, ContinuousGen sourceGen) {
		if(querySchema==null) {
			querySchema = virtualPlot.getSchema();
		} else {
			querySchema = virtualPlot.getValidSchemaEntries(querySchema);
		}
		if(querySchema.length==0) {
			throw new RuntimeException("empty schema");
		}		
		Continuous source = sourceGen.get(virtualPlot.plotID, querySchema);		

		String[] interpolationSchema = Arrays.asList(querySchema)
				.stream()
				.filter(sensorName -> tsdb.getSensor(sensorName).useInterpolation)
				.toArray(String[]::new);

		Continuous[] interpolationSources = virtualPlot.nearestVirtualPlots
				.stream()
				.limit(STATION_INTERPOLATION_COUNT)
				.filter(sourceVirtualPlot -> sourceVirtualPlot.getValidSchemaEntries(interpolationSchema).length>0)
				.map(sourceVirtualPlot -> sourceGen.get(sourceVirtualPlot.plotID, sourceVirtualPlot.getValidSchemaEntries(interpolationSchema)))
				.toArray(Continuous[]::new);

		if(interpolationSources.length<MIN_STATION_INTERPOLATION_COUNT) {
			return source;
		} else {
			return new Interpolated(tsdb, source, interpolationSources, interpolationSchema);
		}
	}

	@Override
	public TsIterator get(Long start, Long end) {
		Long queryStart = start;
		Long queryEnd = end;
		start =  Util.ifnull(start, x->x-TRAINING_TIME_INTERVAL);
		TsIterator source_iterator = source.get(start, end);
		if(source_iterator==null||!source_iterator.hasNext()) {
			return null;
		}
		TimeSeries sourceTimeSeries = source_iterator.toTimeSeries();		
		int linearInterpolatedCount = Interpolator.processOneValueGaps(sourceTimeSeries);
		sourceTimeSeries.addToProcessingChain("InterpolateOneValueGaps");
		long interpolationStart = sourceTimeSeries.getFirstTimestamp();
		long interpolationEnd = sourceTimeSeries.getLastTimestamp();
		
		List<TimeSeries> interpolationTimeSeriesTemp = new ArrayList<TimeSeries>();
		int sourcesLinearInterpolationCount=0;
		for(Continuous interpolationSource:interpolationSources) {
			TsIterator it = interpolationSource.getExactly(interpolationStart, interpolationEnd);//TODO
			if(it!=null&&it.hasNext()) {
				TimeSeries timeSeries = it.toTimeSeries();
				sourcesLinearInterpolationCount += Interpolator.processOneValueGaps(timeSeries);
				interpolationTimeSeriesTemp.add(timeSeries);
			}
		}
		
		
		
		TimeSeries[] interpolationTimeSeries = interpolationTimeSeriesTemp.toArray(new TimeSeries[0]);
		

		int interpolatedCount = 0;
		for(String interpolationName:interpolationSchema) {
			//interpolatedCount += Interpolator.processMultiLinear(interpolationTimeSeries, sourceTimeSeries, interpolationName); //TODO
			interpolatedCount += Interpolator.processLinear(interpolationTimeSeries, sourceTimeSeries, interpolationName); //TODO change interpolatedCount
		}
		System.out.println("interpolated: linear: "+linearInterpolatedCount+"   multi linear: "+interpolatedCount+"   sources linear: "+sourcesLinearInterpolationCount);
		sourceTimeSeries.addToProcessingChain("InterpolateMultiLinear");
		
		sourceTimeSeries.hasDataInterpolatedFlag = true;		
		TsIterator clipIterator = sourceTimeSeries.timeSeriesIteratorCLIP(queryStart, queryEnd);
		TsIterator resultIterator = clipIterator;
		if(interpolatedCount>0) {
			resultIterator = new BadInterpolatedRemoveIterator(tsdb, clipIterator);
		}
		return resultIterator;
	}

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public String[] getSchema() {
		return source.getSchema();
	}

	@Override
	public TsIterator getExactly(long start, long end) {
		return get(start,end);
	}
	
	@Override
	public boolean isContinuous() {
		return source.isContinuous();
	}	

	@Override
	public boolean isConstantTimestep() {
		return source.isConstantTimestep();
	}
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return source.getSourceVirtualPlot();
	}
	
	@Override
	public long[] getTimestampInterval() {
		return source.getTimestampInterval();
	}
}
