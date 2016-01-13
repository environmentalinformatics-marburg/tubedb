package tsdb.iterator;

import static tsdb.util.AssumptionCheck.throwEmpty;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.MoveIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This iterator outputs minimum difference elements of input_iterators to one target_iterator.
 * input_iterators and target_iterator need to be in same timestamp per element order.
 * @author woellauer
 *
 */
public class MinDiffIterator extends MoveIterator {
	//private static final Logger log = LogManager.getLogger();

	//private Map<String, Integer> schemaMap;
	private TsIterator target_iterator;
	private TsIterator[] source_iterators;
	private final int minSources;


	private static TsSchema createSchema(String[] names, TsIterator[] input_iterators) {
		throwEmpty(input_iterators);
		TsSchema[] schemas = TsIterator.toSchemas(input_iterators);
		TsSchema.throwDifferentAggregation(schemas);
		Aggregation aggregation = schemas[0].aggregation;
		TsSchema.throwDifferentTimeStep(schemas);
		int timeStep = schemas[0].timeStep;
		TsSchema.throwDifferentContinuous(schemas);
		boolean isContinuous = schemas[0].isContinuous;
		return new TsSchema(names, aggregation,timeStep ,isContinuous);
	}

	public MinDiffIterator(String[] schema, TsIterator target_iterator, TsIterator[] source_iterators, int minSources) {
		super(createSchema(schema, source_iterators));
		this.target_iterator = target_iterator;
		this.source_iterators = source_iterators;
		this.minSources = minSources;
		//this.schemaMap = Util.stringArrayToMap(schema);
	}

	@Override
	protected TsEntry getNext() {
		if(!target_iterator.hasNext()) {
			return null;
		}
		TsEntry targetEntry = target_iterator.next();
		/*float[] result = TsEntry.createNanData(targetEntry.data.length);
		for(TsIterator it:source_iterators) {
			TsEntry e = it.next();
			String[] schema0 = it.getNames();
			for(int i=0;i<schema0.length;i++) {
				int pos = schemaMap.get(schema0[i]);
				float diff = targetEntry.data[pos] - e.data[i];
				if(Float.isNaN(result[i]) || ((!Float.isNaN(diff)) && diff<result[i])) {
					result[i] = diff;
				}
			}
		}*/
		float target = targetEntry.data[0];
		float result = Float.NaN;
		int count=0;
		for(TsIterator it:source_iterators) {
			TsEntry e = it.next();
			float value = target-e.data[0];
			if(Float.isFinite(value) && (  Float.isNaN(result)  || Math.abs(value)<Math.abs(result)  )) {
				result = value;
				count++;
			}
		}
		if(count<minSources) {
			result = Float.NaN;
		}

		return TsEntry.of(targetEntry.timestamp, result);	
	}
	
	/*@Override
	public ProcessingChain getProcessingChain() {
		return ProcessingChain.of(input_iterators, this);
	}*/
}
