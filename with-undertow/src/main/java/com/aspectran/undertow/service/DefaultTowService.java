/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.undertow.activity.TowActivity;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * <p>Created: 2019-07-27</p>
 */
public class DefaultTowService extends AbstractTowService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTowService.class);

    private volatile long pauseTimeout = -2L;

    public DefaultTowService() {
        super();
    }

    public DefaultTowService(CoreService rootService) {
        super(rootService);
    }

    @Override
    public boolean execute(HttpServerExchange exchange) throws IOException {
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
                    logger.debug(getServiceName() + " has been paused, so did not respond to the request URI \"" +
                            requestPath + "\"");
                }
                exchange.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                return true;
            } else if (pauseTimeout == -2L) {
                logger.error(getServiceName() + " is not yet started");
                exchange.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                return true;
            } else {
                pauseTimeout = 0L;
            }
        }

        try {
            TowActivity activity = new TowActivity(this, exchange);
            activity.prepare(requestPath, exchange.getRequestMethod().toString());
            activity.perform();
        } catch (TransletNotFoundException e) {
            // Provides for "trailing slash" redirects and  serving directory index files
            String transletName = e.getTransletName();
            if (StringUtils.startsWith(transletName, ActivityContext.NAME_SEPARATOR_CHAR) &&
                    !StringUtils.endsWith(transletName, ActivityContext.NAME_SEPARATOR_CHAR)) {
                String transletNameWithSlash = transletName + ActivityContext.NAME_SEPARATOR_CHAR;
                MethodType requestMethod = e.getRequestMethod(MethodType.GET);
                if (getActivityContext().getTransletRuleRegistry().contains(transletNameWithSlash, requestMethod)) {
                    exchange.setStatusCode(HttpStatus.MOVED_PERMANENTLY.value());
                    exchange.getResponseHeaders().put(Headers.LOCATION, transletNameWithSlash);
                    exchange.getResponseHeaders().put(Headers.CONNECTION, "close");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Redirect URL with Trailing Slash: " + e.getTransletName());
                    }
                    return true;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("No translet mapped for request URI [" + requestPath + "]");
            }
            return false;
        } catch (ActivityTerminatedException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Activity terminated: " + e.getMessage());
            }
        } catch (ActivityException e) {
            if (e.getCause() != null) {
                logger.error(e.getCause().getMessage(), e.getCause());
            } else {
                logger.error(e);
            }
            if (e.getCause() instanceof RequestMethodNotAllowedException) {
                exchange.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
            } else if (e.getCause() instanceof SizeLimitExceededException) {
                exchange.setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE.value());
            } else {
                exchange.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing request: " + requestPath, e);
            exchange.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return true;
    }

    private String getRequestInfo(HttpServerExchange exchange) {
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
     *
     * @param rootService the root service
     * @return the instance of {@code DefaultTowService}
     */
    public static DefaultTowService create(CoreService rootService) {
        DefaultTowService service = new DefaultTowService(rootService);
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
                throw new AspectranServiceException("Failed to start DefaultTowService");
            }
        }
        return service;
    }

    /**
     * Returns a new instance of {@code DefaultTowService}.
     *
     * @param aspectranConfig the aspectran configuration
     * @return the instance of {@code DefaultTowService}
     */
    public static DefaultTowService create(AspectranConfig aspectranConfig) {
        DefaultTowService service = new DefaultTowService();
        service.prepare(aspectranConfig);

        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            applyWebConfig(service, webConfig);
        }

        setServiceStateListener(service);
        return service;
    }

    private static void applyWebConfig(DefaultTowService service, WebConfig webConfig) {
        service.setUriDecoding(webConfig.getUriDecoding());
        ExposalsConfig exposalsConfig = webConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            service.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(final DefaultTowService service) {
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
