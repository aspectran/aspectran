package com.aspectran.undertow.server.http;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.util.Assert;
import com.aspectran.undertow.service.AspectranTowService;

/**
 * Implementation of Undertow {@link io.undertow.server.HttpHandler} to handle HTTP
 * requests as activities of Aspectran.
 *
 * <p>Created: 2019-07-27</p>
 */
public class DefaultHttpHandler extends AbstractHttpHandler implements InitializableBean, DisposableBean {

    private volatile AspectranTowService towService;

    public AspectranTowService getTowService() {
        Assert.state(towService != null, "No AspectranTowService configured");
        return towService;
    }

    @Override
    public void initialize() throws Exception {
        Assert.state(towService == null, "Cannot reconfigure AspectranTowService");
        if (getSessionManager() != null) {
            getSessionManager().start();
        }
        towService = AspectranTowService.create(getActivityContext().getRootService());
    }

    @Override
    public void destroy() throws Exception {
        if (getSessionManager() != null) {
            getSessionManager().stop();
        }
    }

}
