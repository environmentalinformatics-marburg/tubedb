package tsdb.web.api;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.ZipExport;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.Pair;

public class ZipExportProxy {

	private static final Logger log = LogManager.getLogger();

	private final RemoteTsDB tsdb;
	private final ExportModel model;
	private File tempFile;

	private Thread workerThread;
	private ZipExport zipexport;

	private ArrayList<String> output_lines;

	private boolean finished = false;


	public ZipExportProxy(RemoteTsDB tsdb, ExportModel model) {
		this.tsdb = tsdb;
		this.model = model;
		this.output_lines = new ArrayList<String>();

		try {
			File downloadDir = new File(TsDBFactory.WEBDOWNLOAD_PATH);
			downloadDir.mkdirs();
			tempFile = File.createTempFile("result_", ".zip", downloadDir);
			log.info(tempFile.getName());
		} catch (IOException e) {
			tempFile = null;
			log.info(e);
		}
	}

	public void startExport() {
		try {
			//OutputStream outputstream = new ByteArrayOutputStream();

			OutputStream outputstream = new BufferedOutputStream(new FileOutputStream(tempFile));

			Region region = model.region;
			String[] sensorNames = tsdb.supplementSchema(model.sensors);
			/*if(Arrays.stream(sensorNames).anyMatch(name->name.equals("WD")) && Arrays.stream(sensorNames).noneMatch(name->name.equals("WV"))) {
			sensorNames = Stream.concat(Arrays.stream(sensorNames), Stream.of("WV")).toArray(String[]::new);
		}
		if(Arrays.stream(sensorNames).anyMatch(name->name.equals("sunshine")) && Arrays.stream(sensorNames).noneMatch(name->name.equals("Rn_300"))) {
			sensorNames = Stream.concat(Arrays.stream(sensorNames), Stream.of("Rn_300")).toArray(String[]::new);
		}*/		
			String[] plotIDs = model.plots;
			AggregationInterval aggregationInterval = model.aggregationInterval;
			DataQuality dataQuality = model.quality;
			boolean interpolated = model.interpolate;
			boolean allinone = model.allinone;
			boolean desc_sensor = model.desc_sensor;
			boolean desc_plot = model.desc_plot;
			boolean desc_settings = model.desc_settings;
			boolean col_plotid = model.col_plotid;
			boolean col_timestamp = model.col_timestamp;
			boolean col_datetime = model.col_datetime;
			boolean col_qualitycounter = model.col_qualitycounter;
			boolean write_header = model.write_header;

			Pair<Long, Long> timespan = model.getTimespan();

			zipexport = new ZipExport(tsdb, region, sensorNames, plotIDs, aggregationInterval, dataQuality, interpolated, allinone,desc_sensor,desc_plot,desc_settings,col_plotid,col_timestamp,col_datetime,write_header,timespan.a,timespan.b,col_qualitycounter);
			zipexport.setPrintCallback(this::println);
			workerThread = new Thread(new Runnable() {					
				@Override
				public void run() {
					try {
						/*boolean ret =*/ zipexport.writeToStream(outputstream);
						outputstream.close();
						finished = true;
					} catch(Exception e) {
						finished = true;	
					}
				}
			});

			workerThread.start();
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void println() {
		synchronized (output_lines) {
			output_lines.add("");
		} 
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

	public boolean getFinished() {
		return finished;
	}

	public int getProcessedPlots() {
		return zipexport.getProcessedPlots();
	}

	public String getFilename() {
		return tempFile.getName();
	}

	public String getTitle() {
		String s="";
		if(model.plots.length<=5) {
			for(String plot:model.plots) {
				s+=plot+"_";
			}
		}
		if(model.sensors.length<=5) {
			for(String sensor:model.sensors) {
				s+=sensor+"_";
			}
		}
		switch(model.timespanType) {
		case ALL:
			//nothing
			break;
		case YEAR:
			s += model.timespanYear+"_";
			break;
		case YEARS:
			s += model.timespanYearsFrom+"_"+model.timespanYearsTo+"_";
			break;
		case DATES:
			s += model.timespanDatesFrom+"_"+model.timespanDatesTo+"_";
			break;
		default:
			//nothing
		}
		return s;
	}
}
