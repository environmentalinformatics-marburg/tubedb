package tsdb.web.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import com.opencsv.CSVWriter;

import tsdb.StationProperties;
import tsdb.component.SourceEntry;
import tsdb.remote.RemoteTsDB;
import tsdb.remote.VirtualPlotInfo;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;
import tsdb.web.util.Web;

/**
 * Get information about time series data files that have been imported into database.
 * <p>
 * returns: CSV-file with one file-entry per row and first row as header:
 * <br>
 * station,first,last,rows,timestep,translation,filename,path  
 * @author woellauer
 *
 */
public class Handler_source_catalog_csv extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_source_catalog_csv(RemoteTsDB tsdb) {
		super(tsdb, "source_catalog.csv");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");

		String plot = request.getParameter("plot");
		if(plot==null && !Web.isAllowed(baseRequest, Web.ROLE_ADMIN)) {
			log.warn("no admin access");
			response.getWriter().write("no admin access");
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		SourceEntry[] catalog = tsdb.getSourceCatalogEntries();


		if(plot!=null) {
			VirtualPlotInfo virtualPlotInfo = tsdb.getVirtualPlot(plot);
			if(virtualPlotInfo==null) {
				catalog = Arrays.stream(catalog).filter(entry->{
					//log.info(entry.stationName+ "=="+plot);
					if(entry.stationName.equals(plot)) {
						return true;
					}
					return false;
				}).toArray(SourceEntry[]::new);
			} else {
				Set<String> stationSet = virtualPlotInfo.intervalList.stream().map(i->i.value.get_serial()).collect(Collectors.toSet());
				catalog = Arrays.stream(catalog).filter(entry->{
					if(stationSet.contains(entry.stationName)) {
						for(TimestampInterval<StationProperties> interval:virtualPlotInfo.intervalList) {
							if(interval.value.get_serial().equals(entry.stationName)) {
								if(interval.contains(entry.firstTimestamp, entry.lastTimestamp)) {
									return true;
								}
							}
						}
					}
					return false;
				}).toArray(SourceEntry[]::new);
			}
		}


		CSVWriter writer = new CSVWriter(response.getWriter(), ',', CSVWriter.NO_QUOTE_CHARACTER);

		String[] headerLine = new String[]{"station", "first", "last", "rows", "timestep", "translation", "filename", "path"};
		writer.writeNext(headerLine);		

		for(SourceEntry e:catalog) {
			String path = e.path;
			String filename = e.filename;
			String first = TimeUtil.oleMinutesToText(e.firstTimestamp);
			String last = TimeUtil.oleMinutesToText(e.lastTimestamp);
			String station = e.stationName;
			String rows = Integer.toString(e.rows);
			String timestep = e.timeStep<0?"":Integer.toString(e.timeStep);
			//String header = arrayToString(e.headerNames);
			//String sensors = arrayToString(e.sensorNames);

			String translationText = e.getTranslation();

			String[] line = new String[]{station, first, last, rows, timestep, translationText, filename, path};
			writer.writeNext(line);
		}

		writer.close();


	}


}
