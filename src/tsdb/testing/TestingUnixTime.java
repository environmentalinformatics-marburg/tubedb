package tsdb.testing;


import org.tinylog.Logger;

import tsdb.util.TimeUtil;


public class TestingUnixTime {
		
	
	public static void main(String[] args) {
		Logger.info(TimeUtil.unixTimeToLocalDateTime(1372339860));
	}
}
