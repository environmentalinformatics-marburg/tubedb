package tsdb.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;

import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.remote.RemoteTsDB;

public class Handler_region_list extends MethodHandler {	
	private static final Logger log = LogManager.getLogger();

	public Handler_region_list(RemoteTsDB tsdb) {
		super(tsdb, "region_list");
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		baseRequest.setHandled(true);
		response.setContentType("text/plain;charset=utf-8");		
		try {
			Region[] regions = tsdb.getRegions();
			if(regions!=null) {
				if(TsDBFactory.JUST_ONE_REGION==null) {
					String[] webList = Arrays.stream(regions).map(r->r.name+";"+r.longName).toArray(String[]::new);
					PrintWriter writer = response.getWriter();
					writeStringArray(writer, webList);
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					boolean ok = false;
					for(Region region:regions) {
						if(region.name.equals(TsDBFactory.JUST_ONE_REGION)) {
							String[] webList = new String[]{region.name+";"+region.longName};
							PrintWriter writer = response.getWriter();
							writeStringArray(writer, webList);
							response.setStatus(HttpServletResponse.SC_OK);
							ok = true;
							break;
						}					
					}
					if(!ok) {
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					}
				}
			} else {
				log.error("regions null");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} catch (Exception e) {
			log.error(e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}		
	}

}
