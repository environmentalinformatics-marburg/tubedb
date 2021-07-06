package tsdb.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestingLogging {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		System.out.println("start");
		log.info("start");
		log.info("end");
		System.out.println("end");
		LogManager.shutdown();
	}

}
