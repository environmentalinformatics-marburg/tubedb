package tsdb.web.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;

public class Web {
	//private static final Logger log = LogManager.getLogger();

	public static final Marker webMarker = MarkerManager.getMarker("web");
	public static final Marker requestMarker = MarkerManager.getMarker("request").addParents(webMarker);
	
	public static final String ROLE_ADMIN = "admin";
	
	public static StringBuilder getRequestLogString(String handlerText, String target, Request request) {
		String user = "?";
		UserIdentity userIdentity = Web.getUserIdentity(request);
		if(userIdentity!=null) {				
			user = userIdentity.getUserPrincipal().getName();
		}
		StringBuilder s = new StringBuilder();
		s.append(timestampText(LocalDateTime.now()));
		s.append(" ");
		s.append(user);
		s.append(" ");
		s.append(request.getRemoteAddr());
		s.append(" ");
		s.append('[');
		s.append(handlerText);
		s.append("] ");
		s.append(target);
		String qs = request.getQueryString();
		if(qs!=null) {
			s.append("?");
			s.append(request.getQueryString());
		}
		String referer = request.getHeader("Referer");
		if(referer!=null) {
			s.append("    ref ");
			s.append(referer);
		}
		return s;		
	}
	
	public static char[] timestampText(LocalDateTime timestamp) {
		char[] c = new char[23];
		LocalDate date = timestamp.toLocalDate();	
		{
			int y = date.getYear();
			c[0] = (char) ('0'+((char) (y/1000)));
			c[1] = (char) ('0'+((char) ((y%1000)/100)));
			c[2] = (char) ('0'+((char) ((y%100)/10)));
			c[3] = (char) ('0'+((char) (y%10)));
		}
		c[4] = '-';
		{
			int m = date.getMonthValue();
			c[5] = (char) ('0'+((char) (m/10)));
			c[6] = (char) ('0'+((char) (m%10)));
		}
		c[7] = '-';
		{
			int d = date.getDayOfMonth();
			c[8] = (char) ('0'+((char) (d/10)));
			c[9] = (char) ('0'+((char) (d%10)));
		}
		c[10] = 'T';
		LocalTime time = timestamp.toLocalTime();
		{
			int h = time.getHour();
			c[11] = (char) ('0'+((char) (h/10)));
			c[12] = (char) ('0'+((char) (h%10)));
		}
		c[13] = ':';
		{
			int h = time.getMinute();
			c[14] = (char) ('0'+((char) (h/10)));
			c[15] = (char) ('0'+((char) (h%10)));
		}
		c[16] = ':';
		{
			int s = time.getSecond();
			c[17] = (char) ('0'+((char) (s/10)));
			c[18] = (char) ('0'+((char) (s%10)));
		}
		c[19] = '.';
		{
			int m = time.getNano() / 1_000_000;
			c[20] = (char) ('0'+((char) (m/100)));
			c[21] = (char) ('0'+((char) ((m%100)/10)));
			c[22] = (char) ('0'+((char) (m%10)));
		}
		return c;
	}

	/*public static StringBuilder getRequestLogString(String handlerText, String target, Request request) {		
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
	}*/
	
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
