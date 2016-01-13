package tsdb.iterator;

import tsdb.util.BaseAggregationTimeUtil;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;

/**
 * This iterator checks values of input_iterator by comparing values to compare_iterator.
 * If value is higher than maxDiff a nan value is inserted. 
 * @author woellauer
 *
 */
public class EmpiricalIterator extends TsIterator {
	//private static final Logger log = LogManager.getLogger();

	private TsIterator input_iterator;
	private TsIterator compare_iterator;
	private Float[] maxDiff;
	private float[] refValues;

	public static TsSchema createSchema(TsSchema schema) {
		schema.throwNotContinuous();
		boolean isContinuous = true;
		schema.throwNoConstantTimeStep();
		Aggregation aggregation = Aggregation.CONSTANT_STEP;
		schema.throwNoBaseAggregation();
		int timeStep = BaseAggregationTimeUtil.AGGREGATION_TIME_INTERVAL;
		schema.throwNoQualityFlags();
		boolean hasQualityFlags = true;
		return new TsSchema(schema.names, aggregation, timeStep, isContinuous, hasQualityFlags);
	}

	public EmpiricalIterator(TsIterator input_iterator, TsIterator compare_iterator, Float[] maxDiff, float[] refValues) {
		super(createSchema(input_iterator.getSchema()));
		this.input_iterator = input_iterator;
		this.compare_iterator = compare_iterator;
		this.maxDiff = maxDiff;
		this.refValues = refValues;
	}

	@Override
	public boolean hasNext() {
		boolean hasNext = input_iterator.hasNext();
		boolean hasNextCompare = compare_iterator.hasNext();
		if(hasNext&&!hasNextCompare) {
			throw new RuntimeException("hasNext&&!hasNextCompare  "+TimeUtil.oleMinutesToText(input_iterator.next().timestamp));
		}
		return hasNext;
	}

	@Override
	public TsEntry next() {
		TsEntry element = input_iterator.next();
		TsEntry compareElement = compare_iterator.next();
		long timestamp = element.timestamp;
		if(timestamp!= compareElement.timestamp) {
			throw new RuntimeException("iterator error");
		}

		float[] result = new float[schema.length];
		DataQuality[] resultQf = new DataQuality[schema.length];
		for(int colIndex=0;colIndex<schema.length;colIndex++) {
			if(element.qualityFlag[colIndex]==DataQuality.STEP) {
				if(maxDiff[colIndex]!=null&&!Float.isNaN(compareElement.data[colIndex])) {
					if(Math.abs((element.data[colIndex]-refValues[colIndex])-compareElement.data[colIndex])<=maxDiff[colIndex]) { // check successful
						resultQf[colIndex] = DataQuality.EMPIRICAL;
						result[colIndex] = element.data[colIndex];
					} else { // remains STEP
						resultQf[colIndex] = DataQuality.STEP;
						result[colIndex] = Float.NaN;
					}
				} else { // no check possible
					resultQf[colIndex] = DataQuality.EMPIRICAL;
					result[colIndex] = element.data[colIndex];
				}
			} else {
				resultQf[colIndex] = element.qualityFlag[colIndex]; // Na, NO or PYSICAL 
				result[colIndex] = Float.NaN;
			}
			//System.out.println(element.qualityFlag[colIndex]+"  "+element.data[colIndex]+":  "+genElement.data[colIndex]+"  "+maxDiff[colIndex]+" -> "+Math.abs(result[colIndex]-genElement.data[colIndex])+"  "+resultQf[colIndex]+"  "+result[colIndex]);
		}
		return new TsEntry(timestamp,result,resultQf);
	}

	@Override
	public ProcessingChain getProcessingChain() {
		return ProcessingChain.of(new TsIterator[]{input_iterator,compare_iterator}, this);
	}
}
