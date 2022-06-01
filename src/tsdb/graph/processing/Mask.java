package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.graph.node.Node;
import tsdb.graph.source.DelegateNode;
import tsdb.iterator.MaskIterator;
import tsdb.util.TimeSeriesMask;
import tsdb.util.iterator.TsIterator;

public class Mask extends DelegateNode {	
	
	private final TimeSeriesMask[] masks;

	protected Mask(TsDB tsdb, Node source, TimeSeriesMask[] masks) {
		super(tsdb, source);
		throwNull(masks);
		this.masks = masks;
	}
	
	public static Node of(TsDB tsdb, Node source) {		
		String sourceName = source.getSourceName();
		String[] sensorNames = source.getSchema();
		TimeSeriesMask[] masks = new TimeSeriesMask[sensorNames.length];
		int mask_counter = 0;
		for (int i = 0; i < sensorNames.length; i++) {
			masks[i] = tsdb.streamStorage.getTimeSeriesMask(sourceName, sensorNames[i]);
			if(masks[i]!=null) {
				mask_counter++;
			}
		}
		Logger.trace("get masks "+mask_counter);
		if(mask_counter>0) {
			return new Mask(tsdb,source,masks);
		} else {
			return source;
		}
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		//Logger.info("with mask !!!");
		MaskIterator it = new MaskIterator(input_iterator,masks);
		return it;
	}	
}