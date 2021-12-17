package tsdb.testing;


import org.tinylog.Logger;

import tsdb.util.Timer;
import tsdb.util.Util;

public class TestingRegex {
	

	public static void main(String[] args) {
		
		//String s = "     								  [       -234.222  		   ,      			 +3422225543.4554332322   		  	 ]					 ";
		String s = "[0,1]";
		
		int REPEATS = 10;
		int LOOPS = 10_000_000;         

		for (int repeat = 0; repeat < REPEATS; repeat++) {
			{
				Timer.start("prog");
				for (int loop = 0; loop < LOOPS; loop++) {
					Util.FloatRange.parse_no_regex("name", s);
				}
				Logger.info(Timer.stop("prog"));				
			}
			{
				Timer.start("regex");
				for (int loop = 0; loop < LOOPS; loop++) {
					Util.FloatRange.parse("name", s);
				}
				Logger.info(Timer.stop("regex"));				
			}

		}


	}

}
