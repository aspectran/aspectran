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
package com.aspectran.jetty.server;

import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.service.DefaultWebServiceBuilder;
import com.aspectran.web.service.WebService;
import jakarta.websocket.server.ServerContainer;
import org.eclipse.jetty.ee10.webapp.WebAppClassLoader;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.ee10.webapp.WebXmlConfiguration;
import org.eclipse.jetty.ee10.websocket.jakarta.server.JakartaWebSocketServerContainer;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.Resources;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * The Class JettyWebAppContext.
 *
 * <p>Created: 2017. 1. 27.</p>
 */
public class JettyWebAppContext extends WebAppContext implements ActivityContextAware {

    private static final Logger logger = LoggerFactory.getLogger(JettyWebAppContext.class);

    private ActivityContext context;

    private JettyWebSocketInitializer webSocketInitializer;

    @Override
    @AvoidAdvice
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
    }

    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                setInitParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void setWar(String war) {
        File warFile = null;
        try {
            warFile = new File(war);
            if (warFile.isDirectory()) {
                if (!warFile.exists() && !warFile.mkdirs()) {
                    throw new IOException("Unable to create war directory: " + warFile);
                }
                setExtractWAR(true);
            }
            super.setWar(warFile.getCanonicalPath());
        } catch (Exception e) {
            logger.error("Failed to establish Scratch directory: " + warFile, e);
        }
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
            super.setTempDirectory(tempDir.getCanonicalFile());
        } catch (Exception e) {
            logger.error("Failed to establish Scratch directory: " + tempDir, e);
        }
    }

    @Override
    public void setDefaultsDescriptor(String defaultsDescriptor) {
        if (StringUtils.hasLength(defaultsDescriptor)) {
            Resource dftResource;
            try {
                dftResource = getResourceFactory().newClassLoaderResource(defaultsDescriptor);
                if (Resources.missing(dftResource)) {
                    String pkg = WebXmlConfiguration.class.getPackageName().replace(".", "/") + "/";
                    if (defaultsDescriptor.startsWith(pkg)) {
                        URL url = WebXmlConfiguration.class.getResource(defaultsDescriptor.substring(pkg.length()));
                        if (url != null) {
                            URI uri = url.toURI();
                            dftResource = getResourceFactory().newResource(uri);
                        }
                    }
                    if (Resources.missing(dftResource)) {
                        dftResource = newResource(defaultsDescriptor);
                    }
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid default descriptor: " + defaultsDescriptor, e);
            }
            if (Resources.isReadableFile(dftResource)) {
                super.setDefaultsDescriptor(defaultsDescriptor);
            } else {
                throw new IllegalArgumentException("Unable to locate default descriptor: " + defaultsDescriptor);
            }
        }
    }

    public void setWebSocketInitializer(JettyWebSocketInitializer webSocketInitializer) {
        this.webSocketInitializer = webSocketInitializer;
    }

    void deferredInitialize(Server server) throws Exception {
        Assert.state(context != null, "No ActivityContext injected");

        WebAppClassLoader webAppClassLoader = new WebAppClassLoader(context.getClassLoader(), this);
        setClassLoader(webAppClassLoader);

        if (webSocketInitializer != null) {
            JakartaWebSocketServletContainerInitializer.configure(this,
                    (servletContext, jettyWebSocketServerContainer) -> {
                        ServerContainer serverContainer = JakartaWebSocketServerContainer.ensureContainer(servletContext);
                        webSocketInitializer.customize(serverContainer);
                    });
        }

        // Create a root web service
        CoreService masterService = context.getMasterService();
        WebService rootWebService = DefaultWebServiceBuilder.build(getServletContext(), masterService, webAppClassLoader);
        if (rootWebService.isOrphan()) {
            server.addEventListener(new LifeCycle.Listener() {
                @Override
                public void lifeCycleStarted(LifeCycle event) {
                    try {
                        rootWebService.getServiceLifeCycle().start();
                    } catch (Exception e) {
                        logger.error("Failed to start root web service", e);
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }

}
