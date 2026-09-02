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

import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.config.AcceptableConfig;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.DefaultCoreService;
import com.aspectran.core.service.RequestAcceptor;
import com.aspectran.netty.server.session.NettySessionConfig;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for {@link NettyService} implementations.
 * <p>Extends {@link DefaultCoreService} and provides configuration for WebConfig,
 * URI decoding, trailing slash redirects, and session management in Netty.</p>
 *
 * <p>Created: 2026-09-02</p>
 */
public abstract class AbstractNettyService extends DefaultCoreService implements NettyService {

    private boolean sessionAdaptable = true;

    private SessionManager sessionManager;

    private NettySessionConfig sessionConfig = new NettySessionConfig();

    private String contextPath = "";

    private String uriDecoding;

    private boolean trailingSlashRedirect;

    protected AbstractNettyService(CoreService parentService, boolean derived) {
        super(parentService, derived);
    }

    @Override
    public boolean isSessionAdaptable() {
        return sessionAdaptable;
    }

    public void setSessionAdaptable(boolean sessionAdaptable) {
        this.sessionAdaptable = sessionAdaptable;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public NettySessionConfig getSessionConfig() {
        return sessionConfig;
    }

    public void setSessionConfig(NettySessionConfig sessionConfig) {
        this.sessionConfig = (sessionConfig != null ? sessionConfig : new NettySessionConfig());
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public void setContextPath(String contextPath) {
        if (contextPath != null && !contextPath.isEmpty() && !contextPath.equals("/")) {
            if (!contextPath.startsWith("/")) {
                contextPath = "/" + contextPath;
            }
            if (contextPath.endsWith("/")) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }
            this.contextPath = contextPath;
        } else {
            this.contextPath = "";
        }
    }

    public String getUriDecoding() {
        return uriDecoding;
    }

    protected void setUriDecoding(String uriDecoding) {
        this.uriDecoding = uriDecoding;
    }

    public boolean isTrailingSlashRedirect() {
        return trailingSlashRedirect;
    }

    public void setTrailingSlashRedirect(boolean trailingSlashRedirect) {
        this.trailingSlashRedirect = trailingSlashRedirect;
    }

    @Override
    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        super.configure(aspectranConfig);

        List<WebConfig> webConfigList = new ArrayList<>();
        if (aspectranConfig.hasWebConfig()) {
            webConfigList.add(aspectranConfig.getWebConfig());
        }
        for (CoreService parentService = getParentService();
             parentService != null; parentService = parentService.getParentService()) {
            if (parentService.getAspectranConfig() != null && parentService.getAspectranConfig().hasWebConfig()) {
                webConfigList.addFirst(parentService.getAspectranConfig().getWebConfig());
            }
        }
        for (WebConfig webConfig : webConfigList) {
            configure(webConfig);
        }
    }

    protected void configure(@NonNull WebConfig webConfig) {
        setUriDecoding(webConfig.getUriDecoding());

        if (webConfig.hasTrailingSlashRedirect()) {
            setTrailingSlashRedirect(webConfig.isTrailingSlashRedirect());
        }

        AcceptableConfig acceptableConfig = webConfig.getAcceptableConfig();
        if (acceptableConfig != null) {
            setRequestAcceptor(new RequestAcceptor(acceptableConfig));
        }
    }

}
