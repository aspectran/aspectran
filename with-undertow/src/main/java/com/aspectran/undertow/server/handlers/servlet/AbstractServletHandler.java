package com.aspectran.undertow.server.handlers.servlet;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.undertow.service.TowService;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.core.ManagedServlet;
import io.undertow.servlet.handlers.ServletHandler;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * <p>Created: 2019-07-31</p>
 */
public abstract class AbstractServletHandler extends ServletHandler implements ActivityContextAware {

    private ActivityContext context;

    public AbstractServletHandler(ManagedServlet managedServlet) {
        super(managedServlet);
    }

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws IOException, ServletException {
        super.handleRequest(exchange);
    }

    public abstract TowService getTowService();

    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

}
