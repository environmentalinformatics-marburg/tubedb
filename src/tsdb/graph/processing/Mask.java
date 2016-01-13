package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Node;
import tsdb.iterator.MaskIterator;
import tsdb.util.TimeSeriesMask;
import tsdb.util.iterator.TsIterator;

public class Mask extends Node.Abstract{
	private static final Logger log = LogManager.getLogger();
	
	private final Node source;
	private final TimeSeriesMask[] masks;

	protected Mask(TsDB tsdb, Node source, TimeSeriesMask[] masks) {
		super(tsdb);
		throwNulls(source, masks);
		this.source = source;
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
		log.info("get masks "+mask_counter);
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
		log.info("with mask !!!");
		MaskIterator it = new MaskIterator(input_iterator,masks);
		return it;
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