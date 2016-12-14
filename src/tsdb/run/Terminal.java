package tsdb.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.run.command.DataClear;
import tsdb.run.command.DataImport;

public class Terminal {
	private static final Logger log = LogManager.getLogger();

	public static enum CommandType {
		NORMAL,
		INTERNAL
	};

	public static interface Command {
		String getName();
		boolean run(String[] parameters);
		CommandType getType();
		default boolean isInternal() {
			return getType()==CommandType.INTERNAL;
		}
		default String getShortDescription() {
			return "";
		}
		default String getDetailedDescription() {
			return getShortDescription();
		}
		default String getShortHelp() {
			return getName()+" : "+getShortDescription();
		}
		default String getDetailedHelp() {
			return getName()+"\n----\n"+getDetailedDescription();
		}
	}

	public static abstract class AbstractCommand implements Command {
		private final String name;
		private final String shortDescription;
		private final CommandType commandType;
		public AbstractCommand(String name, CommandType commandType, String shortDescription) {
			this.name = name;
			this.shortDescription = shortDescription;
			this.commandType = commandType;			
		}
		@Override
		public String getName() {
			return name;
		}
		@Override
		public CommandType getType() {
			return commandType;
		}
		@Override
		public String getShortDescription() {
			return shortDescription;
		}

	}

	public static class CommandMain extends AbstractCommand {		
		private final MainRunnable mainRunnable;

		public CommandMain(String name, CommandType commandType, String description, MainRunnable mainRunnable) {
			super(name, commandType, description);
			this.mainRunnable = mainRunnable;
		}		

		@Override
		public boolean run(String[] parameters) {			
			try {
				mainRunnable.main(parameters);
			} catch(Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}

	public static class CommandSequence extends AbstractCommand {
		private final Command[] commands;
		public CommandSequence(String name, CommandType commandType, String description, Command... commands) {
			super(name, commandType, description);
			this.commands = commands;
		}
		@Override
		public boolean run(String[] parameters) {
			for(Command command:commands) {
				if(!command.run(EMPTY)) {
					return false;
				}
			}
			return true;
		}		
	}


	static Map<String,Command> commandMap = new TreeMap<>();

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

	private static void addCommand(String name, String description, MainRunnable mainRunnable) {
		Command command = new CommandMain(name, CommandType.NORMAL, description, mainRunnable);
		addCommand(command);
	}

	private static void addCommandSequence(String name, String description, String... commandNames) {
		Command[] commands = new Command[commandNames.length];
		for(int i=0;i<commands.length;i++) {
			commands[i] = commandMap.get(commandNames[i]);
			if(commands[i] == null) {
				log.error("command not found: "+commandNames[i]);
				return;
			}
		}
		Command command = new CommandSequence(name, CommandType.NORMAL, description, commands);
		addCommand(command);
	}

	private static void addInternalCommand(String name, String description, MainRunnable mainRunnable) {
		Command command = new CommandMain(name, CommandType.INTERNAL, description, mainRunnable);
		addCommand(command);
	}

	private static void addCommand(Command command) {
		commandMap.put(command.getName(), command);
	}

	static {

		//internal commands

		addInternalCommand("streamdb_import","experiment",StreamDBDataWriter::main);
		addInternalCommand("influxdb_import","experiment",InfluxDBDataWriter::main);
		addInternalCommand("h2_import","experiment",H2DataWriter::main);
		addInternalCommand("druid_csv_write","experiment",DruidCsvWriter::main);

		addInternalCommand("full_read","experiment",FullDataReader::main);		
		addInternalCommand("influxdb_full_read","experiment",InfluxDBDataReader::main);
		addInternalCommand("h2_full_read","experiment",H2DataReader::main);

		addInternalCommand("streamdb_mean_read","experiment",StreamDBMeanReader::main);
		addInternalCommand("influxdb_mean_read","experiment",InfluxDBMeanReader::main);
		addInternalCommand("h2_mean_read","experiment",H2MeanReader::main);

		addInternalCommand("experiment_processing","experiment",ExperimentProcessing::main);

		addInternalCommand("interactive", "prompt for one command", Terminal::command_interactive);

		//helper commands

		addCommand("commands", "list commands", Terminal::command_commands);
		addCommand("internals", "list internal commands", Terminal::command_internals);
		addCommand("help", "print details of command", Terminal::command_help);

		//server commands

		addCommand("server", "start web server", tsdb.web.Main::main);
		addInternalCommand("server_rmi", "start web and rmi server", Terminal::command_interactive);
		
		addCommand("explorer", "run TubeDB desktop application", tsdb.explorer.Explorer::main);

		//administrative commands

		addCommand("clear", "remove all time series data in TubeDB", DataClear::main);
		addCommand("load", "read all data source into TubeDB", DataImport::main);
		addCommand("masks", "update masks", ClearLoadMasks::main);
		addCommand("references", "refresh time series references", CreateStationGroupAverageCache::main);
		addCommand("compact", "defragment free space", RunCompact::main);

		addCommandSequence("import", "composite of: load - masks - references - compact", "load", "masks", "references", "compact");

		addCommandSequence("clear_import", "composite of: clear - import", "clear", "import");

	}

	public static void main(String[] args) {
		if(args.length==0) {
			args = new String[]{"commands"};
		}
		String name = args[0];
		Command command = commandMap.get(name);
		if(command!=null) {
			String[] parameters = removeFirst(args);
			command.run(parameters);
		} else {
			System.out.println("unknown command: "+name);
		}
	}

	public static void command_commands(String[] parameters) {
		System.out.println("TubeDB commands:\n");
		for(Command command:commandMap.values()) {
			if(!command.isInternal()) {
				System.out.println(command.getShortHelp());
			}
		}
		System.out.println();
	}

	public static void command_internals(String[] parameters) {
		System.out.println("TubeDB internal commands:\n");
		for(Command command:commandMap.values()) {
			if(command.isInternal()) {
				System.out.println(command.getShortHelp());
			}
		}
		System.out.println();
	}

	private static final String[] EMPTY = new String[]{};
	
	private static String[] removeFirst(String[] array) {
		if(array==null || array.length<=1) {
			return EMPTY;
		}
		String[] r = new String[array.length-1];
		for(int i=0;i<r.length;i++) {
			r[i] = array[i+1];
		}
		return r;
	}

	public static void command_interactive(String[] parameters) {
		try {
			command_internals(EMPTY);
			command_commands(EMPTY);
			System.out.println("type command:");
			String line = new BufferedReader(new InputStreamReader(System.in)).readLine();
			if(line.length()>0) {
				main(line.split(" "));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static void command_help(String[] parameters) {
		if(parameters.length>0) {
		String name = parameters[0];
		Command command = commandMap.get(name);
		if(command!=null) {
			System.out.println(command.getDetailedHelp());
		} else {
			System.out.println("unknown command: "+name);
		}
		} else {
			System.out.println("parameter needs to be a command: e.g. 'help server'");
		}
	}
}
