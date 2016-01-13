package tsdb.web.util;

import java.time.LocalDateTime;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.eclipse.jetty.server.Request;

public class WebUtil {
	
	public static final Marker webMarker = MarkerManager.getMarker("web");
	public static final Marker requestMarker = MarkerManager.getMarker("request").addParents(webMarker);

	public static StringBuilder getRequestLogString(String handlerText, String target, Request baseRequest) {		
		StringBuilder s = new StringBuilder();
		s.append('[');
		s.append(handlerText);
		s.append("] ");
		s.append(LocalDateTime.now());
		s.append("  ");
		s.append(baseRequest.getRemoteAddr());
		s.append("\t\t");
		s.append(target);
		String qs = baseRequest.getQueryString();
		if(qs!=null) {
			s.append("\t\t\t");
			s.append(baseRequest.getQueryString());
		}
		return s;		
		//return "[tsdb] "+LocalDateTime.now()+"  "+baseRequest.getRemoteAddr()+"\t\t"+target+"\t\t\t"+baseRequest.getQueryString());
	}

}
