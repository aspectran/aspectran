/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.util.Assert;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.aspectran.web.service.WebService.ROOT_WEB_SERVICE_ATTR_NAME;
import static com.aspectran.web.service.WebService.STANDALONE_WEB_SERVICE_ATTR_PREFIX;

/**
 * <p>Created: 01/10/2019</p>
 */
public class WebServiceHolder {

    private static final Map<ClassLoader, WebService> webServicePerThread =
            new ConcurrentHashMap<>(1);

    @Nullable
    private static volatile WebService currentWebService;

    static void putWebService(WebService webService) {
        Assert.notNull(webService, "webService must not be null");
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl == WebServiceHolder.class.getClassLoader()) {
            currentWebService = webService;
        } else if (ccl != null) {
            webServicePerThread.put(ccl, webService);
        }
    }

    static void removeWebService(WebService webService) {
        Assert.notNull(webService, "webService must not be null");
        webServicePerThread.entrySet().removeIf(entry -> (webService.equals(entry.getValue())));
        if (currentWebService != null && currentWebService == webService) {
            currentWebService = null;
        }
    }

    public static WebService getCurrentWebService() {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl != null) {
            WebService webService = webServicePerThread.get(ccl);
            if (webService != null) {
                return webService;
            }
        }
        return currentWebService;
    }

    public static ActivityContext getCurrentActivityContext() {
        WebService webService = getCurrentWebService();
        return (webService != null ? webService.getActivityContext() : null);
    }

    /**
     * Find the root ActivityContext for this web aspectran service.
     *
     * @param servletContext ServletContext to find the web aspectran service for
     * @return the ActivityContext for this web aspectran service
     */
    public static ActivityContext getActivityContext(ServletContext servletContext) {
        ActivityContext activityContext = getActivityContext(servletContext, ROOT_WEB_SERVICE_ATTR_NAME);
        if (activityContext == null) {
            throw new IllegalStateException("No root DefaultWebService found; " +
                    "No WebServiceListener registered?");
        }
        return activityContext;
    }

    /**
     * Find the standalone ActivityContext for this web aspectran service.
     *
     * @param servlet the servlet
     * @return the ActivityContext for this web aspectran service
     */
    public static ActivityContext getActivityContext(HttpServlet servlet) {
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
     *
     * @param servletContext ServletContext to find the web aspectran service for
     * @param attrName the name of the ServletContext attribute to look for
     * @return the ActivityContext for this web aspectran service
     */
    private static ActivityContext getActivityContext(ServletContext servletContext, String attrName) {
        Object attr = servletContext.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (!(attr instanceof DefaultWebService)) {
            throw new IllegalStateException("Context attribute [" + attr + "] is not of type [" +
                    DefaultWebService.class.getName() + "]");
        }
        return ((CoreService)attr).getActivityContext();
    }

}
