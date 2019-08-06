package com.aspectran.undertow.server.session;

import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 2019-08-03</p>
 */
public class TowSessionManager extends DefaultSessionManager implements ActivityContextAware {

    private SessionCookieConfig sessionCookieConfig;

    public SessionCookieConfig getSessionCookieConfig() {
        return sessionCookieConfig;
    }

    public void setSessionCookieConfig(SessionCookieConfig sessionCookieConfig) {
        this.sessionCookieConfig = sessionCookieConfig;
    }

    public SessionAgent newSessionAgent(HttpServerExchange exchange) {
        String sessionId = sessionCookieConfig.findSessionId(exchange);
        SessionAgent sessionAgent = super.newSessionAgent(sessionId);
        if (sessionId == null || !sessionId.equals(sessionAgent.getId())) {
            sessionCookieConfig.setSessionId(exchange, sessionAgent.getId());
        }
        return sessionAgent;
    }

    @Override
    public SessionAgent newSessionAgent() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionAgent newSessionAgent(String id) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doInitialize() throws Exception {
        if (sessionCookieConfig == null) {
            throw new IllegalStateException("Session cookie config is not specified");
        }
        super.doInitialize();
    }

}
