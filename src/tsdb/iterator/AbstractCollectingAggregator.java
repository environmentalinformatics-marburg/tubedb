package tsdb.iterator;

import java.util.ArrayList;

import tsdb.util.AggregationType;
import tsdb.util.processingchain.ProcessingChain;

/**
 * Base class for collecting aggregators
 * @author woellauer
 *
 */
public abstract class AbstractCollectingAggregator implements CollectingAggregator  {
		private CollectingAggregator collectingAggregator;

		ArrayList<Float>[] nextInput;
		long nextInputAggregationTimestamp;

		public final ArrayList<Float>[] outputs;
		public final int[] outputCounters;
		public long outputTimestamp;

		@SuppressWarnings("unchecked")
		public AbstractCollectingAggregator(CollectingAggregator collectingAggregator, int estimatedAggregationCapacity) {
			this.collectingAggregator = collectingAggregator;
			this.outputs = new ArrayList[collectingAggregator.getAttributeCount()];
			this.outputCounters = new int[collectingAggregator.getAttributeCount()];
			this.outputTimestamp = -1;
			for(int i=0;i<outputs.length;i++) {
				outputs[i] = new ArrayList<Float>(estimatedAggregationCapacity);
			}
			calcNextInput();
		}

		private void calcNextInput() {
			long nextInputTimestamp = collectingAggregator.calcNextOutput();
			if(nextInputTimestamp>=0) {
				nextInput = collectingAggregator.getOutputs();
				nextInputAggregationTimestamp = calcAggregationTimestamp(nextInputTimestamp);
			} else {
				nextInput = null;
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
				for(int i=0;i<outputCounters.length;i++) {
					outputCounters[i] = 0;
				}
				long currentAggregationTimestamp = nextInputAggregationTimestamp;
				if(currentAggregationTimestamp<0) {
					outputTimestamp = -1;
					return -1;
				}
				for(int i=0;i<outputs.length;i++) {
					if(nextInput[i].size()>0) {
						outputs[i].addAll(nextInput[i]); //no NaNs
						outputCounters[i]++;
					}
				}
				calcNextInput();
				while(currentAggregationTimestamp==nextInputAggregationTimestamp) {
					for(int i=0;i<outputs.length;i++) {
						if(nextInput[i].size()>0) {
							outputs[i].addAll(nextInput[i]); //no NaNs
							outputCounters[i]++;
						}
					}
					calcNextInput();
				}


				for(int i=0;i<outputs.length;i++) {
					if(/*outputCounters[i]>=27*/isValidAggregate(outputCounters[i],null)) { // change per attribute type
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

		protected abstract long calcAggregationTimestamp(long timestamp);
		
		protected abstract boolean isValidAggregate(int collectorCount, AggregationType aggregationType);

		@Override
		public ArrayList<Float>[] getOutputs() {
			return outputs;
		}

		@Override
		public int getAttributeCount() {
			return collectingAggregator.getAttributeCount();
		}
		
		@Override
		public ProcessingChain getProcessingChain() {		
			return ProcessingChain.of(collectingAggregator,this);
		}

	}