package tsdb.web.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;

import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;

public class Web {

	public static final String WEB_MARKER = "web";
	public static final String REQUEST_MARKER = "request";	
	
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
		//Logger.info("check "+region+"   "+userIdentity.isUserInRole(region, null)+"   "+userIdentity);
		return userIdentity.isUserInRole(region, null);
	}
	
	public static String requestContentToString(Request request) throws IOException {
		return new String(readAllBytes(request.getInputStream(),request.getContentLength()), StandardCharsets.UTF_8);
	}

	private static final int DEFAULT_BUFFER_SIZE = 8192;
	private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;
	public static byte[] readAllBytes(InputStream in) throws IOException {
		return readAllBytes(in, DEFAULT_BUFFER_SIZE);
	}

	//derived from JDK 9
	public static byte[] readAllBytes(InputStream in, int startBufferSize) throws IOException {
		byte[] buf = new byte[DEFAULT_BUFFER_SIZE];
		int capacity = buf.length;
		int nread = 0;
		int n;
		for (;;) {
			while ((n = in.read(buf, nread, capacity - nread)) > 0)
				nread += n;
			if (n < 0)
				break;
			if (capacity <= MAX_BUFFER_SIZE - capacity) {
				capacity = capacity << 1;
			} else {
				if (capacity == MAX_BUFFER_SIZE)
					throw new OutOfMemoryError("Required array size too large");
				capacity = MAX_BUFFER_SIZE;
			}
			buf = Arrays.copyOf(buf, capacity);
		}
		return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
	}

}
