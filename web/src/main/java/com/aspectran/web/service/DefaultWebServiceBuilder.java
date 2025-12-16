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
package com.aspectran.web.service;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.web.servlet.WebActivityServlet;
import com.aspectran.web.websocket.jsr356.ServerEndpointExporter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * A builder class for creating and configuring {@link DefaultWebService} instances.
 * <p>This class provides static factory methods to construct a web service,
 * applying configuration from an {@link com.aspectran.core.context.config.AspectranConfig} object and integrating it
 * with a {@link jakarta.servlet.ServletContext}. It also sets up a {@link com.aspectran.core.service.ServiceStateListener} to
 * manage the service's lifecycle, including session management, registration with
 * the {@link com.aspectran.core.service.CoreServiceHolder}, and pause/resume state.
 * </p>
 */
public class DefaultWebServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebServiceBuilder.class);

    private static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";

    /**
     * Builds a new {@link DefaultWebService} instance for the given {@link ServletContext}.
     * @param servletContext the servlet context
     * @return a new, configured {@code DefaultWebService} instance
     */
    @NonNull
    public static DefaultWebService build(ServletContext servletContext) {
        return build(servletContext, null);
    }

    /**
     * Builds a new {@link DefaultWebService} instance for the given {@link ServletContext} and parent service.
     * @param servletContext the servlet context
     * @param parentService the parent service (optional)
     * @return a new, configured {@code DefaultWebService} instance
     */
    @NonNull
    public static DefaultWebService build(ServletContext servletContext, CoreService parentService) {
        Assert.notNull(servletContext, "servletContext must not be null");

        String aspectranConfigParam = servletContext.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (parentService == null && aspectranConfigParam == null) {
            logger.warn("No specified servlet context initialization parameter for instantiating DefaultWebService");
        }

        DefaultWebService webService;
        if (parentService != null) {
            if (aspectranConfigParam != null) {
                AspectranConfig aspectranConfig = makeAspectranConfig(servletContext, aspectranConfigParam);
                webService = doBuild(servletContext, parentService, aspectranConfig);
            } else {
                webService = doBuild(servletContext, parentService);
            }
        } else {
            AspectranConfig aspectranConfig = makeAspectranConfig(servletContext, aspectranConfigParam);
            webService = doBuild(servletContext, null, aspectranConfig);
        }
        webService.setAltClassLoader(servletContext.getClassLoader());
        WebService.bind(servletContext, webService);
        return webService;
    }

    /**
     * Builds a new {@link DefaultWebService} instance from a {@link WebActivityServlet}.
     * @param servlet the web activity servlet
     * @return a new, configured {@code DefaultWebService} instance
     */
    @Nullable
    public static DefaultWebService build(WebActivityServlet servlet) {
        return build(servlet, null);
    }

    /**
     * Builds a new {@link DefaultWebService} instance from a {@link WebActivityServlet} and a root web service.
     * @param servlet the web activity servlet
     * @param rootWebService the root web service (optional)
     * @return a new, configured {@code DefaultWebService} instance
     */
    @Nullable
    public static DefaultWebService build(WebActivityServlet servlet, WebService rootWebService) {
        Assert.notNull(servlet, "servlet must not be null");

        ServletConfig servletConfig = servlet.getServletConfig();
        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (rootWebService == null && aspectranConfigParam == null) {
            logger.warn("No specified servlet initialization parameter for instantiating DefaultWebService");
        }

        if (rootWebService == null || aspectranConfigParam != null) {
            ServletContext servletContext = servlet.getServletContext();
            AspectranConfig aspectranConfig = makeAspectranConfig(servletContext, aspectranConfigParam);
            return doBuild(servletContext, rootWebService, aspectranConfig);
        } else {
            return null;
        }
    }

    /**
     * Internal helper method to build a derived {@link DefaultWebService}.
     * @param servletContext the servlet context
     * @param parentService the parent service
     * @return a new, configured derived {@code DefaultWebService} instance
     */
    @NonNull
    private static DefaultWebService doBuild(ServletContext servletContext, @NonNull CoreService parentService) {
        DefaultWebService webService = new DefaultWebService(servletContext, parentService, true);
        webService.configure(parentService.getAspectranConfig());
        setServiceStateListener(webService);
        return webService;
    }

    /**
     * Internal helper method to build a new {@link DefaultWebService}.
     * @param servletContext the servlet context
     * @param parentService the parent service (optional)
     * @param aspectranConfig the aspectran configuration
     * @return a new, configured {@code DefaultWebService} instance
     */
    @NonNull
    private static DefaultWebService doBuild(
            ServletContext servletContext,
            @Nullable CoreService parentService,
            @NonNull AspectranConfig aspectranConfig) {
        DefaultWebService webService = new DefaultWebService(servletContext, parentService, false);
        if (parentService instanceof WebService parentWebService) {
            webService.setSessionAdaptable(parentWebService.isSessionAdaptable());
        }
        webService.configure(aspectranConfig);
        setServiceStateListener(webService);
        return webService;
    }

    private static AspectranConfig makeAspectranConfig(ServletContext servletContext, String aspectranConfigParam) {
        AspectranConfig aspectranConfig;
        if (aspectranConfigParam != null) {
            if (aspectranConfigParam.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
                String filePath = aspectranConfigParam.substring(ResourceUtils.FILE_URL_PREFIX.length()).stripLeading();
                try {
                    String realPath = servletContext.getRealPath(filePath);
                    if (realPath == null) {
                        throw new IOException("Failed to resolve real path for [" +
                                ResourceUtils.FILE_URL_PREFIX + filePath + "] in the servlet context. " +
                                "It may be because the content is being made available from a .war archive");
                    }
                    File configFile = new File(realPath);
                    aspectranConfig = new AspectranConfig(configFile);
                } catch (IOException e) {
                    throw new CoreServiceException("Error parsing Aspectran configuration from file: " + filePath, e);
                }
            } else if (aspectranConfigParam.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
                String resourcePath = aspectranConfigParam.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()).stripLeading();
                try {
                    aspectranConfig = new AspectranConfig(ResourceUtils.getResourceAsReader(resourcePath));
                } catch (IOException e) {
                    throw new CoreServiceException("Error parsing Aspectran configuration from resource: " +
                        resourcePath, e);
                }
            } else {
                try {
                    aspectranConfig = new AspectranConfig(aspectranConfigParam);
                } catch (AponParseException e) {
                    throw new CoreServiceException("Error parsing Aspectran configuration from '" +
                        ASPECTRAN_CONFIG_PARAM + "' initialization parameter in web.xml", e);
                }
            }
        } else {
            aspectranConfig = new AspectranConfig();
        }
        return aspectranConfig;
    }

    private static void setServiceStateListener(@NonNull DefaultWebService webService) {
        webService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                CoreServiceHolder.hold(webService);

                // Required for any websocket support
                ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter(webService);
                if (serverEndpointExporter.hasServerContainer()) {
                    for (Class<?> endpointClass : serverEndpointExporter.registerEndpoints()) {
                        CoreServiceHolder.hold(endpointClass, webService);
                    }
                }

                webService.getDefaultServletHttpRequestHandler().lookupDefaultServletName();
                webService.pauseTimeout = 0L;
            }

            @Override
            public void stopped() {
                CoreServiceHolder.release(webService);
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    webService.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0.");
                }
            }

            @Override
            public void paused() {
                webService.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                webService.pauseTimeout = 0L;
            }
        });
    }

}
