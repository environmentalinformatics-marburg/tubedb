package tsdb.graph.processing;

import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuous;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Node: subtracts values to all entries of source
 * @author woellauer
 *
 */
public class Subtraction extends DelegateContinuous {
	
	private final float[] values;
	
	protected Subtraction(Continuous source, float[] values) {
		super(source);
		this.values = values;
	}
	
	public static Subtraction of(Continuous source, float[] values) {
		return new Subtraction(source, values);
	}
	
	public static Subtraction createWithElevationTemperature(TsDB tsdb, Continuous source, String plotID) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot==null) {
			return null;
		}
		if(Float.isNaN(virtualPlot.elevationTemperature)) {
			return null;
		}		
		float[] refs = tsdb.getReferenceValues(plotID, source.getSchema());
		boolean allZero = true;
		for (int i = 0; i < refs.length; i++) {
			if(refs[i] != 0.0f) {
				allZero = false;
				break;
			}
		}
		if(allZero) {
			return null;
		}
		return new Subtraction(source, refs);
	}
	
	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		
		return new InputProcessingIterator(input_iterator,input_iterator.getSchema()) {
			@Override
			protected TsEntry getNext() {
				if(!input_iterator.hasNext()) {
					return null;
				}
				TsEntry element = input_iterator.next();
				float[] data = element.data;
				float[] result = new float[data.length];
				for(int i=0;i<data.length;i++) {
					result[i] = data[i] - values[i];
				}
				return new TsEntry(element.timestamp, result);
			}			
		};
	}	
}
