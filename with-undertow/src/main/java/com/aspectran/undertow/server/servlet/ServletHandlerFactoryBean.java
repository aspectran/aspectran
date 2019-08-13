package com.aspectran.undertow.server.servlet;

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import io.undertow.server.HttpHandler;

/**
 * <p>Created: 2019-08-04</p>
 */
public class ServletHandlerFactoryBean extends ServletHandlerFactory
        implements InitializableBean, FactoryBean<HttpHandler> {

    private HttpHandler servletHandler;

    @Override
    public void initialize() throws Exception {
        servletHandler = createServletHandler();
    }

    @Override
    public HttpHandler getObject() throws Exception {
        return servletHandler;
    }

}
