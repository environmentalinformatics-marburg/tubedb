package tsdb.util;

import java.io.IOException;
import java.io.Reader;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class TableUtil {
	
	private static final String UTF8_BOM = "\uFEFF";
	
	static CSVReader buildCSVReader(Reader reader, char separator) {
		CSVParser csvParser = new CSVParserBuilder().withSeparator(separator).build();
		return new CSVReaderBuilder(reader).withCSVParser(csvParser).build();
	}
	
	static boolean readHeader(AbstractTable table, CSVReader csvReader) throws IOException {
		String[] curRow = csvReader.readNextSilently();
		if(curRow != null) {
			String[] columnsNames = curRow;
			if(columnsNames.length>0) { // filter UTF8 BOM
				if(columnsNames[0].startsWith(UTF8_BOM)) {
					columnsNames[0] = columnsNames[0].substring(1, columnsNames[0].length());
				}
			}			
			table.updateNames(columnsNames);
			//Logger.info("names: "+Arrays.toString(table.names)+"   in "+filename);
			return true;
		} else {
			return false;
		}
	}
}
