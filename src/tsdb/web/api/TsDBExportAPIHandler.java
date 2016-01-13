package tsdb.web.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONObject;
import org.json.JSONWriter;

import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.ZipExport;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.Pair;
import tsdb.web.api.ExportModel.TimespanType;
import tsdb.web.util.WebUtil;

public class TsDBExportAPIHandler extends AbstractHandler {

	private static final Logger log = LogManager.getLogger();

	private final RemoteTsDB tsdb;

	public TsDBExportAPIHandler(RemoteTsDB tsdb) {
		this.tsdb = tsdb;
	}
	
	private void resetModel(ExportModel model) {
		model.reset();
		//model.plots = new String[]{"HEG01"};
		//model.sensors = new String[]{"Ta_200"};
		model.plots = new String[]{};
		model.sensors = new String[]{};
		model.aggregationInterval = AggregationInterval.HOUR;
		model.timespanYear = 2014;
		model.timespanYearsFrom = 2008;
		model.timespanYearsTo = 2014;
		model.timespanDatesFrom = "2014-04";
		model.timespanDatesTo = "2014-09";
		try {
			if(TsDBFactory.JUST_ONE_REGION==null) {
				model.region = tsdb.getRegions()[0];
			} else {
				Region[] regions = tsdb.getRegions();
				model.region = regions[0];
				for(Region region:regions) {
					if(region.name.equals(TsDBFactory.JUST_ONE_REGION)) {
						model.region = region;
						break;
					}
				}
			}
		} catch(Exception e) {
			log.error(e);
		}
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		log.info(WebUtil.requestMarker,WebUtil.getRequestLogString("export", target, baseRequest));

		//response.setHeader("Server", "");
		//response.setHeader("Date", null);

		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");

		HttpSession session = request.getSession();
		if(session.isNew()) {
			ExportModel model = new ExportModel();
			session.setAttribute("ExportModel", model);
			resetModel(model);
		}
		ExportModel model = (ExportModel) session.getAttribute("ExportModel");
		boolean ret = false;

		switch(target) {
		case "/plots": {
			ret = handle_plots(response.getWriter(),model);
			break;
		}
		case "/sensors": {
			ret = handle_sensors(response.getWriter(),model);
			break;
		}
		case "/apply_plots": {
			ArrayList<String> lines = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = reader.readLine();
			while(line!=null) {
				lines.add(line);
				line = reader.readLine();
			}			
			ret = apply_plots(response.getWriter(),model,lines);
			break;
		}
		case "/apply_sensors": {
			ArrayList<String> lines = new ArrayList<String>();
			BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			String line = reader.readLine();
			while(line!=null) {
				lines.add(line);
				line = reader.readLine();
			}			
			ret = apply_sensors(response.getWriter(),model,lines);
			break;
		}
		case "/result.zip": {
			ret = handle_download(response,model);
			break;
		}
		case "/create": {
			ret = handle_create(response,model);
			break;
		}
		case "/create_get_output": {
			try {
				long id = fromJsonID(request.getParameter("id"));
				ret = handle_create_get_output(response,model,id);
			} catch(Exception e) {
				log.error(e);
			}
			break;
		}
		/*case "/create.zip": {
			try {
				long id = Long.parseLong(request.getParameter("id"));			
				ret = handle_create_download(response,model,id);
			} catch(Exception e) {
				log.error(e);
			}
			break;
		}*/
		case "/settings": {
			ret = handle_settings(response.getWriter(),model);
			break;
		}
		case "/apply_settings": {
			BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			ret = apply_settings(reader,model);
			break;
		}
		case "/region": {
			ret = handle_region(response.getWriter(),model);
			break;
		}
		case "/apply_region": {
			BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			ret = handle_apply_region(reader,model);
			break;
		}
		case "/reset": {
			ret = handle_reset(model);
			break;
		}
		default: {
			ret = handle_error(response.getWriter(), baseRequest.getRequestURI());
		}
		}

		if(ret) {
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private boolean handle_reset(ExportModel model) {
		resetModel(model);
		return true;
	}

	private boolean handle_error(PrintWriter writer, String target) {
		writer.println("tsdb export API error: unknown query: "+target);
		return false;
	}

	private boolean handle_plots(PrintWriter writer, ExportModel model) {		
		writeStringArray(writer,model.plots);
		return true;
	}

	private boolean handle_sensors(PrintWriter writer, ExportModel model) {		
		writeStringArray(writer,model.sensors);		
		return true;
	}

	private static void writeStringArray(PrintWriter writer, String[] array) {
		if(array==null) {
			return;
		}
		boolean notFirst = false;
		for(String s:array) {
			if(notFirst) {
				writer.print('\n');
			}
			writer.print(s);
			notFirst = true;
		}
	}

	private boolean apply_plots(PrintWriter writer, ExportModel model, ArrayList<String> lines) {		
		model.plots = lines.toArray(new String[0]);		
		System.out.println(lines);
		return true;
	}

	private boolean apply_sensors(PrintWriter writer, ExportModel model, ArrayList<String> lines) {		
		if(model.aggregationInterval == AggregationInterval.RAW) {
			model.sensors = lines.toArray(new String[0]);
		} else {
			ArrayList<String> sensorNameList = new ArrayList<String>();
			try {
				Sensor[] allSensors = tsdb.getSensors();
				if(allSensors!=null) {
					Map<String, Sensor> allSensorsMap = Arrays.stream(allSensors).collect(Collectors.toMap(Sensor::getName, Function.identity()));
					for(String sensorName:lines) {
						if(allSensorsMap.containsKey(sensorName)) {
							if(allSensorsMap.get(sensorName).isAggregable()) {
								sensorNameList.add(sensorName);
							}
						}
					}
					model.sensors = sensorNameList.toArray(new String[0]);
				} else {
					model.sensors = lines.toArray(new String[0]);
				}
			} catch (RemoteException e) {
				log.warn(e);
				model.sensors = lines.toArray(new String[0]);
			}
		}
		return true;
	}

	private boolean handle_settings(PrintWriter writer, ExportModel model) {

		JSONWriter json = new JSONWriter(writer);
		json.object();
		json.key("interpolate");
		json.value(model.interpolate);
		json.key("desc_sensor");
		json.value(model.desc_sensor);
		json.key("desc_plot");
		json.value(model.desc_plot);
		json.key("desc_settings");
		json.value(model.desc_settings);		
		json.key("allinone");
		json.value(model.allinone);
		json.key("timestep");
		json.value(model.aggregationInterval.getText());
		json.key("quality");
		json.value(model.quality.getText());
		json.key("col_plotid");
		json.value(model.col_plotid);
		json.key("col_timestamp");
		json.value(model.col_timestamp);
		json.key("col_datetime");
		json.value(model.col_datetime);
		json.key("col_qualitycounter");
		json.value(model.col_qualitycounter);
		json.key("write_header");
		json.value(model.write_header);

		json.key("timespan_type");
		json.value(model.timespanType.toText());	


		json.key("timespan_year");
		json.value(model.timespanYear);

		json.key("timespan_years_from");
		json.value(model.timespanYearsFrom);

		json.key("timespan_years_to");
		json.value(model.timespanYearsTo);

		json.key("timespan_dates_from");
		json.value(model.timespanDatesFrom);

		json.key("timespan_dates_to");
		json.value(model.timespanDatesTo);


		json.endObject();


		return true;
	}

	private boolean apply_settings(BufferedReader reader, ExportModel model) {
		try {
			String line = reader.readLine();
			JSONObject json = new JSONObject(line);
			model.interpolate = json.getBoolean("interpolate");
			model.desc_sensor = json.getBoolean("desc_sensor");
			model.desc_plot = json.getBoolean("desc_plot");
			model.desc_settings = json.getBoolean("desc_settings");
			model.allinone = json.getBoolean("allinone");
			model.aggregationInterval = AggregationInterval.parse(json.getString("timestep"));
			model.quality = DataQuality.parse(json.getString("quality"));
			model.col_plotid = json.getBoolean("col_plotid");
			model.col_timestamp = json.getBoolean("col_timestamp");
			model.col_datetime = json.getBoolean("col_datetime");
			model.col_qualitycounter = json.getBoolean("col_qualitycounter");
			model.write_header = json.getBoolean("write_header");


			TimespanType timespanType = TimespanType.parseText(json.getString("timespan_type"));
			switch(timespanType) {
			case ALL:
				model.timespanType = timespanType; 
				break;
			case YEAR:
				model.timespanYear = json.getInt("timespan_year");
				model.timespanType = timespanType;
				break;
			case YEARS:
				model.timespanYearsFrom = json.getInt("timespan_years_from");
				model.timespanYearsTo = json.getInt("timespan_years_to");
				if(model.timespanYearsFrom>model.timespanYearsTo) {
					int temp = model.timespanYearsFrom;
					model.timespanYearsFrom = model.timespanYearsTo;
					model.timespanYearsTo = temp;
				}
				model.timespanType = timespanType;
				break;
			case DATES:
				String textFrom = json.getString("timespan_dates_from");
				String textTo = json.getString("timespan_dates_to");
				ExportModel.parseDateFrom(textFrom);
				ExportModel.parseDateTo(textTo);
				model.timespanDatesFrom = textFrom;
				model.timespanDatesTo =  textTo;
				model.timespanType = timespanType;
				break;
			default:
				log.error("unknown timespantype: "+model.timespanType);
			}


			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	private boolean handle_region(PrintWriter writer, ExportModel model) {
		writer.print(model.region.name+";"+model.region.longName);
		return true;
	}

	private boolean handle_apply_region(BufferedReader reader, ExportModel model) {
		try {
			String line = reader.readLine();
			for(Region region:tsdb.getRegions()) {
				if(region.name.equals(line)) {
					if(!model.region.name.equals(region.name)) {
						model.region = region;
						model.plots = new String[0];
						model.sensors = new String[0];
					}					
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean handle_download(HttpServletResponse response, ExportModel model) {
		response.setContentType("application/zip");

		try {
			OutputStream outputstream = response.getOutputStream();
			Region region = model.region;
			String[] sensorNames = tsdb.supplementSchema(model.sensors);
			
			/*
			
			if(Arrays.stream(sensorNames).anyMatch(name->name.equals("WD")) && Arrays.stream(sensorNames).noneMatch(name->name.equals("WV"))) {
				sensorNames = Stream.concat(Arrays.stream(sensorNames), Stream.of("WV")).toArray(String[]::new);
			}
			if(Arrays.stream(sensorNames).anyMatch(name->name.equals("sunshine")) && Arrays.stream(sensorNames).noneMatch(name->name.equals("Rn_300"))) {
				sensorNames = Stream.concat(Arrays.stream(sensorNames), Stream.of("Rn_300")).toArray(String[]::new);
			}
			
			*/
			
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

			ZipExport zipexport = new ZipExport(tsdb, region, sensorNames, plotIDs, aggregationInterval, dataQuality, interpolated, allinone,desc_sensor,desc_plot,desc_settings,col_plotid,col_timestamp,col_datetime,write_header,timespan.a,timespan.b,col_qualitycounter);
			boolean ret = zipexport.writeToStream(outputstream);
			return ret;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}	
	}

	private SecureRandom random = new SecureRandom();
	//private long counter = 1000000001;
	private HashMap<Long,ZipExportProxy> zipExportProxyMap = new HashMap<Long,ZipExportProxy>();

	private long storeEntry(ZipExportProxy zipExportProxy) {
		synchronized (zipExportProxyMap) {			
			long id = random.nextLong();
			while(/*id<0 ||*/ zipExportProxyMap.containsKey(id)) {
				id = random.nextLong();
			}
			zipExportProxyMap.put(id, zipExportProxy);	
			return id;
		}
	}

	private boolean handle_create(HttpServletResponse response, ExportModel model) {
		try {
			//System.out.println(SecureRandom.getInstanceStrong().nextLong());
			ZipExportProxy zipExportProxy = new ZipExportProxy(tsdb,model);
			final long id = storeEntry(zipExportProxy);

			log.info("new export create id: "+toJsonID(id));

			//zipExportProxyMap.put(id, zipExportProxy);			

			response.setContentType("application/json");			
			JSONWriter json = new JSONWriter(response.getWriter());
			json.object();
			json.key("id");
			json.value(toJsonID(id));
			json.key("plots");
			json.value(model.plots.length);
			json.endObject();

			zipExportProxy.startExport();

			return true;
		} catch(Exception e) {
			log.error(e);
			return false;
		}
	}
	
	public static String toJsonID(long id) {
		return Long.toHexString(id);
	}
	
	/**
	 * Throws exception if wrong format
	 * @param text
	 * @return
	 */
	public static long fromJsonID(String text) {
		//long id = Long.parseLong(text);
		//long id = Long.parseLong(text, 16);
		return new BigInteger(text, 16).longValue();		
	}

	private boolean handle_create_get_output(HttpServletResponse response, ExportModel model, final long id) {
		try {
			ZipExportProxy zipExportProxy = zipExportProxyMap.get(id);
			if(zipExportProxy==null) {
				log.info("id not found "+id);
				return false;
			}

			final boolean finished = zipExportProxy.getFinished();

			String[] output_lines = zipExportProxy.getOutputLines();


			response.setContentType("application/json");			
			JSONWriter json = new JSONWriter(response.getWriter());
			json.object();
			json.key("id");
			json.value(toJsonID(id));

			json.key("finished");
			json.value(finished);

			json.key("processed_plots");
			json.value(zipExportProxy.getProcessedPlots());

			json.key("output_lines");
			json.array();
			for(String line:output_lines) {
				json.value(line);
			}
			json.endArray();

			if(finished) {
				json.key("filename");
				json.value(zipExportProxy.getFilename());
				
				json.key("title");
				json.value(zipExportProxy.getTitle());
			}

			json.endObject();
			return true;
		} catch(Exception e) {
			log.error(e);
			return false;
		}		
	}

	/*@Deprecated
	private boolean handle_create_download(HttpServletResponse response, ExportModel model, long id) {
		try {
			response.setContentType("application/zip");
			return true;
		} catch(Exception e) {
			log.error(e);
			return false;
		}
	}*/
}
