package tsdb.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TimeUtil;


public class TestingUnixTime {
	private static final Logger log = LogManager.getLogger();	
	
	public static void main(String[] args) {
		log.info(TimeUtil.unixTimeToLocalDateTime(1372339860));
	}
}
