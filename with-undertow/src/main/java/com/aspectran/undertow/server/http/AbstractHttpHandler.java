package com.aspectran.undertow.server.http;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.ResourceUtils;
import com.aspectran.undertow.service.TowService;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionManager;

import java.io.File;
import java.io.IOException;

/**
 * <p>Created: 2019-07-31</p>
 */
public abstract class AbstractHttpHandler implements HttpHandler, ActivityContextAware {

    private ActivityContext context;

    private ResourceHandler resourceHandler;

    private volatile SessionManager sessionManager;

    private volatile SessionConfig sessionConfig;

    public void setResourceBase(String resourceBase) throws IOException {
        ResourceManager resourceManager;
        if (resourceBase.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            String basePackage = resourceBase.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            resourceManager = new ClassPathResourceManager(context.getApplicationAdapter().getClassLoader(), basePackage);
        } else {
            File basePath = context.getApplicationAdapter().toRealPathAsFile(resourceBase);
            resourceManager = new FileResourceManager(basePath);
        }
        resourceHandler = new ResourceHandler(resourceManager, ResponseCodeHandler.HANDLE_404);
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
            exchange.putAttachment(SessionManager.ATTACHMENT_KEY, sessionManager);
            exchange.putAttachment(SessionConfig.ATTACHMENT_KEY, sessionConfig);
            UpdateLastAccessTimeListener listener = new UpdateLastAccessTimeListener(sessionConfig, sessionManager);
            exchange.addExchangeCompleteListener(listener);

            boolean processed = getTowService().execute(exchange);
            if (!processed && resourceHandler != null) {
                resourceHandler.handleRequest(exchange);
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

        private final SessionConfig sessionConfig;

        private final SessionManager sessionManager;

        private UpdateLastAccessTimeListener(final SessionConfig sessionConfig, final SessionManager sessionManager) {
            this.sessionConfig = sessionConfig;
            this.sessionManager = sessionManager;
        }

        @Override
        public void exchangeEvent(final HttpServerExchange exchange, final NextListener next) {
            try {
                final Session session = sessionManager.getSession(exchange, sessionConfig);
                if (session != null) {
                    session.requestDone(exchange);
                }
            } finally {
                next.proceed();
            }
        }

    }

}
