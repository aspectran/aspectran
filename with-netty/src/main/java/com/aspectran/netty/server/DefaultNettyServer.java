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
package com.aspectran.netty.server;

import com.aspectran.core.component.bean.ablility.DisposableBean;
import com.aspectran.core.component.bean.ablility.InitializableBean;
import com.aspectran.core.component.bean.aware.ActivityContextAware;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.service.CoreService;
import com.aspectran.netty.server.session.NettySessionManager;
import com.aspectran.netty.service.AbstractNettyService;
import com.aspectran.netty.service.DefaultNettyServiceBuilder;
import com.aspectran.netty.service.NettyService;

/**
 * The default concrete implementation of {@link AbstractNettyServer}.
 * <p>Integrates Netty server lifecycle with Aspectran's bean container
 * via {@link InitializableBean}, {@link DisposableBean}, and {@link ActivityContextAware}.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public class DefaultNettyServer extends AbstractNettyServer
        implements InitializableBean, DisposableBean, ActivityContextAware {

    private SessionManager sessionManager;

    private ActivityContext activityContext;

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        applySessionManager(getNettyService());
    }

    @Override
    public void setActivityContext(ActivityContext context) {
        this.activityContext = context;
        if (getNettyService() == null && getContexts().isEmpty()) {
            CoreService masterService = context.getMasterService();
            if (masterService != null) {
                NettyService service = DefaultNettyServiceBuilder.build(masterService);
                applySessionManager(service);
                setNettyService(service);
            }
        }
    }

    @Override
    public void initialize() throws Exception {
        if (activityContext != null) {
            for (NettyContext context : getContexts()) {
                context.setActivityContext(activityContext);
            }
        }
        applySessionManager(getNettyService());
        if (isAutoStart() && !isRunning()) {
            start();
        }
    }

    private void applySessionManager(NettyService nettyService) {
        if (nettyService != null && sessionManager != null) {
            if (nettyService.getSessionManager() == null) {
                nettyService.setSessionManager(sessionManager);
            }
            if (sessionManager instanceof NettySessionManager nsm && nettyService instanceof AbstractNettyService ans) {
                ans.setSessionConfig(nsm.getSessionConfig());
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (isRunning()) {
            stop();
        }
    }

}
