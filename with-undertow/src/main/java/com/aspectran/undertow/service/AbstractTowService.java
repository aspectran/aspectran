package com.aspectran.undertow.service;

import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.CoreService;

/**
 * <p>Created: 2019-07-27</p>
 */
public abstract class AbstractTowService extends AspectranCoreService implements TowService {

    private String uriDecoding;

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

}
