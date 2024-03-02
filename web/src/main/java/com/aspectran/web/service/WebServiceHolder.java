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
import com.aspectran.core.context.resource.SiblingsClassLoader;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.aspectran.web.service.WebService.ROOT_WEB_SERVICE_ATTR_NAME;
import static com.aspectran.web.service.WebService.STANDALONE_WEB_SERVICE_ATTR_PREFIX;

/**
 * <p>Created: 01/10/2019</p>
 */
public class WebServiceHolder {

    private static final Map<ClassLoader, WebService> webServicePerThread =
            new ConcurrentHashMap<>();

    private static final Map<Class<?>, WebService> webServicePerClass =
            new ConcurrentHashMap<>();

    private static volatile WebService currentWebService;

    static void hold(WebService webService) {
        Assert.notNull(webService, "webService must not be null");
        Assert.state(webService.getActivityContext() != null, "No ActivityContext");
        ClassLoader classLoader = webService.getServiceClassLoader();
        if (classLoader != null) {
            if (classLoader == WebServiceHolder.class.getClassLoader()) {
                currentWebService = webService;
            } else {
                webServicePerThread.put(classLoader, webService);
            }
            if (webService.getAltClassLoader() != null) {
                hold(webService, webService.getAltClassLoader());
            }
        }
    }

    public static void hold(WebService webService, ClassLoader classLoader) {
        Assert.notNull(webService, "webService must not be null");
        Assert.notNull(classLoader, "classLoader must not be null");
        Assert.state(currentWebService == webService || webServicePerThread.containsValue(webService),
            "Unregistered web service: " + webService);
        webServicePerThread.put(classLoader, webService);
    }

    public static void hold(WebService webService, Class<?> clazz) {
        Assert.notNull(webService, "webService must not be null");
        Assert.notNull(clazz, "clazz must not be null");
        Assert.state(currentWebService == webService || webServicePerThread.containsValue(webService),
            "Unregistered web service: " + webService);
        webServicePerClass.put(clazz, webService);
    }

    static void release(WebService webService) {
        Assert.notNull(webService, "webService must not be null");
        webServicePerThread.entrySet().removeIf(entry -> (webService.equals(entry.getValue())));
        webServicePerClass.entrySet().removeIf(entry -> (webService.equals(entry.getValue())));
        if (currentWebService != null && currentWebService == webService) {
            currentWebService = null;
        }
    }

    public static WebService acquire() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            WebService webService = webServicePerThread.get(classLoader);
            if (webService == null && !(classLoader instanceof SiblingsClassLoader)) {
                webService = webServicePerThread.get(classLoader.getParent());
            }
            if (webService != null) {
                return webService;
            }
        }
        return currentWebService;
    }

    public static WebService acquire(Class<?> clazz) {
        WebService webService = webServicePerClass.get(clazz);
        if (webService == null) {
            webService = acquire();
        }
        return webService;
    }

    @Nullable
    public static ActivityContext getActivityContext() {
        WebService webService = acquire();
        return (webService != null ? webService.getActivityContext() : null);
    }

    @Nullable
    public static ActivityContext getActivityContext(Class<?> clazz) {
        WebService webService = acquire(clazz);
        return (webService != null ? webService.getActivityContext() : null);
    }

    /**
     * Find the root ActivityContext for this web aspectran service.
     * @param servletContext ServletContext to find the web aspectran service for
     * @return the ActivityContext for this web aspectran service
     */
    @NonNull
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
     * @param servlet the servlet
     * @return the ActivityContext for this web aspectran service
     */
    @NonNull
    public static ActivityContext getActivityContext(HttpServlet servlet) {
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
