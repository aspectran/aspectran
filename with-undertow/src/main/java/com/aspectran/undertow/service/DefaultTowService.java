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
import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.IOException;
import java.net.URLDecoder;

import static com.aspectran.core.component.session.MaxSessionsExceededException.MAX_SESSIONS_EXCEEDED;

/**
 * <p>Created: 2019-07-27</p>
 */
public class DefaultTowService extends AbstractTowService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTowService.class);

    protected volatile long pauseTimeout = -2L;

    DefaultTowService(CoreService parentService, boolean derived) {
        super(parentService, derived);
    }

    @Override
    public boolean service(@NonNull HttpServerExchange exchange) throws IOException {
        if (checkPaused(exchange)) {
            return false;
        }

        final String requestName;
        if (getUriDecoding() != null) {
            requestName = URLDecoder.decode(exchange.getRequestPath(), getUriDecoding());
        } else {
            requestName = exchange.getRequestPath();
        }
        final MethodType requestMethod = MethodType.resolve(exchange.getRequestMethod().toString(), MethodType.GET);

        if (logger.isDebugEnabled()) {
            logger.debug(getRequestInfo(exchange, requestName, requestMethod));
        }

        if (!isAcceptable(requestName)) {
            sendError(exchange, HttpStatus.NOT_FOUND, "Not Exposed");
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

    private void perform(TowActivity activity) {
        try {
            activity.perform();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            sendError(activity, e);
        }
    }

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
            logger.trace("No translet mapped for " + activity.getFullRequestName());
        }
        sendError(activity.getExchange(), HttpStatus.NOT_FOUND, null);
    }

    private void sendError(@NonNull TowActivity activity, Exception e) {
        Throwable t;
        if (activity.getRaisedException() != null) {
            t = activity.getRaisedException();
        } else {
            t = e;
        }
        Throwable cause = ExceptionUtils.getRootCause(t);
        logger.error("Error occurred while processing request: " + activity.getFullRequestName(), t);
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

    private void sendError(@NonNull HttpServerExchange exchange, @NonNull HttpStatus status, String msg) {
        ToStringBuilder tsb = new ToStringBuilder("Response");
        tsb.append("code", status.value());
        tsb.append("message", msg);
        if (logger.isDebugEnabled()) {
            logger.debug(tsb.toString());
        }
        exchange.setStatusCode(status.value());
        if (msg != null) {
            exchange.setReasonPhrase(msg);
        }
    }

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

    private boolean checkPaused(@NonNull HttpServerExchange exchange) {
        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(getServiceName() + " is paused, so did not respond to requests");
                }
                sendError(exchange, HttpStatus.SERVICE_UNAVAILABLE, "Paused");
                return true;
            } else if (pauseTimeout == -2L) {
                logger.warn(getServiceName() + " is not yet started");
                sendError(exchange, HttpStatus.SERVICE_UNAVAILABLE, "Starting... Try again in a moment.");
                return true;
            } else {
                pauseTimeout = 0L;
            }
        }
        return false;
    }

}
