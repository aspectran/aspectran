/*
 * Copyright (c) 2008-2021 The Aspectran Project
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

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.startup.servlet.WebActivityServlet;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * Provides overall functionality for building web applications within a web
 * application container.
 */
public class DefaultWebService extends AspectranCoreService implements WebService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebService.class);

    private static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";

    private static final String DEFAULT_APP_CONFIG_ROOT_FILE = "/WEB-INF/aspectran/app-config.xml";

    private final ServletContext servletContext;

    private final DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;

    private String uriDecoding;

    private volatile long pauseTimeout = -2L;

    private DefaultWebService(ServletContext servletContext) {
        super();
        this.servletContext = servletContext;
        this.defaultServletHttpRequestHandler = new DefaultServletHttpRequestHandler(servletContext);
        setBasePath(servletContext.getRealPath("/"));
    }

    private DefaultWebService(ServletContext servletContext, CoreService rootService) {
        super(rootService);
        this.servletContext = servletContext;
        this.defaultServletHttpRequestHandler = new DefaultServletHttpRequestHandler(servletContext);
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    protected void setUriDecoding(String uriDecoding) {
        this.uriDecoding = uriDecoding;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUri = request.getRequestURI();
        if (uriDecoding != null) {
            requestUri = URLDecoder.decode(requestUri, uriDecoding);
        }
        if (!isExposable(requestUri)) {
            try {
                if (!defaultServletHttpRequestHandler.handleRequest(request, response)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e) {
                logger.error("An error occurred while processing by the default servlet", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(getRequestInfo(request));
        }

        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(getServiceName() + " has been paused, so did not respond to the request URI \"" +
                            requestUri + "\"");
                }
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            } else if (pauseTimeout == -2L) {
                logger.error(getServiceName() + " is not yet started");
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            } else {
                pauseTimeout = 0L;
            }
        }

        try {
            WebActivity activity = new WebActivity(getActivityContext(), request, response);
            activity.prepare(requestUri, request.getMethod());
            activity.perform();
        } catch (TransletNotFoundException e) {
            // Provides for "trailing slash" redirects and  serving directory index files
            String transletName = e.getTransletName();
            if (StringUtils.startsWith(transletName, ActivityContext.NAME_SEPARATOR_CHAR) &&
                    !StringUtils.endsWith(transletName, ActivityContext.NAME_SEPARATOR_CHAR)) {
                String transletNameWithSlash = transletName + ActivityContext.NAME_SEPARATOR_CHAR;
                MethodType requestMethod = e.getRequestMethod(MethodType.GET);
                if (getActivityContext().getTransletRuleRegistry().contains(transletNameWithSlash, requestMethod)) {
                    response.setStatus(HttpStatus.MOVED_PERMANENTLY.value());
                    response.setHeader(HttpHeaders.LOCATION, transletNameWithSlash);
                    response.setHeader(HttpHeaders.CONNECTION, "close");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Redirect URL with Trailing Slash: " + e.getTransletName());
                    }
                    return;
                }
            }

            try {
                if (!defaultServletHttpRequestHandler.handleRequest(request, response)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No translet mapped for request URI [" + requestUri + "]");
                    }
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e2) {
                logger.error(e2);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing request: " + requestUri, e);
            if (!response.isCommitted()) {
                if (e.getCause() instanceof RequestMethodNotAllowedException) {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                } else if (e.getCause() instanceof SizeLimitExceededException) {
                    response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        }
    }

    private String getRequestInfo(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod()).append(" ");
        sb.append(request.getRequestURI()).append(" ");
        sb.append(request.getProtocol()).append(" ");
        String remoteAddr = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(remoteAddr)) {
            sb.append(remoteAddr);
        } else {
            sb.append(request.getRemoteAddr());
        }
        return sb.toString();
    }

    private DefaultServletHttpRequestHandler getDefaultServletHttpRequestHandler() {
        return defaultServletHttpRequestHandler;
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @return the instance of {@code DefaultWebService}
     */
    public static DefaultWebService create(ServletContext servletContext) {
        String aspectranConfigParam = servletContext.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam == null) {
            logger.warn("No specified servlet context initialization parameter for instantiating WebService");
        }

        DefaultWebService service = create(servletContext, aspectranConfigParam);
        servletContext.setAttribute(ROOT_WEB_SERVICE_ATTR_NAME, service);

        if (logger.isDebugEnabled()) {
            logger.debug("The Root WebService attribute in ServletContext has been created; " +
                    ROOT_WEB_SERVICE_ATTR_NAME + ": " + service);
        }

        WebServiceHolder.putWebService(service);
        return service;
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @param rootService the root service
     * @return the instance of {@code DefaultWebService}
     */
    public static DefaultWebService create(ServletContext servletContext, CoreService rootService) {
        DefaultWebService service = new DefaultWebService(servletContext, rootService);
        AspectranConfig aspectranConfig = rootService.getAspectranConfig();
        if (aspectranConfig != null) {
            WebConfig webConfig = aspectranConfig.getWebConfig();
            if (webConfig != null) {
                applyWebConfig(service, webConfig);
            }
        }

        setServiceStateListener(service);

        if (service.isLateStart()) {
            try {
                service.getServiceController().start();
            } catch (Exception e) {
                throw new AspectranServiceException("Failed to start DefaultWebService");
            }
        }

        WebServiceHolder.putWebService(service);
        return service;
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servlet the web activity servlet
     * @return the instance of {@code DefaultWebService}
     */
    public static DefaultWebService create(WebActivityServlet servlet) {
        ServletContext servletContext = servlet.getServletContext();
        ServletConfig servletConfig = servlet.getServletConfig();
        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam == null) {
            logger.warn("No specified servlet initialization parameter for instantiating DefaultWebService");
        }

        DefaultWebService service = create(servletContext, aspectranConfigParam);
        String attrName = STANDALONE_WEB_SERVICE_ATTR_PREFIX + servlet.getServletName();
        servletContext.setAttribute(attrName, service);

        if (logger.isDebugEnabled()) {
            logger.debug("The Standalone WebService attribute in ServletContext has been created; " +
                    attrName + ": " + service);
        }

        WebServiceHolder.putWebService(service);
        return service;
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servlet the servlet
     * @param rootService the root service
     * @return the instance of {@code DefaultWebService}
     */
    public static DefaultWebService create(WebActivityServlet servlet, DefaultWebService rootService) {
        ServletContext servletContext = servlet.getServletContext();
        ServletConfig servletConfig = servlet.getServletConfig();
        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam != null) {
            DefaultWebService service = create(servletContext, aspectranConfigParam);
            String attrName = STANDALONE_WEB_SERVICE_ATTR_PREFIX + servlet.getServletName();
            servletContext.setAttribute(attrName, service);

            if (logger.isDebugEnabled()) {
                logger.debug("The Standalone WebService attribute in ServletContext has been created; " +
                    attrName + ": " + service);
            }

            WebServiceHolder.putWebService(service);
            return service;
        } else {
            WebServiceHolder.putWebService(rootService);
            return rootService;
        }
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @param aspectranConfigParam the parameter for aspectran configuration
     * @return the instance of {@code DefaultWebService}
     */
    private static DefaultWebService create(ServletContext servletContext, String aspectranConfigParam) {
        AspectranConfig aspectranConfig;
        if (aspectranConfigParam != null) {
            try {
                aspectranConfig = new AspectranConfig(aspectranConfigParam);
            } catch (IOException e) {
                throw new AspectranServiceException("Error parsing '" + ASPECTRAN_CONFIG_PARAM +
                        "' initialization parameter for Aspectran Configuration", e);
            }
        } else {
            aspectranConfig = new AspectranConfig();
        }

        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        String rootFile = contextConfig.getRootFile();
        if (!StringUtils.hasText(rootFile) && !contextConfig.hasAspectranParameters()) {
            contextConfig.setRootFile(DEFAULT_APP_CONFIG_ROOT_FILE);
        }

        DefaultWebService service = new DefaultWebService(servletContext);
        service.prepare(aspectranConfig);

        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            applyWebConfig(service, webConfig);
        }

        setServiceStateListener(service);
        return service;
    }

    private static void applyWebConfig(DefaultWebService service, WebConfig webConfig) {
        service.setUriDecoding(webConfig.getUriDecoding());

        String defaultServletName = webConfig.getDefaultServletName();
        if (defaultServletName != null) {
            service.getDefaultServletHttpRequestHandler().setDefaultServletName(defaultServletName);
        }

        ExposalsConfig exposalsConfig = webConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            service.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(final DefaultWebService service) {
        service.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                service.pauseTimeout = 0L;
            }

            @Override
            public void restarted() {
                started();
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    service.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0");
                }
            }

            @Override
            public void paused() {
                service.pauseTimeout = -1L;
            }

            @Override
            public void resumed() {
                started();
            }

            @Override
            public void stopped() {
                paused();
            }
        });
    }

}
