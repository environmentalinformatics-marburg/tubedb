package tsdb.run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;


import org.tinylog.Logger;
import org.yaml.snakeyaml.Yaml;

import tsdb.experiment.Experiment;
import tsdb.remote.StartServerTsDB;
import tsdb.run.command.ClearMasks;
import tsdb.run.command.Command;
import tsdb.run.command.CommandMain;
import tsdb.run.command.CommandSequence;
import tsdb.run.command.CommandType;
import tsdb.run.command.Command_export_csv;
import tsdb.run.command.CountTsDBValues;
import tsdb.run.command.DataClear;
import tsdb.run.command.DataImport;
import tsdb.run.command.Command_export_tsa;
import tsdb.run.command.MainRunnable;
import tsdb.run.command.LoadMasks;
import tsdb.util.yaml.YamlMap;

public class Terminal {
	

	private static final String COMMAND_DESCRIPTIONS_FILENAME = "/command_descriptions.yaml";
	private static YamlMap commandDetailDescriptionMap = YamlMap.EMPTY_MAP;

	static Map<String,Command> commandMap = new TreeMap<>();

	private static void addCommand(String name, String shortDescription, MainRunnable mainRunnable) {
		String detailedDescription = getDetailDescription(name, shortDescription);
		Command command = new CommandMain(name, CommandType.NORMAL, shortDescription, detailedDescription, mainRunnable);
		addCommand(command);
	}

	private static void addCommandSequence(String name, String shortDescription, String... commandNames) {
		String detailedDescription = getDetailDescription(name, shortDescription);
		Command[] commands = new Command[commandNames.length];
		for(int i=0;i<commands.length;i++) {
			commands[i] = commandMap.get(commandNames[i]);
			if(commands[i] == null) {
				Logger.error("command not found: "+commandNames[i]);
				return;
			}
		}
		Command command = new CommandSequence(name, CommandType.NORMAL, shortDescription, detailedDescription, commands);
		addCommand(command);
	}

	private static void addInternalCommand(String name, String shortDescription, MainRunnable mainRunnable) {
		String detailedDescription = getDetailDescription(name, shortDescription);
		Command command = new CommandMain(name, CommandType.INTERNAL, shortDescription, detailedDescription, mainRunnable);
		addCommand(command);
	}

	private static void addCommand(Command command) {
		commandMap.put(command.getName(), command);
	}

	private static String getDetailDescription(String commandName, String shortDescription) {
		return commandDetailDescriptionMap.optString(commandName, shortDescription);
	}

	static {
		try (InputStream descriptionStream = Terminal.class.getResourceAsStream(COMMAND_DESCRIPTIONS_FILENAME)) {
			if(descriptionStream!=null) {
				Object yamlObject = new Yaml().load(descriptionStream);
				commandDetailDescriptionMap = YamlMap.ofObject(yamlObject);
			} else {
				System.err.println("file not found "+COMMAND_DESCRIPTIONS_FILENAME);
			}
		} catch(Exception e) {
			System.err.println("ERROR loading "+COMMAND_DESCRIPTIONS_FILENAME+" "+e);
		}

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
		
		addInternalCommand("experiment","performcance comparisons", Experiment::main);

		//helper commands

		addCommand("commands", "list commands", Terminal::command_commands);
		addCommand("internals", "list internal commands", Terminal::command_internals);
		addCommand("help", "print details of command", Terminal::command_help);

		//server commands

		addCommand("server", "start web server", tsdb.web.Main::main);
		addInternalCommand("server_rmi", "start web and rmi server", StartServerTsDB::main);

		try {
			//addCommand("explorer", "run TubeDB desktop application", tsdb.explorer.Explorer::main);
		} catch (NoClassDefFoundError e) {
			Logger.info("JavaFX not available");
		}

		//administrative commands

		addCommand("clear", "remove all time series data and masks in TubeDB", DataClear::main);
		addCommand("load", "read all data source into TubeDB", DataImport::main);
		addCommand("clear_masks", "clear all masks", ClearMasks::main);
		addCommand("masks", "load masks", LoadMasks::main);
		addCommand("references", "refresh time series references", CreateStationGroupAverageCache::main);
		//addCommand("compact", "defragment free space", RunCompact::main); // 'compact' not usable because of bug in MapDB.
		addCommand("count", "count values in database", CountTsDBValues::main);
		addCommand("export_tsa", "export time series - 1 or 2 parameters: output filename and optional region name", Command_export_tsa::main);
		addCommand("export_csv", "export time series to one CSV file per station in raw format", Command_export_csv::main);

		//addCommandSequence("import", "composite of: load - masks - references - compact", "load", "masks", "references", "compact");  // 'compact' not usable because of bug in MapDB.
		addCommandSequence("import", "composite of: load - masks - references", "load", "masks", "references");

		addCommandSequence("clear_import", "composite of: clear - import", "clear", "import");
		addCommandSequence("clear_load_masks", "composite of: clear_masks - masks", "clear_masks", "masks");

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
			//System.out.println("Command run done.");			
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

	static final String[] EMPTY = new String[]{};

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
		String name = parameters.length==0?"help":parameters[0];
		Command command = commandMap.get(name);
		if(command!=null) {
			System.out.println(command.getDetailedHelp());
		} else {
			System.out.println("unknown command: "+name);
		}		
	}
}
