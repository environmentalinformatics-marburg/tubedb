package tsdb.web.api;

import java.io.IOException;
import java.lang.reflect.Field;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.tinylog.Logger;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Authentication.User;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.json.JSONWriter;

import tsdb.TsDBFactory;
import tsdb.remote.RemoteTsDB;

/**
 * get meta data of region 
 * @author woellauer
 *
 */
public class Handler_identity extends MethodHandler {	
	
	
	public static final Field FIELD_ROLES;

	static {
		Field fieldRoles = null;
		try {
			fieldRoles = DefaultUserIdentity.class.getDeclaredField("_roles");
			fieldRoles.setAccessible(true);
		} catch(Exception e) {}
		FIELD_ROLES = fieldRoles;		
	}
	
	public Handler_identity(RemoteTsDB tsdb) {
		super(tsdb, "identity");
	}

	@Override
	public void handle(String target, Request request, HttpServletRequest req, HttpServletResponse response) throws IOException, ServletException {		
		request.setHandled(true);
		response.setContentType("application/json;charset=utf-8");
		JSONWriter json = new JSONWriter(response.getWriter());

		String user = "anonymous";
		String authMethod = "";
		String[] roles = new String[]{};

		Authentication authentication = request.getAuthentication();
		if(authentication != null && (authentication instanceof User)) {
			User authUser = (User) authentication;
			UserIdentity userIdentity = authUser.getUserIdentity();
			user = userIdentity.getUserPrincipal().getName();
			authMethod = authUser.getAuthMethod();

			if(FIELD_ROLES != null && (userIdentity instanceof DefaultUserIdentity)) {
				try {
					roles = (String[]) FIELD_ROLES.get(userIdentity);
				} catch(Exception e) {
					Logger.warn(e);
				}
			}
		} else {
			roles = new String[]{"admin"};
		}

		json.object();
		json.key("tubedb_version");
		json.value(tsdb.get_tubedb_version());
		json.key("ip");
		json.value(request.getRemoteAddr());
		json.key("user");
		json.value(user);
		json.key("auth_method");
		json.value(authMethod);
		json.key("roles");
		json.value(roles);
		json.key("request_port");
		json.value(request.getServerPort());
		if(TsDBFactory.WEB_SERVER_HTTPS) {
			json.key("https_port");
			json.value(TsDBFactory.WEB_SERVER_HTTPS_PORT);
		}
		json.endObject();		
	}
}
