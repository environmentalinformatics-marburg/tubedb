package tsdb.testing;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TimeUtil;
import tsdb.util.Timer;

public class DateTimeWritePerformance {
	private static final Logger log = LogManager.getLogger();

	static int ROUNDS = 1000;
	static int LOOPS = 10_000_000;

	static ByteArrayOutputStream out = new ByteArrayOutputStream();
	static OutputStreamWriter writer = new OutputStreamWriter(out);
	static BufferedWriter bufferedWriter = new BufferedWriter(writer);

	static LocalDateTime datetime = LocalDateTime.now();

	public static void main(String[] args) throws IOException {		
		for(int r=0;r<ROUNDS;r++) {
			run_toString();
			run_toString2();
			run_toString_time();
			run_date_time();
			run_datetime();
		}
	}


	private static void run_toString() throws IOException {
		out.flush();
		writer.flush();
		bufferedWriter.flush();
		out.reset();
		Timer.start("toString");
		for(int i=0;i<LOOPS;i++) {
			bufferedWriter.write(datetime.toString());
		}
		out.flush();
		writer.flush();
		bufferedWriter.flush();		
		log.info(Timer.stop("toString")+"  "+out.size());

	}

	private static void run_toString2() throws IOException {
		out.flush();
		writer.flush();
		bufferedWriter.flush();
		out.reset();
		Timer.start("toString2");
		for(int i=0;i<LOOPS;i++) {
			bufferedWriter.write(datetime.toLocalDate().toString());
			bufferedWriter.write('T');
			bufferedWriter.write(datetime.toLocalTime().toString());
		}
		out.flush();
		writer.flush();
		bufferedWriter.flush();		
		log.info(Timer.stop("toString2")+"  "+out.size());		
	}

	private static void run_toString_time() throws IOException {
		out.flush();
		writer.flush();
		bufferedWriter.flush();
		out.reset();
		Timer.start("toString_time");
		for(int i=0;i<LOOPS;i++) {
			bufferedWriter.write(datetime.toLocalDate().toString());
			bufferedWriter.write('T');
			bufferedWriter.write(TimeUtil.fastTimeWrite(datetime.toLocalTime()));
		}
		out.flush();
		writer.flush();
		bufferedWriter.flush();		
		log.info(Timer.stop("toString_time")+"  "+out.size());		
	}
	
	private static void run_date_time() throws IOException {
		out.flush();
		writer.flush();
		bufferedWriter.flush();
		out.reset();
		Timer.start("date_time");
		for(int i=0;i<LOOPS;i++) {
			bufferedWriter.write(TimeUtil.fastDateWrite(datetime.toLocalDate()));
			bufferedWriter.write('T');
			bufferedWriter.write(TimeUtil.fastTimeWrite(datetime.toLocalTime()));
			//System.out.println(new String(out.toByteArray()));
		}
		out.flush();
		writer.flush();
		bufferedWriter.flush();		
		log.info(Timer.stop("date_time")+"  "+out.size());		
	}
	
	private static void run_datetime() throws IOException {
		out.flush();
		writer.flush();
		bufferedWriter.flush();
		out.reset();
		Timer.start("datetime");
		for(int i=0;i<LOOPS;i++) {
			bufferedWriter.write(TimeUtil.fastDateTimeWrite(datetime));
			//System.out.println(new String(out.toByteArray()));
		}
		out.flush();
		writer.flush();
		bufferedWriter.flush();		
		log.info(Timer.stop("datetime")+"  "+out.size());		
	}

}
