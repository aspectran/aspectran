/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
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
     * The prefix of the ServletContext property name used to get the standalone WebService object.
     */
    String STANDALONE_WEB_SERVICE_ATTR_PREFIX = WebService.class.getName() + ".STANDALONE:";

    /**
     * Returns a reference to the {@link ServletContext} in which this WebService is running.
     * @return a {@link ServletContext} object, used by this WebService to interact with
     *      its servlet container
     */
    ServletContext getServletContext();

    /**
     * Executes web activity.
     * @param request current HTTP servlet request
     * @param response current HTTP servlet response
     * @throws IOException If an error occurs during Activity execution
     */
    void service(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * Find the root ActivityContext for this web aspectran service.
     * @param servletContext ServletContext to find the web aspectran service for
     * @return the ActivityContext for this web aspectran service
     */
    @NonNull
    static ActivityContext getActivityContext(ServletContext servletContext) {
        ActivityContext activityContext = getActivityContext(servletContext, ROOT_WEB_SERVICE_ATTR_NAME);
        if (activityContext == null) {
            throw new IllegalStateException("No root DefaultWebService found; " +
                "No WebServiceListener registered?");
        }
        return activityContext;
    }

    /**
     * Find the standalone ActivityContext for this web aspectran service.
     * @param servlet the servlet
     * @return the ActivityContext for this web aspectran service
     */
    @NonNull
    static ActivityContext getActivityContext(HttpServlet servlet) {
        Assert.notNull(servlet, "servlet must not be null");
        ServletContext servletContext = servlet.getServletContext();
        String attrName = STANDALONE_WEB_SERVICE_ATTR_PREFIX + servlet.getServletName();
        ActivityContext activityContext = getActivityContext(servletContext, attrName);
        if (activityContext != null) {
            return activityContext;
        } else {
            return getActivityContext(servletContext);
        }
    }

    /**
     * Find the ActivityContext for this web aspectran service.
     * @param servletContext ServletContext to find the web aspectran service for
     * @param attrName the name of the ServletContext attribute to look for
     * @return the ActivityContext for this web aspectran service
     */
    @Nullable
    private static ActivityContext getActivityContext(@NonNull ServletContext servletContext, String attrName) {
        Object attr = servletContext.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (!(attr instanceof DefaultWebService defaultWebService)) {
            throw new IllegalStateException("Context attribute [" + attr + "] is not of type [" +
                DefaultWebService.class.getName() + "]");
        }
        return defaultWebService.getActivityContext();
    }

}
