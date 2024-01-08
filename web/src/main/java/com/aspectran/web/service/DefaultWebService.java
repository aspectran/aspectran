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

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.component.session.MaxSessionsExceededException;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.utils.Assert;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.ResourceUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.apon.AponParseException;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.startup.servlet.WebActivityServlet;
import com.aspectran.web.support.http.HttpHeaders;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides overall functionality for building web applications within a web
 * application container.
 */
public class DefaultWebService extends AspectranCoreService implements WebService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebService.class);

    private static final String ASPECTRAN_CONFIG_PARAM = "aspectran:config";

    private static final String ASPECTRAN_CONFIG_FILE_FROM = "file:";

    private static final String ASPECTRAN_CONFIG_RESOURCE_FROM = "resource:";

    private static final String DEFAULT_APP_CONTEXT_FILE = "/WEB-INF/aspectran/app-context.xml";

    private final ServletContext servletContext;

    private final DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;

    private String uriDecoding;

    private boolean trailingSlashRedirect;

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

    public void setTrailingSlashRedirect(boolean trailingSlashRedirect) {
        this.trailingSlashRedirect = trailingSlashRedirect;
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String requestUri;
        if (uriDecoding != null) {
            requestUri = URLDecoder.decode(request.getRequestURI(), uriDecoding);
        } else {
            requestUri = request.getRequestURI();
        }
        if (!isExposable(requestUri)) {
            try {
                if (!defaultServletHttpRequestHandler.handleRequest(request, response)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e) {
                logger.error("Error while processing with default servlet", e);
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
                    logger.debug(getServiceName() + " has been paused, so did not respond to request " + requestUri);
                }
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            } else if (pauseTimeout == -2L) {
                logger.warn(getServiceName() + " is not yet started");
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Starting... Try again in a moment.");
                return;
            } else {
                pauseTimeout = 0L;
            }
        }

        TransletRuleRegistry transletRuleRegistry = getActivityContext().getTransletRuleRegistry();
        final MethodType requestMethod = MethodType.resolve(request.getMethod(), MethodType.GET);
        TransletRule transletRule = transletRuleRegistry.getTransletRule(requestUri, requestMethod);
        if (transletRule == null) {
            // Provides for "trailing slash" redirects and serving directory index files
            if (trailingSlashRedirect &&
                    requestMethod == MethodType.GET &&
                    StringUtils.startsWith(requestUri, ActivityContext.NAME_SEPARATOR_CHAR) &&
                    !StringUtils.endsWith(requestUri, ActivityContext.NAME_SEPARATOR_CHAR)) {
                String transletNameWithSlash = requestUri + ActivityContext.NAME_SEPARATOR_CHAR;
                if (transletRuleRegistry.contains(transletNameWithSlash, requestMethod)) {
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader(HttpHeaders.LOCATION, transletNameWithSlash);
                    response.setHeader(HttpHeaders.CONNECTION, "close");
                    if (logger.isTraceEnabled()) {
                        logger.trace("Redirect URL with a Trailing Slash: " + requestUri);
                    }
                    return;
                }
            }
            try {
                if (!defaultServletHttpRequestHandler.handleRequest(request, response)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No translet mapped for " + requestMethod + " " + requestUri);
                    }
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e) {
                logger.error(e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        if (transletRule.isAsync() && request.isAsyncSupported()) {
            asyncPerform(request, response, requestUri, requestMethod, transletRule);
        } else {
            perform(request, response, requestUri, requestMethod, transletRule, null);
        }
    }

    private void asyncPerform(@NonNull HttpServletRequest request, HttpServletResponse response,
                              String requestUri, MethodType requestMethod, TransletRule transletRule) {
        final AsyncContext asyncContext;
        if (request.isAsyncStarted()) {
            asyncContext = request.getAsyncContext();
            if (transletRule.getTimeout() != null) {
                try {
                    asyncContext.setTimeout(transletRule.getTimeout());
                } catch (IllegalStateException ex) {
                    logger.warn("Servlet request has been put into asynchronous mode by an external force. " +
                            "Proceeding with the existing AsyncContext instance, " +
                            "but cannot guarantee the correct behavior of JAX-RS AsyncResponse time-out support.");
                }
            }
        } else {
            asyncContext = request.startAsync();
            if (logger.isDebugEnabled()) {
                logger.debug("Async Started " + asyncContext);
            }
            if (transletRule.getTimeout() != null) {
                asyncContext.setTimeout(transletRule.getTimeout());
            }
        }
        final AtomicReference<Activity> activityReference = new AtomicReference<>();
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) throws IOException {
                if (logger.isDebugEnabled()) {
                    logger.debug("Async Completed " + asyncEvent);
                }
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
                Activity activity = activityReference.get();
                if (activity != null && !activity.isCommitted() && !activity.isExceptionRaised()) {
                    activity.setRaisedException(new ActivityTerminatedException("Async Timeout " + asyncEvent));
                } else {
                    logger.error("Async Timeout " + asyncEvent);
                }
            }

            @Override
            public void onError(AsyncEvent asyncEvent) throws IOException {
                logger.error("Async Error " + asyncEvent);
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
                if (logger.isDebugEnabled()) {
                    logger.debug("Async Started " + asyncEvent);
                }
            }
        });
        asyncContext.start(() -> {
            perform(request, response, requestUri, requestMethod, transletRule, activityReference);
            asyncContext.complete();
        });
    }

    private void perform(HttpServletRequest request, HttpServletResponse response,
                         String requestUri, MethodType requestMethod, TransletRule transletRule,
                         AtomicReference<Activity> activityReference) {
        WebActivity activity = null;
        try {
            activity = new WebActivity(getActivityContext(), request, response);
            if (activityReference != null) {
                activityReference.set(activity);
            }
            activity.prepare(requestUri, requestMethod, transletRule);
            activity.perform();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            Throwable throwable;
            if (activity != null && activity.getRaisedException() != null) {
                throwable = activity.getRaisedException();
            } else {
                throwable = e;
            }
            Throwable cause = ExceptionUtils.getRootCause(throwable);
            logger.error("Error while processing " + requestMethod + " request " + requestUri +
                    "; Cause: " + ExceptionUtils.getSimpleMessage(cause), throwable);
            if (!response.isCommitted()) {
                if (cause instanceof RequestMethodNotAllowedException) {
                    sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, null);
                } else if (cause instanceof SizeLimitExceededException) {
                    sendError(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, null);
                } else if (cause instanceof MaxSessionsExceededException) {
                    sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                            MaxSessionsExceededException.MAX_SESSIONS_EXCEEDED);
                } else {
                    sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
                }
            }
        }
    }

    private void sendError(HttpServletResponse response, int sc, String msg) {
        try {
            if (msg != null) {
                response.sendError(sc, msg);
            } else {
                response.sendError(sc);
            }
        } catch (IOException e) {
            logger.error("Failed to send an error response to the client with status code " + sc, e);
        }
    }

    @NonNull
    private String getRequestInfo(@NonNull HttpServletRequest request) {
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
    @NonNull public static DefaultWebService create(ServletContext servletContext) {
        Assert.notNull(servletContext, "servletContext must not be null");
        String aspectranConfigParam = servletContext.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam == null) {
            logger.warn("No specified servlet context initialization parameter for instantiating WebService");
        }

        DefaultWebService webService = create(servletContext, aspectranConfigParam);
        servletContext.setAttribute(ROOT_WEB_SERVICE_ATTR_NAME, webService);

        if (logger.isDebugEnabled()) {
            logger.debug("The Root WebService attribute in ServletContext has been created; " +
                    ROOT_WEB_SERVICE_ATTR_NAME + ": " + webService);
        }

        WebServiceHolder.putWebService(webService);
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
        AspectranConfig aspectranConfig = rootService.getAspectranConfig();
        if (aspectranConfig != null) {
            WebConfig webConfig = aspectranConfig.getWebConfig();
            if (webConfig != null) {
                applyWebConfig(webService, webConfig);
            }
        }

        setServiceStateListener(webService);

        if (webService.isLateStart()) {
            try {
                webService.getServiceController().start();
            } catch (Exception e) {
                throw new AspectranServiceException("Failed to start DefaultWebService");
            }
        }

        WebServiceHolder.putWebService(webService);
        return webService;
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servlet the web activity servlet
     * @return the instance of {@code DefaultWebService}
     */
    @NonNull
    public static DefaultWebService create(WebActivityServlet servlet) {
        Assert.notNull(servlet, "servlet must not be null");
        ServletContext servletContext = servlet.getServletContext();
        ServletConfig servletConfig = servlet.getServletConfig();
        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam == null) {
            logger.warn("No specified servlet initialization parameter for instantiating DefaultWebService");
        }

        DefaultWebService webService = create(servletContext, aspectranConfigParam);
        String attrName = STANDALONE_WEB_SERVICE_ATTR_PREFIX + servlet.getServletName();
        servletContext.setAttribute(attrName, webService);

        if (logger.isDebugEnabled()) {
            logger.debug("The Standalone WebService attribute in ServletContext has been created; " +
                    attrName + ": " + webService);
        }

        WebServiceHolder.putWebService(webService);
        return webService;
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servlet the web activity servlet
     * @param rootWebService the root web service
     * @return the instance of {@code DefaultWebService}
     */
    public static DefaultWebService create(WebActivityServlet servlet, DefaultWebService rootWebService) {
        Assert.notNull(servlet, "servlet must not be null");
        Assert.notNull(rootWebService, "rootWebService must not be null");
        ServletContext servletContext = servlet.getServletContext();
        ServletConfig servletConfig = servlet.getServletConfig();
        String aspectranConfigParam = servletConfig.getInitParameter(ASPECTRAN_CONFIG_PARAM);
        if (aspectranConfigParam != null) {
            DefaultWebService webService = create(servletContext, aspectranConfigParam);
            String attrName = STANDALONE_WEB_SERVICE_ATTR_PREFIX + servlet.getServletName();
            servletContext.setAttribute(attrName, webService);

            if (logger.isDebugEnabled()) {
                logger.debug("The Standalone WebService attribute in ServletContext has been created; " +
                    attrName + ": " + webService);
            }

            WebServiceHolder.putWebService(webService);
            return webService;
        } else {
            WebServiceHolder.putWebService(rootWebService);
            return rootWebService;
        }
    }

    /**
     * Returns a new instance of {@code DefaultWebService}.
     * @param servletContext the servlet context
     * @param aspectranConfigParam the parameter for aspectran configuration
     * @return the instance of {@code DefaultWebService}
     */
    private static DefaultWebService create(@NonNull ServletContext servletContext, String aspectranConfigParam) {
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

        ContextConfig contextConfig = aspectranConfig.touchContextConfig();
        String[] contextRules = contextConfig.getContextRules();
        if (ObjectUtils.isEmpty(contextRules) && !contextConfig.hasAspectranParameters()) {
            contextConfig.setContextRules(new String[] { DEFAULT_APP_CONTEXT_FILE });
        }

        DefaultWebService webService = new DefaultWebService(servletContext);
        webService.prepare(aspectranConfig);

        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            applyWebConfig(webService, webConfig);
        }

        setServiceStateListener(webService);
        return webService;
    }

    private static void applyWebConfig(@NonNull DefaultWebService webService, @NonNull WebConfig webConfig) {
        webService.setUriDecoding(webConfig.getUriDecoding());

        String defaultServletName = webConfig.getDefaultServletName();
        if (defaultServletName != null) {
            webService.getDefaultServletHttpRequestHandler().setDefaultServletName(defaultServletName);
        }

        webService.setTrailingSlashRedirect(webConfig.isTrailingSlashRedirect());

        ExposalsConfig exposalsConfig = webConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            webService.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(@NonNull final DefaultWebService webService) {
        webService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
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
            }
        });
    }

}
