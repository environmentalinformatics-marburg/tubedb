package tsdb.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class NoindexHandler extends AbstractHandler {
	private static final Logger log = LogManager.getLogger();

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setHeader("X-Robots-Tag", "noindex, nofollow");
		//HttpSession session = request.getSession(); // create missing session
		//log.info("this session " + session.getId());
	}
}
