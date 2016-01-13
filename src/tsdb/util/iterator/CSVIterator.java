package tsdb.util.iterator;

import java.nio.file.Path;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.Table;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;

public class CSVIterator extends TsIterator {

	private static final Logger log = LogManager.getLogger();

	public static TsSchema createSchema(String[] sensorNames) {
		return new TsSchema(sensorNames);
	}

	public static CSVIterator create(Path path, boolean trimSpacesInHeader) {
		return create(path.toString(), trimSpacesInHeader);
	}

	public static CSVIterator create(String filename, boolean trimSpacesInHeader) {
		Table table = Table.readCSV(filename, ',');
		String[] schema = Arrays.copyOfRange(table.names, 2, table.names.length);
		if(trimSpacesInHeader) {
			for(int i=0; i<schema.length; i++) {		
				schema[i] = schema[i].trim();
			}
		}
		return new CSVIterator(schema,table.rows,filename);
	}

	private final String filename;//for debug
	private String[][] rows;
	private int currIndex;


	public CSVIterator(String[] sensorNames, String[][] rows, String filename) {
		super(createSchema(sensorNames));
		this.filename = filename;
		this.rows = rows;
		this.currIndex = 0;
	}

	@Override
	public boolean hasNext() {
		return currIndex<rows.length;
	}

	@Override
	public TsEntry next() {		
		String[] row = rows[currIndex];
		currIndex++;
		long timestamp = TimeUtil.parseTimestamp(row[0], row[1], true);		
		float[] data = new float[schema.length];
		for(int colIndex=0;colIndex<schema.length;colIndex++) {
			try {
				if(!row[colIndex+2].isEmpty()) {
					data[colIndex] = Float.parseFloat(row[colIndex+2]);
				} else {
					data[colIndex] = Float.NaN;
				}
			} catch (Exception e) {
				data[colIndex] = Float.NaN;
				log.warn(e+ "   csv line "+(currIndex+1)+"  col "+(colIndex+2)+"   in "+filename+"   ->|"+row[colIndex+2]+"|<-");
			}
		}
		return new TsEntry(timestamp,data);
	}
}
