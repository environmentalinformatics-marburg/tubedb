package tsdb.iterator;

import java.util.ArrayList;

import tsdb.util.processingchain.ProcessingChainNode;

/**
 * Interface for collecting aggregators
 * @author woellauer
 *
 */
public interface CollectingAggregator extends ProcessingChainNode {
	int getAttributeCount();
	long calcNextOutput();
	ArrayList<Float>[] getOutputs();
}
