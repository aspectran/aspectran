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
package com.aspectran.undertow.server.servlet;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.undertow.server.session.TowSessionManager;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.service.WebServiceClassLoader;
import io.undertow.server.HandlerWrapper;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import jakarta.servlet.ServletContainerInitializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents a servlet context that can be added to a deployment.
 *
 * <p>Created: 2019-08-05</p>
 */
public class TowServletContext extends DeploymentInfo implements ActivityContextAware {

    private static final Set<Class<?>> NO_CLASSES = Collections.emptySet();

    private ActivityContext context;

    private TowSessionManager sessionManager;

    /**
     * Instantiates a new Tow servlet context.
     */
    public TowServletContext() {
    }

    /**
     * Returns the activity context.
     * @return the activity context
     */
    @NonNull
    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
        ClassLoader webServiceClassLoader = new WebServiceClassLoader(context.getClassLoader());
        setClassLoader(webServiceClassLoader);
    }

    /**
     * Returns the application adapter.
     * @return the application adapter
     */
    @NonNull
    public ApplicationAdapter getApplicationAdapter() {
        return getActivityContext().getApplicationAdapter();
    }

    /**
     * Sets the scratch directory for this servlet context.
     * @param scratchDir the scratch directory
     * @throws IOException if an I/O error occurs
     */
    public void setScratchDir(String scratchDir) throws IOException {
        Path dir = getApplicationAdapter().getRealPath(scratchDir);
        Files.createDirectories(dir);
        if (!Files.isDirectory(dir) || !Files.isWritable(dir)) {
            throw new IOException("Could not create scratch directory: " + dir);
        }
        setTempDir(dir);
    }

    /**
     * Returns the session manager.
     * @return the session manager
     */
    public TowSessionManager getTowSessionManager() {
        return sessionManager;
    }

    /**
     * Sets the session manager for this servlet context.
     * @param towSessionManager the session manager
     */
    public void setSessionManager(TowSessionManager towSessionManager) {
        this.sessionManager = towSessionManager;
        setSessionManagerFactory(deployment -> {
            if (towSessionManager != null) {
                towSessionManager.setClassLoader(getClassLoader());
                try {
                    towSessionManager.initialize();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return towSessionManager;
        });
    }

    /**
     * Sets the init parameters for this servlet context.
     * @param initParams the init parameters
     */
    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                addInitParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Sets the listeners for this servlet context.
     * @param towListeners the listeners
     */
    public void setListeners(TowListener[] towListeners) {
        if (towListeners != null) {
            for (TowListener towListener : towListeners) {
                addListener(towListener);
            }
        }
    }

    /**
     * Sets the servlets for this servlet context.
     * @param towServlets the servlets
     */
    public void setServlets(TowServlet[] towServlets) {
        if (towServlets != null) {
            for (TowServlet towServlet : towServlets) {
                ServletInfo existingServlet = getServlets().get(towServlet.getName());
                if (getServlets().containsKey(towServlet.getName())) {
                    throw new IllegalArgumentException("Duplicate servlet name detected: " +
                            "Existing: " + existingServlet + "; This: " + towServlet + "; " +
                            "Each servlet added to the servlet context must have a unique name. " +
                            "Otherwise existing servlets will be ignored.");
                }
                addServlet(towServlet);
            }
        }
    }

    /**
     * Sets the filters for this servlet context.
     * @param towFilters the filters
     */
    public void setFilters(TowFilter[] towFilters) {
        if (towFilters != null) {
            for (TowFilter towFilter : towFilters) {
                addFilter(towFilter);
                if (towFilter.getUrlMappings() != null) {
                    for (TowFilterUrlMapping urlMapping : towFilter.getUrlMappings()) {
                        addFilterUrlMapping(urlMapping.getFilterName(), urlMapping.getMapping(),
                                urlMapping.getDispatcher());
                    }
                }
                if (towFilter.getServletMappings() != null) {
                    for (TowFilterServletMapping servletMapping : towFilter.getServletMappings()) {
                        addFilterServletNameMapping(servletMapping.getFilterName(), servletMapping.getMapping(),
                                servletMapping.getDispatcher());
                    }
                }
            }
        }
    }

    /**
     * Sets the servlet container initializers for this servlet context.
     * @param servletContainerInitializers the servlet container initializers
     */
    public void setServletContainerInitializers(ServletContainerInitializer[] servletContainerInitializers) {
        Assert.notNull(servletContainerInitializers, "servletContainerInitializers must not be null");
        for (ServletContainerInitializer initializer : servletContainerInitializers) {
            Class<? extends ServletContainerInitializer> servletContainerInitializerClass = initializer.getClass();
            InstanceFactory<? extends ServletContainerInitializer> instanceFactory = new ImmediateInstanceFactory<>(initializer);
            ServletContainerInitializerInfo sciInfo = new ServletContainerInitializerInfo(servletContainerInitializerClass,
                    instanceFactory, NO_CLASSES);
            addServletContainerInitializer(sciInfo);
        }
    }

    /**
     * Sets the web socket server container initializer for this servlet context.
     * @param webSocketServerContainerInitializer the web socket server container initializer
     */
    public void setWebSocketServerContainerInitializer(TowWebSocketServerContainerInitializer webSocketServerContainerInitializer) {
        if (webSocketServerContainerInitializer != null) {
            webSocketServerContainerInitializer.initialize(this);
        }
    }

    /**
     * Sets the welcome pages for this servlet context.
     * @param welcomePages the welcome pages
     */
    public void setWelcomePages(String[] welcomePages) {
        if (welcomePages != null) {
            addWelcomePages(welcomePages);
        }
    }

    /**
     * Sets the error pages for this servlet context.
     * @param errorPages the error pages
     */
    public void setErrorPages(TowErrorPage[] errorPages) {
        if (errorPages != null) {
            addErrorPages(errorPages);
        }
    }

    /**
     * Sets the initial handler chain wrappers for this servlet context.
     * @param wrappers the wrappers
     */
    public void setInitialHandlerChainWrappers(HandlerWrapper[] wrappers) {
        if (wrappers != null) {
            for (HandlerWrapper wrapper : wrappers) {
                addInitialHandlerChainWrapper(wrapper);
            }
        }
    }

    /**
     * Sets the inner handler chain wrappers for this servlet context.
     * @param wrappers the wrappers
     */
    public void setInnerHandlerChainWrappers(HandlerWrapper[] wrappers) {
        if (wrappers != null) {
            for (HandlerWrapper wrapper : wrappers) {
                addInnerHandlerChainWrapper(wrapper);
            }
        }
    }

    /**
     * Sets the outer handler chain wrappers for this servlet context.
     * @param wrappers the wrappers
     */
    public void setOuterHandlerChainWrappers(HandlerWrapper[] wrappers) {
        if (wrappers != null) {
            for (HandlerWrapper wrapper : wrappers) {
                addOuterHandlerChainWrapper(wrapper);
            }
        }
    }

}
