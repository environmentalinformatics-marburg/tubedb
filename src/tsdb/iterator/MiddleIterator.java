package tsdb.iterator;

import static tsdb.util.AssumptionCheck.throwEmpty;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.Util;
import tsdb.util.iterator.MoveIterator;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;

/**
 * This iterator outputs elements of average values of input_iterator values.
 * input_iterators need to be in same timestamp per element order.
 * @author woellauer
 *
 */
public class MiddleIterator extends MoveIterator {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private Map<String, Integer> schemaMap;
	private TsIterator[] input_iterators;
	//private static final int SOURCE_COUNT = 3;

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

	public MiddleIterator(String[] schema, TsIterator[] input_iterators) {
		super(createSchema(schema, input_iterators));
		this.input_iterators = input_iterators;
		this.schemaMap = Util.stringArrayToMap(schema);
	}

	@Override
	protected TsEntry getNext() {
		float[] v0 = new float[this.schema.length];	
		float[] v1 = new float[this.schema.length];
		float[] v2 = new float[this.schema.length];
		float[] vselect = new float[this.schema.length];	
		
		if(!input_iterators[0].hasNext()||!input_iterators[1].hasNext()||!input_iterators[2].hasNext()) {
			return null;
		}
		
		TsEntry element0 = input_iterators[0].next();
		TsEntry element1 = input_iterators[1].next();
		TsEntry element2 = input_iterators[2].next();
		long timestamp = element0.timestamp;
		if(timestamp != element1.timestamp || timestamp != element2.timestamp) {
			throw new RuntimeException("iterator error");
		}

		String[] schema0 = input_iterators[0].getNames();
		for(int i=0;i<schema0.length;i++) {
			int pos = schemaMap.get(schema0[i]);
			v0[pos] = element0.data[i];			
		}
		
		String[] schema1 = input_iterators[1].getNames();
		for(int i=0;i<schema1.length;i++) {
			int pos = schemaMap.get(schema1[i]);
			v1[pos] = element1.data[i];			
		}
		
		String[] schema2 = input_iterators[0].getNames();
		for(int i=0;i<schema2.length;i++) {
			int pos = schemaMap.get(schema2[i]);
			v2[pos] = element2.data[i];			
		}
		
		for(int i=0;i<vselect.length;i++) {
			float avg = (v0[i]+v1[i]+v2[i])/3;
			float d0 = Math.abs(v0[i]-avg);
			float d1 = Math.abs(v1[i]-avg);
			float d2 = Math.abs(v2[i]-avg);
			if(d0<d1) { // d0 < d1
				if(d1<d2) { // d0 < d1 < d2
					vselect[i] = (v0[i]+v1[i])/2;
				} else { // d0,d2 < d1
					vselect[i] = (v0[i]+v2[i])/2;
				}
			} else { // d1 < d0
				if(d0<d2) { // d1 < d0 < d2
					vselect[i] = (v1[i]+v0[i])/2;
				} else {// d1, d2 < d0
					vselect[i] = (v1[i]+v2[i])/2;
				}
			}
		}

		return new TsEntry(timestamp, vselect);	
	}
	
	@Override
	public ProcessingChain getProcessingChain() {
		return ProcessingChain.of(input_iterators, this);
	}
}
