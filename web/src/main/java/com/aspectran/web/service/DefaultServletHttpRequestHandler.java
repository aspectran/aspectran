/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.web.service;

import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.thread.ThreadContextHelper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Handles requests for static resources by forwarding them to the Servlet container's default servlet.
 * <p>This class is responsible for determining the appropriate default servlet name for various
 * application servers and dispatching requests for static content (e.g., CSS, JavaScript, images)
 * to that servlet, allowing Aspectran to focus on dynamic content generation.
 */
public class DefaultServletHttpRequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultServletHttpRequestHandler.class);

    /** Default Servlet name used by Undertow, Jetty, Tomcat, JBoss, and Glassfish */
    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    /** Default Servlet name used by Resin */
    private static final String RESIN_DEFAULT_SERVLET_NAME = "resin-file";

    /** Default Servlet name used by WebLogic */
    private static final String WEBLOGIC_DEFAULT_SERVLET_NAME = "FileServlet";

    /** Default Servlet name used by WebSphere */
    private static final String WEBSPHERE_DEFAULT_SERVLET_NAME = "SimpleFileServlet";

    /** Default Servlet name used by Google App Engine */
    private static final String GAE_DEFAULT_SERVLET_NAME = "_ah_default";

    /** Default Servlet name used by Jeus */
    private static final String JEUS_DEFAULT_SERVLET_NAME = "WorkerServlet";

    private final ServletContext servletContext;

    private final WebService webService;

    private String defaultServletName;

    /**
     * Constructs a new DefaultServletHttpRequestHandler.
     * @param servletContext the servlet context
     * @param webService the web service instance
     */
    public DefaultServletHttpRequestHandler(ServletContext servletContext, WebService webService) {
        this.servletContext = servletContext;
        this.webService = webService;
    }

    /**
     * Returns the name of the default servlet to which static resource requests are forwarded.
     * @return the default servlet name
     */
    public String getDefaultServletName() {
        return defaultServletName;
    }

    /**
     * Sets the name of the default Servlet to be forwarded to for static resource requests.
     * @param defaultServletName the new default servlet name
     */
    public void setDefaultServletName(String defaultServletName) {
        this.defaultServletName = defaultServletName;
    }

    /**
     * Attempts to automatically look up the name of the default servlet for the current container.
     * <p>This method checks for common default servlet names used by various application servers.
     * If a name is explicitly set to "none", lookup is skipped.
     */
    public void lookupDefaultServletName() {
        // To avoid default servlet lookup
        if ("none".equals(defaultServletName)) {
            defaultServletName = null;
            return;
        }
        if (defaultServletName != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Default servlet name: {}", defaultServletName);
            }
            return;
        }
        if (servletContext.getNamedDispatcher(COMMON_DEFAULT_SERVLET_NAME) != null) {
            defaultServletName = COMMON_DEFAULT_SERVLET_NAME;
        } else if (servletContext.getNamedDispatcher(RESIN_DEFAULT_SERVLET_NAME) != null) {
            defaultServletName = RESIN_DEFAULT_SERVLET_NAME;
        } else if (servletContext.getNamedDispatcher(WEBLOGIC_DEFAULT_SERVLET_NAME) != null) {
            defaultServletName = WEBLOGIC_DEFAULT_SERVLET_NAME;
        } else if (servletContext.getNamedDispatcher(WEBSPHERE_DEFAULT_SERVLET_NAME) != null) {
            defaultServletName = WEBSPHERE_DEFAULT_SERVLET_NAME;
        } else if (servletContext.getNamedDispatcher(GAE_DEFAULT_SERVLET_NAME) != null) {
            defaultServletName = GAE_DEFAULT_SERVLET_NAME;
        } else if (servletContext.getNamedDispatcher(JEUS_DEFAULT_SERVLET_NAME) != null) {
            defaultServletName = JEUS_DEFAULT_SERVLET_NAME;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to locate the default servlet for serving static content. " +
                    "Please set the 'web.defaultServletName' property explicitly.");
            }
        }
    }

    /**
     * Handles the request by dispatching it to the default servlet.
     * @param request the current HTTP servlet request
     * @param response the current HTTP servlet response
     * @return {@code true} if the request was handled (dispatched to default servlet), {@code false} otherwise
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    public boolean handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (defaultServletName != null) {
            ClassLoader origClassLoader = ThreadContextHelper.overrideClassLoader(webService.getServiceClassLoader());
            try {
                dispatch(request, response);
            } finally {
                ThreadContextHelper.restoreClassLoader(origClassLoader);
            }
            return true;
        } else {
            return false;
        }
    }

    private void dispatch(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher rd = servletContext.getNamedDispatcher(defaultServletName);
        if (rd == null) {
            throw new IllegalStateException("A RequestDispatcher could not be located for the default servlet '" +
                    defaultServletName + "'");
        }
        if (logger.isDebugEnabled()) {
            rd.forward(request, new ErrorLoggingHttpServletResponse(response));
        } else {
            rd.forward(request, response);
        }
    }

    private static class ErrorLoggingHttpServletResponse extends HttpServletResponseWrapper {

        public ErrorLoggingHttpServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            ToStringBuilder tsb = new ToStringBuilder("Response");
            tsb.append("code", sc);
            tsb.append("message", msg);
            logger.debug(tsb.toString());

            super.sendError(sc, msg);
        }

        /**
         * The default behavior of this method is to call sendError(int sc) on the wrapped response object.
         */
        @Override
        public void sendError(int sc) throws IOException {
            ToStringBuilder tsb = new ToStringBuilder("Response");
            tsb.append("code", sc);
            logger.debug(tsb.toString());

            super.sendError(sc);
        }

    }

}
