package tsdb.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class NoindexHandler extends AbstractHandler {
	//

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setHeader("X-Robots-Tag", "noindex, nofollow");
		//Logger.info("baseRequest.getRemoteAddr() " + baseRequest.getRemoteAddr());
		if("127.0.0.1".equals(baseRequest.getRemoteAddr())  || "[0:0:0:0:0:0:0:1]".equals(baseRequest.getRemoteAddr())) {
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Headers", "content-type");
			if(baseRequest.getMethod().equals("OPTIONS")) {
				baseRequest.setHandled(true);
			}
		}
	}
}
