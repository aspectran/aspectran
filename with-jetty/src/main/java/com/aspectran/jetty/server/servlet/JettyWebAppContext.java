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
package com.aspectran.jetty.server.servlet;

import com.aspectran.core.component.bean.annotation.AvoidAdvice;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.Assert;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.service.DefaultWebService;
import com.aspectran.web.service.DefaultWebServiceBuilder;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.websocket.server.ServerContainer;
import org.eclipse.jetty.ee10.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.ee10.servlet.ServletMapping;
import org.eclipse.jetty.ee10.webapp.WebAppClassLoader;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.ee10.webapp.WebXmlConfiguration;
import org.eclipse.jetty.ee10.websocket.jakarta.server.JakartaWebSocketServerContainer;
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.Resources;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import static com.aspectran.utils.ResourceUtils.CLASSPATH_URL_PREFIX;

/**
 * The Class JettyWebAppContext.
 *
 * <p>Created: 2017. 1. 27.</p>
 */
public class JettyWebAppContext extends WebAppContext implements ActivityContextAware {

    private static final Logger logger = LoggerFactory.getLogger(JettyWebAppContext.class);

    private ActivityContext context;

    private List<JettyErrorPage> errorPages;

    private JettyWebSocketServerContainerInitializer webSocketServerContainerInitializer;

    private DefaultWebService rootWebService;

    @NonNull
    private ActivityContext getActivityContext() {
        Assert.state(context != null, "No ActivityContext injected");
        return context;
    }

    @Override
    @AvoidAdvice
    public void setActivityContext(@NonNull ActivityContext context) {
        this.context = context;
    }

    @Override
    public void setWar(String war) {
        try {
            Path path = getActivityContext().getApplicationAdapter().getRealPath(war);
            if (Files.isDirectory(path)) {
                Files.createDirectories(path);
                if (!Files.exists(path)) {
                    throw new IOException("Could not create WAR directory: " + path);
                }
            }
            super.setWar(path.toString());
        } catch (Exception e) {
            logger.error("Failed to establish WAR for webapp: " + war, e);
        }
    }

    public void setTempDirectory(String tempDirectory) {
        try {
            Path path = getActivityContext().getApplicationAdapter().getRealPath(tempDirectory);
            Files.createDirectories(path);
            if (!Files.isDirectory(path) || !Files.isWritable(path)) {
                throw new IOException("Could not create scratch directory: " + path);
            }
            super.setTempDirectory(path.toFile());
        } catch (Exception e) {
            logger.error("Failed to establish scratch directory: " + tempDirectory, e);
        }
    }

    @Override
    public void setDefaultsDescriptor(String defaultsDescriptor) {
        Assert.notNull(defaultsDescriptor, "defaultsDescriptor must not be null");
        String path = defaultsDescriptor;
        Resource resource;
        if (path.startsWith(CLASSPATH_URL_PREFIX)) {
            path = path.substring(CLASSPATH_URL_PREFIX.length());
            try {
                resource = getResourceFactory().newClassLoaderResource(path);
                if (Resources.missing(resource)) {
                    String pkg = WebXmlConfiguration.class.getPackageName().replace(".", "/") + "/";
                    if (path.startsWith(pkg)) {
                        URL url = WebXmlConfiguration.class.getResource(path.substring(pkg.length()));
                        if (url != null) {
                            URI uri = url.toURI();
                            resource = getResourceFactory().newResource(uri);
                        }
                    }
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid default descriptor: " + defaultsDescriptor, e);
            }
        } else {
            path = getActivityContext().getApplicationAdapter().getRealPath(path).toString();
            resource = newResource(path);
        }
        if (Resources.isReadableFile(resource)) {
            super.setDefaultsDescriptor(path);
        } else {
            throw new IllegalArgumentException("Unable to locate default descriptor: " + defaultsDescriptor);
        }
    }

    public void setInitParams(Map<String, String> initParams) {
        if (initParams != null) {
            for (Map.Entry<String, String> entry : initParams.entrySet()) {
                setInitParameter(entry.getKey(), entry.getValue());
            }
        }
    }

    public void setListeners(JettyListener[] jettyListeners) {
        if (jettyListeners != null) {
            for (JettyListener jettyListener : jettyListeners) {
                getServletHandler().addListener(jettyListener);
            }
        }
    }

    /**
     * Any event listeners added as WebListener must implement one or more of the
     * {@link jakarta.servlet.ServletContextListener}, {@link jakarta.servlet.ServletContextAttributeListener},
     * {@link jakarta.servlet.ServletRequestListener}, {@link jakarta.servlet.ServletRequestAttributeListener},
     * {@link jakarta.servlet.http.HttpSessionListener}, or {@link jakarta.servlet.http.HttpSessionAttributeListener}, or
     * {@link jakarta.servlet.http.HttpSessionIdListener} interfaces.
     */
    public void setWebListeners(EventListener[] eventListeners) {
        if (eventListeners != null) {
            for (EventListener eventListener : eventListeners) {
                addEventListener(eventListener);
            }
        }
    }

    public void setServlets(JettyServlet[] servlets) {
        if (servlets != null) {
            for (JettyServlet servlet : servlets) {
                getServletHandler().addServlet(servlet);
                ServletMapping mapping = new ServletMapping();
                mapping.setServletName(servlet.getName());
                mapping.setPathSpecs(servlet.getMappings());
                getServletHandler().addServletMapping(mapping);
            }
        }
    }

    public void setFilters(JettyFilter[] jettyFilters) {
        if (jettyFilters != null) {
            for (JettyFilter jettyFilter : jettyFilters) {
                getServletHandler().addFilter(jettyFilter);
                if (jettyFilter.getUrlMappings() != null) {
                    for (JettyFilterUrlMapping urlMapping : jettyFilter.getUrlMappings()) {
                        getServletHandler().addFilterMapping(urlMapping);
                    }
                }
                if (jettyFilter.getServletMappings() != null) {
                    for (JettyFilterServletMapping servletMapping : jettyFilter.getServletMappings()) {
                        getServletHandler().addFilterMapping(servletMapping);
                    }
                }
            }
        }
    }

    public void setErrorPages(JettyErrorPage[] errorPages) {
        if (errorPages != null && errorPages.length > 0) {
            this.errorPages = Arrays.asList(errorPages);
        } else {
            this.errorPages = null;
        }
    }

    public void setServletContainerInitializers(ServletContainerInitializer[] servletContainerInitializers) {
        Assert.notNull(servletContainerInitializers, "servletContainerInitializers must not be null");
        for (ServletContainerInitializer initializer : servletContainerInitializers) {
            addServletContainerInitializer(initializer);
        }
    }

    public void setWebSocketServerContainerInitializer(JettyWebSocketServerContainerInitializer webSocketServerContainerInitializer) {
        this.webSocketServerContainerInitializer = webSocketServerContainerInitializer;
    }

    public void deferredInitialize(Server server) {
        WebAppClassLoader webAppClassLoader = new WebAppClassLoader(getActivityContext().getClassLoader(), this);
        setClassLoader(webAppClassLoader);

        if (errorPages != null) {
            ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
            for (JettyErrorPage errorPage : errorPages) {
                if (StringUtils.hasText(errorPage.getLocation())) {
                    if (errorPage.getErrorCode() != null) {
                        if (errorPage.getToErrorCode() != null) {
                            errorHandler.addErrorPage(errorPage.getErrorCode(), errorPage.getToErrorCode(), errorPage.getLocation());
                        } else {
                            errorHandler.addErrorPage(errorPage.getErrorCode(), errorPage.getLocation());
                        }
                    } else if (errorPage.getExceptionType() != null) {
                        errorHandler.addErrorPage(errorPage.getExceptionType(), errorPage.getLocation());
                    }
                }
            }
            setErrorHandler(errorHandler);
        }

        if (webSocketServerContainerInitializer != null) {
            JakartaWebSocketServletContainerInitializer.configure(this,
                    (servletContext, serverContainer)
                        -> webSocketServerContainerInitializer.customize(serverContainer));
        }

        // Create a root web service
        CoreService masterService = getActivityContext().getMasterService();
        rootWebService = DefaultWebServiceBuilder.build(getServletContext(), masterService);
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

    public void deferredDispose() {
        if (rootWebService != null) {
            if (rootWebService.isActive()) {
                rootWebService.stop();
            }
            rootWebService.withdraw();
            rootWebService = null;
        }
    }

}
