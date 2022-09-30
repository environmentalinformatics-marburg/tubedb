package tsdb.util.iterator;

import java.nio.file.Path;
import java.util.Arrays;


import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import tsdb.util.Table;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;

/**
 * A TsIterator with CSV-file as source.
 * 
 * CSV-file format:
 * 
 * header:
 * [Date resp. first entry],[Time resp. second entry],[sensor_name_1 (third entry)],[sensor_name_2 (fourth entry)],...
 * 
 * e.g.:
 * data,time,Ta_200,rH_200
 * 
 * 
 * row:
 * [ISO-date YYYY-MM-DD],[time hh:mm:ss],[value_1],[value_2],...
 * 
 * e.g.:
 * 2016-01-11,17:32:00,12.34,23.45
 * 
 * missing values are denoted by empty entry or NA
 * missing time leads to 12:00:00
 * e.g.:
 * 2016-01-11,,12.34,23.45
 * 2016-01-11,17:32:00,,23.45
 * 2016-01-11,NA,12.34,23.45
 * 2016-01-11,17:32:00,NA,23.45
 * 
 * @author woellauer
 *
 */
public class CSVIterator extends TsIterator {

	

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
				if( !(row[colIndex+2].isEmpty() || row[colIndex+2].equals("NA")) ) {
					//data[colIndex] = Float.parseFloat(row[colIndex+2]);
					data[colIndex] = (float) FastDoubleParser.parseDouble(row[colIndex+2]);
				} else {
					data[colIndex] = Float.NaN;
				}
			} catch (Exception e) {
				data[colIndex] = Float.NaN;
				Logger.warn(e+ "   csv line "+(currIndex+1)+"  col "+(colIndex+2)+"   in "+filename+"   ->|"+row[colIndex+2]+"|<-");
			}
		}
		return new TsEntry(timestamp,data);
	}
}
