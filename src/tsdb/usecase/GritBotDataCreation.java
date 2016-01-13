package tsdb.usecase;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.LocalDateTime;

import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

@Deprecated
public class GritBotDataCreation {

	public static void main(String[] args) throws FileNotFoundException {
		/*System.out.println("start...");
		TsDB timeSeriesDatabase = TsDBFactory.createDefault();
		QueryProcessor qp = new QueryProcessor(timeSeriesDatabase);

		String plotID = "HEW42";
		String[] querySchema = new String[]{"Ta_200","Ta_10"};
		Long queryStart = null;
		Long queryEnd = null;
		DataQuality dataQuality = DataQuality.STEP;		
		
		TsIterator result_iterator = qp.query_raw_with_bad_quality_removed(plotID, querySchema, queryStart, queryEnd, dataQuality);
		//CSV.writeNoHeader(result_iterator, "c:/vm_share/gritbot.data", ",", "?", CSVTimeType.TIMESTAMP);
		writeGritbotName("c:/vm_share/gritbot.names",querySchema);		
		writeGritbotData("c:/vm_share/gritbot.data",result_iterator);

		System.out.println("...end");*/

	}
	
	public static void writeGritbotData(String filename, TsIterator it) throws FileNotFoundException {
		PrintStream printStream = new PrintStream(filename);

		while(it.hasNext()) {
			TsEntry e = it.next();
			
			LocalDateTime datetime = TimeUtil.oleMinutesToLocalDateTime(e.timestamp);
			
			printStream.print(datetime.getYear());
			printStream.print("-");
			int month = datetime.getMonthValue();
			if(month<10) {
				printStream.print("0");
			}
			printStream.print(month);
			printStream.print("-");
			int day = datetime.getDayOfMonth();
			if(day<10) {
				printStream.print("0");
			}
			printStream.print(day);
			printStream.print(" ");
			int hour = datetime.getHour();
			if(hour<10) {
				printStream.print("0");
			}
			printStream.print(hour);
			printStream.print(":");
			int minute = datetime.getMinute();
			if(minute<10) {
				printStream.print("0");
			}
			printStream.print(minute);
			printStream.print(":");
			int second = datetime.getSecond();
			if(second<10) {
				printStream.print("0");
			}
			printStream.print(second);
			
			for(float v:e.data) {
				printStream.print(",");
				if(Float.isNaN(v)) {
					printStream.print("?");
				} else {
					printStream.print(v);
				}
				
			}
			
			printStream.println();
			
			
		}
		
		printStream.close();
		
	}

	public static void writeGritbotName(String filename, String[] schema) throws FileNotFoundException {
		PrintStream printStream = new PrintStream(filename);
		
		printStream.println("start.");
		
		printStream.println("timestamp: timestamp.");
		//printStream.println("datetime: label.");
		
		for(String name:schema) {
			printStream.println(name+": "+"continuous.");
		}		
		
		printStream.close();
	}

}
