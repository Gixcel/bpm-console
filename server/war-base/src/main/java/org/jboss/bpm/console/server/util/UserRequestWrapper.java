package org.jboss.bpm.console.server.util;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class UserRequestWrapper extends HttpServletRequestWrapper {
	String user;
	HttpServletRequest realRequest;

	public UserRequestWrapper(String user, HttpServletRequest request) {
		super(request);
		this.user = user;

		this.realRequest = request;
	}

	@Override
	public boolean isUserInRole(String role) {
		// TODO Implement this
		return true;
	}

	@Override
	public Principal getUserPrincipal() {
		if (this.user == null) {
			return realRequest.getUserPrincipal();
		}

		// make an anonymous implementation to just return our user
		return new Principal() {
			public String getName() {
				return user;
			}
		};
	}

}
