package cz.cuni.mff.xrg.odcs.frontend;

import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;

/**
 * Handles login and logout actions in frontend application.
 *
 * @author Jan Vojt
 */
public class AuthenticationService {

	/**
	 * Attribute key for storing {@link Authentication} in HTTP session.
	 */
	public static final String SESSION_KEY = "authentication";
	
	@Autowired
	@Qualifier("authenticationManager")
	private AuthenticationManager authManager;
	
	@Autowired
	private LogoutHandler logoutHandler;

	/**
	 * Creates security context and saves authentication details into session.
	 *
	 * @param login
	 * @param password
	 * @param httpRequest
	 * @throws AuthenticationException
	 */
	public void login(String login, String password, HttpServletRequest httpRequest)
			throws AuthenticationException {

		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(login, password);

		token.setDetails(new WebAuthenticationDetails(httpRequest));

		Authentication authentication = authManager.authenticate(token);

		HttpSession session = RequestHolder.getRequest().getSession();
		session.setAttribute(SESSION_KEY, authentication);

		httpRequest.getSession().setAttribute(SESSION_KEY, authentication);

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 * Clears security context and removes authentication from session.
	 *
	 * @param httpRequest
	 */
	public void logout(HttpServletRequest httpRequest) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		logoutHandler.logout(httpRequest, null, authentication);

		// clear session
		RequestHolder.getRequest().getSession().removeAttribute(SESSION_KEY);
	}
}
