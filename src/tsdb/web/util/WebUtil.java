package tsdb.web.util;

import java.time.LocalDateTime;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.eclipse.jetty.server.Request;

public class WebUtil {

	public static final Marker webMarker = MarkerManager.getMarker("web");
	public static final Marker requestMarker = MarkerManager.getMarker("request").addParents(webMarker);

	public static StringBuilder getRequestLogString(String handlerText, String target, Request request) {		
		StringBuilder s = new StringBuilder();
		s.append('[');
		s.append(handlerText);
		s.append("] ");
		s.append(LocalDateTime.now());
		s.append("  ");
		s.append(request.getRemoteAddr());
		s.append("\t\t");
		s.append(target);
		String qs = request.getQueryString();
		if(qs!=null) {
			s.append("\t\t\t");
			s.append(request.getQueryString());			
		}
		String referer = request.getHeader("Referer");
		if(referer!=null) {
			s.append("\t\t\tReferer ");
			s.append(referer);
		}
		return s;		
		//return "[tsdb] "+LocalDateTime.now()+"  "+baseRequest.getRemoteAddr()+"\t\t"+target+"\t\t\t"+baseRequest.getQueryString());
	}

}
