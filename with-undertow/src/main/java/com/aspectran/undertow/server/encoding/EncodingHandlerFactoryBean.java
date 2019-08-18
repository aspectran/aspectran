package com.aspectran.undertow.server.encoding;

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.encoding.EncodingHandler;

/**
 * <p>Created: 2019-08-18</p>
 */
public class EncodingHandlerFactoryBean extends EncodingHandlerFactory
        implements InitializableBean, FactoryBean<HttpHandler> {

    private EncodingHandler encodingHandler;

    @Override
    public void initialize() throws Exception {
        encodingHandler = createEncodingHandler();
    }

    @Override
    public EncodingHandler getObject() throws Exception {
        return encodingHandler;
    }

}
