package tsdb.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import tsdb.remote.PlotInfo;
import tsdb.remote.RemoteTsDB;

public class TimeSeriesHandler extends AbstractHandler {
	
	private RemoteTsDB tsdb; 

	public TimeSeriesHandler(RemoteTsDB tsdb) {
		this.tsdb = tsdb;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		System.out.println("handle: "+target+"  "+request.getAttributeNames());
		baseRequest.setHandled(true);
		
		Enumeration<String> re = request.getParameterNames();
		while(re.hasMoreElements()) {
			String e = re.nextElement();
			String v = request.getParameter(e);
			System.out.println(e+" -> "+v);
		}
		
		
		PrintWriter writer = response.getWriter();
		
		writer.print("<html><body>");
		
		PlotInfo[] plotInfos = tsdb.getPlots();
		for(PlotInfo plotInfo:plotInfos) {
			writer.println(plotInfo.toString()+"<br>");
		}
		
		writer.print("</body></html>");
		
	}

}
