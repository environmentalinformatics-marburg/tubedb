package tsdb.run;

import java.util.ArrayList;

import tsdb.Station;
import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.component.Sensor;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;

/**
 * TsDB console
 * @author woellauer
 *
 */
public class ConsoleRunner implements Runnable {

	private final TsDB tsdb;
	private final String input_line;
	private ArrayList<String> output_lines;

	public ConsoleRunner(TsDB tsdb, String input_line){
		this.tsdb = tsdb;
		this.input_line = input_line;
		this.output_lines = new ArrayList<String>();
	}


	@Override
	public void run() {
		interprete(input_line);
	}	

	public void interprete(String line) {
		if(line==null||line.isEmpty()) {
			return;
		}
		String command = line;
		String parameter="";
		int endIndex = line.indexOf(' ');
		if(endIndex>=0) {
			command = line.substring(0, endIndex);
			parameter = line.substring(endIndex);
		}
		command = command.trim().toLowerCase();
		parameter = parameter.trim().toLowerCase();
		//System.out.println("command: ["+command+"]");
		switch(command) {
		case "intro":
			command_intro();			
			break;
		case "exit":
			command_exit();
			break;
		case "commands":
			command_commands();
			break;
		case "status":
			command_status();
			break;
		case "create-empirical-reference":
			command_create_empirical_reference();
			break;
		case "plots":
			command_plots();
			break;
		case "sensors":
			command_sensors();
			break;
		case "stations":
			command_stations();
			break;
		case "help":
			command_help();
			break;
		case "info":
			command_info(parameter);
			break;
		default:
			command_unknown(command);
		}
	}

	private void command_info(String parameter) {
		int endIndex = parameter.indexOf(':');
		if(endIndex<0) {
			println("info no type of info: "+parameter);
			return;
		}
		String type = parameter.substring(0, endIndex);
		String arg = parameter.substring(endIndex+1);
		type = type.trim().toLowerCase();
		arg = arg.trim();
		switch(type) {
		case "plot":
			command_info_plot(arg);
			break;
		default:
			println("unknown type: "+type);
		}
	}
	
	private void command_info_plot(String plot) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plot.toLowerCase());
		String plotID = plot;
		if(virtualPlot!=null) {
			plotID = virtualPlot.plotID;
			println("virtual plot: "+plotID+" in "+virtualPlot.generalStation.name+" ("+virtualPlot.generalStation.longName+")");
			println("position: "+virtualPlot.geoPosEasting+" "+virtualPlot.geoPosNorthing+" elevation: "+virtualPlot.elevation);
			
			StringBuilder s = new StringBuilder();
			s.append("stations: ");
			for(TimestampInterval<StationProperties> interval:virtualPlot.intervalList) {
				s.append(interval.value.get_serial());
				s.append(' ');
			}
			println(s.toString());
			long[] baseInterval = virtualPlot.getTimestampBaseInterval();
			if(baseInterval!=null) {
				println("data time span: "+TimeUtil.oleMinutesToText(baseInterval[0])+" - "+TimeUtil.oleMinutesToText(baseInterval[1]));
			}
			return;
		}
		Station station = tsdb.getStation(plot.toUpperCase());
		if(station!=null) {
			plotID = station.stationID;
			println("station plot: "+plotID+" in "+station.generalStation.name+" ("+station.generalStation.longName+")");
			println("position: "+station.geoPosLongitude+" "+station.geoPosLongitude);
			return;
		}
		println("unknown plot: "+plot);
	}


	private void command_help() {
		println("With this console commands can be executed at tsdb server.");

	}


	private void command_stations() {
		for(Station station:tsdb.getStations()) {
			println(station.stationID);
		}		
	}


	private void command_sensors() {
		for(Sensor sensor:tsdb.getSensors()) {
			println(sensor.name);
		}		
	}

	private void command_plots() {
		for(VirtualPlot virtualplot:tsdb.getVirtualPlots()) {
			println(virtualplot.plotID);
		}
		for(Station station:tsdb.getStations()) {
			if(station.isPlot) {
				println(station.stationID);
			}
		}
	}


	public void println() {
		synchronized (output_lines) {
			output_lines.add("");
		} 
		System.out.println();
	}


	public void println(String text) {
		synchronized (output_lines) {
			output_lines.add(text);
		}		
	}

	public String[] getOutputLines() {
		String[] lines;
		synchronized (output_lines) {
			lines = output_lines.toArray(new String[0]);
			output_lines.clear();
		}
		return lines;
	}

	public void command_intro() {
		println();
		println("tsdb*------------------------------------------*tsdb");
		println("tsdb*----------------tsdb-console--------------*tsdb");
		println("tsdb*------------------------------------------*tsdb");
		println();
	}

	public void command_exit() {
		println("...closing console...");
	}

	private void command_status() {
		println("--- tsdb status ---");		
	}

	private void command_unknown(String command) {
		println("unknown command: "+command);	
	}

	private void command_commands() {
		println("commands: intro exit commands status plots sensors stations");		
	}

	private void command_create_empirical_reference() {
		println("create group averages...");	
		new CreateStationGroupAverageCache_NEW(tsdb,this::println).run();
	}


}
