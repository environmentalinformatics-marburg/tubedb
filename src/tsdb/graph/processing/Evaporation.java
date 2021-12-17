package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import java.util.Arrays;


import org.tinylog.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Node;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Virtual sensor Sunshine calculates sunshine duration from radiation source.
 * @author woellauer
 *
 */
public class Evaporation extends Node.Abstract{

	public static final String SOURCE_SENSOR_NAME = "P_container_NRT";
	public static final String SENSOR_NAME = "evaporation";

	private final Node source;
	private final double latitude_DEG;
	private final double longitude_DEG;

	protected Evaporation(TsDB tsdb,Node source) {
		super(tsdb);
		throwNull(source);
		this.source = source;
		double[] latlon = source.getSourcePlot().getLatLon();
		System.out.println(Arrays.toString(latlon));
		this.latitude_DEG = latlon[0];
		this.longitude_DEG = latlon[1]; 
	}

	public static Evaporation of(TsDB tsdb, Node source) {
		return new Evaporation(tsdb, source);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}			
		return new EvaporationIterator(input_iterator, latitude_DEG, longitude_DEG);
	}

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
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
	public String[] getSchema() {
		return source.getSchema();
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

class EvaporationIterator extends InputIterator {
	

	private int sensor_pos = -1;
	private int prevTimestamp = -1;
	private float prevValue = -1000;

	public EvaporationIterator(TsIterator input_iterator, double latitude_DEG, double longitude_DEG) {
		super(input_iterator, input_iterator.getSchema());

		String[] names = this.getNames();
		for(int i=0;i<names.length;i++) {
			if(names[i].equals(Evaporation.SENSOR_NAME)) {
				sensor_pos = i;
				break;
			}
		}
		AssumptionCheck.throwTrue(sensor_pos<0,"sensor not found for EvaporationIterator");		
	}

	@Override
	public TsEntry next() {
		TsEntry entry = input_iterator.next();
		float[] data = Arrays.copyOf(entry.data, entry.data.length);
		float value = entry.data[sensor_pos];
		data[sensor_pos] = Float.isNaN(value)?Float.NaN:calc((int) entry.timestamp, value);
		DataQuality[] qf;
		if(entry.qualityFlag!=null) {
			qf = Arrays.copyOf(entry.qualityFlag, entry.qualityFlag.length);
		} else {
			qf = null;
		}
		return new TsEntry(entry.timestamp, data, qf);
	}

	public float calc(int timestamp, float value) {
		int deltaT = timestamp - prevTimestamp;
		float result = (prevValue - value)*60f/deltaT;
		prevValue = value;
		prevTimestamp = timestamp;
		return 120<deltaT || result<0f || result>2f ? Float.NaN : result;
	}

}
