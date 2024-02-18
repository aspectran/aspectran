/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import com.aspectran.core.adapter.ApplicationAdapter;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.ExposalsConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.service.AspectranCoreService;
import com.aspectran.core.service.AspectranServiceException;
import com.aspectran.core.service.CoreService;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import jakarta.servlet.ServletContext;

public abstract class AbstractWebService extends AspectranCoreService implements WebService {

    private final String contextPath;

    private final ServletContext servletContext;

    private DefaultServletHttpRequestHandler defaultServletHttpRequestHandler;

    private String uriDecoding;

    private boolean trailingSlashRedirect;

    AbstractWebService(@NonNull ServletContext servletContext) {
        this(servletContext, null);
    }

    protected AbstractWebService(@NonNull ServletContext servletContext, @Nullable CoreService rootService) {
        super(rootService);
        this.contextPath = StringUtils.emptyToNull(servletContext.getContextPath());
        this.servletContext = servletContext;
    }

    public String getContextPath() {
        return contextPath;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public DefaultServletHttpRequestHandler getDefaultServletHttpRequestHandler() {
        return defaultServletHttpRequestHandler;
    }

    public String getUriDecoding() {
        return uriDecoding;
    }

    public boolean isTrailingSlashRedirect() {
        return trailingSlashRedirect;
    }

    @Override
    protected void prepare(@NonNull AspectranConfig aspectranConfig) throws AspectranServiceException {
        prepare(aspectranConfig, null);
    }

    @Override
    protected void prepare(@NonNull AspectranConfig aspectranConfig, ApplicationAdapter applicationAdapter)
        throws AspectranServiceException {
        this.defaultServletHttpRequestHandler = new DefaultServletHttpRequestHandler(servletContext);

        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            applyWebConfig(webConfig);
        }

        if (isDerived()) {
            setBasePath(getRootService().getBasePath());
            setServiceClassLoader(new WebServiceClassLoader(getRootService().getActivityContext().getClassLoader()));
        } else {
            setBasePath(servletContext.getRealPath("/"));
            super.prepare(aspectranConfig, applicationAdapter);
        }
    }

    @Override
    protected void afterContextLoaded() throws Exception {
        super.afterContextLoaded();
        setServiceClassLoader(new WebServiceClassLoader(getActivityContext().getClassLoader()));
    }

    private void applyWebConfig(@NonNull WebConfig webConfig) {
        this.uriDecoding = webConfig.getUriDecoding();

        String defaultServletName = webConfig.getDefaultServletName();
        if (defaultServletName != null) {
            if (!"none".equals(defaultServletName)) {
                this.defaultServletHttpRequestHandler.setDefaultServletName(defaultServletName);
            }
        } else {
            this.defaultServletHttpRequestHandler.lookupDefaultServletName();
        }

        this.trailingSlashRedirect = webConfig.isTrailingSlashRedirect();

        ExposalsConfig exposalsConfig = webConfig.getExposalsConfig();
        if (exposalsConfig != null) {
            String[] includePatterns = exposalsConfig.getIncludePatterns();
            String[] excludePatterns = exposalsConfig.getExcludePatterns();
            setExposals(includePatterns, excludePatterns);
        }
    }

}
