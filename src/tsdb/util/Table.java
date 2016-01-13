package tsdb.util;

import static tsdb.util.AssumptionCheck.throwFalse;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import tsdb.util.Table.ColumnReaderIntFunc.IntegerParser;

/**
 * Helper class to read csv files and get data as a table
 * @author woellauer
 *
 */
public class Table {
	private static final Logger log = LogManager.getLogger();
	
	private static final Charset UTF8 = Charset.forName("UTF-8");
	private static final String UTF8_BOM = "\uFEFF";

	public static class ColumnReader {
		public final int rowIndex;
		public ColumnReader(int rowIndex) {
			throwFalse(rowIndex>=0);
			this.rowIndex = rowIndex;
		}
	}

	public static class ColumnReaderString extends ColumnReader {
		public ColumnReaderString(int rowIndex) {
			super(rowIndex);
		}
		public String get(String[] row) {
			return row[rowIndex];
		}
	}

	public static class ColumnReaderFloat extends ColumnReader {
		public ColumnReaderFloat(int rowIndex) {
			super(rowIndex);
		}
		public float get(String[] row, boolean warnIfEmpty) {			
			try {
				String textValue = row[rowIndex];
				if(!warnIfEmpty&&textValue.isEmpty()) {
					return Float.NaN;
				}
				return Float.parseFloat(row[rowIndex]);
			} catch(NumberFormatException e) {
				if(row[rowIndex].toLowerCase().equals("na")||row[rowIndex].toLowerCase().equals("null")) {
					return Float.NaN;
				} else {
					log.warn(row[rowIndex]+" not parsed");
					e.printStackTrace();
					return Float.NaN;
				}
			}
		}
	}
	
	public static class ColumnReaderDouble extends ColumnReader {
		public ColumnReaderDouble(int rowIndex) {
			super(rowIndex);
		}
		public double get(String[] row, boolean warnIfEmpty) {			
			try {
				String textValue = row[rowIndex];
				if(!warnIfEmpty&&textValue.isEmpty()) {
					return Double.NaN;
				}
				return Double.parseDouble(row[rowIndex]);
			} catch(NumberFormatException e) {
				if(row[rowIndex].toLowerCase().equals("na")||row[rowIndex].toLowerCase().equals("null")) {
					return Double.NaN;
				} else {
					log.warn(row[rowIndex]+" not parsed");
					e.printStackTrace();
					return Double.NaN;
				}
			}
		}
	}
	
	public static class ColumnReaderInt extends ColumnReader {
		public ColumnReaderInt(int rowIndex) {
			super(rowIndex);
		}
		public int get(String[] row) {
			return Integer.parseInt(row[rowIndex]);
		}
	}
	
	public static class ColumnReaderIntFunc extends ColumnReader {
		private final IntegerParser parser;
		public ColumnReaderIntFunc(int rowIndex, IntegerParser parser) {
			super(rowIndex);
			this.parser = parser;
		}
		public int get(String[] row) {
			return parser.parse(row[rowIndex]);
		}
		public interface IntegerParser {
			int parse(String text);
		}
	}
	
	public static interface ColumnReaderTimestamp {
		public long get(String[] row);
	}

	public static class ColumnReaderTimestampTwoCols implements ColumnReaderTimestamp {

		private final int rowIndexDate;
		private final int rowIndexTime;

		public ColumnReaderTimestampTwoCols(int rowIndexDate, int rowIndexTime) {
			this.rowIndexDate = rowIndexDate;
			this.rowIndexTime = rowIndexTime;
		}
		public long get(String[] row) {			
			try {
				return TimeUtil.parseTimestamp(row[rowIndexDate], row[rowIndexTime], true);				
			} catch(NumberFormatException e) {
				log.warn(row[rowIndexDate]+"  "+row[rowIndexTime]+"not parsed");
				return -1;
			}
		}
	}
	
	public static class ColumnReaderSlashTimestamp implements ColumnReaderTimestamp {
		private final int rowIndexDateTime;
		public ColumnReaderSlashTimestamp(int rowIndexDateTime) {
			this.rowIndexDateTime = rowIndexDateTime;
		}
		public long get(String[] row) {			
			try {
				return TimeUtil.parseTimestampSlashFormat(row[rowIndexDateTime]);				
			} catch(NumberFormatException e) {
				log.warn(row[rowIndexDateTime]+"  not parsed");
				return -1;
			}
		}
	}
	
	public static class ColumnReaderMonthNameTimestamp implements ColumnReaderTimestamp {
		private final int rowIndexDateTime;
		public ColumnReaderMonthNameTimestamp(int rowIndexDateTime) {
			this.rowIndexDateTime = rowIndexDateTime;
		}
		public long get(String[] row) {			
			try {
				return TimeUtil.parseTimestampMonthNameFormat(row[rowIndexDateTime]);				
			} catch(NumberFormatException e) {
				log.warn(row[rowIndexDateTime]+"  not parsed");
				return -1;
			}
		}
	}
	
	
	public static class ColumnReaderDateFullHourTimestamp implements ColumnReaderTimestamp {
		private final int columnIndexDate;
		private final int columnIndexFullHour;
		public ColumnReaderDateFullHourTimestamp(int columnIndexDate, int columnIndexTime) {
			this.columnIndexDate = columnIndexDate;
			this.columnIndexFullHour = columnIndexTime;
		}
		public long get(String[] row) {			
			try {
				return TimeUtil.parseTimestampDateFullHourFormat(row[columnIndexDate], Integer.parseInt(row[columnIndexFullHour]));		
			} catch(NumberFormatException e) {
				log.warn(row[columnIndexDate]+"  not parsed");
				return -1;
			}
		}
	}
	
	/**
	 * example: "2014-04-14";"23";"45";"0"
	 * example: "2014-04-14";"24";"0";"0"
	 * example: "2014-04-15";"0";"30";"0"
	 * example: "2014-04-15";"0";"15";"0"
	 * example: "2014-04-15";"0";"45";"0" 
	 *
	 */
	public static class ColumnReaderDateHourWrapMinuteTimestamp implements ColumnReaderTimestamp {
		private final int columnIndexDate;
		private final int columnIndexHourWrap;
		private final int columnIndexMinute;
		public ColumnReaderDateHourWrapMinuteTimestamp(int columnIndexDate, int columnIndexHourWrap, int columnIndexMinute) {
			this.columnIndexDate = columnIndexDate;
			this.columnIndexHourWrap = columnIndexHourWrap;
			this.columnIndexMinute = columnIndexMinute;
		}
		public long get(String[] row) {			
			try {
				int hour = Integer.parseInt(row[columnIndexHourWrap]);
				boolean wrap = false;
				if(hour==24) {
					wrap = true;
					hour = 0;
				}
				LocalDate date = LocalDate.parse(row[columnIndexDate], DateTimeFormatter.ISO_DATE);
				if(wrap) {
					date = date.plusDays(1);
				}
				LocalTime time = LocalTime.of(hour,Integer.parseInt(row[columnIndexMinute]));
				return TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(date,time));		
			} catch(NumberFormatException e) {
				log.warn(row[columnIndexDate]+"  not parsed");
				return -1;
			}
		}
	}

	/**
	 * header names in csv file
	 */
	public String[] names;

	/**
	 * header name -> column position
	 */
	public Map<String, Integer> nameMap;

	/**
	 * table rows of csv file
	 */
	public String[][] rows;

	private Table() {}
	
	public static Table readCSV(Path filename, char separator) {
		return readCSV(filename.toString(),separator);
	}

	/**
	 * create a Table Object from CSV-File
	 * @param filename
	 * @return
	 */
	public static Table readCSV(String filename, char separator) {
		try {
			Table table = new Table();

			//CSVReader reader = new CSVReader(new FileReader(filename),separator);
			InputStreamReader in = new InputStreamReader(new FileInputStream(filename),UTF8);
			CSVReader reader = new CSVReader(in,separator);
			
			List<String[]> list = reader.readAll();

			table.names = list.get(0);
			
			if(table.names.length>0) { // filter UTF8 BOM
				if(table.names[0].startsWith(UTF8_BOM)) {
					table.names[0] = table.names[0].substring(1, table.names[0].length());
				}
			}
			
			//log.info("names: "+Arrays.toString(table.names)+"   in "+filename);

			table.nameMap = new HashMap<String, Integer>();

			for(int i=0;i<table.names.length;i++) {
				if(table.nameMap.containsKey(table.names[i])) {
					log.error("dublicate name: "+table.names[i]);
				} else {
					table.nameMap.put(table.names[i], i);
				}
			}

			table.rows = new String[list.size()-1][];

			for(int i=1;i<list.size();i++) {
				table.rows[i-1] = list.get(i);
			}

			for(int i=0;i<table.rows.length;i++) {

			}
			reader.close();
			return table;
		} catch(Exception e) {
			log.error(e);
			return null;
		}
	}
	
	public static Table readCSVFirstDataRow(String filename, char separator) {
		try {
			Table table = new Table();

			CSVReader reader = new CSVReader(new FileReader(filename),separator);
			
			String[] headerRow = reader.readNext();
			String[] dataRow = reader.readNext();
			
			reader.close();

			table.names = headerRow;

			table.nameMap = new HashMap<String, Integer>();

			for(int i=0;i<table.names.length;i++) {
				if(table.nameMap.containsKey(table.names[i])) {
					log.error("dublicate name: "+table.names[i]);
				} else {
					table.nameMap.put(table.names[i], i);
				}
			}

			table.rows = new String[1][];
			table.rows[0] = dataRow;			
			
			return table;
		} catch(Exception e) {
			log.error(e);
			return null;
		}
	}

	/**
	 * get column position of one header name
	 * @param name
	 * @return if name not found -1
	 */
	public int getColumnIndex(String name) {
		Integer index = nameMap.get(name);
		if(index==null) {
			log.error("name not found in table: "+name);
			return -1;
		}
		return index;
	}

	public ColumnReaderString createColumnReader(String name) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return new ColumnReaderString(columnIndex);
	}

	public ColumnReaderFloat createColumnReaderFloat(String name) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return new ColumnReaderFloat(columnIndex);
	}
	
	public ColumnReaderDouble createColumnReaderDouble(String name) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return new ColumnReaderDouble(columnIndex);
	}
	
	public ColumnReaderInt createColumnReaderInt(String name) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return new ColumnReaderInt(columnIndex);
	}
	
	public ColumnReaderIntFunc createColumnReaderInt(String name, IntegerParser parser) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return new ColumnReaderIntFunc(columnIndex, parser);
	}

	public ColumnReaderTimestampTwoCols createColumnReaderTimestamp(String colDate, String colTime) {
		int columnIndexDate = getColumnIndex(colDate);
		if(columnIndexDate<0) {
			return null;
		}
		int columnIndexTime = getColumnIndex(colTime);
		if(columnIndexTime<0) {
			return null;
		}

		return new ColumnReaderTimestampTwoCols(columnIndexDate, columnIndexTime);	
	}
	
	public ColumnReaderSlashTimestamp createColumnReaderSlashTimestamp(String name) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return new ColumnReaderSlashTimestamp(columnIndex);
	}
	
	public ColumnReaderMonthNameTimestamp createColumnReaderMonthNameTimestamp(String name) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return new ColumnReaderMonthNameTimestamp(columnIndex);
	}
	
	//parseTimestampDateFullHourFormat(String dateText, int fullHour)
	public ColumnReaderDateFullHourTimestamp createColumnReaderDateFullHourTimestamp(String colDate, String colFullHour) {
		int columnIndexDate = getColumnIndex(colDate);		
		if(columnIndexDate<0) {
			return null;
		}
		int columnIndexFullHour = getColumnIndex(colFullHour);
		if(columnIndexFullHour<0) {
			return null;
		}
		return new ColumnReaderDateFullHourTimestamp(columnIndexDate,columnIndexFullHour);
	}

	public ColumnReaderDateHourWrapMinuteTimestamp createColumnReaderDateHourWrapMinuteTimestamp(String colDate, String colHourWrap, String colMinute) {
		int columnIndexDate = getColumnIndex(colDate);		
		if(columnIndexDate<0) {
			return null;
		}
		int columnIndexHourWrap = getColumnIndex(colHourWrap);
		if(columnIndexHourWrap<0) {
			return null;
		}
		int columnIndexMinute = getColumnIndex(colMinute);
		if(columnIndexMinute<0) {
			return null;
		}
		return new ColumnReaderDateHourWrapMinuteTimestamp(columnIndexDate,columnIndexHourWrap,columnIndexMinute);
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
