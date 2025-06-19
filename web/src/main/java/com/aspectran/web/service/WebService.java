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
 * The Interface WebService.
 *
 * <p>Created: 2017. 10. 28.</p>
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

    boolean isSessionAdaptable();

    /**
     * Executes web activity.
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @throws IOException If an error occurs during Activity execution
     */
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException;

    static void bind(ServletContext servletContext, WebService service) {
        Assert.notNull(servletContext, "servletContext must not be null");
        Assert.notNull(service, "service must not be null");
        servletContext.setAttribute(ROOT_WEB_SERVICE_ATTR_NAME, service);
    }

    static void bind(WebActivity activity, WebService service) {
        Assert.notNull(activity, "activity must not be null");
        Assert.notNull(service, "service must not be null");
        activity.getRequest().setAttribute(DERIVED_WEB_SERVICE_ATTR_NAME, service);
    }

    /**
     * Find the root web service from ServletContext.
     * @param servletContext ServletContext to find the root web service for
     * @return the root web service
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
     * Finds the root web service from the ServletContext and returns its ActivityContext.
     * @param servletContext ServletContext to find the root web service for
     * @return ActivityContext of root web service
     */
    @NonNull
    static ActivityContext findActivityContext(ServletContext servletContext) {
        return findWebService(servletContext).getActivityContext();
    }

    /**
     * Finds the derived web service from the ServletRequest and returns its ActivityContext.
     * @param servletRequest ServletRequest to find the derived web service for
     * @return ActivityContext of derived web service
     */
    static ActivityContext findActivityContext(ServletRequest servletRequest) {
        return findWebService(servletRequest).getActivityContext();
    }

    /**
     * Find the root web service from ServletContext.
     * @param servletContext ServletContext to find the root web service for
     * @param attrName the name of the ServletContext attribute to look for
     * @return the root web service
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
     * Find the derived web service from ServletRequest.
     * @param servletRequest ServletRequest to find the derived web service for
     * @param attrName the name of the ServletRequest attribute to look for
     * @return the derived web service
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
