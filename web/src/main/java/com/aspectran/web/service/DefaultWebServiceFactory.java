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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ObjectUtils;
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

import static com.aspectran.web.service.WebService.ROOT_WEB_SERVICE_ATTR_NAME;
import static com.aspectran.web.service.WebService.STANDALONE_WEB_SERVICE_ATTR_PREFIX;

public class DefaultWebServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebServiceFactory.class);

    private static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";

    private static final String ASPECTRAN_CONFIG_FILE_FROM = "file:";

    private static final String ASPECTRAN_CONFIG_RESOURCE_FROM = "resource:";

    private static final String DEFAULT_APP_CONTEXT_FILE = "/WEB-INF/aspectran/app-context.xml";

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @return the instance of {@code DefaultWebService}
     */
    @NonNull
    public static DefaultWebService create(ServletContext servletContext) {
        Assert.notNull(servletContext, "servletContext must not be null");
        String aspectranConfigParam = servletContext.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam == null) {
            logger.warn("No specified servlet context initialization parameter for instantiating DefaultWebService");
        }
        AspectranConfig aspectranConfig = makeAspectranConfig(servletContext, aspectranConfigParam);
        DefaultWebService webService = create(servletContext, aspectranConfig, null);
        servletContext.setAttribute(ROOT_WEB_SERVICE_ATTR_NAME, webService);
        if (logger.isDebugEnabled()) {
            logger.debug("The Root WebService attribute in ServletContext has been created; " +
                    ROOT_WEB_SERVICE_ATTR_NAME + ": " + webService);
        }
        return webService;
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @param rootService the root service
     * @return the instance of {@code DefaultWebService}
     */
    @NonNull
    public static DefaultWebService create(ServletContext servletContext, CoreService rootService) {
        Assert.notNull(servletContext, "servletContext must not be null");
        Assert.notNull(rootService, "rootService must not be null");
        DefaultWebService webService = new DefaultWebService(servletContext, rootService);
        webService.configure(rootService.getAspectranConfig());
        setServiceStateListener(webService);
        if (webService.isLateStart()) {
            try {
                webService.getServiceController().start();
            } catch (Exception e) {
                throw new AspectranServiceException("Failed to start DefaultWebService");
            }
        }
        servletContext.setAttribute(ROOT_WEB_SERVICE_ATTR_NAME, webService);
        return webService;
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servlet the web activity servlet
     * @return the instance of {@code DefaultWebService}
     */
    @Nullable
    public static DefaultWebService create(WebActivityServlet servlet, WebService rootWebService) {
        Assert.notNull(servlet, "servlet must not be null");
        ServletContext servletContext = servlet.getServletContext();
        ServletConfig servletConfig = servlet.getServletConfig();
        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (rootWebService == null || aspectranConfigParam != null) {
            if (aspectranConfigParam == null) {
                logger.warn("No specified servlet initialization parameter for instantiating DefaultWebService");
            }
            AspectranConfig aspectranConfig = makeAspectranConfig(servletContext, aspectranConfigParam);
            ApplicationAdapter applicationAdapter = null;
            if (rootWebService != null && rootWebService.getActivityContext() != null) {
                applicationAdapter = rootWebService.getActivityContext().getApplicationAdapter();
            }
            DefaultWebService webService = create(servletContext, aspectranConfig, applicationAdapter);
            String attrName = STANDALONE_WEB_SERVICE_ATTR_PREFIX + servlet.getServletName();
            servletContext.setAttribute(attrName, webService);
            if (logger.isDebugEnabled()) {
                logger.debug("The Standalone WebService attribute in ServletContext has been created; " +
                    attrName + ": " + webService);
            }
            return webService;
        } else {
            return null;
        }
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @param aspectranConfig the aspectran configuration
     * @return the instance of {@code DefaultWebService}
     */
    @NonNull
    private static DefaultWebService create(ServletContext servletContext,
                                            @NonNull AspectranConfig aspectranConfig,
                                            @Nullable ApplicationAdapter applicationAdapter) {
        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        String[] contextRules = contextConfig.getContextRules();
        if (ObjectUtils.isEmpty(contextRules) && !contextConfig.hasAspectranParameters()) {
            contextConfig.setContextRules(new String[] { DEFAULT_APP_CONTEXT_FILE });
        }
        DefaultWebService webService = new DefaultWebService(servletContext);
        webService.configure(aspectranConfig, applicationAdapter);
        setServiceStateListener(webService);
        return webService;
    }

    private static AspectranConfig makeAspectranConfig(ServletContext servletContext, String aspectranConfigParam) {
        AspectranConfig aspectranConfig;
        if (aspectranConfigParam != null) {
            if (aspectranConfigParam.startsWith(ASPECTRAN_CONFIG_FILE_FROM)) {
                String filePath = aspectranConfigParam.substring(ASPECTRAN_CONFIG_FILE_FROM.length()).stripLeading();
                try {
                    File configFile = new File(servletContext.getRealPath(filePath));
                    aspectranConfig = new AspectranConfig(configFile);
                } catch (IOException e) {
                    throw new AspectranServiceException("Error parsing Aspectran configuration from file: " + filePath, e);
                }
            } else if (aspectranConfigParam.startsWith(ASPECTRAN_CONFIG_RESOURCE_FROM)) {
                String resourcePath = aspectranConfigParam.substring(ASPECTRAN_CONFIG_RESOURCE_FROM.length()).stripLeading();
                try {
                    File configFile = ResourceUtils.getResourceAsFile(resourcePath);
                    aspectranConfig = new AspectranConfig(configFile);
                } catch (IOException e) {
                    throw new AspectranServiceException("Error parsing Aspectran configuration from resource: " +
                        resourcePath, e);
                }
            } else {
                try {
                    aspectranConfig = new AspectranConfig(aspectranConfigParam);
                } catch (AponParseException e) {
                    throw new AspectranServiceException("Error parsing Aspectran configuration from '" +
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
                WebServiceHolder.putWebService(webService);

                // Required for any websocket support
                ServerEndpointExporter serverEndpointExporter = new ServerEndpointExporter(webService);
                if (serverEndpointExporter.hasServerContainer()) {
                    for (Class<?> endpointClass : serverEndpointExporter.registerEndpoints()) {
                        WebServiceHolder.putWebService(endpointClass, webService);
                    }
                }

                webService.pauseTimeout = 0L;
            }

            @Override
            public void restarted() {
                started();
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
                started();
            }

            @Override
            public void stopped() {
                paused();
                WebServiceHolder.removeWebService(webService);
            }
        });
    }

}
