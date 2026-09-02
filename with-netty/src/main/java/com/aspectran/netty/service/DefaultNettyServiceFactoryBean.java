/*
 * Copyright (c) 2008-present The Aspectran Project
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
package com.aspectran.netty.service;

import com.aspectran.core.component.bean.ablility.FactoryBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.Assert;

/**
 * {@link FactoryBean} that creates and initializes a {@link DefaultNettyService} instance.
 *
 * <p>Created: 2026-09-02</p>
 */
public class DefaultNettyServiceFactoryBean
        implements FactoryBean<DefaultNettyService>, ActivityContextAware, InitializableBean {

    private ActivityContext context;

    private SessionManager sessionManager;

    private DefaultNettyService nettyService;

    @Override
    public void setActivityContext(ActivityContext context) {
        this.context = context;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void initialize() throws Exception {
        if (nettyService == null) {
            CoreService masterService = (context != null ? context.getMasterService() : null);
            Assert.notNull(masterService, "masterService must not be null");
            nettyService = DefaultNettyServiceBuilder.build(masterService);
            if (sessionManager != null) {
                nettyService.setSessionManager(sessionManager);
            }
        }
    }

    @Override
    public DefaultNettyService getObject() {
        return nettyService;
    }

}
