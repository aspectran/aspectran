package com.aspectran.undertow.server;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.Assert;
import com.aspectran.undertow.service.AspectranUndertowService;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 2019-07-27</p>
 */
public class AspectranHttpHandler implements HttpHandler, ActivityContextAware {

    private AspectranUndertowService undertowService;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        undertowService.execute(exchange);
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        Assert.notNull(context, "context must not be null");
        undertowService = AspectranUndertowService.create(context.getRootService());
    }

}
