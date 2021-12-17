package tsdb.testing;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Formatter;
import java.util.Locale;


import org.tinylog.Logger;

import tsdb.util.Timer;
import tsdb.util.Util;

public class FloatFormatPerformance {
	

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
		Logger.info("toString "+Float.toString(testValue));
		Logger.info("format "+String.format(Locale.ENGLISH, "%.2f", testValue));
		Logger.info("decimalFormat "+decimalFormat.format(testValue));
		Logger.info("fastWriteFloat "+new String(Util.fastWriteFloat(testValue)));



		for(int r=0;r<ROUNDS;r++) {
			/*Timer.start("toString");
			for(int i=0;i<LOOPS;i++) {
				s.setLength(0);
				float v = i/100f;
				s.append(Float.toString(v));
				cnt += s.length();
			}
			Logger.info(Timer.stop("toString")+"  "+cnt);*/

			/*Timer.start("append");
			for(int i=0;i<LOOPS;i++) {
				s.setLength(0);
				float v = i/100f;
				s.append(v);
				cnt += s.length();
			}
			Logger.info(Timer.stop("append")+"  "+cnt);*/

			run_formatter();

			/*Timer.start("format");
			for(int i=0;i<LOOPS;i++) {
				s.setLength(0);
				float v = i/100f;
				s.append(String.format(Locale.ENGLISH, "%.2f", v));
				cnt += s.length();
			}
			Logger.info(Timer.stop("format")+"  "+cnt);*/

			run_decimalFormat();
			
			run_fastWriteFloat();
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
		Logger.info(Timer.stop("formatter")+"  "+cnt);

	}

	private static void run_decimalFormat() {
		Timer.start("decimalFormat");
		for(int i=0;i<LOOPS;i++) {
			s.setLength(0);
			float v = i/100f;
			s.append(decimalFormat.format(v));
			cnt += s.length();
		}
		Logger.info(Timer.stop("decimalFormat")+"  "+cnt);

	}
	
	private static void run_fastWriteFloat() {
		Timer.start("fastWriteFloat");
		for(int i=0;i<LOOPS;i++) {
			s.setLength(0);
			float v = i/100f;
			s.append(Util.fastWriteFloat(v));
			cnt += s.length();
		}
		Logger.info(Timer.stop("fastWriteFloat")+"  "+cnt);

	}

}
