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
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.util.WebUtils;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.atomic.AtomicReference;

import static com.aspectran.core.component.session.MaxSessionsExceededException.MAX_SESSIONS_EXCEEDED;

/**
 * Provides overall functionality for building web applications within a web
 * application container.
 */
public class DefaultWebService extends AbstractWebService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebService.class);

    protected volatile long pauseTimeout = -2L;

    DefaultWebService(@NonNull ServletContext servletContext) {
        super(servletContext);
    }

    DefaultWebService(@NonNull ServletContext servletContext, @Nullable CoreService rootService) {
        super(servletContext, rootService);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String requestUri;
        if (getUriDecoding() != null) {
            requestUri = URLDecoder.decode(request.getRequestURI(), getUriDecoding());
        } else {
            requestUri = request.getRequestURI();
        }

        final String requestName = WebUtils.getRelativePath(getContextPath(), requestUri);
        if (!isExposable(requestName)) {
            try {
                if (!getDefaultServletHttpRequestHandler().handleRequest(request, response)) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                }
            } catch (Exception e) {
                logger.error("Error while processing with default servlet", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(getRequestInfo(request, requestUri, requestName));
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
        TransletRule transletRule = transletRuleRegistry.getTransletRule(requestName, requestMethod);
        if (transletRule == null) {
            // Provides for "trailing slash" redirects and serving directory index files
            if (isTrailingSlashRedirect() &&
                    requestMethod == MethodType.GET &&
                    StringUtils.startsWith(requestName, ActivityContext.NAME_SEPARATOR_CHAR) &&
                    !StringUtils.endsWith(requestName, ActivityContext.NAME_SEPARATOR_CHAR)) {
                String requestNameWithTrailingSlash = requestName + ActivityContext.NAME_SEPARATOR_CHAR;
                if (transletRuleRegistry.contains(requestNameWithTrailingSlash, requestMethod)) {
                    String requestUriWithTrailingSlash = requestUri + ActivityContext.NAME_SEPARATOR_CHAR;
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.setHeader(HttpHeaders.LOCATION, requestUriWithTrailingSlash);
                    response.setHeader(HttpHeaders.CONNECTION, "close");
                    if (logger.isTraceEnabled()) {
                        logger.trace("Redirect URL with a Trailing Slash: " + requestUriWithTrailingSlash);
                    }
                    return;
                }
            }
            try {
                if (!getDefaultServletHttpRequestHandler().handleRequest(request, response)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("No translet mapped for " + requestMethod + " " + requestName);
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
            asyncPerform(request, response, requestName, requestMethod, transletRule);
        } else {
            perform(request, response, requestName, requestMethod, transletRule, null);
        }
    }

    private void asyncPerform(@NonNull HttpServletRequest request, HttpServletResponse response,
                              String requestName, MethodType requestMethod, TransletRule transletRule) {
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
            perform(request, response, requestName, requestMethod, transletRule, activityReference);
            asyncContext.complete();
        });
    }

    private void perform(HttpServletRequest request, HttpServletResponse response,
                         String requestName, MethodType requestMethod, TransletRule transletRule,
                         AtomicReference<Activity> activityReference) {
        ClassLoader origClassLoader = ClassUtils.overrideThreadContextClassLoader(getServiceClassLoader());
        WebActivity activity = null;
        try {
            activity = new WebActivity(getActivityContext(), getContextPath(), request, response);
            if (activityReference != null) {
                activityReference.set(activity);
            }
            activity.prepare(requestName, requestMethod, transletRule);
            activity.perform();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            Throwable t;
            if (activity != null && activity.getRaisedException() != null) {
                t = activity.getRaisedException();
            } else {
                t = e;
            }
            Throwable cause = ExceptionUtils.getRootCause(t);
            logger.error("Error occurred while processing request: " + requestMethod + " " + requestName, t);
            if (!response.isCommitted()) {
                if (cause instanceof RequestMethodNotAllowedException) {
                    sendError(response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, null);
                } else if (cause instanceof SizeLimitExceededException) {
                    sendError(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, null);
                } else if (cause instanceof MaxSessionsExceededException) {
                    sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, MAX_SESSIONS_EXCEEDED);
                } else {
                    sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
                }
            }
        } finally {
            ClassUtils.restoreThreadContextClassLoader(origClassLoader);
        }
    }

    private void sendError(HttpServletResponse response, int sc, String msg) {
        ToStringBuilder tsb = new ToStringBuilder("Send error response");
        tsb.append("code", sc);
        tsb.append("message", msg);
        logger.error(tsb.toString());
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
    private String getRequestInfo(@NonNull HttpServletRequest request, String requestUri, String requestName) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.getMethod()).append(" ");
        if (WebActivity.isRequestWithContextPath(getContextPath(), request)) {
            sb.append(requestUri);
        } else {
            sb.append(requestName);
        }
        sb.append(" ");
        sb.append(request.getProtocol()).append(" ");
        String remoteAddr = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(remoteAddr)) {
            sb.append(remoteAddr);
        } else {
            sb.append(request.getRemoteAddr());
        }
        return sb.toString();
    }

}
