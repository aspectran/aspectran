package com.aspectran.undertow.server.http;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.undertow.server.resource.StaticResourceHandler;
import com.aspectran.undertow.service.TowService;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.ResourceSupplier;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

/**
 * <p>Created: 2019-07-31</p>
 */
public abstract class AbstractHttpHandler extends ResourceHandler implements ActivityContextAware {

    private ActivityContext context;

    private StaticResourceHandler staticResourceHandler;

    private volatile SessionManager sessionManager;

    private volatile SessionConfig sessionConfig;

    public AbstractHttpHandler(ResourceManager resourceManager) {
        super(resourceManager);
    }

    public AbstractHttpHandler(ResourceManager resourceManager, HttpHandler next) {
        super(resourceManager, next);
    }

    public AbstractHttpHandler(ResourceSupplier resourceSupplier) {
        super(resourceSupplier);
    }

    public AbstractHttpHandler(ResourceSupplier resourceSupplier, HttpHandler next) {
        super(resourceSupplier, next);
    }

    public StaticResourceHandler getStaticResourceHandler() {
        return staticResourceHandler;
    }

    public void setStaticResourceHandler(StaticResourceHandler staticResourceHandler) {
        this.staticResourceHandler = staticResourceHandler;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public SessionConfig getSessionConfig() {
        return sessionConfig;
    }

    public void setSessionConfig(SessionConfig sessionConfig) {
        this.sessionConfig = sessionConfig;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
        } else {
            if (staticResourceHandler != null && staticResourceHandler.hasPatterns()) {
                staticResourceHandler.handleRequest(exchange);
                if (exchange.isComplete()) {
                    return;
                }
            }

            exchange.putAttachment(SessionManager.ATTACHMENT_KEY, sessionManager);
            exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, sessionConfig);
            UpdateLastAccessTimeListener listener = new UpdateLastAccessTimeListener(sessionManager, sessionConfig);
            exchange.addExchangeCompleteListener(listener);

            boolean processed = getTowService().execute(exchange);
            if (!processed) {
                super.handleRequest(exchange);
            }
        }
    }

    public abstract TowService getTowService();

    public ActivityContext getActivityContext() {
        return context;
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    private static class UpdateLastAccessTimeListener implements ExchangeCompletionListener {

        private final SessionManager sessionManager;

        private final SessionConfig sessionConfig;

        private UpdateLastAccessTimeListener(SessionManager sessionManager, SessionConfig sessionConfig) {
            this.sessionManager = sessionManager;
            this.sessionConfig = sessionConfig;
        }

        @Override
        public void exchangeEvent(HttpServerExchange exchange, NextListener next) {
            try {
                Session session = sessionManager.getSession(exchange, sessionConfig);
                if (session != null) {
                    session.requestDone(exchange);
                }
            } finally {
                next.proceed();
            }
        }

    }

}
