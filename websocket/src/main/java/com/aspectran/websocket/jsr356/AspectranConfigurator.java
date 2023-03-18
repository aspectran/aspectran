/*
 * Copyright (c) 2008-2023 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.websocket.jsr356;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import com.aspectran.web.service.WebServiceHolder;

import javax.websocket.server.ServerEndpointConfig.Configurator;

/**
 * A {@link Configurator} for initializing ServerEndpoint-annotated
 * classes through Aspectran.
 *
 * <p>Created: 01/10/2019</p>
 */
public class AspectranConfigurator extends Configurator {

    private static final Logger logger = LoggerFactory.getLogger(AspectranConfigurator.class);

    @Override
    public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
        ActivityContext context = WebServiceHolder.getCurrentActivityContext();
        if (context == null) {
            String message = "Failed to find WebService";
            logger.error(message);
            throw new IllegalStateException(message);
        }
        T endpoint = context.getBeanRegistry().getBean(endpointClass);
        if (logger.isTraceEnabled()) {
            logger.trace("Using @ServerEndpoint singleton " + endpoint);
        }
        return endpoint;
    }

}
