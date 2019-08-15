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
package com.aspectran.undertow.server.http;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.util.Assert;
import com.aspectran.undertow.service.AspectranTowService;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.server.handlers.resource.ResourceSupplier;
import io.undertow.server.session.SessionCookieConfig;

/**
 * Implementation of Undertow {@link io.undertow.server.HttpHandler} to handle HTTP
 * requests as activities of Aspectran.
 *
 * <p>Created: 2019-07-27</p>
 */
public class DefaultHttpHandler extends AbstractHttpHandler implements InitializableBean, DisposableBean {

    private volatile AspectranTowService towService;

    public DefaultHttpHandler(ResourceManager resourceManager) {
        super(resourceManager);
    }

    public DefaultHttpHandler(ResourceManager resourceManager, HttpHandler next) {
        super(resourceManager, next);
    }

    public DefaultHttpHandler(ResourceSupplier resourceSupplier) {
        super(resourceSupplier);
    }

    public DefaultHttpHandler(ResourceSupplier resourceSupplier, HttpHandler next) {
        super(resourceSupplier, next);
    }

    public AspectranTowService getTowService() {
        Assert.state(towService != null, "No AspectranTowService configured");
        return towService;
    }

    @Override
    public void initialize() throws Exception {
        Assert.state(towService == null, "Cannot reconfigure AspectranTowService");
        if (getSessionManager() != null) {
            if (getSessionConfig() == null) {
                setSessionConfig(new SessionCookieConfig());
            }
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
