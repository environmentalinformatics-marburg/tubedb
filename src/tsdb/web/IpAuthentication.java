package tsdb.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.handler.AbstractHandler;

import tsdb.web.OpenPropertyUserStore.OpenUser;

/**
 * Injects Authentication into Request if IP is in ipMap and user is in loginService.
 * @author woellauer
 *
 */
public class IpAuthentication extends AbstractHandler {
	
	
	private final OpenPropertyUserStore userStore;
	private Map<String, String> ipMap = new HashMap<String, String>();
	
	/**
	 * Create IpAuthentication handler.
	 * @param loginService with user mapping (live lookup)
	 * @param ipMap (entries are copied. lookup at creation time)
	 */
	IpAuthentication(OpenPropertyUserStore userStore, Map<String, String> ipMap) {
		this.userStore = userStore;
		this.ipMap = new HashMap<String, String>(ipMap);
	}

	@Override
	public void handle(String target, Request request, HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {
		String ip = request.getRemoteAddr();
		//Logger.info("ip "+ip);
		String user = ipMap.get(ip);
		if(user != null) {
			//Logger.info("user "+user);
			OpenUser openUser = userStore.getOpenUser(user);
			if(openUser == null) {
				Logger.warn("no identiy for user " + user);
			} else {
				//Logger.info("identity "+userIdentity);
				//Subject subject = userIdentity.getSubject();
				//Logger.info(subject.getPrivateCredentials().iterator().next().getClass());
				//Logger.info(subject.getPublicCredentials());
				//Logger.info(userIdentity.getUserPrincipal().getClass());
				Authentication authentication = new UserAuthentication("IP", openUser);
				request.setAuthentication(authentication);
			}
		}		
	}
}
