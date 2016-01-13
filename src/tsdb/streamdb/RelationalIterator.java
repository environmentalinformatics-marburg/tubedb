package tsdb.streamdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.DataEntry;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;
import tsdb.util.processingchain.ProcessingChainEntry;

/**
 * Joins a collection of StreamIterators to TsIterator with outputSchema.
 * Fills not existing source rows with NaN-values.
 * @author woellauer
 *
 */
public class RelationalIterator extends TsIterator {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private StreamIterator[] iterators;
	private TsEntry curr;
	private DataEntry[] next;
	private int[] posIndex;

	private float[] NAN_DATA;

	public RelationalIterator(Collection<StreamIterator> iterators, String[] outputSchema) {
		this(iterators.toArray(new StreamIterator[0]),outputSchema);
	}

	public RelationalIterator(StreamIterator[] iterators, String[] outputSchema) {
		super(new TsSchema(outputSchema));
		this.iterators = iterators;
		Map<String, Integer> outputMap = Util.stringArrayToMap(outputSchema);
		posIndex = new int[iterators.length];
		for(int i=0;i<iterators.length;i++) {
			Integer pos = outputMap.get(iterators[i].sensorName);
			if(pos==null) {
				throw new RuntimeException("input not in output: "+iterators[i].sensorName+"  "+Arrays.toString(outputSchema));
			}
			posIndex[i] = pos;
		}
		NAN_DATA = new float[outputSchema.length];
		for(int i=0;i<outputSchema.length;i++) {
			NAN_DATA[i] = Float.NaN;
		}

		next = new DataEntry[iterators.length];		
		for(int i=0;i<iterators.length;i++) {
			if(iterators[i].hasNext()) {
				next[i] = iterators[i].next(); 
			} else {
				next[i] = null;
			}				
		}
		calcNext();
	}

	private void calcNext() {
		int timestamp = Integer.MAX_VALUE;
		for(int i=0;i<iterators.length;i++) {
			if(next[i]!=null&&next[i].timestamp<timestamp) {
				timestamp = next[i].timestamp;
			}
		}
		if(timestamp==Integer.MAX_VALUE) {
			curr = null;
			return;
		}
		float[] data = Arrays.copyOf(NAN_DATA, NAN_DATA.length);
		for(int i=0;i<iterators.length;i++) {
			DataEntry x = next[i];
			if(x!=null&&x.timestamp==timestamp) {
				//if(!Float.isNaN(x.value)) {
				data[posIndex[i]] = x.value;
				//}
				if(iterators[i].hasNext()) {
					next[i] = iterators[i].next();
				} else {
					next[i] = null;
				}
			}
		}
		curr = new TsEntry(timestamp, data);		
	}

	@Override
	public boolean hasNext() {
		return curr!=null;
	}

	@Override
	public TsEntry next() {
		TsEntry r = curr;
		calcNext();
		return r;
	}

	@Override
	public ProcessingChain getProcessingChain() {
		if(iterators.length==0) {
			return ProcessingChain.of(this);
		}
		TreeMap<String,ArrayList<String>> stationMap = new TreeMap<String,ArrayList<String>>();
		for(StreamIterator it:iterators) {
			ArrayList<String> list = stationMap.get(it.stationName);
			if(list==null) {
				list = new ArrayList<String>();
				stationMap.put(it.stationName, list);
			}
			list.add(it.sensorName);
		}
		String s="(";
		for(Entry<String, ArrayList<String>> entry:stationMap.entrySet()) {
			if(entry.getValue().size()==1) {
			s+=entry.getKey()+"/"+entry.getValue().get(0);
			} else {
				s+=entry.getKey()+"/{";//+entry.getValue().get(0);
				boolean first=true;
				for(String name:entry.getValue()) {
					if(first) {
						first = false;
					} else {
						s+=",";
					}
					s+=name;
				}
				s+="}";
			}
		}
		s+=")"+schema.toString();
		return ProcessingChain.of(ProcessingChainEntry.of(this.getProcessingTitle()+s));
	}
	
}
