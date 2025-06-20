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
 * The Class DefaultServletHttpRequestHandler.
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

    public DefaultServletHttpRequestHandler(ServletContext servletContext, WebService webService) {
        this.servletContext = servletContext;
        this.webService = webService;
    }

    /**
     * Gets the default servlet name.
     * @return the default servlet name
     */
    public String getDefaultServletName() {
        return defaultServletName;
    }

    /**
     * Set the name of the default Servlet to be forwarded to for static resource requests.
     * @param defaultServletName the new default servlet name
     */
    public void setDefaultServletName(String defaultServletName) {
        this.defaultServletName = defaultServletName;
    }

    /**
     * Lookup default servlet name.
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
     * Process the actual dispatching.
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @return true, if successful
     * @throws ServletException the servlet exception
     * @throws IOException if an input or output error occurs while the servlet is handling the HTTP request
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
