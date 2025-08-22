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

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.web.activity.WebActivity;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * The main interface for the Aspectran Web service.
 * <p>This service specializes the core Aspectran service for a Java Servlet-based
 * web environment. It acts as the bridge between the generic Aspectran core and
 * the specific world of {@link jakarta.servlet.http.HttpServletRequest},
 * {@link jakarta.servlet.http.HttpServletResponse}, and {@link jakarta.servlet.ServletContext}.
 *
 * @since 2.0.0
 */
public interface WebService extends CoreService {

    /**
     * ServletContext attribute name used to obtain the root WebService object.
     */
    String ROOT_WEB_SERVICE_ATTR_NAME = WebService.class.getName() + ".ROOT";

    /**
     * ServletRequest attribute name used to obtain the derived WebService object.
     */
    String DERIVED_WEB_SERVICE_ATTR_NAME = WebService.class.getName() + ".DERIVED";

    /**
     * Returns a reference to the {@link ServletContext} in which this WebService is running.
     * @return a {@link ServletContext} object, used by this WebService to interact with
     *      its servlet container
     */
    ServletContext getServletContext();

    /**
     * Returns whether session adaptation is enabled for this web service.
     * @return {@code true} if session adaptation is enabled, {@code false} otherwise
     */
    boolean isSessionAdaptable();

    /**
     * Processes an incoming HTTP request and generates a response.
     * This is the main entry point for handling web requests in Aspectran.
     * @param request the current HTTP servlet request
     * @param response the current HTTP servlet response
     * @throws IOException if an input or output error occurs while handling the request
     */
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * Binds a {@link WebService} instance to the {@link ServletContext}.
     * This makes the service globally accessible within the web application.
     * @param servletContext the servlet context to bind to
     * @param service the web service instance to bind
     */
    static void bind(ServletContext servletContext, WebService service) {
        Assert.notNull(servletContext, "servletContext must not be null");
        Assert.notNull(service, "service must not be null");
        servletContext.setAttribute(ROOT_WEB_SERVICE_ATTR_NAME, service);
    }

    /**
     * Binds a {@link WebService} instance to a {@link WebActivity}'s request attributes.
     * This is typically used for derived services within a specific request scope.
     * @param activity the web activity to bind to
     * @param service the web service instance to bind
     */
    static void bind(WebActivity activity, WebService service) {
        Assert.notNull(activity, "activity must not be null");
        Assert.notNull(service, "service must not be null");
        activity.getRequest().setAttribute(DERIVED_WEB_SERVICE_ATTR_NAME, service);
    }

    /**
     * Finds the root {@link DefaultWebService} from the {@link ServletContext}.
     * @param servletContext the servlet context to search within
     * @return the root web service
     * @throws IllegalStateException if no WebService is found
     */
    @NonNull
    static DefaultWebService findWebService(ServletContext servletContext) {
        Assert.notNull(servletContext, "servletContext must not be null");
        DefaultWebService webService = findWebService(servletContext, ROOT_WEB_SERVICE_ATTR_NAME);
        if (webService == null) {
            throw new IllegalStateException("No WebService found");
        }
        return webService;
    }

    /**
     * Finds the {@link DefaultWebService} associated with the given {@link ServletRequest}.
     * It first checks for a derived service, then falls back to the root service.
     * @param servletRequest the servlet request to search within
     * @return the found web service
     * @throws IllegalStateException if no WebService is found
     */
    @NonNull
    static DefaultWebService findWebService(ServletRequest servletRequest) {
        Assert.notNull(servletRequest, "servletRequest must not be null");
        DefaultWebService webService = findWebService(servletRequest, DERIVED_WEB_SERVICE_ATTR_NAME);
        if (webService == null) {
            webService = findWebService(servletRequest.getServletContext(), ROOT_WEB_SERVICE_ATTR_NAME);
        }
        if (webService == null) {
            throw new IllegalStateException("No WebService found");
        }
        return webService;
    }

    /**
     * Finds the {@link ActivityContext} associated with the root web service from the {@link ServletContext}.
     * @param servletContext the servlet context to search within
     * @return the {@link ActivityContext} of the root web service
     */
    @NonNull
    static ActivityContext findActivityContext(ServletContext servletContext) {
        return findWebService(servletContext).getActivityContext();
    }

    /**
     * Finds the {@link ActivityContext} associated with the web service linked to the given {@link ServletRequest}.
     * @param servletRequest the servlet request to search within
     * @return the {@link ActivityContext} of the web service
     */
    static ActivityContext findActivityContext(ServletRequest servletRequest) {
        return findWebService(servletRequest).getActivityContext();
    }

    /**
     * Internal helper method to find a {@link DefaultWebService} from a {@link ServletContext} attribute.
     * @param servletContext the servlet context to search within
     * @param attrName the name of the {@link ServletContext} attribute to look for
     * @return the found {@link DefaultWebService}, or {@code null} if not found or not of the correct type
     */
    @Nullable
    private static DefaultWebService findWebService(@NonNull ServletContext servletContext, String attrName) {
        Object attr = servletContext.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (!(attr instanceof DefaultWebService webService)) {
            throw new IllegalStateException("Context attribute [" + attr + "] is not of type [" +
                    DefaultWebService.class.getName() + "]");
        }
        return webService;
    }

    /**
     * Internal helper method to find a {@link DefaultWebService} from a {@link ServletRequest} attribute.
     * @param servletRequest the servlet request to search within
     * @param attrName the name of the {@link ServletRequest} attribute to look for
     * @return the found {@link DefaultWebService}, or {@code null} if not found or not of the correct type
     */
    @Nullable
    private static DefaultWebService findWebService(@NonNull ServletRequest servletRequest, String attrName) {
        Object attr = servletRequest.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (!(attr instanceof DefaultWebService webService)) {
            throw new IllegalStateException("Context attribute [" + attr + "] is not of type [" +
                    DefaultWebService.class.getName() + "]");
        }
        return webService;
    }

}
