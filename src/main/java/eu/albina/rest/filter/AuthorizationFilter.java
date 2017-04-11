package eu.albina.rest.filter;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import eu.albina.exception.AlbinaException;
import eu.albina.model.enumerations.Role;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter {

	// private static Logger logger =
	// LoggerFactory.getLogger(AuthorizationFilter.class);

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) {
		// TODO fix role based authorization
		// // Get the resource class which matches with the requested URL
		// // Extract the roles declared by it
		// Class<?> resourceClass = resourceInfo.getResourceClass();
		// List<Role> classRoles = extractRoles(resourceClass);
		//
		// // Get the resource method which matches with the requested URL
		// // Extract the roles declared by it
		// Method resourceMethod = resourceInfo.getResourceMethod();
		// List<Role> methodRoles = extractRoles(resourceMethod);
		//
		// try {
		// // Check if the user is allowed to execute the method
		// // The method annotations override the class annotations
		// if (methodRoles.isEmpty()) {
		// checkPermissions(classRoles, requestContext);
		// } else {
		// checkPermissions(methodRoles, requestContext);
		// }
		//
		// } catch (Exception e) {
		// requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
		// }
	}

	// Extract the roles from the annotated element
	private List<Role> extractRoles(AnnotatedElement annotatedElement) {
		if (annotatedElement == null) {
			return new ArrayList<Role>();
		} else {
			Secured secured = annotatedElement.getAnnotation(Secured.class);
			if (secured == null) {
				return new ArrayList<Role>();
			} else {
				Role[] allowedRoles = secured.value();
				return Arrays.asList(allowedRoles);
			}
		}
	}

	private void checkPermissions(List<Role> allowedRoles, ContainerRequestContext requestContext) throws Exception {
		final SecurityContext currentSecurityContext = requestContext.getSecurityContext();

		for (Role role : allowedRoles)
			if (currentSecurityContext.isUserInRole(role.toString()))
				return;

		throw new AlbinaException("User has no role permission!");
	}
}