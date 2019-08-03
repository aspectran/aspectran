package com.aspectran.undertow.service;

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;

/**
 * <p>Created: 2019-07-27</p>
 */
public abstract class AbstractTowService extends AspectranCoreService implements TowService {

    private String uriDecoding;

    private SessionManager sessionManager;

    public AbstractTowService(ApplicationAdapter applicationAdapter) {
        super(applicationAdapter);
    }

    public AbstractTowService(CoreService rootService) {
        super(rootService);
    }

    public String getUriDecoding() {
        return uriDecoding;
    }

    protected void setUriDecoding(String uriDecoding) {
        this.uriDecoding = uriDecoding;
    }

    @Override
    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        if (this.sessionManager != null) {
            throw new IllegalStateException("Session manager already exists");
        }
        this.sessionManager = sessionManager;
    }

    protected void initSessionManager() {
        if (sessionManager != null) {
            try {
                sessionManager.initialize();
            } catch (Exception e) {
                throw new AspectranServiceException("Failed to initialize session manager", e);
            }
        }
    }

    protected void destroySessionManager() {
        if (sessionManager != null) {
            sessionManager.destroy();
            sessionManager = null;
        }
    }

}
