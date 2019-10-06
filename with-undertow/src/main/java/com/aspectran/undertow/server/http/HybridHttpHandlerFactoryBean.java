package com.aspectran.undertow.server.http;

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import io.undertow.server.HttpHandler;

/**
 * <p>Created: 06/10/2019</p>
 */
public class HybridHttpHandlerFactoryBean extends HybridHttpHandlerFactory
        implements InitializableBean, FactoryBean<HttpHandler> {

    private volatile HttpHandler handler;

    @Override
    public void initialize() throws Exception {
        handler = createHandler();
    }

    @Override
    public HttpHandler getObject() throws Exception {
        return handler;
    }

}
