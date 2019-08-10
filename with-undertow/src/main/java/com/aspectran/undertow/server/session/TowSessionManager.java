package com.aspectran.undertow.server.session;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.bean.aware.ApplicationAdapterAware;
import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionHandler;
import com.aspectran.core.context.config.SessionFileStoreConfig;
import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.context.rule.type.SessionStoreType;
import com.aspectran.core.util.StringUtils;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionConfig;
import io.undertow.server.session.SessionListener;
import io.undertow.server.session.SessionManager;
import io.undertow.server.session.SessionManagerStatistics;
import io.undertow.util.AttachmentKey;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * <p>Created: 2019-08-07</p>
 */
public class TowSessionManager implements SessionManager, ApplicationAdapterAware {

    private final AttachmentKey<TowSession> NEW_SESSION = AttachmentKey.create(TowSession.class);

    private final DefaultSessionManager sessionManager;

    private ApplicationAdapter applicationAdapter;

    public TowSessionManager() {
        this(null);
    }

    public TowSessionManager(String deploymentName) {
        DefaultSessionManager defaultSessionManager = new DefaultSessionManager();
        defaultSessionManager.setWorkerName(deploymentName);
        this.sessionManager = defaultSessionManager;
    }

    @Override
    public void setApplicationAdapter(ApplicationAdapter applicationAdapter) {
        this.applicationAdapter = applicationAdapter;
    }

    public SessionHandler getSessionHandler() {
        return sessionManager.getSessionHandler();
    }

    @Override
    public String getDeploymentName() {
        return sessionManager.getWorkerName();
    }

    public void setSessionManagerConfig(SessionManagerConfig sessionManagerConfig) throws IOException {
        if (sessionManagerConfig != null) {
            if (applicationAdapter != null) {
                String storeType = sessionManagerConfig.getStoreType();
                SessionStoreType sessionStoreType = SessionStoreType.resolve(storeType);
                if (sessionStoreType == SessionStoreType.FILE) {
                    SessionFileStoreConfig fileStoreConfig = sessionManagerConfig.getFileStoreConfig();
                    if (fileStoreConfig != null) {
                        String storeDir = fileStoreConfig.getStoreDir();
                        if (StringUtils.hasText(storeDir)) {
                            String basePath = applicationAdapter.getBasePath();
                            String canonPath = new File(basePath, storeDir).getCanonicalPath();
                            fileStoreConfig.setStoreDir(canonPath);
                        }
                    }
                }
            }
            sessionManager.setSessionManagerConfig(sessionManagerConfig);
        }
    }

//    public void setSessionManagerConfig(String apon) {
//        SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();
//        try {
//            sessionManagerConfig.readFrom(apon);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        setSessionManagerConfig(sessionManagerConfig);
//    }

    @Override
    public void start() {
        try {
            sessionManager.initialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        try {
            sessionManager.destroy();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Session createSession(HttpServerExchange serverExchange, SessionConfig sessionConfig) {
        String sessionId = sessionConfig.findSessionId(serverExchange);
        if (sessionId == null) {
            sessionId = sessionManager.createSessionId(hashCode());
        }
        com.aspectran.core.component.session.Session session = sessionManager.createSession(sessionId);
        TowSession towSession = new TowSession(session, this);
        sessionConfig.setSessionId(serverExchange, session.getId());
        serverExchange.putAttachment(NEW_SESSION, towSession);
        return towSession;
    }

    @Override
    public Session getSession(HttpServerExchange serverExchange, SessionConfig sessionConfig) {
        if (serverExchange != null) {
            TowSession newSession = serverExchange.getAttachment(NEW_SESSION);
            if(newSession != null) {
                return newSession;
            }
        }
        String sessionId = sessionConfig.findSessionId(serverExchange);
        TowSession towSession = (TowSession)getSession(sessionId);
        if(towSession != null && serverExchange != null) {
            towSession.requestStarted(serverExchange);
        }
        return towSession;
    }

    @Override
    public Session getSession(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        com.aspectran.core.component.session.Session session = sessionManager.getSession(sessionId);
        if (session != null) {
            return new TowSession(session, this);
        } else {
            return null;
        }
    }

    @Override
    public void registerSessionListener(SessionListener listener) {

    }

    @Override
    public void removeSessionListener(SessionListener listener) {

    }

    @Override
    public void setDefaultSessionTimeout(int timeout) {
        sessionManager.setDefaultMaxIdleSecs(timeout);
    }

    @Override
    public Set<String> getTransientSessions() {
        return null;
    }

    @Override
    public Set<String> getActiveSessions() {
        return null;
    }

    @Override
    public Set<String> getAllSessions() {
        return null;
    }

    @Override
    public SessionManagerStatistics getStatistics() {
        return null;
    }

}
