package tsdb.graph.processing;

import tsdb.TsDB;
import tsdb.graph.node.Node;
import tsdb.graph.source.DelegateNode;
import tsdb.iterator.SunshineOlivieriIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Virtual sensor Sunshine calculates sunshine duration from radiation source.
 * @author woellauer
 *
 */
public class SunshineOlivieri extends DelegateNode {
	
	private final double latitude_DEG;
	private final double longitude_DEG;
	
	protected SunshineOlivieri(TsDB tsdb,Node source) {
		super(tsdb, source);
		double[] latlon = source.getSourcePlot().getLatLon();
		//System.out.println(Arrays.toString(latlon));
		this.latitude_DEG = latlon[0];
		this.longitude_DEG = latlon[1]; 
	}
	
	public static SunshineOlivieri of(TsDB tsdb, Node source) {
		return new SunshineOlivieri(tsdb, source);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}			
		return new SunshineOlivieriIterator(input_iterator, latitude_DEG, longitude_DEG);
	}	
}