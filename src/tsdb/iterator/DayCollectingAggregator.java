package tsdb.iterator;

import java.util.ArrayList;

import tsdb.TsDB;
import tsdb.component.Sensor;
import tsdb.util.AggregationType;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;

/**
 * collecting aggregator to day
 * @author woellauer
 *
 */
public class DayCollectingAggregator implements CollectingAggregator {

	private final TsIterator input_iterator;
	private final Sensor[] sensors;
	
	private TsEntry nextInputEntry;
	private long nextInputAggregationTimestamp;

	private final ArrayList<Float>[] outputs; //No NaNs
	private long outputTimestamp;

	
	@SuppressWarnings("unchecked")
	public DayCollectingAggregator(TsDB tsdb, TsIterator input_iterator) {
		this.input_iterator = input_iterator;
		this.sensors = tsdb.getSensors(input_iterator.getSchema().names);
		this.outputs = new ArrayList[input_iterator.getSchema().length];
		this.outputTimestamp = -1;
		for(int i=0;i<outputs.length;i++) {
			outputs[i] = new ArrayList<Float>(24);
		}
		calcNextInput();
	}
	
	@Override
	public int getAttributeCount() {
		return input_iterator.getSchema().length;
	}

	private void calcNextInput() {
		if(input_iterator.hasNext()) {
			nextInputEntry = input_iterator.next();
			nextInputAggregationTimestamp = calcAggregationTimestamp(nextInputEntry.timestamp);
		} else {
			nextInputEntry = null;
			nextInputAggregationTimestamp = -1;
		}		
	}

	@Override
	public long calcNextOutput() {
		//int validAttributesCount = 0;
		//while(validAttributesCount==0) {
			for(ArrayList<Float> list:outputs) {
				list.clear();
			}
			long currentAggregationTimestamp = nextInputAggregationTimestamp;
			if(currentAggregationTimestamp<0) {
				outputTimestamp=-1;
				return -1;
			}
			for(int i=0;i<nextInputEntry.data.length;i++) {
				if(!Float.isNaN(nextInputEntry.data[i])) {
					outputs[i].add(nextInputEntry.data[i]);
				}
			}
			calcNextInput();
			while(currentAggregationTimestamp==nextInputAggregationTimestamp) {
				for(int i=0;i<nextInputEntry.data.length;i++) {
					if(!Float.isNaN(nextInputEntry.data[i])) {
						outputs[i].add(nextInputEntry.data[i]);
					}
				}
				calcNextInput();
			}
			for(int i=0;i<outputs.length;i++) {
				if(/*outputs[i].size()>=22*/isValidAggregate(outputs[i].size(),sensors[i].baseAggregationType)) { // change per attribute type
					//validAttributesCount++;
				} else {
					outputs[i].clear();
				}
			}
			//if(validAttributesCount>0) {
				outputTimestamp = currentAggregationTimestamp;
			//} else {
			//	outputTimestamp = -1;
			//}
		//}
		return outputTimestamp;
	}

	private long calcAggregationTimestamp(long timestamp) {
		return timestamp - timestamp%(24*60);
	}

	private boolean isValidAggregate(int collectorCount, AggregationType aggregationType) {
		if(aggregationType == AggregationType.AVERAGE_ALBEDO) {
			return 5<=collectorCount;
		}
		return 22<=collectorCount;				
	}

	@Override
	public ArrayList<Float>[] getOutputs() {
		return outputs;
	}

	@Override
	public String getProcessingTitle() {
		return "DayCollectingAggregator";
	}

	@Override
	public ProcessingChain getProcessingChain() {		
		return ProcessingChain.of(input_iterator,this);
	}
}
