package tsdb.run;

import java.util.Map;
import java.util.TreeMap;

public class Terminal {

	static Map<String,Runnable> commandMap = new TreeMap<>();

	@FunctionalInterface
	private interface MainRunnable extends Runnable {
		void main(String[] args) throws Exception;
		@Override
		default void run() {
			try {
				main(null);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void addCommand(String name, MainRunnable command) {
		addCommandRunnable(name,command);
	}

	private static void addCommandRunnable(String name, Runnable command) {
		commandMap.put(name, command);
	}

	static {
		addCommand("full_read",FullDataReader::main);
		addCommand("influxdb_import",InfluxDBDataWriter::main);
		addCommand("influxdb_full_read",InfluxDBDataReader::main);
		addCommand("clear_import",ClearImportSources::main);
	}

	public static void main(String[] args) {
		if(args!=null) {
			switch(args.length) {
			case 0:
				System.out.println("commands:\n");
				for(String key:commandMap.keySet()) {
					System.out.println(key);
				}
				System.out.println();
				break;
			case 1:
				run(args[0]);
				break;
			default:
				System.out.println("parameter error");
			}
		} else {
			System.out.println("parameter error");
		}
	}

	private static void run(String name) {
		Runnable command = commandMap.get(name);
		if(command!=null) {
			command.run();
		} else {
			System.out.println("unknown command: "+name);
		}
	}
}
