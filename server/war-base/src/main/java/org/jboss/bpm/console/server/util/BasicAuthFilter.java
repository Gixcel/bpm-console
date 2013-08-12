package org.jboss.bpm.console.server.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.jboss.resteasy.util.Base64;

public class BasicAuthFilter implements Filter {
	private String credentialsCharset = "UTF-8";

	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	public void destroy() {
		// TODO Auto-generated method stub
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException {
		final HttpServletRequest request = (HttpServletRequest) req;

		String header = request.getHeader("Authorization");
		if (header == null || !header.startsWith("Basic ")) {
			chain.doFilter(req, res);
			return;
		}

		String[] tokens = extractAndDecodeHeader(header);
		assert tokens.length == 2;
		String username = tokens[0];
		chain.doFilter(new UserRequestWrapper(username, request), res);
	}

	/**
	 * Decodes the header into a username and password.
	 * 
	 * @throws BadCredentialsException
	 *             if the Basic header is not present or is not valid Base64
	 */
	private String[] extractAndDecodeHeader(String header) throws IOException {

		byte[] base64Token = header.substring(6).getBytes("UTF-8");
		byte[] decoded;
		try {
			decoded = Base64.decode(base64Token);
		} catch (IllegalArgumentException e) {
			throw new IOException("Failed to decode basic authentication token");
		}

		String token = new String(decoded, credentialsCharset);

		int delim = token.indexOf(":");

		if (delim == -1) {
			throw new IOException("Invalid basic authentication token");
		}
		return new String[] { token.substring(0, delim), token.substring(delim + 1) };
	}
}
