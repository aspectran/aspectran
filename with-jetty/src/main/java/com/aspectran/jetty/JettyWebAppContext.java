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
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.SimpleInstanceManager;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.server.ServerContainer;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * The Class JettyWebAppContext.
 *
 * <p>Created: 2017. 1. 27.</p>
 */
public class JettyWebAppContext extends WebAppContext implements ActivityContextAware, InitializableBean {

    private static final Logger logger = LoggerFactory.getLogger(JettyWebAppContext.class);

    private ActivityContext context;

    private boolean webSocketEnabled;

    private boolean derived;

    @Override
    @AvoidAdvice
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public void setWebSocketEnabled(boolean webSocketEnabled) {
        this.webSocketEnabled = webSocketEnabled;
    }

    /**
     * Specifies whether this is a derived web service that inherits the root web service.
     */
    public void setDerived(boolean derived) {
        this.derived = derived;
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

        if (derived) {
            CoreService rootService = context.getRootService();
            WebService webService = DefaultWebService.create(getServletContext(), rootService);
            setAttribute(WebService.ROOT_WEB_SERVICE_ATTR_NAME, webService);
        }

        ClassLoader parent = context.getApplicationAdapter().getClassLoader();
        WebAppClassLoader webAppClassLoader = new WebAppClassLoader(parent, this);
        setClassLoader(webAppClassLoader);

        /*
         * Configure the application to support the compilation of JSP files.
         * We need a new class loader and some stuff so that Jetty can call the
         * onStartup() methods as required.
         */
        setAttribute("org.eclipse.jetty.containerInitializers", jspInitializers());
        setAttribute(InstanceManager.class.getName(), new SimpleInstanceManager());
        addBean(new ServletContainerInitializersStarter(this), true);

        if (webSocketEnabled) {
            ServerContainer serverContainer = WebSocketServerContainerInitializer.initialize(this);
            ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter(context);
            serverEndpointExporter.setServerContainer(serverContainer);
            serverEndpointExporter.registerEndpoints();
        }
    }

    private List<ContainerInitializer> jspInitializers() {
        JettyJasperInitializer sci = new JettyJasperInitializer();
        ContainerInitializer initializer = new ContainerInitializer(sci, null);
        return Collections.singletonList(initializer);
    }

}
