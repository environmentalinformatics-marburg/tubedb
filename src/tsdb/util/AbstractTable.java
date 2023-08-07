package tsdb.util;

import static tsdb.util.AssumptionCheck.throwFalse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.JavaDoubleParser;
import ch.randelshofer.fastdoubleparser.JavaFloatParser;

public abstract class AbstractTable {

	public static class ColumnReader {
		public static final int MISSING_COLUMN = Integer.MAX_VALUE;
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
		public ColumnReaderString then(UnaryOperator<String> func) {
			ColumnReaderString outher = this;
			return new ColumnReaderString(rowIndex) {				
				@Override
				public String get(String[] row) {
					return func.apply(outher.get(row));
				}				
			};
		}
	}
	public static class ColumnReaderStringMissing extends ColumnReaderString {
		private String missing;
		public ColumnReaderStringMissing(String missing) {
			super(MISSING_COLUMN);
			this.missing = missing;
		}
		@Override
		public String get(String[] row) {
			return missing;
		}		
	}
	public static class ColumnReaderFloat extends ColumnReader {
		public ColumnReaderFloat(int rowIndex) {
			super(rowIndex);
		}
		public float get(String[] row, boolean warnIfEmpty) {			
			try {
				String textValue = row[rowIndex];
				if(textValue.isEmpty()) {
					if(warnIfEmpty) {
						Logger.warn("empty");
					}
					return Float.NaN;
				}
				//return Float.parseFloat(row[rowIndex]);
				return (float) JavaFloatParser.parseFloat(row[rowIndex]);
			} catch(NumberFormatException e) {
				if(row[rowIndex].toLowerCase().equals("na")||row[rowIndex].toLowerCase().equals("null")||row[rowIndex].toLowerCase().equals("nan")) {
					return Float.NaN;
				} else {
					Logger.warn(row[rowIndex]+" not parsed");
					e.printStackTrace();
					return Float.NaN;
				}
			}
		}
		public ColumnReaderFloat then(UnaryOperator<Float> func) {
			ColumnReaderFloat outher = this;
			return new ColumnReaderFloat(rowIndex) {				
				@Override
				public float get(String[] row, boolean warnIfEmpty) {	
					return func.apply(outher.get(row, warnIfEmpty));
				}				
			};
		}
	}
	public static class ColumnReaderFloatMissing extends ColumnReaderFloat {
		private float missing;
		public ColumnReaderFloatMissing(float missing) {
			super(MISSING_COLUMN);
			this.missing = missing;
		}
		@Override
		public float get(String[] row, boolean warnIfEmpty) {
			return missing;
		}		
	}
	public static class ColumnReaderDoubleMissing extends ColumnReaderDouble {
		private double missing;
		public ColumnReaderDoubleMissing(double missing) {
			super(MISSING_COLUMN);
			this.missing = missing;
		}
		@Override
		public double get(String[] row, boolean warnIfEmpty) {
			return missing;
		}		
	}
	public static class ColumnReaderDouble extends ColumnReader {
		public ColumnReaderDouble(int rowIndex) {
			super(rowIndex);
		}
		public double get(String[] row, boolean warnIfEmpty) {			
			try {
				String textValue = row[rowIndex];
				if(textValue.isEmpty()) {
					if(warnIfEmpty) {
						Logger.warn("empty");
					}
					return Double.NaN;
				}
				//return Double.parseDouble(row[rowIndex]);
				return JavaDoubleParser.parseDouble(row[rowIndex]);
			} catch(NumberFormatException e) {
				if(row[rowIndex].toLowerCase().equals("na")||row[rowIndex].toLowerCase().equals("null")||row[rowIndex].toLowerCase().equals("nan")) {
					return Double.NaN;
				} else {
					Logger.warn(row[rowIndex]+" not parsed");
					e.printStackTrace();
					return Double.NaN;
				}
			}
		}
		public ColumnReaderDouble then(UnaryOperator<Double> func) {
			ColumnReaderDouble outher = this;
			return new ColumnReaderDouble(rowIndex) {				
				@Override
				public double get(String[] row, boolean warnIfEmpty) {	
					return func.apply(outher.get(row, warnIfEmpty));
				}				
			};
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
	public static abstract class ColumnReaderBoolean extends ColumnReader {
		public ColumnReaderBoolean(int rowIndex) {
			super(rowIndex);
		}
		public abstract boolean get(String[] row);
	}
	public static class ColumnReaderBooleanMissing extends ColumnReaderBoolean {
		private final boolean missing;
		public ColumnReaderBooleanMissing(boolean missing) {
			super(MISSING_COLUMN);
			this.missing = missing;
		}
		@Override
		public boolean get(String[] row) {
			return missing;
		}		
	}
	public static class ColumnReaderBooleanYN extends ColumnReaderBoolean {
		private final boolean missing;
		public ColumnReaderBooleanYN(int rowIndex, boolean missing) {
			super(rowIndex);
			this.missing = missing;
		}
		@Override
		public boolean get(String[] row) {
			String text = row[rowIndex];
			if(text.isEmpty()) {
				return missing;
			}
			if(text.length()!=1) {
				text = text.trim();
				if(text.length()!=1) {
					Logger.warn("boolean not parsed "+text);
					return missing;
				}
			}
			char c = text.toUpperCase().charAt(0);
			if(c=='Y') {
				return true;
			}
			if(c=='N') {
				return false;
			}
			Logger.warn("boolean not parsed "+text);
			return missing;
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
				Logger.warn(row[rowIndexDate]+"  "+row[rowIndexTime]+"not parsed");
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
				Logger.warn(row[rowIndexDateTime]+"  not parsed");
				return -1;
			}
		}
	}
	public static class ColumnReaderSpaceTimestamp implements ColumnReaderTimestamp {
		private final int rowIndexDateTime;
		public ColumnReaderSpaceTimestamp(int rowIndexDateTime) {
			this.rowIndexDateTime = rowIndexDateTime;
		}
		public long get(String[] row) {			
			try {
				return TimeUtil.parseTimestampSpaceFormat(row[rowIndexDateTime]);				
			} catch(NumberFormatException e) {
				Logger.warn(row[rowIndexDateTime]+"  not parsed");
				return -1;
			}
		}
	}
	public static class ColumnReaderDayFirstAmPmTimestamp implements ColumnReaderTimestamp {
		private final int rowIndexDateTime;
		public ColumnReaderDayFirstAmPmTimestamp(int rowIndexDateTime) {
			this.rowIndexDateTime = rowIndexDateTime;
		}
		public long get(String[] row) {			
			try {
				String text = row[rowIndexDateTime];
				if(text.endsWith("M")) {
					return TimeUtil.parseTimestampMonthFirstAmPmFormat(text);
				} else {
					return TimeUtil.parseTimestampMonthFirstFormat(text);	
				}
			} catch(NumberFormatException e) {
				Logger.warn(row[rowIndexDateTime]+"  not parsed");
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
				Logger.warn(row[rowIndexDateTime]+"  not parsed");
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
				Logger.warn(row[columnIndexDate]+"  not parsed");
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
				Logger.warn(row[columnIndexDate]+"  not parsed");
				return -1;
			}
		}
	}
	public static interface ReaderConstructor<T> {
		T create(int a);
	}
	/**
	 * header names in csv file
	 */
	public String[] names;
	/**
	 * header name -> column position
	 */
	public Map<String, Integer> nameMap;
	public void updateNames(String[] columnNames) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
	
		StringBuilder dublicatesReplaced = null;
		for(int i=0;i<columnNames.length;i++) {
			if(map.containsKey(columnNames[i])) {
				int nameNumber = 2;
				String name2 = columnNames[i] + nameNumber;
				while(map.containsKey(name2)) {
					nameNumber++;
					name2 = columnNames[i] + nameNumber;
				}
				if(dublicatesReplaced == null) {
					dublicatesReplaced = new StringBuilder();
				}
				dublicatesReplaced.append(" '");
				dublicatesReplaced.append(columnNames[i]);
				dublicatesReplaced.append("'->'");
				dublicatesReplaced.append(name2);
				dublicatesReplaced.append("'");
				//Logger.warn("dublicate name: '"+columnNames[i] + "' replaced with '" + name2 + "'");
				columnNames[i] = name2;
				map.put(columnNames[i], i);
			} else {
				map.put(columnNames[i], i);
			}
		}
		if(dublicatesReplaced != null) {
			Logger.warn("dublicatesReplaced:" + dublicatesReplaced.toString());
		}
	
		this.names = columnNames;
		this.nameMap = map;
	}
	/**
	 * get column position of one header name
	 * @param name
	 * @return if name not found -1
	 */
	public int getColumnIndex(String name) {
		return getColumnIndex(name, true);
	}
	/**
	 * get column position of one header name
	 * @param name
	 * @return if name not found -1
	 */
	public int getColumnIndex(String name, boolean warn) {
		Integer index = nameMap.get(name);
		if(index==null) {			
			if(warn) {
				Logger.error("name not found in table: "+name);
			}
			return -1;
		}
		return index;
	}
	public boolean containsColumn(String name) {
		return nameMap.containsKey(name);
	}
	public String getName(ColumnReader cr) {
		return names[cr.rowIndex];
	}
	public ColumnReaderString createColumnReader(String name) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return new ColumnReaderString(columnIndex);
	}
	public ColumnReaderString createColumnReader(String name, String missing) {
		int columnIndex = getColumnIndex(name, false);
		if(columnIndex<0) {
			return new ColumnReaderStringMissing(missing);
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
	/**
	 * Creates reader of column or producer of value "missing" if columns does not exist.
	 * @param name
	 * @param missing
	 * @return
	 */
	public ColumnReaderFloat createColumnReaderFloat(String name, float missing) {
		int columnIndex = getColumnIndex(name, false);
		if(columnIndex<0) {
			return new ColumnReaderFloatMissing(missing);
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
	public ColumnReaderDouble createColumnReaderDouble(String name, double missing) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return new ColumnReaderDoubleMissing(missing);
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
	public ColumnReaderIntFunc createColumnReaderInt(String name, tsdb.util.Table.ColumnReaderIntFunc.IntegerParser parser) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return new ColumnReaderIntFunc(columnIndex, parser);
	}
	public ColumnReaderBoolean createColumnReaderBooleanYN(String name, boolean missing) {
		int columnIndex = getColumnIndex(name, false);
		if(columnIndex<0) {
			return new ColumnReaderBooleanMissing(missing);
		}
		return new ColumnReaderBooleanYN(columnIndex, missing);
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
	public <T> T getColumnReader(String name, ReaderConstructor<T> readerConstructor) {
		int columnIndex = getColumnIndex(name);
		if(columnIndex<0) {
			return null;
		}
		return readerConstructor.create(columnIndex);
	}
}
