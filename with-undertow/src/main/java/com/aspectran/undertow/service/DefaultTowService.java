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
package com.aspectran.undertow.service;

import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.component.session.MaxSessionsExceededException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.CoreService;
import com.aspectran.undertow.activity.TowActivity;
import com.aspectran.utils.ExceptionUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.thread.ThreadContextHelper;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;

import static com.aspectran.core.component.session.MaxSessionsExceededException.MAX_SESSIONS_EXCEEDED;

/**
 * Default implementation of the {@link TowService} interface.
 * <p>This class provides the core functionality for building web applications
 * on Undertow. It handles incoming {@link HttpServerExchange} requests,
 * dispatches them to Aspectran's processing pipeline, and manages web-specific
 * concerns like URI decoding and request filtering.</p>
 */
public class DefaultTowService extends AbstractTowService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTowService.class);

    protected volatile long pauseTimeout = -2L;

    /**
     * Instantiates a new DefaultTowService.
     * @param parentService the parent core service
     * @param derived whether this service is derived from a parent
     */
    DefaultTowService(CoreService parentService, boolean derived) {
        super(parentService, derived);
    }

    /**
     * Handles an incoming {@link HttpServerExchange}.
     * <p>This is the main entry point for all web requests. It creates and executes a
     * {@link TowActivity} to process the request, handling various outcomes such as
     * translet not found, exceptions, and successful completion.</p>
     * @param exchange the HTTP request/response exchange
     * @return true if the request was handled by an Aspectran translet; false otherwise
     * @throws IOException if an I/O error occurs
     */
    @Override
    public boolean service(@NonNull HttpServerExchange exchange) throws IOException {
        if (checkPaused(exchange)) {
            return false;
        }

        final String requestName;
        if (getUriDecoding() != null) {
            requestName = URLDecoder.decode(exchange.getRequestURI(), getUriDecoding());
        } else {
            requestName = exchange.getRequestURI();
        }
        final MethodType requestMethod = MethodType.resolve(exchange.getRequestMethod().toString(), MethodType.GET);

        if (logger.isDebugEnabled()) {
            logger.debug(getRequestInfo(exchange, requestName, requestMethod));
        }

        if (!isRequestAcceptable(requestName)) {
            sendError(exchange, HttpStatus.NOT_FOUND, null);
            return false;
        }

        TowActivity activity = new TowActivity(this, exchange);
        activity.setRequestName(requestName);
        activity.setRequestMethod(requestMethod);
        try {
            activity.prepare();
        } catch (TransletNotFoundException e) {
            transletNotFound(activity);
            return false;
        } catch (Exception e) {
            sendError(activity, e);
            return false;
        }
        perform(activity);
        return true;
    }

    /**
     * Executes the main processing logic of the activity and handles any exceptions.
     * @param activity the prepared {@link TowActivity} to perform
     */
    private void perform(TowActivity activity) {
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

    /**
     * Handles the case where no translet is found for the request.
     * <p>This method implements special logic for "trailing slash" redirects.
     * If not handled, it sends a 404 Not Found error.</p>
     * @param activity the current activity
     */
    private void transletNotFound(TowActivity activity) {
        // Provides for "trailing slash" redirects and serving directory index files
        if (isTrailingSlashRedirect() &&
                activity.getRequestMethod() == MethodType.GET &&
                StringUtils.startsWith(activity.getRequestName(), ActivityContext.NAME_SEPARATOR_CHAR) &&
                !StringUtils.endsWith(activity.getRequestName(), ActivityContext.NAME_SEPARATOR_CHAR)) {
            String requestNameWithTrailingSlash = activity.getRequestName() + ActivityContext.NAME_SEPARATOR_CHAR;
            if (getActivityContext().getTransletRuleRegistry().contains(requestNameWithTrailingSlash, activity.getRequestMethod())) {
                activity.getExchange().getResponseHeaders().put(Headers.LOCATION, requestNameWithTrailingSlash);
                activity.getExchange().getResponseHeaders().put(Headers.CONNECTION, "close");
                sendError(activity.getExchange(), HttpStatus.MOVED_PERMANENTLY, null);
                return;
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("No translet mapped for {}", activity.getFullRequestName());
        }
        sendError(activity.getExchange(), HttpStatus.NOT_FOUND, null);
    }

    /**
     * Sends an appropriate HTTP error response based on the type of exception thrown.
     * @param activity the current activity
     * @param e the exception that was thrown
     */
    private void sendError(@NonNull TowActivity activity, Exception e) {
        Throwable t;
        if (activity.isExceptionRaised()) {
            t = activity.getRaisedException();
        } else {
            t = e;
        }
        Throwable cause = ExceptionUtils.getRootCause(t);
        logger.error("Error occurred while processing request: {}", activity.getFullRequestName(), t);
        if (!activity.getExchange().isComplete()) {
            if (cause instanceof RequestMethodNotAllowedException) {
                sendError(activity.getExchange(), HttpStatus.METHOD_NOT_ALLOWED, null);
            } else if (cause instanceof SizeLimitExceededException) {
                sendError(activity.getExchange(), HttpStatus.PAYLOAD_TOO_LARGE, null);
            } else if (cause instanceof MaxSessionsExceededException) {
                sendError(activity.getExchange(), HttpStatus.SERVICE_UNAVAILABLE, MAX_SESSIONS_EXCEEDED);
            } else {
                sendError(activity.getExchange(), HttpStatus.INTERNAL_SERVER_ERROR, null);
            }
        }
    }

    /**
     * Sends a specific HTTP error status and message to the client.
     * @param exchange the current HTTP exchange
     * @param status the HTTP status to send
     * @param msg the reason phrase to send
     */
    private void sendError(@NonNull HttpServerExchange exchange, @NonNull HttpStatus status, String msg) {
        if (logger.isDebugEnabled()) {
            ToStringBuilder tsb = new ToStringBuilder("Response");
            tsb.append("code", status.value());
            tsb.append("message", msg);
            logger.debug(tsb.toString());
        }
        exchange.setStatusCode(status.value());
        if (msg != null) {
            exchange.setReasonPhrase(msg);
        }
    }

    /**
     * Generates a concise, one-line log message for an incoming request.
     * @param exchange the current HTTP exchange
     * @param requestName the processed request name
     * @param requestMethod the processed request method
     * @return a formatted string for logging
     */
    @NonNull
    private String getRequestInfo(@NonNull HttpServerExchange exchange, String requestName, MethodType requestMethod) {
        StringBuilder sb = new StringBuilder();
        sb.append(requestMethod).append(" ");
        sb.append(requestName).append(" ");
        sb.append(exchange.getProtocol()).append(" ");
        String remoteAddr = exchange.getRequestHeaders().getFirst(HttpHeaders.X_FORWARDED_FOR);
        if (StringUtils.hasLength(remoteAddr)) {
            sb.append(remoteAddr);
        } else {
            sb.append(exchange.getSourceAddress());
        }
        return sb.toString();
    }

    /**
     * Checks if the service is currently paused and, if so, sends a 503 Service Unavailable response.
     * @param exchange the current HTTP exchange
     * @return true if the service is paused, false otherwise
     */
    private boolean checkPaused(@NonNull HttpServerExchange exchange) {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} is paused, so did not respond to requests", getServiceName());
                }
                sendError(exchange, HttpStatus.SERVICE_UNAVAILABLE, "Paused");
                return true;
            } else if (pauseTimeout == -2L) {
                logger.warn("{} is not yet started", getServiceName());
                sendError(exchange, HttpStatus.SERVICE_UNAVAILABLE, "Starting... Try again in a moment.");
                return true;
            } else {
                pauseTimeout = 0L;
            }
        }
        return false;
    }

}
