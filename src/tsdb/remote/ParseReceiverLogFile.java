package tsdb.remote;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ParseReceiverLogFile {
	private static final Logger log = LogManager.getLogger();

	public Map<String, PlotMessage> plotMap = new TreeMap<>();
	public LocalDateTime lastDateTime = LocalDateTime.of(1900, 1, 1, 0, 0);
	
	public void insertDirectory(String directory) {
		insertDirectory(Paths.get(directory));
	}

	public void insertDirectory(Path directory) {
		try {
			DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory,p->(!p.toFile().isDirectory())&&p.toString().endsWith(".log"));
			for(Path file:directoryStream) {
				log.info("read "+file);
				insertFile(file);
			}
		} catch(Exception e) {
			log.error(e);
		}
	}

	public void insertFile(Path file) {
		try {
			List<String> lines = Files.readAllLines(file, StandardCharsets.ISO_8859_1);		

			String currPlot = null;
			LocalDateTime currDateTime = null;

			for(String s:lines) {
				String line = s.trim();
				if(line.isEmpty()) {
					continue;
				}
				try {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
					LocalDateTime datetime = LocalDateTime.parse(line.subSequence(0, line.indexOf('-')-1), formatter);
					if(datetime.isAfter(lastDateTime)) {
						lastDateTime = datetime;
					}
					String plot = line.substring(line.indexOf('-')+1).trim();
					//System.out.println(plot+"  "+datetime);
					if(currPlot!=null) {
						insertEntry(new PlotMessage(currPlot, currDateTime, "OK"));
					}
					currPlot = plot;
					currDateTime = datetime;
				} catch (Exception e) {
					//System.out.println(e);
					if(currPlot!=null) {
						try {
							String message = line.substring(line.indexOf(':')+1).trim();
							//System.out.println(currPlot+"  "+currDateTime+" "+message);
							insertEntry(new PlotMessage(currPlot, currDateTime, message));
						} catch(Exception e1) {
							log.warn(e1+"  "+line);
							insertEntry(new PlotMessage(currPlot, currDateTime, "error"));
						}
					}
					currPlot = null;
					currDateTime = null;
				}
			}
			if(currPlot!=null) {
				insertEntry(new PlotMessage(currPlot, currDateTime, "OK"));
			}
		} catch(Exception e) {
			log.error(e);
		}
	}

	private void insertEntry(PlotMessage plotMessage) {
		PlotMessage oldPlotMessage = plotMap.get(plotMessage.plot);
		if(oldPlotMessage==null) {
			plotMap.put(plotMessage.plot, plotMessage);
		} else {
			if(plotMessage.dateTime.isAfter(oldPlotMessage.dateTime)) {
				plotMap.put(plotMessage.plot, plotMessage);
			}
		}		
	}

	public static void main(String[] args) throws IOException {
		ParseReceiverLogFile prlf = new ParseReceiverLogFile();
		prlf.insertDirectory(Paths.get("C:/temp/logs"));

		log.info("last "+prlf.lastDateTime);
		log.info("plots "+prlf.plotMap.size());
		for(PlotMessage v:prlf.plotMap.values()) {
			log.info(v.plot+"  "+v.dateTime+"  "+v.message);
		}
	}

}
