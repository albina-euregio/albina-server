// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.rest.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet Filter implementation class CORSFilter
 */
// Enable it for Servlet 3.x implementations
/* @ WebFilter(asyncSupported = true, urlPatterns = { "/*" }) */
public class CorsFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(CorsFilter.class);

	/**
	 * Default constructor.
	 */
	public CorsFilter() {
	}

	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		logger.trace("CORSFilter HTTP Request: {}", request.getMethod());

		// Authorize (allow) all domains to consume the content
		((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Origin", "*");
		((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Methods",
				"GET, OPTIONS, HEAD, PUT, POST, DELETE");
		((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Headers",
				"origin, content-type, accept, authorization, if-none-match, x-client-version");
		((HttpServletResponse) servletResponse).addHeader("Access-Control-Allow-Credentials", "true");
		((HttpServletResponse) servletResponse).addHeader("Access-Control-Max-Age", "1209600");
		((HttpServletResponse) servletResponse).addHeader("Access-Control-Expose-Headers", "ETag");

		HttpServletResponse resp = (HttpServletResponse) servletResponse;

		// For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS
		// handshake
		if (request.getMethod().equals("OPTIONS")) {
			resp.setStatus(HttpServletResponse.SC_ACCEPTED);
			return;
		}

		// pass the request along the filter chain
		chain.doFilter(request, servletResponse);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	@Override
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
