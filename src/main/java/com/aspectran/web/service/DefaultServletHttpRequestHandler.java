/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.aspectran.web.service;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * The Class DefaultServletHttpRequestHandler.
 */
public class DefaultServletHttpRequestHandler {

	private static final Log log = LogFactory.getLog(DefaultServletHttpRequestHandler.class);
	
	/** Default Servlet name used by Tomcat, Jetty, JBoss, and Glassfish */
	private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

	/** Default Servlet name used by Resin */
	private static final String RESIN_DEFAULT_SERVLET_NAME = "resin-file";

	/** Default Servlet name used by WebLogic */
	private static final String WEBLOGIC_DEFAULT_SERVLET_NAME = "FileServlet";

	/** Default Servlet name used by WebSphere */
	private static final String WEBSPHERE_DEFAULT_SERVLET_NAME = "SimpleFileServlet";
	
	/** Default Servlet name used by Google App Engine */
	private static final String GAE_DEFAULT_SERVLET_NAME = "_ah_default";

	private final ServletContext servletContext;
	
	private String defaultServletName;

	public DefaultServletHttpRequestHandler(ServletContext servletContext) {
		this.servletContext = servletContext;
		
		lookupDefaultServletName(servletContext);
	}

	/**
	 * Gets the default servlet name.
	 *
	 * @return the default servlet name
	 */
	public String getDefaultServletName() {
		return defaultServletName;
	}

	/**
	 * Set the name of the default Servlet to be forwarded to for static resource requests.
	 *
	 * @param defaultServletName the new default servlet name
	 */
	public void setDefaultServletName(String defaultServletName) {
		this.defaultServletName = defaultServletName;
	}
	
	private void lookupDefaultServletName(ServletContext servletContext) {
		if(servletContext.getNamedDispatcher(COMMON_DEFAULT_SERVLET_NAME) != null) {
			defaultServletName = COMMON_DEFAULT_SERVLET_NAME;
		} else if(servletContext.getNamedDispatcher(RESIN_DEFAULT_SERVLET_NAME) != null) {
			defaultServletName = RESIN_DEFAULT_SERVLET_NAME;
		} else if(servletContext.getNamedDispatcher(WEBLOGIC_DEFAULT_SERVLET_NAME) != null) {
			defaultServletName = WEBLOGIC_DEFAULT_SERVLET_NAME;
		} else if(servletContext.getNamedDispatcher(WEBSPHERE_DEFAULT_SERVLET_NAME) != null) {
			defaultServletName = WEBSPHERE_DEFAULT_SERVLET_NAME;
		} else if(servletContext.getNamedDispatcher(GAE_DEFAULT_SERVLET_NAME) != null) {
			defaultServletName = GAE_DEFAULT_SERVLET_NAME;
		} else {
			log.warn("Unable to locate the default servlet for serving static content. Please set the 'aspectran:defaultServletName'");
		}
	}

	public boolean handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(defaultServletName != null) {
			RequestDispatcher rd = servletContext.getNamedDispatcher(defaultServletName);
			
			if(rd == null)
				throw new IllegalStateException("A RequestDispatcher could not be located for the default servlet '" + defaultServletName + "'");
			
			rd.forward(request, response);
			
			return true;
		} else {
			return false;
		}
	}

}
