package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.TsDB;
import tsdb.component.Sensor;
import tsdb.component.iterator.PeakFlagIterator;
import tsdb.component.iterator.PhysicalFlagIterator;
import tsdb.graph.node.Node;
import tsdb.graph.source.DelegateNode;
import tsdb.util.DataQuality;
import tsdb.util.iterator.LowQualityToNanIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node filters source with range or with range and step check.
 * @author woellauer
 *
 */
public class RangeStepFiltered extends DelegateNode { // just range and step
	
	private final DataQuality dataQuality;

	protected RangeStepFiltered(TsDB tsdb, Node source, DataQuality dataQuality) {
		super(tsdb, source);
		throwNull(dataQuality);
		this.dataQuality = dataQuality;
	}
	
	public static RangeStepFiltered of(TsDB tsdb, Node source, DataQuality dataQuality) {
		if(DataQuality.Na == dataQuality) {
			throw new RuntimeException();
		}
		return new RangeStepFiltered(tsdb, source, dataQuality);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		Sensor[] sensors = tsdb.getSensors(input_iterator.getNames());
		//TsIterator qf = new PhysicalStepFlagIterator(sensors,input_iterator);
		//TsIterator qf = new PhysicalFlagIterator(sensors, input_iterator); // !!! physical check only	
		TsIterator qf = new PeakFlagIterator(sensors, new PhysicalFlagIterator(sensors, input_iterator)); // !!! added PeakFlagIterator		
		if(qf==null||!qf.hasNext()) {
			return null;
		}
		if(DataQuality.NO == dataQuality) {
			return qf;
		}
		DataQuality filterQuality = dataQuality == DataQuality.EMPIRICAL ? DataQuality.STEP : dataQuality;
		LowQualityToNanIterator bqi = new LowQualityToNanIterator(qf, filterQuality);
		if(bqi==null||!bqi.hasNext()) {
			return null;
		}		
		return bqi;
	}	
}
