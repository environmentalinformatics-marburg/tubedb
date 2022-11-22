package tsdb.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.tinylog.Logger;

import com.opencsv.CSVReader;

public class StreamTable extends AbstractTable {

	private CSVReader csvReader;

	public static StreamTable openCSV(String filename, char separator) throws FileNotFoundException, IOException {
		return openCSV(new File(filename), separator);
	}

	public static StreamTable openCSV(Path filename, char separator) throws FileNotFoundException, IOException {
		return openCSV(filename.toFile(),separator);
	}

	/**
	 * create a Table Object from CSV-File
	 * @param filename
	 * @return
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static StreamTable openCSV(File file, char separator) throws FileNotFoundException, IOException {	
		FileInputStream in = new FileInputStream(file);
		return openCSV(in, separator);
	}

	public static StreamTable openCSV(InputStream in, char separator) throws IOException {
		Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
		return openCSV(reader, separator);	
	}

	public static StreamTable openCSV(Reader reader, char separator) throws IOException {
		StreamTable table = new StreamTable();
		CSVReader csvReader = TableUtil.buildCSVReader(reader, separator);
		if(TableUtil.readHeader(table, csvReader)) {
			table.csvReader = csvReader;
			return table;
		} else {
			throw new RuntimeException("no CSV header");
		}

	}

	public String[] readNext() throws IOException {
		return csvReader.readNextSilently();
	}

	public void close() {
		CSVReader r = csvReader;
		if(r != null) {
			try {
				r.close();
				csvReader = null;
			} catch (IOException e) {
				Logger.warn(e);				
			}
		}
	}	
}
