package com.aspectran.undertow.service;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.activity.ActivityTerminatedException;
import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.activity.request.RequestMethodNotAllowedException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.adapter.ApplicationAdapter;
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
import com.aspectran.undertow.activity.TowActivity;
import com.aspectran.undertow.adapter.TowApplicationAdapter;
import com.aspectran.web.support.http.HttpStatus;
import io.undertow.server.HttpServerExchange;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * <p>Created: 2019-07-27</p>
 */
public class AspectranUndertowService extends AspectranCoreService implements UndertowService {

    private static final Log log = LogFactory.getLog(AspectranUndertowService.class);

    private String uriDecoding;

    private long pauseTimeout = -2L;

    public AspectranUndertowService(ApplicationAdapter applicationAdapter) {
        super(applicationAdapter);
    }

    public AspectranUndertowService(CoreService rootService) {
        super(rootService);
    }

    protected void setUriDecoding(String uriDecoding) {
        this.uriDecoding = uriDecoding;
    }

    @Override
    public void execute(HttpServerExchange exchange) throws IOException {
        String requestUri = exchange.getRequestURI();
        if (uriDecoding != null) {
            requestUri = URLDecoder.decode(requestUri, uriDecoding);
        }
        if (!isExposable(requestUri)) {
            exchange.setStatusCode(HttpStatus.NOT_FOUND.value());
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug(getRequestInfo(exchange));
        }

        if (pauseTimeout != 0L) {
            if (pauseTimeout == -1L || pauseTimeout >= System.currentTimeMillis()) {
                if (log.isDebugEnabled()) {
                    log.debug("AspectranWebService has been paused, so did not respond to the request URI \"" +
                            requestUri + "\"");
                }
                exchange.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                return;
            } else if (pauseTimeout == -2L) {
                log.error("AspectranWebService is not yet started");
                exchange.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE.value());
                return;
            } else {
                pauseTimeout = 0L;
            }
        }

        Activity activity = null;
        try {
            activity = new TowActivity(getActivityContext(), exchange);
            activity.prepare(requestUri, exchange.getRequestMethod().toString());
            activity.perform();
        } catch (TransletNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("No translet mapped to the request URI [" + requestUri + "]");
            }
            exchange.setStatusCode(HttpStatus.NOT_FOUND.value());
//            try {
//                if (!defaultServletHttpRequestHandler.handle(request, response)) {
//                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
//                }
//            } catch (Exception e2) {
//                log.error(e2.getMessage(), e2);
//                exchange.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            }
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
     * Returns a new instance of {@code AspectranUndertowService}.
     *
     * @param rootService the root service
     * @return the instance of {@code AspectranUndertowService}
     */
    public static AspectranUndertowService create(CoreService rootService) {
        AspectranUndertowService service = new AspectranUndertowService(rootService);
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
                throw new AspectranServiceException("Failed to start AspectranUndertowService");
            }
        }
        return service;
    }

    /**
     * Returns a new instance of {@code AspectranUndertowService}.
     *
     * @param aspectranConfig the aspectran configuration
     * @return the instance of {@code AspectranUndertowService}
     */
    public static AspectranUndertowService create(AspectranConfig aspectranConfig) {
        ContextConfig contextConfig = aspectranConfig.touchContextConfig();

        ApplicationAdapter applicationAdapter = new TowApplicationAdapter();
        AspectranUndertowService service = new AspectranUndertowService(applicationAdapter);
        service.prepare(aspectranConfig);

        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            applyWebConfig(service, webConfig);
        }

        setServiceStateListener(service);
        return service;
    }

    private static void applyWebConfig(AspectranUndertowService service, WebConfig webConfig) {
        service.setUriDecoding(webConfig.getUriDecoding());
        ExposalsConfig exposalsConfig = webConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            service.setExposals(includePatterns, excludePatterns);
        }
    }

    private static void setServiceStateListener(final AspectranUndertowService service) {
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
                if (millis < 0L) {
                    throw new IllegalArgumentException("Pause timeout in milliseconds " +
                            "needs to be set to a value of greater than 0");
                }
                service.pauseTimeout = System.currentTimeMillis() + millis;
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
