package com.aspectran.undertow.service;

import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.undertow.server.session.TowSessionManager;

/**
 * <p>Created: 2019-07-27</p>
 */
public abstract class AbstractTowService extends AspectranCoreService implements TowService {

    private String uriDecoding;

    private TowSessionManager towSessionManager;

    public AbstractTowService() {
        super();
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
    public TowSessionManager getTowSessionManager() {
        return towSessionManager;
    }

    public void setTowSessionManager(TowSessionManager towSessionManager) {
        if (this.towSessionManager != null) {
            throw new IllegalStateException("Tow session manager already exists");
        }
        this.towSessionManager = towSessionManager;
    }

    protected void initSessionManager() {
        if (towSessionManager != null) {
            try {
                towSessionManager.initialize();
            } catch (Exception e) {
                throw new AspectranServiceException("Failed to initialize session manager", e);
            }
        }
    }

    protected void destroySessionManager() {
        if (towSessionManager != null) {
            towSessionManager.destroy();
            towSessionManager = null;
        }
    }

}
