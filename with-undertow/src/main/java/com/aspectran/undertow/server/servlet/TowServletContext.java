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
package com.aspectran.undertow.server.servlet;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.undertow.server.session.TowSessionManager;
import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.DefaultWebServiceBuilder;
import com.aspectran.web.service.WebServiceClassLoader;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowServletContext extends DeploymentInfo implements ActivityContextAware {

    private static final Set<Class<?>> NO_CLASSES = Collections.emptySet();

    private ActivityContext context;

    private SessionManager sessionManager;

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
        File dir = getApplicationAdapter().toRealPathAsFile(scratchDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        setTempDir(getApplicationAdapter().toRealPathAsFile(scratchDir));
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        setSessionManagerFactory(deployment -> {
            if (sessionManager instanceof TowSessionManager towSessionManager) {
                towSessionManager.setClassLoader(getClassLoader());
                try {
                    towSessionManager.initialize();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return sessionManager;
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

    public void setWebSocketInitializer(TowWebSocketInitializer webSocketInitializer) {
        if (webSocketInitializer != null) {
            webSocketInitializer.initialize(this);
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

    void createRootWebService(ServletContext servletContext) throws Exception {
        CoreService masterService = getActivityContext().getMasterService();
        DefaultWebService rootWebService = DefaultWebServiceBuilder.build(servletContext, masterService);
        if (rootWebService.isOrphan()) {
            rootWebService.getServiceLifeCycle().start();
        }
    }

}
