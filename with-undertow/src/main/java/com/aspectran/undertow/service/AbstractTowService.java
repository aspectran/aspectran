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
package com.aspectran.undertow.service;

import com.aspectran.core.context.config.AcceptablesConfig;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.context.config.WebConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.core.service.DefaultCoreService;
import com.aspectran.core.service.ServiceAcceptables;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Abstract base class for {@code TowService} implementations.
 *
 * <p>Created: 2019-07-27</p>
 */
public abstract class AbstractTowService extends DefaultCoreService implements TowService {

    private String uriDecoding;

    private boolean trailingSlashRedirect;

    AbstractTowService(CoreService parentService, boolean derived) {
        super(parentService, derived);
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
        WebConfig webConfig = aspectranConfig.getWebConfig();
        if (webConfig != null) {
            configure(webConfig);
        }
        super.configure(aspectranConfig);
    }

    protected void configure(@NonNull WebConfig webConfig) {
        setUriDecoding(webConfig.getUriDecoding());
        setTrailingSlashRedirect(webConfig.isTrailingSlashRedirect());
        AcceptablesConfig acceptablesConfig = webConfig.getAcceptablesConfig();
        if (acceptablesConfig != null) {
            setServiceAcceptables(new ServiceAcceptables(acceptablesConfig));
        }
    }

}
