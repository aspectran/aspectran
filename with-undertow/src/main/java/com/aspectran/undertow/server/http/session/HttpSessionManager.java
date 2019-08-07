package com.aspectran.undertow.server.http.session;

import com.aspectran.core.activity.aspect.SessionScopeAdvisor;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionListener;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.config.SessionFileStoreConfig;
import com.aspectran.core.context.rule.type.SessionStoreType;
import com.aspectran.core.util.StringUtils;
import io.undertow.server.HttpServerExchange;

import java.io.File;

/**
 * <p>Created: 2019-08-03</p>
 */
public class HttpSessionManager extends DefaultSessionManager implements ActivityContextAware {

    private ActivityContext context;

    private SessionCookieConfig sessionCookieConfig;

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

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
        if (context == null) {
            throw new IllegalStateException("No ActivityContext specified");
        }
        if (sessionCookieConfig == null) {
            throw new IllegalStateException("No SessionCookieConfig specified");
        }

        if (getSessionConfig() != null) {
            String storeType = getSessionConfig().getStoreType();
            SessionStoreType sessionStoreType = SessionStoreType.resolve(storeType);
            if (sessionStoreType == SessionStoreType.FILE) {
                SessionFileStoreConfig fileStoreConfig = getSessionConfig().getFileStoreConfig();
                if (fileStoreConfig != null) {
                    String storeDir = fileStoreConfig.getStoreDir();
                    if (StringUtils.hasText(storeDir)) {
                        String basePath = context.getApplicationAdapter().getBasePath();
                        String canonPath = new File(basePath, storeDir).getCanonicalPath();
                        fileStoreConfig.setStoreDir(canonPath);
                    }
                }
            }
        }

        final SessionScopeAdvisor sessionScopeAdvisor = SessionScopeAdvisor.create(context);
        if (sessionScopeAdvisor != null) {
            addEventListener(new SessionListener() {
                @Override
                public void sessionCreated(Session session) {
                    sessionScopeAdvisor.executeBeforeAdvice();
                }

                @Override
                public void sessionDestroyed(Session session) {
                    sessionScopeAdvisor.executeAfterAdvice();
                }
            });
        }

        super.doInitialize();
    }

}
