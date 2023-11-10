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
package com.aspectran.jetty;

import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.util.Assert;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.WebService;
import com.aspectran.websocket.jsr356.ServerEndpointExporter;
import jakarta.websocket.server.ServerContainer;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.websocket.jakarta.server.internal.JakartaWebSocketServerContainer;

import java.io.File;
import java.io.IOException;

/**
 * The Class JettyWebAppContext.
 *
 * <p>Created: 2017. 1. 27.</p>
 */
public class JettyWebAppContext extends WebAppContext implements ActivityContextAware, InitializableBean {

    private static final Logger logger = LoggerFactory.getLogger(JettyWebAppContext.class);

    private ActivityContext context;

    private JettyWebSocketInitializer webSocketInitializer;

    private boolean webServiceDerived;

    @Override
    @AvoidAdvice
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public void setWebSocketInitializer(JettyWebSocketInitializer webSocketInitializer) {
        this.webSocketInitializer = webSocketInitializer;
    }

    /**
     * Specifies whether to use a web service derived from the root web service.
     */
    public void setWebServiceDerived(boolean webServiceDerived) {
        this.webServiceDerived = webServiceDerived;
    }

    public void setTempDirectory(String tempDirectory) {
        File tempDir = null;
        try {
            tempDir = new File(tempDirectory);
            if (!tempDir.exists()) {
                if (!tempDir.mkdirs()) {
                    throw new IOException("Unable to create scratch directory: " + tempDir);
                }
            }
            super.setTempDirectory(tempDir);
        } catch (Exception e) {
            logger.error("Failed to establish Scratch directory: " + tempDir, e);
        }
    }

    @Override
    public void initialize() throws Exception {
        Assert.state(context != null, "No ActivityContext injected");

        if (webServiceDerived) {
            CoreService rootService = context.getRootService();
            WebService webService = DefaultWebService.create(getServletContext(), rootService);
            setAttribute(WebService.ROOT_WEB_SERVICE_ATTR_NAME, webService);
        }

        ClassLoader parent = context.getApplicationAdapter().getClassLoader();
        WebAppClassLoader webAppClassLoader = new WebAppClassLoader(parent, this);
        setClassLoader(webAppClassLoader);

        if (webSocketInitializer != null) {
            JakartaWebSocketServletContainerInitializer.configure(this,
                    (servletContext, jettyWebSocketServerContainer) -> {
                ServerContainer serverContainer = JakartaWebSocketServerContainer.ensureContainer(servletContext);
                webSocketInitializer.customize(serverContainer);
                ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter(context);
                serverEndpointExporter.setServerContainer(serverContainer);
                serverEndpointExporter.registerEndpoints();
            });
        }
    }

}
