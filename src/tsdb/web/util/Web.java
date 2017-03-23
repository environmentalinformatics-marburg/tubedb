package tsdb.web.util;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.Authentication.User;

public class Web {
	private static final Logger log = LogManager.getLogger();

	public static final Marker webMarker = MarkerManager.getMarker("web");
	public static final Marker requestMarker = MarkerManager.getMarker("request").addParents(webMarker);
	
	public static final String ROLE_ADMIN = "admin";

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
	
	public static UserIdentity getUserIdentity(Request request) {
		Authentication authentication = request.getAuthentication();
		if(authentication == null || !(authentication instanceof User)) {
			return null;
		}
		UserIdentity userIdentity = ((User) authentication).getUserIdentity();
		return userIdentity;
	}
	
	public static boolean isAllowed(Request request, String region) {
		return isAllowed(getUserIdentity(request), region);		
	}
	
	public static boolean isAllowed(UserIdentity userIdentity, String region) {
		if(userIdentity==null) {
			return true;
		}
		if(userIdentity.isUserInRole(ROLE_ADMIN, null)) {
			return true;
		}
		//log.info("check "+region+"   "+userIdentity.isUserInRole(region, null)+"   "+userIdentity);
		return userIdentity.isUserInRole(region, null);
	}

}
