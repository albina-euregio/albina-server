// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.filter;

import java.security.Principal;
import java.util.Date;

import javax.annotation.Priority;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import eu.albina.controller.UserController;
import eu.albina.model.User;
import eu.albina.model.enumerations.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.interfaces.DecodedJWT;

import eu.albina.controller.AuthenticationController;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	@Override
	public void filter(ContainerRequestContext requestContext) {

		// Get the HTTP Authorization header from the request
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

		// Check if the HTTP Authorization header is present and formatted
		// correctly
		if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
			throw new NotAuthorizedException("Authorization header must be provided");
		}

		// Extract the token from the HTTP Authorization header
		String token = authorizationHeader.substring("Bearer".length()).trim();

		try {
			// Validate the token
			DecodedJWT decodedToken = AuthenticationController.getInstance().decodeToken(token);
			Date currentDate = new Date();
			if (currentDate.after(decodedToken.getExpiresAt())) {
				logger.warn("Token expired!");
				throw new Exception();
			}

			final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
			requestContext.setSecurityContext(new SecurityContext() {

				@Override
				public Principal getUserPrincipal() {

					return new Principal() {

						@Override
						public String getName() {
							return decodedToken.getSubject();
						}
					};
				}

				@Override
				public boolean isUserInRole(String role) {
					User user = UserController.getInstance().getUser(decodedToken.getSubject());
					return user.hasRole(Role.valueOf(role));
				}

				@Override
				public boolean isSecure() {
					return currentSecurityContext.isSecure();
				}

				@Override
				public String getAuthenticationScheme() {
					return "Bearer";
				}
			});
		} catch (Exception e) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
		}
	}
}
