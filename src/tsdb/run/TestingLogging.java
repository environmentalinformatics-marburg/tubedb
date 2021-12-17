package tsdb.run;


import org.tinylog.Logger;

public class TestingLogging {
	

	public static void main(String[] args) {
		System.out.println("start");
		Logger.info("start");
		Logger.info("end");
		System.out.println("end");
	}

}
