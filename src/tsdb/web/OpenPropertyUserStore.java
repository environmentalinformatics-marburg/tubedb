package tsdb.web;

import javax.security.auth.Subject;

import org.eclipse.jetty.security.PropertyUserStore;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.security.Credential;

public class OpenPropertyUserStore extends PropertyUserStore {

	public class OpenUser extends User implements UserIdentity {

		private final Subject subject;
		private final String[] roles;		

		public OpenUser(String username, Credential credential, String[] roles) {
			super(username, credential, roles);
			subject = new Subject();
			this.roles = roles;
		}

		@Override
		public Subject getSubject() {
			return subject;			
		}

		@Override
		public UserPrincipal getUserPrincipal() {
			return _userPrincipal;

		}

		@Override
		public boolean isUserInRole(String role, Scope scope)
		{
			//Servlet Spec 3.1, pg 125
			if ("*".equals(role)) {
				return false;
			}

			String roleToTest = null;
			if (scope != null && scope.getRoleRefMap() != null) {
				roleToTest = scope.getRoleRefMap().get(role);
			}

			//Servlet Spec 3.1, pg 125
			if (roleToTest == null) {
				roleToTest = role;
			}

			for (String r : roles) {
				if (r.equals(roleToTest)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString()
		{
			return OpenUser.class.getSimpleName() + "('" + _userPrincipal + "')";
		}
	}

	@Override 
	public void addUser(String username, Credential credential, String[] roles) {
		_users.put(username, new OpenUser(username, credential, roles));
	}

	public OpenUser getOpenUser(String userName) {
		return (OpenUser) _users.get(userName);
	}
}
