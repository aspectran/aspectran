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
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.component.session.MaxSessionsExceededException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.undertow.activity.TowActivity;
import com.aspectran.utils.Assert;
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

    private boolean trailingSlashRedirect;

    private volatile long pauseTimeout = -2L;

    public DefaultTowService() {
        super();
    }

    public DefaultTowService(CoreService rootService) {
        super(rootService);
    }

    public void setTrailingSlashRedirect(boolean trailingSlashRedirect) {
        this.trailingSlashRedirect = trailingSlashRedirect;
    }

    @Override
    public boolean service(@NonNull HttpServerExchange exchange) throws IOException {
        String requestPath = exchange.getRequestPath();
        if (getUriDecoding() != null) {
            requestPath = URLDecoder.decode(requestPath, getUriDecoding());
        }
        if (!isExposable(requestPath)) {
            return false;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(getRequestInfo(exchange));
        }

        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(getServiceName() + " has been paused, so did not respond to request " + requestPath);
                }
                exchange.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                return true;
            } else if (pauseTimeout == -2L) {
                logger.warn(getServiceName() + " is not yet started");
                exchange.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                exchange.setReasonPhrase("Starting... Try again in a moment.");
                return true;
            } else {
                pauseTimeout = 0L;
            }
        }

        MethodType requestMethod = MethodType.resolve(exchange.getRequestMethod().toString());
        if (requestMethod == null) {
            requestMethod = MethodType.GET;
        }
        TransletRule transletRule = getActivityContext()
                .getTransletRuleRegistry()
                .getTransletRule(requestPath, requestMethod);
        if (transletRule == null) {
            // Provides for "trailing slash" redirects and serving directory index files
            if (trailingSlashRedirect &&
                    requestMethod == MethodType.GET &&
                    StringUtils.startsWith(requestPath, ActivityContext.NAME_SEPARATOR_CHAR) &&
                    !StringUtils.endsWith(requestPath, ActivityContext.NAME_SEPARATOR_CHAR)) {
                String transletNameWithSlash = requestPath + ActivityContext.NAME_SEPARATOR_CHAR;
                if (getActivityContext().getTransletRuleRegistry().contains(transletNameWithSlash, requestMethod)) {
                    exchange.setStatusCode(HttpStatus.MOVED_PERMANENTLY.value());
                    exchange.getResponseHeaders().put(Headers.LOCATION, transletNameWithSlash);
                    exchange.getResponseHeaders().put(Headers.CONNECTION, "close");
                    if (logger.isTraceEnabled()) {
                        logger.trace("Redirect URL with a Trailing Slash: " + requestPath);
                    }
                    return true;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("No translet mapped for " + requestMethod + " " + requestPath);
            }
            exchange.setStatusCode(HttpStatus.NOT_FOUND.value());
            return true;
        }

        perform(exchange, requestPath, requestMethod, transletRule);

        return true;
    }

    private void perform(HttpServerExchange exchange, String requestPath,
                         MethodType requestMethod, TransletRule transletRule) {
        TowActivity activity = null;
        try {
            activity = new TowActivity(this, exchange);
            activity.prepare(requestPath, requestMethod, transletRule);
            activity.perform();
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (Exception e) {
            Throwable cause;
            if (activity != null && activity.getRaisedException() != null) {
                cause = ExceptionUtils.getRootCause(activity.getRaisedException());
            } else {
                cause = ExceptionUtils.getRootCause(e);
            }
            logger.error("Error occurred while processing request: " + requestMethod + " " + requestPath, cause);
            if (cause instanceof RequestMethodNotAllowedException) {
                sendError(exchange, HttpStatus.METHOD_NOT_ALLOWED.value(), null);
            } else if (cause instanceof SizeLimitExceededException) {
                sendError(exchange, HttpStatus.PAYLOAD_TOO_LARGE.value(), null);
            } else if (cause instanceof MaxSessionsExceededException) {
                sendError(exchange, HttpStatus.SERVICE_UNAVAILABLE.value(), MAX_SESSIONS_EXCEEDED);
            } else {
                sendError(exchange, HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
            }
        }
    }

    private void sendError(HttpServerExchange exchange, int sc, String msg) {
        ToStringBuilder tsb = new ToStringBuilder("Send error response");
        tsb.append("code", sc);
        tsb.append("message", msg);
        logger.error(tsb.toString());
        if (msg != null) {
            exchange.setStatusCode(sc);
            exchange.setReasonPhrase(msg);
        } else {
            exchange.setStatusCode(sc);
        }
    }

    @NonNull
    private String getRequestInfo(@NonNull HttpServerExchange exchange) {
        StringBuilder sb = new StringBuilder();
        sb.append(exchange.getRequestMethod()).append(" ");
        sb.append(exchange.getRequestURI()).append(" ");
        sb.append(exchange.getProtocol()).append(" ");
        String remoteAddr = exchange.getRequestHeaders().getFirst(HttpHeaders.X_FORWARDED_FOR);
        if (!StringUtils.isEmpty(remoteAddr)) {
            sb.append(remoteAddr);
        } else {
            sb.append(exchange.getSourceAddress());
        }
        return sb.toString();
    }

    /**
     * Returns a new instance of {@code DefaultTowService}.
     * @param rootService the root service
     * @return the instance of {@code DefaultTowService}
     */
    @NonNull
    public static DefaultTowService create(CoreService rootService) {
        Assert.notNull(rootService, "rootService must not be null");
        DefaultTowService towService = new DefaultTowService(rootService);
        AspectranConfig aspectranConfig = rootService.getAspectranConfig();
        if (aspectranConfig != null) {
            WebConfig webConfig = aspectranConfig.getWebConfig();
            if (webConfig != null) {
                applyWebConfig(towService, webConfig);
            }
        }
        setServiceStateListener(towService);
        if (towService.isLateStart()) {
            try {
                towService.getServiceController().start();
            } catch (Exception e) {
                throw new AspectranServiceException("Failed to start DefaultTowService");
            }
        }
        return towService;
    }

    /**
     * Returns a new instance of {@code DefaultTowService}.
     * @param aspectranConfig the aspectran configuration
     * @return the instance of {@code DefaultTowService}
     */
    @NonNull
    public static DefaultTowService create(AspectranConfig aspectranConfig) {
        Assert.notNull(aspectranConfig, "aspectranConfig must not be null");
        DefaultTowService towService = new DefaultTowService();
        towService.prepare(aspectranConfig);
        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            applyWebConfig(towService, webConfig);
        }
        setServiceStateListener(towService);
        return towService;
    }

    private static void applyWebConfig(@NonNull DefaultTowService towService, @NonNull WebConfig webConfig) {
        towService.setUriDecoding(webConfig.getUriDecoding());
        towService.setTrailingSlashRedirect(webConfig.isTrailingSlashRedirect());
        ExposalsConfig exposalsConfig = webConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            towService.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(@NonNull final DefaultTowService towService) {
        towService.setServiceStateListener(new ServiceStateListener() {
            @Override
            public void started() {
                towService.pauseTimeout = 0L;
            }

            @Override
            public void restarted() {
                started();
            }

            @Override
            public void paused(long millis) {
                if (millis > 0L) {
                    towService.pauseTimeout = System.currentTimeMillis() + millis;
                } else {
                    logger.warn("Pause timeout in milliseconds needs to be set " +
                            "to a value of greater than 0");
                }
            }

            @Override
            public void paused() {
                towService.pauseTimeout = -1L;
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
