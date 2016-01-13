package tsdb.web;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class InvalidUrlServlet extends HttpServlet {
	private static final long serialVersionUID = -7066685622900263032L;
	private final String message;	
	public InvalidUrlServlet(String message) {
		this.message = message;
	}
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		resp.setContentType("text/html;charset=utf-8");
		//response.setHeader("Server", "");
		//response.setHeader("Date", null);
		PrintWriter writer = resp.getWriter();
		writer.print("<!DOCTYPE html>");
		writer.print("<html lang=\"en\">");
		writer.print("<head>");
		writer.print("<meta charset=\"utf-8\">");
		writer.print("<meta name=\"robots\" content=\"noindex\" />");
		writer.print("<title>");
		writer.print(message);
		writer.print("</title>");
		writer.print("</head>");
		writer.print("<body>");
		writer.print(message);
		writer.print("</body>");
		writer.print("</html>");
	}
	//public static final ServletHolder SERVLET_HOLDER = new ServletHolder(new InvalidUrlServlet("error"));
}
