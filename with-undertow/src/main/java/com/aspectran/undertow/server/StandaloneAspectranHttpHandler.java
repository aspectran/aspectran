package com.aspectran.undertow.server;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.util.Assert;
import com.aspectran.undertow.service.AspectranUndertowService;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 2019-07-27</p>
 */
public class StandaloneAspectranHttpHandler implements HttpHandler, InitializableBean, DisposableBean {

    private final AspectranConfig aspectranConfig;

    private AspectranUndertowService undertowService;

    public StandaloneAspectranHttpHandler(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        undertowService.execute(exchange);
    }

    @Override
    public void initialize() throws Exception {
        Assert.notNull(undertowService, "undertowService already initialized");
        Assert.notNull(aspectranConfig, "aspectranConfig must not be null");
        undertowService = AspectranUndertowService.create(aspectranConfig);
    }

    @Override
    public void destroy() throws Exception {
        if (undertowService != null) {
            undertowService.stop();
            undertowService = null;
        }
    }

}
