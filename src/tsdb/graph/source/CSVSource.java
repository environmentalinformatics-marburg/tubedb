package tsdb.graph.source;

import java.util.Arrays;

import static tsdb.util.AssumptionCheck.throwNull;
import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Node;
import tsdb.util.Table;
import tsdb.util.Util;
import tsdb.util.iterator.CSVIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node provides a source from csv-file.
 * @author woellauer
 *
 */
public class CSVSource implements Node {
	
	private final String[] sensorNames;
	private final String[][] rows;
	private final String filename; //for debug

	protected CSVSource(String[] sensorNames,String[][] rows,String filename) {
		this.sensorNames = sensorNames;
		this.rows = rows;
		this.filename = filename;
		throwNull((Object)sensorNames);
		throwNull((Object)rows);
	}
	
	public static CSVSource of(String filename) {		
		Table table = Table.readCSV(filename, ',');
		System.out.println("header: "+Util.arrayToString(table.names));
		String[] sensorNames = Arrays.copyOfRange(table.names, 2, table.names.length);
		return new CSVSource(sensorNames, table.rows, filename);
	}

	@Override
	public TsIterator get(Long start, Long end) { // TODO start, end		
		return new CSVIterator(sensorNames, rows, filename);
	}

	@Override
	public Station getSourceStation() {
		return null;
	}

	@Override
	public boolean isContinuous() {
		return false;
	}

	@Override
	public boolean isConstantTimestep() {
		return false;
	}

	@Override
	public String[] getSchema() {
		return sensorNames;
	}
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return null;
	}
	
	@Override
	public long[] getTimestampInterval() {//TODO
		throw new UnsupportedOperationException();
	}

}
