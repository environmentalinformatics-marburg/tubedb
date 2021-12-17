package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;

import tsdb.TsDBFactory;
import tsdb.remote.GeneralStationInfo;
import tsdb.remote.PlotInfo;
import tsdb.remote.RemoteTsDB;
import tsdb.util.Table;
import tsdb.util.Table.ColumnReaderInt;
import tsdb.util.Table.ColumnReaderString;
import tsdb.web.util.Web;

/**
 * Get list of plots with name and long name.
 * <p>
 * Parameter: 
 * <br>
 * (optional) region
 * <br>
 * (optional) generalstation
 * <br>
 * (optional) comment: value of year to query comments of.
 * <p>
 * Either region or generalstation needs to be set to filter plots.
 * <p>
 * Returns one plot per row with columns:
 * <br>
 * 1. plot name
 * <br>
 * 2. plot category (normal, vip)
 * <br>
 * 3. logger type name or virtual (if plot is not identical with station)
 * <br>
 * 4. (if parameter comment was set) comment for this plot as text  
 * 
 * @author woellauer
 *
 */
public class Handler_plot_list extends MethodHandler {	
	

	public Handler_plot_list(RemoteTsDB tsdb) {
		super(tsdb, "plot_list");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		UserIdentity userIdentity = Web.getUserIdentity(baseRequest);
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");
		String generalstationName = request.getParameter("generalstation");
		String regionName = request.getParameter("region");
		if(regionName != null && !Web.isAllowed(userIdentity, regionName)) {
			Logger.warn("no access to region " + regionName);
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		if((generalstationName == null && regionName == null) || (generalstationName != null && regionName != null)) {
			Logger.warn("wrong call");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		Map<String,String> commentMap = null;
		String comment = request.getParameter("comment");
		if(comment != null) {
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
								Logger.warn("overwrite "+plot+"  "+commentMap.get(plot)+" with "+Arrays.toString(row));
							}
							commentMap.put(plot, plotYearComment);
						}
					} catch(Exception e) {
						Logger.warn(e);
					}
				}
				
				} else {
					Logger.warn("file not found "+filename);
				}				
			} catch(Exception e) {
				Logger.error(e);
			}
		}
		
		
		try {
			HashMap<String, GeneralStationInfo> assigned_plotMap = new HashMap<String, GeneralStationInfo>();
			
			if(generalstationName != null) {
				GeneralStationInfo[] generalStationInfos = tsdb.getGeneralStations();
				if(generalStationInfos == null) {
					Logger.error("generalStationInfos null: ");
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
					return;
				}
				for(GeneralStationInfo generalStationInfo : generalStationInfos) {
					if(generalStationInfo.name.equals(generalstationName)) {
						if(generalStationInfo.assigned_plots != null && Web.isAllowed(userIdentity, generalStationInfo.region.name)) {
							for(String assigned_plot : generalStationInfo.assigned_plots) {
								assigned_plotMap.put(assigned_plot, generalStationInfo);
							}
						}
					}
				}
			} else {
				GeneralStationInfo[] generalStationInfos = tsdb.getGeneralStationsOfRegion(regionName);			
				if(generalStationInfos == null) {
					Logger.error("generalStationInfos null: ");
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
					return;
				}
				for(GeneralStationInfo generalStationInfo : generalStationInfos) {
					if(generalStationInfo.assigned_plots != null && Web.isAllowed(userIdentity, generalStationInfo.region.name)) {
						for(String assigned_plot : generalStationInfo.assigned_plots) {
							assigned_plotMap.put(assigned_plot, generalStationInfo);
						}
					}				
				}
			}			
			
			PlotInfo[] plotInfos = tsdb.getPlots();
			if(plotInfos==null) {
				Logger.error("plotInfos null: ");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);				
				return;
			}
			Predicate<PlotInfo> plotFilter;
			if(generalstationName != null) {
				plotFilter = p -> (Web.isAllowed(userIdentity, p.generalStationInfo.region.name) && p.generalStationInfo.name.equals(generalstationName)) 
						|| assigned_plotMap.containsKey(p.name);
			} else {
				plotFilter = p -> p.generalStationInfo.region.name.equals(regionName) || assigned_plotMap.containsKey(p.name);
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
			Logger.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}
}
