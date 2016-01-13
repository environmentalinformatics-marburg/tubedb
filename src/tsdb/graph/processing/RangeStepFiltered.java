package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.component.Sensor;
import tsdb.component.iterator.QualityFlagIterator;
import tsdb.graph.node.Node;
import tsdb.util.DataQuality;
import tsdb.util.iterator.LowQualityToNanIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node filters source with range or with range and step check.
 * @author woellauer
 *
 */
public class RangeStepFiltered extends Node.Abstract{ // just range and step
	
	private final Node source;
	private final DataQuality dataQuality;

	protected RangeStepFiltered(TsDB tsdb, Node source, DataQuality dataQuality) {
		super(tsdb);
		throwNulls(source, dataQuality);
		this.source = source;
		this.dataQuality = dataQuality;
	}
	
	public static RangeStepFiltered of(TsDB tsdb, Node source, DataQuality dataQuality) {
		if(DataQuality.Na==dataQuality) {
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
		QualityFlagIterator qf = new QualityFlagIterator(sensors,input_iterator);
		if(qf==null||!qf.hasNext()) {
			return null;
		}
		if(DataQuality.NO==dataQuality) {
			return qf;
		}
		DataQuality filterQuality = dataQuality==DataQuality.EMPIRICAL?DataQuality.STEP:dataQuality;
		LowQualityToNanIterator bqi = new LowQualityToNanIterator(qf, filterQuality);
		if(bqi==null||!bqi.hasNext()) {
			return null;
		}		
		return bqi;
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
