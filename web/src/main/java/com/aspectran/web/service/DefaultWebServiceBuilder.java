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

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.CoreServiceException;
import com.aspectran.core.service.CoreServiceHolder;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.servlet.WebActivityServlet;
import com.aspectran.web.websocket.jsr356.ServerEndpointExporter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import java.io.File;
import java.io.IOException;

public class DefaultWebServiceBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebServiceBuilder.class);

    private static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @return the instance of {@code DefaultWebService}
     */
    @NonNull
    public static DefaultWebService build(ServletContext servletContext) {
        return build(servletContext, null);
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @param parentService the parent service
     * @return the instance of {@code DefaultWebService}
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
     * Returns a new instance of {@code DefaultWebService}.
     * @param servlet the web activity servlet
     * @return the instance of {@code DefaultWebService}
     */
    @Nullable
    public static DefaultWebService build(WebActivityServlet servlet) {
        return build(servlet, null);
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servlet the web activity servlet
     * @param rootWebService the root web service
     * @return the instance of {@code DefaultWebService}
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
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @param parentService the parent service
     * @return the instance of {@code DefaultWebService}
     */
    @NonNull
    private static DefaultWebService doBuild(ServletContext servletContext, @NonNull CoreService parentService) {
        DefaultWebService webService = new DefaultWebService(servletContext, parentService, true);
        webService.configure(parentService.getAspectranConfig());
        setServiceStateListener(webService);
        return webService;
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @param parentService the parent service
     * @param aspectranConfig the aspectran configuration
     * @return the instance of {@code DefaultWebService}
     */
    @NonNull
    private static DefaultWebService doBuild(ServletContext servletContext,
                                             @Nullable CoreService parentService,
                                             @NonNull AspectranConfig aspectranConfig) {
        DefaultWebService webService = new DefaultWebService(servletContext, parentService, false);
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
                    File configFile = new File(servletContext.getRealPath(filePath));
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

    private static void setServiceStateListener(@NonNull final DefaultWebService webService) {
        webService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                CoreServiceHolder.hold(webService);

                // Required for any websocket support
                ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter(webService);
                if (serverEndpointExporter.hasServerContainer()) {
                    for (Class<?> endpointClass : serverEndpointExporter.registerEndpoints()) {
                        CoreServiceHolder.hold(webService, endpointClass);
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
                            "to a value of greater than 0");
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
