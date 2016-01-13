package tsdb.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

public class Robots_txt_Handler extends AbstractHandler {
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		if(request.getRequestURI().equals("/robots.txt")) {
			baseRequest.setHandled(true);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("text/plain;charset=utf-8");
			PrintWriter writer = response.getWriter();
			writer.println("User-agent: *");
			writer.println("Disallow: /");
		}
	}

	public static ContextHandler createContextHandler() {
		ContextHandler contextHandler = new ContextHandler();
		contextHandler.setHandler(new Robots_txt_Handler());
		return contextHandler;
	}

	public final static ContextHandler CONTEXT_HANDLER = createContextHandler();
}
