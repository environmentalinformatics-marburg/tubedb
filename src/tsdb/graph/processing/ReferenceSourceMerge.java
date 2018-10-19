package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.graph.source.GroupAverageSource_NEW;
import tsdb.iterator.MergeIterator;
import tsdb.util.BaseAggregationTimeUtil;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

public class ReferenceSourceMerge extends Continuous.Abstract {

	private static final Logger log = LogManager.getLogger();

	private final Continuous source; //not null
	private final Continuous refSource; //not null	
	private final String plotID; //not null
	private final String[] refRenameSchema;
	private final String[] targetSchema;


	public ReferenceSourceMerge(TsDB tsdb, Continuous source, Continuous refSource, String plotID, String[] refRenameSchema, String[] targetSchema) {
		super(tsdb);
		throwNulls(source,refSource, plotID);
		this.source = source;
		this.refSource = refSource;
		this.plotID = plotID;
		this.refRenameSchema = refRenameSchema;
		this.targetSchema = targetSchema;  
	}

	public static Continuous of(TsDB tsdb, Continuous continuous, String plotID, String[] refSchema, String[] refRenameSchema, String[] targetSchema) {		
		Continuous refSource = GroupAverageSource_NEW.ofPlot(tsdb, plotID, refSchema);
		if(refSource!=null) {
			return new ReferenceSourceMerge(tsdb,continuous,refSource, plotID, refRenameSchema, targetSchema);
		} else {
			log.warn("reference source");
			return continuous;
		}
	}

	@Override
	public TsIterator get(Long start, Long end) {		
		if(start==null||end==null) {
			long[] interval = tsdb.getBaseTimeInterval(plotID);
			if(interval==null) {
				return null;
			}
			if(start==null) {
				start = interval[0];
			}
			if(end==null) {
				end = interval[1];
			}
		}
		
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		
		TsIterator ref_iterator = refSource.get(start, end);
		if(ref_iterator==null||!ref_iterator.hasNext()) {
			new RuntimeException().printStackTrace();
			log.info("no reference source iterator " + plotID + "  r " + Arrays.toString(refRenameSchema) + "  t " + Arrays.toString(targetSchema));
			return input_iterator;
		}
		log.info("reeeeeeeeef:"+ref_iterator.toString());
		ref_iterator = new RenameQfIterator(ref_iterator, refRenameSchema);
		
		
		return new MergeIterator(targetSchema, new TsIterator[] {input_iterator, ref_iterator}, plotID);
	}

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public String[] getSchema() {
		return targetSchema;
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
		return source.isContinuous();
	}
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return source.getSourceVirtualPlot();
	}
	
	@Override
	public long[] getTimestampInterval() {
		return source.getTimestampInterval();
	}
	
	private static class RenameQfIterator extends InputIterator {
		private final int len;
		public static TsSchema createSchema(TsSchema tsSchema, String[] renames) {
			if(tsSchema.length != renames.length) {
				throw new RuntimeException("rename error");
			}
			String[] names = renames;
			Aggregation aggregation = Aggregation.CONSTANT_STEP;
			int timeStep = BaseAggregationTimeUtil.AGGREGATION_TIME_INTERVAL;
			boolean isContinuous = tsSchema.isContinuous;
			if(tsSchema.hasQualityFlags) {
				log.warn("quality flags will be ignored");
			}
			boolean hasQualityFlags = true;
			boolean hasInterpolatedFlags = false;
			boolean hasQualityCounters = false;
			return new TsSchema(names, aggregation, timeStep, isContinuous, hasQualityFlags, hasInterpolatedFlags, hasQualityCounters);
		}
		public RenameQfIterator(TsIterator it, String[] renames) {
			super(it,createSchema(it.getSchema(), renames));
			len = renames.length;
		}
		@Override
		public TsEntry next() {
			TsEntry e = input_iterator.next();
			DataQuality[] qf = TsEntry.createNaQuality(len);
			return new TsEntry(e.timestamp, e.data, qf, null);
		}		
	}
}