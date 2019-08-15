package com.aspectran.undertow.server.http;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ContextConfig;
import com.aspectran.core.util.Assert;
import com.aspectran.undertow.service.AspectranTowService;
import com.aspectran.undertow.service.TowService;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.ResourceSupplier;
import io.undertow.server.session.SessionCookieConfig;

/**
 * <p>Created: 2019-07-27</p>
 */
public class StandaloneHttpHandler extends AbstractHttpHandler implements InitializableBean, DisposableBean {

    private AspectranConfig aspectranConfig;

    private volatile AspectranTowService towService;

    public StandaloneHttpHandler(ResourceManager resourceManager) {
        super(resourceManager);
    }

    public StandaloneHttpHandler(ResourceManager resourceManager, HttpHandler next) {
        super(resourceManager, next);
    }

    public StandaloneHttpHandler(ResourceSupplier resourceSupplier) {
        super(resourceSupplier);
    }

    public StandaloneHttpHandler(ResourceSupplier resourceSupplier, HttpHandler next) {
        super(resourceSupplier, next);
    }

    public AspectranConfig getAspectranConfig() {
        return aspectranConfig;
    }

    public void setAspectranConfig(AspectranConfig aspectranConfig) {
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
        if (getSessionManager() != null) {
            if (getSessionConfig() == null) {
                setSessionConfig(new SessionCookieConfig());
            }
            getSessionManager().start();
        }
        ContextConfig contextConfig = aspectranConfig.getContextConfig();
        if (contextConfig != null) {
            String basePath = contextConfig.getBasePath();
            if (basePath == null) {
                contextConfig.setBasePath(getActivityContext().getApplicationAdapter().getBasePath());
            }
        }
        towService = AspectranTowService.create(aspectranConfig);
    }

    @Override
    public void destroy() throws Exception {
        if (towService != null) {
            towService.stop();
            towService = null;
        }
        if (getSessionManager() != null) {
            getSessionManager().stop();
        }
    }

}
