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
package com.aspectran.undertow.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.ServiceStateListener;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.undertow.activity.TowActivity;
import com.aspectran.web.support.http.HttpStatus;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * <p>Created: 2019-07-27</p>
 */
public class AspectranTowService extends AbstractTowService {

    private static final Log log = LogFactory.getLog(AspectranTowService.class);

    private long pauseTimeout = -2L;

    public AspectranTowService() {
        super();
    }

    public AspectranTowService(CoreService rootService) {
        super(rootService);
    }

    @Override
    public boolean execute(HttpServerExchange exchange) throws IOException {
        String requestUri = exchange.getRequestURI();
        if (getUriDecoding() != null) {
            requestUri = URLDecoder.decode(requestUri, getUriDecoding());
        }
        if (!isExposable(requestUri)) {
            return false;
        }

        if (log.isDebugEnabled()) {
            log.debug(getRequestInfo(exchange));
        }

        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (log.isDebugEnabled()) {
                    log.debug("AspectranTowService has been paused, so did not respond to the request URI \"" +
                            requestUri + "\"");
                }
                exchange.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                return true;
            } else if (pauseTimeout == -2L) {
                log.error("AspectranTowService is not yet started");
                exchange.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                return true;
            } else {
                pauseTimeout = 0L;
            }
        }

        Activity activity = null;
        try {
            activity = new TowActivity(this, exchange);
            activity.prepare(requestUri, exchange.getRequestMethod().toString());
            activity.perform();
        } catch (TransletNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("No translet mapped to the request URI [" + requestUri + "]");
            }
            return false;
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
                exchange.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
            } else if (e.getCause() instanceof SizeLimitExceededException) {
                exchange.setStatusCode(HttpStatus.PAYLOAD_TOO_LARGE.value());
            } else {
                exchange.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } catch (Exception e) {
            log.error("An error occurred while processing request: " + requestUri, e);
            exchange.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } finally {
            if (activity != null) {
                activity.finish();
            }
        }
        return true;
    }

    private String getRequestInfo(HttpServerExchange exchange) {
        StringBuilder sb = new StringBuilder();
        sb.append(exchange.getRequestMethod()).append(" ");
        sb.append(exchange.getRequestURI()).append(" ");
        sb.append(exchange.getProtocol()).append(" ");
        String remoteAddr = exchange.getRequestHeaders().getFirst("X-FORWARDED-FOR");
        if (!StringUtils.isEmpty(remoteAddr)) {
            sb.append(remoteAddr);
        } else {
            sb.append(exchange.getSourceAddress());
        }
        return sb.toString();
    }

    /**
     * Returns a new instance of {@code AspectranTowService}.
     *
     * @param rootService the root service
     * @return the instance of {@code AspectranTowService}
     */
    public static AspectranTowService create(CoreService rootService) {
        AspectranTowService service = new AspectranTowService(rootService);
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
                throw new AspectranServiceException("Failed to start AspectranTowService");
            }
        }
        return service;
    }

    /**
     * Returns a new instance of {@code AspectranTowService}.
     *
     * @param aspectranConfig the aspectran configuration
     * @return the instance of {@code AspectranTowService}
     */
    public static AspectranTowService create(AspectranConfig aspectranConfig) {
        AspectranTowService service = new AspectranTowService();
        service.prepare(aspectranConfig);

        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            applyWebConfig(service, webConfig);
        }

        setServiceStateListener(service);
        return service;
    }

    private static void applyWebConfig(AspectranTowService service, WebConfig webConfig) {
        service.setUriDecoding(webConfig.getUriDecoding());
        ExposalsConfig exposalsConfig = webConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            service.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(final AspectranTowService service) {
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

}
