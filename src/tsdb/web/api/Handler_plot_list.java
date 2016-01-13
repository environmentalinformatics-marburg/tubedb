package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.TsDBFactory;
import tsdb.remote.PlotInfo;
import tsdb.remote.RemoteTsDB;
import tsdb.util.Table;
import tsdb.util.Table.ColumnReaderInt;
import tsdb.util.Table.ColumnReaderString;

public class Handler_plot_list extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_plot_list(RemoteTsDB tsdb) {
		super(tsdb, "plot_list");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");
		String generalstationName = request.getParameter("generalstation");
		String regionName = request.getParameter("region");
		if((generalstationName==null&&regionName==null)||(generalstationName!=null&&regionName!=null)) {
			log.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		Map<String,String> commentMap = null;
		String comment = request.getParameter("comment");
		if(comment!=null) {
			try {
				commentMap = new HashMap<String,String>();
				int commentYear = Integer.parseInt(comment);				
				String filename = TsDBFactory.WEBFILES_PATH+'/'+"plot_comment.csv";
				if(Files.exists(Paths.get(filename))) {
				Table table = Table.readCSV(filename,',');
				ColumnReaderString plotReader = table.createColumnReader("plot");
				ColumnReaderInt yearReader = table.createColumnReaderInt("year");
				ColumnReaderString commentReader = table.createColumnReader("comment");
				
				for(String[] row:table.rows) {
					try {
						int year = yearReader.get(row);
						if(year==commentYear) {
							String plot = plotReader.get(row);
							String plotYearComment = commentReader.get(row);
							if(commentMap.containsKey(plot)) {
								log.warn("overwrite "+plot+"  "+commentMap.get(plot)+" with "+Arrays.toString(row));
							}
							commentMap.put(plot, plotYearComment);
						}
					} catch(Exception e) {
						log.warn(e);
					}
				}
				
				} else {
					log.warn("file not found "+filename);
				}				
			} catch(Exception e) {
				log.error(e);
			}
		}
		
		
		try {
			PlotInfo[] plotInfos = tsdb.getPlots();
			if(plotInfos==null) {
				log.error("plotInfos null: ");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}
			Predicate<PlotInfo> plotFilter;
			if(generalstationName!=null) {
				plotFilter = p->p.generalStationInfo.name.equals(generalstationName);
			} else {
				plotFilter = p->p.generalStationInfo.region.name.equals(regionName);
			}
			final Map<String, String> cMap = commentMap;
			String[] webList = Arrays.stream(plotInfos)
					.filter(plotFilter)
					.map(p->{
						String s = p.name;
						s += p.isVIP?";vip":";normal";
						s += ";"+p.loggerTypeName;
						if(cMap!=null) {
							String plotYearComment = cMap.get(p.name);
							if(plotYearComment!=null) {
								s += ";"+plotYearComment;
							} else {
								s += ";-";
							}
						}
						return s;
					})
					.toArray(String[]::new);
			PrintWriter writer = response.getWriter();
			writeStringArray(writer, webList);
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			log.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
