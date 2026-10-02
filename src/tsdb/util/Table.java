package tsdb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;

import org.tinylog.Logger;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

/**
 * Helper class to read csv files and get data as a table
 * @author woellauer
 *
 */
public class Table extends AbstractTable {

	/**
	 * table rows of csv file
	 */
	public String[][] rows;

	protected Table() {
		rows = new String[0][];
	}

	public static Table createEmpty() {
		return new Table();
	}

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
		try(InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			return readCSV(reader, separator);			
		} catch(Exception e) {
			Logger.error(e);
			return null;
		}
	}

	public static Table readCSV(Reader reader, char separator) {
		try {
			Table table = new Table();
			try(CSVReader csvReader = TableUtil.buildCSVReader(reader, separator)) {
				//List<String[]> list = reader.readAll(); // very slow because of linkedlist for indexed access
				if(TableUtil.readHeader(table, csvReader)) {
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
		try(CSVReader csvReader = TableUtil.buildCSVReader(reader, separator)) {
			//List<String[]> list = reader.readAll(); // very slow because of linkedlist for indexed access
			if(TableUtil.readHeader(table, csvReader)) {
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
			try(CSVReader csvReader = TableUtil.buildCSVReader(reader, separator)) {
				if(TableUtil.readHeader(table, csvReader)) {
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

	public void writeCSV(File file, char separator) {
		File tempFile = null;
		try {
			File parentDir = file.getParentFile();
			if(parentDir != null && !parentDir.exists()) {
				parentDir.mkdirs();
			}
			tempFile = File.createTempFile(file.getName(), ".tmp", parentDir);
			 tempFile.deleteOnExit();

			try (
					FileWriter writer = new FileWriter(tempFile, StandardCharsets.UTF_8); 
					CSVWriter csvWriter = new CSVWriter(writer, separator, ICSVWriter.DEFAULT_QUOTE_CHARACTER, ICSVWriter.DEFAULT_ESCAPE_CHARACTER, ICSVWriter.DEFAULT_LINE_END)
					) {
				csvWriter.writeNext(names, false);
				for (String[] row : rows) {
					csvWriter.writeNext(row, false);
				}
			}

			Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			tempFile = null;

		} catch (IOException e) {
			Logger.error("Error writing CSV file: " + file.getAbsolutePath(), e);
			throw new RuntimeException(e);
		} finally {
			if(tempFile != null && tempFile.exists()) {
				tempFile.delete();
			}
		}
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

	public void addRow(String[] row) {
		rows = Arrays.copyOf(rows, rows.length + 1);
		rows[rows.length - 1] = row;
	}

	public String[] newEmptyRow() {
		return new String[names.length];
	}
}
