package com.aspectran.undertow.server.http;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.util.Assert;
import com.aspectran.undertow.service.AspectranTowService;
import com.aspectran.undertow.service.TowService;
import io.undertow.server.HttpServerExchange;

/**
 * <p>Created: 2019-07-27</p>
 */
public class StandaloneHttpHandler extends AbstractHttpHandler
        implements InitializableBean, DisposableBean {

    private final AspectranConfig aspectranConfig;

    private AspectranTowService towService;

    public StandaloneHttpHandler(AspectranConfig aspectranConfig) {
        this.aspectranConfig = aspectranConfig;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        towService.execute(exchange);
    }

    @Override
    public TowService getTowService() {
        Assert.state(towService != null, "No AspectranTowService configured");
        return towService;
    }

    @Override
    public void initialize() throws Exception {
        Assert.state(towService == null, "Cannot reconfigure AspectranTowService");
        Assert.notNull(aspectranConfig, "aspectranConfig must not be null");
        towService = AspectranTowService.create(aspectranConfig);
    }

    @Override
    public void destroy() throws Exception {
        if (towService != null) {
            towService.stop();
            towService = null;
        }
    }

}
