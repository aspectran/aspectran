/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.web.service;

import com.aspectran.core.context.config.AcceptableConfig;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.DefaultCoreService;
import com.aspectran.core.service.RequestAcceptor;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import jakarta.servlet.ServletContext;

public abstract class AbstractWebService extends DefaultCoreService implements WebService {

    private final String contextPath;

    private final ServletContext servletContext;

    private final DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;

    private String uriDecoding;

    private boolean trailingSlashRedirect;

    private boolean legacyHeadHandling;

    AbstractWebService(@NonNull ServletContext servletContext, @Nullable CoreService parentService, boolean derived) {
        super(parentService, derived);
        this.contextPath = StringUtils.emptyToNull(servletContext.getContextPath());
        this.servletContext = servletContext;
        this.defaultServletHttpRequestHandler = new DefaultServletHttpRequestHandler(servletContext, this);
    }

    @Nullable
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @NonNull
    public DefaultServletHttpRequestHandler getDefaultServletHttpRequestHandler() {
        return defaultServletHttpRequestHandler;
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

    protected void setTrailingSlashRedirect(boolean trailingSlashRedirect) {
        this.trailingSlashRedirect = trailingSlashRedirect;
    }

    public boolean isLegacyHeadHandling() {
        return legacyHeadHandling;
    }

    protected void setLegacyHeadHandling(boolean legacyHeadHandling) {
        this.legacyHeadHandling = legacyHeadHandling;
    }

    @Override
    protected void afterContextLoaded() throws Exception {
        super.afterContextLoaded();
        setServiceClassLoader(new WebServiceClassLoader(getActivityContext().getClassLoader()));
    }

    @Override
    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        if (!isDerived()) {
            if (getBasePath() == null) {
                setBasePath(servletContext.getRealPath("/"));
            }
            super.configure(aspectranConfig);
        }

        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            configure(webConfig);
        }

        if (getParentService() != null && isDerived()) {
            setServiceClassLoader(new WebServiceClassLoader(getParentService().getActivityContext().getClassLoader()));
        }
    }

    private void configure(@NonNull WebConfig webConfig) {
        setUriDecoding(webConfig.getUriDecoding());

        String defaultServletName = webConfig.getDefaultServletName();
        if (defaultServletName != null) {
            this.defaultServletHttpRequestHandler.setDefaultServletName(defaultServletName);
        }

        setTrailingSlashRedirect(webConfig.isTrailingSlashRedirect());
        setLegacyHeadHandling(webConfig.isLegacyHeadHandling());

        AcceptableConfig acceptableConfig = webConfig.getAcceptableConfig();
        if (acceptableConfig != null) {
            setRequestAcceptor(new RequestAcceptor(acceptableConfig));
        }
    }

}
