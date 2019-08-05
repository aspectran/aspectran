/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.startup.servlet.WebActivityServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

/**
 * Provides overall functionality for building web applications within a web
 * application container.
 */
public class AspectranWebService extends AspectranCoreService implements WebService {

    private static final Log log = LogFactory.getLog(AspectranWebService.class);

    private static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";

    private static final String DEFAULT_APP_CONFIG_ROOT_FILE = "/WEB-INF/aspectran/app-config.xml";

    private String uriDecoding;

    private DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;

    private long pauseTimeout = -2L;

    private AspectranWebService(ServletContext servletContext) {
        super();
        setBasePath(servletContext.getRealPath("/"));
        setDefaultServletHttpRequestHandler(servletContext);
    }

    private AspectranWebService(ServletContext servletContext, CoreService rootService) {
        super(rootService);
        setDefaultServletHttpRequestHandler(servletContext);
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
                if (!defaultServletHttpRequestHandler.handle(request, response)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e) {
                log.error("An error occurred while processing by the default servlet", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug(getRequestInfo(request));
        }

        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (log.isDebugEnabled()) {
                    log.debug("AspectranWebService has been paused, so did not respond to the request URI \"" +
                            requestUri + "\"");
                }
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            } else if (pauseTimeout == -2L) {
                log.error("AspectranWebService is not yet started");
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            } else {
                pauseTimeout = 0L;
            }
        }

        Activity activity = null;
        try {
            activity = new WebActivity(getActivityContext(), request, response);
            activity.prepare(requestUri, request.getMethod());
            activity.perform();
        } catch (TransletNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("No translet mapped to the request URI [" + requestUri + "]");
            }
            try {
                if (!defaultServletHttpRequestHandler.handle(request, response)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e2) {
                log.error(e2.getMessage(), e2);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (ActivityTerminatedException e) {
            if (log.isDebugEnabled()) {
                log.debug("Activity terminated: " + e.getMessage());
            }
        } catch (ActivityException e) {
            if (e.getCause() != null) {
                log.error(e.getCause().getMessage(), e.getCause());
            } else {
                log.error(e.getMessage(), e);
            }
            if (e.getCause() instanceof RequestMethodNotAllowedException) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            } else if (e.getCause() instanceof SizeLimitExceededException) {
                response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            } else {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.error("An error occurred while processing request: " + requestUri, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (activity != null) {
                activity.finish();
            }
        }
    }

    private String getRequestInfo(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod()).append(" ");
        sb.append(request.getRequestURI()).append(" ");
        sb.append(request.getProtocol()).append(" ");
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
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

    private void setDefaultServletHttpRequestHandler(ServletContext servletContext) {
        defaultServletHttpRequestHandler = new DefaultServletHttpRequestHandler(servletContext);
    }

    /**
     * Returns a new instance of {@code AspectranWebService}.
     *
     * @param servletContext the servlet context
     * @return the instance of {@code AspectranWebService}
     */
    public static AspectranWebService create(ServletContext servletContext) {
        String aspectranConfigParam = servletContext.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam == null) {
            log.warn("No specified servlet context initialization parameter for instantiating WebService");
        }

        AspectranWebService service = create(servletContext, aspectranConfigParam);
        servletContext.setAttribute(ROOT_WEB_SERVICE_ATTRIBUTE, service);

        if (log.isDebugEnabled()) {
            log.debug("The Root WebService attribute in ServletContext has been created; " +
                    ROOT_WEB_SERVICE_ATTRIBUTE + ": " + service);
        }

        return service;
    }

    /**
     * Returns a new instance of {@code AspectranWebService}.
     *
     * @param servletContext the servlet context
     * @param rootService the root service
     * @return the instance of {@code AspectranWebService}
     */
    public static AspectranWebService create(ServletContext servletContext, CoreService rootService) {
        AspectranWebService service = new AspectranWebService(servletContext, rootService);
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
                throw new AspectranServiceException("Failed to start AspectranWebService");
            }
        }
        return service;
    }

    /**
     * Returns a new instance of {@code AspectranWebService}.
     *
     * @param servlet the web activity servlet
     * @return the instance of {@code AspectranWebService}
     */
    public static AspectranWebService create(WebActivityServlet servlet) {
        ServletContext servletContext = servlet.getServletContext();
        ServletConfig servletConfig = servlet.getServletConfig();
        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam == null) {
            log.warn("No specified servlet initialization parameter for instantiating AspectranWebService");
        }

        AspectranWebService service = create(servletContext, aspectranConfigParam);
        String attrName = STANDALONE_WEB_SERVICE_ATTRIBUTE_PREFIX + servlet.getServletName();
        servletContext.setAttribute(attrName, service);

        if (log.isDebugEnabled()) {
            log.debug("The AspectranWebService attribute in ServletContext has been created; " +
                    attrName + ": " + service);
        }

        return service;
    }

    /**
     * Returns a new instance of {@code AspectranWebService}.
     *
     * @param servlet the servlet
     * @param rootService the root service
     * @return the instance of {@code AspectranWebService}
     */
    public static AspectranWebService create(WebActivityServlet servlet, AspectranWebService rootService) {
        ServletContext servletContext = servlet.getServletContext();
        ServletConfig servletConfig = servlet.getServletConfig();
        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam != null) {
            AspectranWebService service = create(servletContext, aspectranConfigParam);
            servletContext.setAttribute(STANDALONE_WEB_SERVICE_ATTRIBUTE_PREFIX +
                    servlet.getServletName(), service);
            return service;
        } else {
            return rootService;
        }
    }

    /**
     * Returns a new instance of {@code AspectranWebService}.
     *
     * @param servletContext the servlet context
     * @param aspectranConfigParam the parameter for aspectran configuration
     * @return the instance of {@code AspectranWebService}
     */
    private static AspectranWebService create(ServletContext servletContext, String aspectranConfigParam) {
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

        AspectranWebService service = new AspectranWebService(servletContext);
        service.prepare(aspectranConfig);

        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            applyWebConfig(service, webConfig);
        }

        setServiceStateListener(service);
        return service;
    }

    private static void applyWebConfig(AspectranWebService service, WebConfig webConfig) {
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

    private static void setServiceStateListener(final AspectranWebService service) {
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
                    log.warn("Pause timeout in milliseconds needs to be set " +
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

    /**
     * Find the root ActivityContext for this web aspectran service.
     *
     * @param servletContext ServletContext to find the web aspectran service for
     * @return the ActivityContext for this web aspectran service
     */
    public static ActivityContext getActivityContext(ServletContext servletContext) {
        ActivityContext activityContext = getActivityContext(servletContext, ROOT_WEB_SERVICE_ATTRIBUTE);
        if (activityContext == null) {
            throw new IllegalStateException("No Root AspectranWebService found; " +
                    "No AspectranServiceListener registered?");
        }
        return activityContext;
    }

    /**
     * Find the standalone ActivityContext for this web aspectran service.
     *
     * @param servlet the servlet
     * @return the ActivityContext for this web aspectran service
     */
    public static ActivityContext getActivityContext(HttpServlet servlet) {
        ServletContext servletContext = servlet.getServletContext();
        String attrName = STANDALONE_WEB_SERVICE_ATTRIBUTE_PREFIX + servlet.getServletName();
        ActivityContext activityContext = getActivityContext(servletContext, attrName);
        if (activityContext != null) {
            return activityContext;
        } else {
            return getActivityContext(servletContext);
        }
    }

    /**
     * Find the ActivityContext for this web aspectran service.
     *
     * @param servletContext ServletContext to find the web aspectran service for
     * @param attrName the name of the ServletContext attribute to look for
     * @return the ActivityContext for this web aspectran service
     */
    private static ActivityContext getActivityContext(ServletContext servletContext, String attrName) {
        Object attr = servletContext.getAttribute(attrName);
        if (attr == null) {
            return null;
        }
        if (!(attr instanceof AspectranWebService)) {
            throw new IllegalStateException("Context attribute [" + attr + "] is not of type [" +
                    AspectranWebService.class.getName() + "]");
        }
        return ((CoreService)attr).getActivityContext();
    }

}
