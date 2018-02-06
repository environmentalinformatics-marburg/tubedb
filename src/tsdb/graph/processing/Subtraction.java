package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Node: subtracts values to all entries of source
 * @author woellauer
 *
 */
public class Subtraction implements Continuous {
	
	private final Continuous source;
	private final float[] values;
	
	protected Subtraction(Continuous source, float[] values) {
		throwNull(source);
		this.source = source;
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

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public boolean isConstantTimestep() {
		return source.isConstantTimestep();
	}

	@Override
	public String[] getSchema() {
		return source.getSchema();
	}

	@Override
	public TsIterator getExactly(long start, long end) {
		return get(start,end);
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
