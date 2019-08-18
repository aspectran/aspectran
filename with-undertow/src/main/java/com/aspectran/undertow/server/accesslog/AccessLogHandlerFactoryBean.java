package com.aspectran.undertow.server.accesslog;

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;

/**
 * <p>Created: 2019-08-18</p>
 */
public class AccessLogHandlerFactoryBean extends AccessLogHandlerFactory
        implements InitializableBean, FactoryBean<HttpHandler> {

    private AccessLogHandler accessLogHandler;

    @Override
    public void initialize() throws Exception {
        accessLogHandler = createAccessLogHandler();
    }

    @Override
    public AccessLogHandler getObject() throws Exception {
        return accessLogHandler;
    }

}
