package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.Arrays;

import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuousAbstract;
import tsdb.graph.source.GroupAverageSource_NEW;
import tsdb.iterator.MergeIterator;
import tsdb.util.BaseAggregationTimeUtil;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

public class ReferenceSourceMerge extends DelegateContinuousAbstract {	

	private final Continuous refSource; //not null	
	private final String plotID; //not null
	private final String[] refRenameSchema;
	private final String[] targetSchema;

	public ReferenceSourceMerge(TsDB tsdb, Continuous source, Continuous refSource, String plotID, String[] refRenameSchema, String[] targetSchema) {
		super(tsdb, source);
		throwNulls(refSource, plotID);
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
			Logger.warn("reference source");
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
			Logger.info("no reference source iterator " + plotID + "  r " + Arrays.toString(refRenameSchema) + "  t " + Arrays.toString(targetSchema));
			return input_iterator;
		}
		Logger.info("ref: " + ref_iterator.toString());
		ref_iterator = new RenameQfIterator(ref_iterator, refRenameSchema);
		
		
		return new MergeIterator(targetSchema, new TsIterator[] {input_iterator, ref_iterator}, plotID);
	}
	
	@Override
	public String[] getSchema() {
		return targetSchema;
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
				Logger.warn("quality flags will be ignored");
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