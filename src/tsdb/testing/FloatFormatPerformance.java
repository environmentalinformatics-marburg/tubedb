package tsdb.testing;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Formatter;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.Timer;

public class FloatFormatPerformance {
	private static final Logger log = LogManager.getLogger();
	
	static int ROUNDS = 1000;
	static int LOOPS = 1_000_000;
	
	static StringBuilder s = new StringBuilder();
	static Formatter formatter = new Formatter(s,Locale.ENGLISH);
	static DecimalFormat decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ENGLISH));
	
	static long cnt=0;

	public static void main(String[] args) {
		

		
		float testValue = 1234.56789f;
		//float testValue = 1234f;
		//float testValue = 0f;
		//float testValue = 0.3f;
		log.info("toString "+Float.toString(testValue));
		log.info("format "+String.format(Locale.ENGLISH, "%.2f", testValue));
		log.info("decimalFormat "+decimalFormat.format(testValue));



		for(int r=0;r<ROUNDS;r++) {
			/*Timer.start("toString");
			for(int i=0;i<LOOPS;i++) {
				s.setLength(0);
				float v = i/100f;
				s.append(Float.toString(v));
				cnt += s.length();
			}
			log.info(Timer.stop("toString")+"  "+cnt);*/
			
			/*Timer.start("append");
			for(int i=0;i<LOOPS;i++) {
				s.setLength(0);
				float v = i/100f;
				s.append(v);
				cnt += s.length();
			}
			log.info(Timer.stop("append")+"  "+cnt);*/
			
			run_formatter();
			
			/*Timer.start("format");
			for(int i=0;i<LOOPS;i++) {
				s.setLength(0);
				float v = i/100f;
				s.append(String.format(Locale.ENGLISH, "%.2f", v));
				cnt += s.length();
			}
			log.info(Timer.stop("format")+"  "+cnt);*/
			
			run_decimalFormat();
		}



	}
	
	
	private static void run_formatter() {
		Timer.start("formatter");
		for(int i=0;i<LOOPS;i++) {
			s.setLength(0);
			float v = i/100f;
			formatter.format(Locale.ENGLISH, "%.2f", v);
			cnt += s.length();
		}
		log.info(Timer.stop("formatter")+"  "+cnt);
		
	}
	
	private static void run_decimalFormat() {
		Timer.start("decimalFormat");
		for(int i=0;i<LOOPS;i++) {
			s.setLength(0);
			float v = i/100f;
			s.append(decimalFormat.format(v));
			cnt += s.length();
		}
		log.info(Timer.stop("decimalFormat")+"  "+cnt);
		
	}

}
