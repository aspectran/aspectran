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

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.component.session.MaxSessionsExceededException;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.thread.ThreadContextHelper;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.util.WebUtils;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;

import static com.aspectran.core.component.session.MaxSessionsExceededException.MAX_SESSIONS_EXCEEDED;

/**
 * Default implementation of the {@link WebService} interface.
 * <p>This class provides the core functionality for building web applications
 * within a Servlet container. It handles incoming HTTP requests, dispatches them
 * to Aspectran's processing pipeline, and manages web-specific concerns like
 * URI decoding, static resource handling, and asynchronous request processing.
 * </p>
 */
public class DefaultWebService extends AbstractWebService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultWebService.class);

    protected volatile long pauseTimeout = -2L;

    DefaultWebService(@NonNull ServletContext servletContext, @Nullable CoreService parentService, boolean derived) {
        super(servletContext, parentService, derived);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkPaused(response)) {
            return;
        }

        final String requestUri;
        if (getUriDecoding() != null) {
            requestUri = URLDecoder.decode(request.getRequestURI(), getUriDecoding());
        } else {
            requestUri = request.getRequestURI();
        }

        final String requestName = WebUtils.getRelativePath(getContextPath(), requestUri);
        final MethodType requestMethod = MethodType.resolve(request.getMethod(), MethodType.GET);
        final String reverseContextPath = WebUtils.getReverseContextPath(request, getContextPath());

        if (logger.isDebugEnabled()) {
            logger.debug(getRequestInfo(request, reverseContextPath, requestName, requestMethod));
        }

        if (!isRequestAcceptable(requestName)) {
            try {
                if (!getDefaultServletHttpRequestHandler().handleRequest(request, response)) {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, null);
                }
            } catch (Exception e) {
                logger.error("Error while processing with default servlet", e);
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
            }
            return;
        }

        WebActivity activity = new WebActivity(this, getContextPath(), reverseContextPath, request, response);
        activity.setRequestName(requestName);
        activity.setRequestMethod(requestMethod);

        try {
            activity.prepare();
        } catch (TransletNotFoundException e) {
            transletNotFound(activity);
            return;
        } catch (Exception e) {
            sendError(activity, e);
            return;
        }

        WebService.bind(activity, this);

        if (activity.isAsync() && request.isAsyncSupported()) {
            asyncPerform(activity);
        } else {
            perform(activity);
        }
    }

    private void asyncPerform(@NonNull WebActivity activity) {
        final AsyncContext asyncContext;
        if (activity.getRequest().isAsyncStarted()) {
            asyncContext = activity.getRequest().getAsyncContext();
            if (activity.getTimeout() != null) {
                try {
                    asyncContext.setTimeout(activity.getTimeout());
                } catch (IllegalStateException ex) {
                    logger.warn("Servlet request has been put into asynchronous mode by an external force. " +
                            "Proceeding with the existing AsyncContext instance, " +
                            "but cannot guarantee the correct behavior of JAX-RS AsyncResponse time-out support.");
                }
            }
        } else {
            asyncContext = activity.getRequest().startAsync();
            if (logger.isDebugEnabled()) {
                logger.debug("Async Started {}", asyncContext);
            }
            if (activity.getTimeout() != null) {
                asyncContext.setTimeout(activity.getTimeout());
            }
        }
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent asyncEvent) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Async Completed {}", asyncEvent);
                }
            }

            @Override
            public void onTimeout(AsyncEvent asyncEvent) {
                if (!activity.isResponded() && !activity.isExceptionRaised()) {
                    activity.setRaisedException(new ActivityTerminatedException("Async Timeout " + asyncEvent));
                } else {
                    logger.error("Async Timeout {}", asyncEvent);
                }
            }

            @Override
            public void onError(AsyncEvent asyncEvent) {
                logger.error("Async Error {}", asyncEvent);
            }

            @Override
            public void onStartAsync(AsyncEvent asyncEvent) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Async Started {}", asyncEvent);
                }
            }
        });
        asyncContext.start(() -> {
            perform(activity);
            asyncContext.complete();
        });
    }

    private void perform(@NonNull WebActivity activity) {
        ClassLoader origClassLoader = ThreadContextHelper.overrideClassLoader(getServiceClassLoader());
        try {
            activity.perform();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: {}", e.getMessage());
            }
        } catch (Exception e) {
            sendError(activity, e);
        } finally {
            ThreadContextHelper.restoreClassLoader(origClassLoader);
        }
    }

    private void transletNotFound(@NonNull WebActivity activity) {
        // Provides for "trailing slash" redirects and serving directory index files
        if (isTrailingSlashRedirect() &&
                activity.getRequestMethod() == MethodType.GET &&
            StringUtils.startsWith(activity.getRequestName(), ActivityContext.NAME_SEPARATOR_CHAR) &&
            !StringUtils.endsWith(activity.getRequestName(), ActivityContext.NAME_SEPARATOR_CHAR)) {
            String requestNameWithTrailingSlash = activity.getRequestName() + ActivityContext.NAME_SEPARATOR_CHAR;
            TransletRuleRegistry transletRuleRegistry = getActivityContext().getTransletRuleRegistry();
            if (transletRuleRegistry.contains(requestNameWithTrailingSlash, activity.getRequestMethod())) {
                String location;
                if (StringUtils.hasLength(activity.getReverseContextPath())) {
                    location = activity.getReverseContextPath() + activity.getRequestName() + ActivityContext.NAME_SEPARATOR;
                } else {
                    location = activity.getRequestName() + ActivityContext.NAME_SEPARATOR;
                }
                activity.getResponse().setHeader(HttpHeaders.LOCATION, location);
                activity.getResponse().setHeader(HttpHeaders.CONNECTION, "close");
                sendError(activity.getResponse(), HttpServletResponse.SC_MOVED_PERMANENTLY, null);
                return;
            }
        }
        try {
            if (!getDefaultServletHttpRequestHandler().handleRequest(activity.getRequest(), activity.getResponse())) {
                if (logger.isTraceEnabled()) {
                    logger.trace("No translet mapped for {}", activity.getFullRequestName());
                }
                sendError(activity.getResponse(), HttpServletResponse.SC_NOT_FOUND, null);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            sendError(activity.getResponse(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
        }
    }

    private void sendError(@NonNull WebActivity activity, Exception e) {
        Throwable t;
        if (activity.isExceptionRaised()) {
            t = activity.getRaisedException();
        } else {
            t = e;
        }
        logger.error("Error occurred while processing request: {}", activity.getFullRequestName(), t);
        if (!activity.getResponse().isCommitted()) {
            Throwable cause = ExceptionUtils.getRootCause(t);
            if (cause instanceof RequestMethodNotAllowedException) {
                sendError(activity.getResponse(), HttpServletResponse.SC_METHOD_NOT_ALLOWED, null);
            } else if (cause instanceof SizeLimitExceededException) {
                sendError(activity.getResponse(), HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, null);
            } else if (cause instanceof MaxSessionsExceededException) {
                sendError(activity.getResponse(), HttpServletResponse.SC_SERVICE_UNAVAILABLE, MAX_SESSIONS_EXCEEDED);
            } else {
                sendError(activity.getResponse(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
            }
        }
    }

    private void sendError(HttpServletResponse response, int sc, String msg) {
        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Response");
            tsb.append("code", sc);
            tsb.append("message", msg);
            logger.debug(tsb.toString());
        }
        try {
            if (msg != null) {
                response.sendError(sc, msg);
            } else {
                response.sendError(sc);
            }
        } catch (IOException e) {
            logger.error("Failed to send an error response to the client with status code {}", sc, e);
        }
    }

    @NonNull
    private String getRequestInfo(@NonNull HttpServletRequest request, String reverseContextPath,
                                  String requestName, MethodType requestMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestMethod).append(" ");
        if (StringUtils.hasLength(reverseContextPath)) {
            sb.append(reverseContextPath);
        }
        sb.append(requestName).append(" ");
        sb.append(request.getProtocol()).append(" ");
        String remoteAddr = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
        if (StringUtils.hasLength(remoteAddr)) {
            sb.append(remoteAddr);
        } else {
            sb.append(request.getRemoteAddr());
        }
        return sb.toString();
    }

    /**
     * Checks if the service is currently paused and, if so, sends a 503 Service Unavailable response.
     * @param response the current HTTP servlet response
     * @return true if the service is paused, false otherwise
     */
    private boolean checkPaused(@NonNull HttpServletResponse response) {
        // A value of 0L means the service is active.
        // A value of -1L means the service is paused indefinitely.
        // A value of -2L means the service is not yet started.
        // Any other positive value is the time in milliseconds until the service is paused.
        if (pauseTimeout != 0L) {
            // If the service is not yet started, wait for it to start.
            // This is necessary because a request can come in before the service is fully initialized.
            if (pauseTimeout == -2L) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is not yet started, waiting for it to start...", getServiceName());
                }
                while (pauseTimeout == -2L) {
                    try {
                        // Poll every 100ms to see if the state has changed.
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.warn("Interrupted while waiting for service to start", e);
                        sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                                "Service is starting. Please try again in a moment.");
                        return true;
                    }
                }
                // If the service has started successfully, pauseTimeout will be 0L.
                // In this case, we can proceed with the request.
                if (pauseTimeout == 0L) {
                    return false;
                }
                // If the service state changes to paused (-1L) during startup,
                // fall through to the next check.
            }

            // Check if the service is paused (indefinitely or temporarily).
            // This check is separate from the one above to handle the race condition where
            // the service is paused while it is starting up.
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is paused, so did not respond to requests", getServiceName());
                }
                sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Service is temporarily unavailable. Please try again later.");
                return true;
            } else {
                // If a temporary pause has expired, reset the timeout and allow requests.
                pauseTimeout = 0L;
            }
        }
        return false;
    }

}
