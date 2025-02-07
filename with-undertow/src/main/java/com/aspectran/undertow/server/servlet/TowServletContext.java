/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
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
 * <p>Created: 2019-08-05</p>
 */
public class TowServletContext extends DeploymentInfo implements ActivityContextAware {

    private static final Set<Class<?>> NO_CLASSES = Collections.emptySet();

    private ActivityContext context;

    private TowSessionManager sessionManager;

    public TowServletContext() {
    }

    @NonNull
    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    @AvoidAdvice
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
        ClassLoader webServiceClassLoader = new WebServiceClassLoader(context.getClassLoader());
        setClassLoader(webServiceClassLoader);
    }

    @NonNull
    public ApplicationAdapter getApplicationAdapter() {
        return getActivityContext().getApplicationAdapter();
    }

    public void setScratchDir(String scratchDir) throws IOException {
        Path dir = getApplicationAdapter().getRealPath(scratchDir);
        Files.createDirectories(dir);
        if (!Files.isDirectory(dir) || !Files.isWritable(dir)) {
            throw new IOException("Could not create scratch directory: " + dir);
        }
        setTempDir(dir);
    }

    public TowSessionManager getTowSessionManager() {
        return sessionManager;
    }

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

    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                addInitParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setListeners(TowListener[] towListeners) {
        if (towListeners != null) {
            for (TowListener towListener : towListeners) {
                addListener(towListener);
            }
        }
    }

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

    public void setWebSocketServerContainerInitializer(TowWebSocketServerContainerInitializer webSocketServerContainerInitializer) {
        if (webSocketServerContainerInitializer != null) {
            webSocketServerContainerInitializer.initialize(this);
        }
    }

    public void setWelcomePages(String[] welcomePages) {
        if (welcomePages != null) {
            addWelcomePages(welcomePages);
        }
    }

    public void setErrorPages(TowErrorPage[] errorPages) {
        if (errorPages != null) {
            addErrorPages(errorPages);
        }
    }

    public void setInitialHandlerChainWrappers(HandlerWrapper[] wrappers) {
        if (wrappers != null) {
            for (HandlerWrapper wrapper : wrappers) {
                addInitialHandlerChainWrapper(wrapper);
            }
        }
    }

    public void setInnerHandlerChainWrappers(HandlerWrapper[] wrappers) {
        if (wrappers != null) {
            for (HandlerWrapper wrapper : wrappers) {
                addInnerHandlerChainWrapper(wrapper);
            }
        }
    }

    public void setOuterHandlerChainWrappers(HandlerWrapper[] wrappers) {
        if (wrappers != null) {
            for (HandlerWrapper wrapper : wrappers) {
                addOuterHandlerChainWrapper(wrapper);
            }
        }
    }

}
