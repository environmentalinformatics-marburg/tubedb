package tsdb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;

import org.tinylog.Logger;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Helper class to read csv files and get data as a table
 * @author woellauer
 *
 */
public class Table extends AbstractTable {

	static final Charset UTF8 = Charset.forName("UTF-8");
	private static final String UTF8_BOM = "\uFEFF";

	/**
	 * table rows of csv file
	 */
	public String[][] rows;

	protected Table() {}

	public static Table readCSV(Path filename, char separator) {
		return readCSV(filename.toFile(),separator);
	}

	public static Table readCSV(String filename, char separator) {
		return readCSV(new File(filename), separator);
	}
	
	/**
	 * create a Table Object from CSV-File
	 * @param filename
	 * @return
	 */
	public static Table readCSV(File file, char separator) {
		try(FileInputStream in = new FileInputStream(file)) {			
			return readCSV(in, separator);			
		} catch(Exception e) {
			Logger.error(e);
			return null;
		}
	}
	
	public static Table readCSV(InputStream in, char separator) {
		try(InputStreamReader reader = new InputStreamReader(in, UTF8)) {
			return readCSV(reader, separator);			
		} catch(Exception e) {
			Logger.error(e);
			return null;
		}
	}
	
	public static Table readCSV(Reader reader, char separator) {
		try {
			Table table = new Table();
			try(CSVReader csvReader = buildCSVReader(reader, separator)) {
				//List<String[]> list = reader.readAll(); // very slow because of linkedlist for indexed access
				if(Table.readHeader(table, csvReader)) {
					table.readRows(csvReader);
				}
			}
			return table;
		} catch(Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public static Table readCSVThrow(Reader reader, char separator) throws Exception {
		Table table = new Table();
		try(CSVReader csvReader = buildCSVReader(reader, separator)) {
			//List<String[]> list = reader.readAll(); // very slow because of linkedlist for indexed access
			if(Table.readHeader(table, csvReader)) {
				table.readRows(csvReader);
			}
		}
		return table;	 	
	}
	
	public static Table readCSVFirstDataRow(String filename, char separator) {
		try {
			return readCSVFirstDataRow(new FileReader(filename), separator);
		} catch(Exception e) {
			Logger.error(e);
			return null;
		}
	}
	
	public static Table readCSVFirstDataRow(Reader reader, char separator) {
		try {
			Table table = new Table();
			try(CSVReader csvReader = buildCSVReader(reader, separator)) {
				if(Table.readHeader(table, csvReader)) {
					String[] dataRow = csvReader.readNext();
					if(dataRow != null) {
						table.rows = new String[][] {dataRow};
					} else {
						return null;
					}	
				} else {
					return null;
				}
			}
			return table;
		} catch(Exception e) {
			Logger.error(e);
			return null;
		}
	}
	
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

	private void readRows(CSVReader reader) throws IOException {
		ArrayList<String[]> dataRowList = readRowList(reader);				
		String[][] tabeRows = dataRowList.toArray(new String[0][]);
		this.rows = tabeRows;
	}
	
	public static ArrayList<String[]> readRowList(CSVReader reader) throws IOException {
		ArrayList<String[]> dataRowList = new ArrayList<String[]>();
		String[] curRow = reader.readNextSilently();
		while(curRow != null){
			dataRowList.add(curRow);
			curRow = reader.readNextSilently();
		}				
		return dataRowList;
	}

	/**
	 * Comment line starts with '#'
	 * @param row
	 * @return
	 */
	public static boolean isComment(String[] row) {
		return row.length > 0 && row[0].length() > 0 && row[0].charAt(0) == '#';

	}

	/**
	 * Comment line starts with '#'
	 * @param row
	 * @return
	 */
	public static boolean isNoComment(String[] row) {
		return row.length == 0 || row[0].length() == 0 || row[0].charAt(0) != '#';

	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for(String name:names) {
			s.append(name);
			s.append(' ');
		}
		s.append('\n');
		for(String[] row:rows) {
			for(String cell:row) {
				s.append(cell);
				s.append(' ');
			}
			s.append('\n');
		}
		return s.toString();
	}
}
