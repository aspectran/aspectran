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
package com.aspectran.undertow.service;

import com.aspectran.core.context.config.AcceptableConfig;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.DefaultCoreService;
import com.aspectran.core.service.RequestAcceptor;
import com.aspectran.utils.annotation.jsr305.NonNull;
import io.undertow.server.handlers.resource.ResourceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for {@link TowService} implementations.
 * <p>This class extends {@link DefaultCoreService} and provides common infrastructure
 * for running Aspectran on an embedded Undertow server. It handles web-specific configurations
 * from {@link WebConfig} and provides properties for URI decoding, trailing slash redirects,
 * and session adaptability.</p>
 */
public abstract class AbstractTowService extends DefaultCoreService implements TowService {

    private ResourceManager resourceManager;

    private boolean sessionAdaptable = true;

    private String uriDecoding;

    private boolean trailingSlashRedirect;

    /**
     * Instantiates a new AbstractTowService.
     * @param parentService the parent core service
     * @param derived whether this service is derived from a parent
     */
    AbstractTowService(CoreService parentService, boolean derived) {
        super(parentService, derived);
    }

    @Override
    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    /**
     * Sets the resource manager for serving static files.
     * @param resourceManager the static resource manager
     */
    protected void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    @Override
    public boolean isSessionAdaptable() {
        return sessionAdaptable;
    }

    /**
     * Sets whether Undertow's session management should be adapted and made available to translets.
     * @param sessionAdaptable true to enable session adaptation; false otherwise
     */
    public void setSessionAdaptable(boolean sessionAdaptable) {
        this.sessionAdaptable = sessionAdaptable;
    }

    /**
     * Returns the character encoding for decoding the URI.
     * @return the URI character encoding
     */
    public String getUriDecoding() {
        return uriDecoding;
    }

    /**
     * Sets the character encoding for decoding the URI.
     * @param uriDecoding the URI character encoding
     */
    protected void setUriDecoding(String uriDecoding) {
        this.uriDecoding = uriDecoding;
    }

    /**
     * Returns whether to redirect requests with a trailing slash.
     * @return true if trailing slash redirect is enabled, false otherwise
     */
    public boolean isTrailingSlashRedirect() {
        return trailingSlashRedirect;
    }

    /**
     * Sets whether to redirect requests with a trailing slash.
     * @param trailingSlashRedirect true to enable trailing slash redirect; false otherwise
     */
    protected void setTrailingSlashRedirect(boolean trailingSlashRedirect) {
        this.trailingSlashRedirect = trailingSlashRedirect;
    }

    /**
     * Overrides the default configuration process to apply web-specific settings.
     * @param aspectranConfig the main Aspectran configuration
     */
    @Override
    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        super.configure(aspectranConfig);

        List<WebConfig> webConfigList = new ArrayList<>();
        if (aspectranConfig.hasWebConfig()) {
            webConfigList.add(aspectranConfig.getWebConfig());
        }
        for (CoreService parentService = getParentService();
             parentService != null; parentService = parentService.getParentService()) {
            webConfigList.addFirst(parentService.getAspectranConfig().getWebConfig());
        }
        for (WebConfig webConfig : webConfigList) {
            configure(webConfig);
        }
    }

    /**
     * Applies settings from a {@link WebConfig} object to this service.
     * @param webConfig the web configuration to apply
     */
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
