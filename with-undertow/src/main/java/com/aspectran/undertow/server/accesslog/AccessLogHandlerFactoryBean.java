/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
