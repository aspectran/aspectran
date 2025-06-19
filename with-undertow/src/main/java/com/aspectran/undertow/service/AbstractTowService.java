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

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for {@code TowService} implementations.
 *
 * <p>Created: 2019-07-27</p>
 */
public abstract class AbstractTowService extends DefaultCoreService implements TowService {

    private String uriDecoding;

    private boolean trailingSlashRedirect;

    private boolean sessionAdaptable = true;

    AbstractTowService(CoreService parentService, boolean derived) {
        super(parentService, derived);
    }

    @Override
    public boolean isSessionAdaptable() {
        return sessionAdaptable;
    }

    public void setSessionAdaptable(boolean sessionAdaptable) {
        this.sessionAdaptable = sessionAdaptable;
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

    @Override
    protected void configure(@NonNull AspectranConfig aspectranConfig) {
        super.configure(aspectranConfig);

        List<WebConfig> webConfigList = new ArrayList<>();
        if (aspectranConfig.hasWebConfig()) {
            webConfigList.add(aspectranConfig.getWebConfig());
        }
        for (CoreService parentService = getParentService();
             parentService != null; parentService = parentService.getParentService()) {
            webConfigList.add(0, parentService.getAspectranConfig().getWebConfig());
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
