package tsdb.web.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import au.com.bytecode.opencsv.CSVWriter;
import tsdb.component.SourceEntry;
import tsdb.remote.RemoteTsDB;
import tsdb.util.TimeUtil;

public class Handler_source_catalog_csv extends MethodHandler {	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	public Handler_source_catalog_csv(RemoteTsDB tsdb) {
		super(tsdb, "source_catalog.csv");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");

		SourceEntry[] catalog = tsdb.getSourceCatalogEntries();

		CSVWriter writer = new CSVWriter(response.getWriter());

		String[] headerLine = new String[]{"station", "first", "last", "rows", "timestep", "translation", "filename", "path"};
		writer.writeNext(headerLine);		

		for(SourceEntry e:catalog) {
			String path = e.path;
			String filename = e.filename;
			String first = TimeUtil.oleMinutesToText(e.firstTimestamp);
			String last = TimeUtil.oleMinutesToText(e.lastTimestamp);
			String station = e.stationName;
			String rows = Integer.toString(e.rows);
			String timestep = e.timeStep<0?"?":Integer.toString(e.timeStep);
			//String header = arrayToString(e.headerNames);
			//String sensors = arrayToString(e.sensorNames);

			String translationText = e.getTranslation();

			String[] line = new String[]{station, first, last, rows, timestep, translationText, filename, path};
			writer.writeNext(line);
		}

		writer.close();


	}

	
}
