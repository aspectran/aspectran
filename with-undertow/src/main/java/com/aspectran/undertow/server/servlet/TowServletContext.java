/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManager;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ErrorPage;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.ServletContainerInitializerInfo;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;

import javax.servlet.ServletContainerInitializer;
import javax.websocket.CloseReason;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>Created: 2019-08-05</p>
 */
public class TowServletContext extends DeploymentInfo implements ApplicationAdapterAware {

    public static final String DERIVED_WEB_SERVICE_ATTR = TowServletContext.class.getName() + ".DERIVED_WEB_SERVICE";

    private static final String WEBSOCKET_CURRENT_CONNECTIONS_ATTR = "io.undertow.websocket.current-connections";

    private static final Set<Class<?>> NO_CLASSES = Collections.emptySet();

    private ApplicationAdapter applicationAdapter;

    private SessionManager sessionManager;

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
        setClassLoader(applicationAdapter.getClassLoader());
    }

    public void setScratchDir(String scratchDir) throws IOException {
        File dir = applicationAdapter.toRealPathAsFile(scratchDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        setTempDir(applicationAdapter.toRealPathAsFile(scratchDir));
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        setSessionManagerFactory(deployment -> sessionManager);
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
                addServlet(towServlet);
            }
        }
    }

    public void setFilters(TowFilter[] towFilters) {
        if (towFilters != null) {
            for (TowFilter towFilter : towFilters) {
                addFilter(towFilter);
            }
        }
    }

    public void setFilterUrlMappings(TowFilterUrlMapping[] towFilterUrlMappings) {
        if (towFilterUrlMappings != null) {
            for (TowFilterUrlMapping filterUrlMapping : towFilterUrlMappings) {
                addFilterUrlMapping(filterUrlMapping.getFilterName(), filterUrlMapping.getMapping(),
                        filterUrlMapping.getDispatcher());
            }
        }
    }

    public void setFilterServletMappings(TowFilterServletMapping[] towFilterServletMappings) {
        if (towFilterServletMappings != null) {
            for (TowFilterServletMapping filterServletMapping : towFilterServletMappings) {
                addFilterServletNameMapping(filterServletMapping.getFilterName(), filterServletMapping.getMapping(),
                        filterServletMapping.getDispatcher());
            }
        }
    }

    public void setServletContainerInitializers(ServletContainerInitializer[] servletContainerInitializers) {
        for (ServletContainerInitializer initializer : servletContainerInitializers) {
            Class<? extends ServletContainerInitializer> servletContainerInitializerClass = initializer.getClass();
            InstanceFactory<? extends ServletContainerInitializer> instanceFactory = new ImmediateInstanceFactory<>(initializer);
            ServletContainerInitializerInfo sciInfo = new ServletContainerInitializerInfo(servletContainerInitializerClass,
                    instanceFactory, NO_CLASSES);
            addServletContainerInitializer(sciInfo);
        }
    }

    public void setWelcomePages(String[] welcomePages) {
        if (welcomePages != null) {
            addWelcomePages(welcomePages);
        }
    }

    public void setErrorPages(ErrorPage[] errorPages) {
        if (errorPages != null) {
            addErrorPages(errorPages);
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

    public void setWebSocketInitializer(TowWebSocketInitializer webSocketInitializer) {
        webSocketInitializer.initialize(this);
    }

    /**
     * Specifies whether this is a derived web service that inherits the root web service.
     */
    public void setDerived(boolean derived) {
        if (derived) {
            addServletContextAttribute(DERIVED_WEB_SERVICE_ATTR, "true");
        } else {
            getServletContextAttributes().remove(DERIVED_WEB_SERVICE_ATTR);
        }
    }

    public static class WebSocketConnectionsUnboundListener implements SessionListener {

        @Override
        public void attributeUpdated(Session session, String name, Object newValue, Object oldValue) {
            if (oldValue != null && oldValue != newValue) {
                closeWebSockets(name, oldValue);
            }
        }

        @Override
        public void attributeRemoved(Session session, String name, Object oldValue) {
            closeWebSockets(name, oldValue);
        }

        private void closeWebSockets(String name, Object value) {
            if (WEBSOCKET_CURRENT_CONNECTIONS_ATTR.equals(name)) {
                @SuppressWarnings("unchecked")
                List<WebSocketChannel> connections = (List<WebSocketChannel>)value;
                for (WebSocketChannel c : new ArrayList<>(connections)) {
                    WebSockets.sendClose(CloseReason.CloseCodes.VIOLATED_POLICY.getCode(), "", c, null);
                }
            }
        }

    }

}
