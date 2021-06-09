package tsdb.web;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RobotsTxtServlet extends HttpServlet {
	private static final long serialVersionUID = -3383614386092827045L;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/plain;charset=utf-8");
		PrintWriter writer = resp.getWriter();
		writer.println("User-agent: *");
		writer.println("Disallow: /");
		//Filter filter = null;
	}
	//public static final ServletHolder SERVLET_HOLDER = new ServletHolder(new RobotsTxtServlet());
}
