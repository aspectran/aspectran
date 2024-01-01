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
package com.aspectran.core.context.config;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class WebConfig extends AbstractParameters {

    private static final ParameterKey uriDecoding;
    private static final ParameterKey defaultServletName;
    private static final ParameterKey trailingSlashRedirect;
    private static final ParameterKey exposals;

    private static final ParameterKey[] parameterKeys;

    static {
        uriDecoding = new ParameterKey("uriDecoding", ValueType.STRING);
        defaultServletName = new ParameterKey("defaultServletName", ValueType.STRING);
        trailingSlashRedirect = new ParameterKey("trailingSlashRedirect", ValueType.BOOLEAN);
        exposals = new ParameterKey("exposals", ExposalsConfig.class);

        parameterKeys = new ParameterKey[] {
                uriDecoding,
                defaultServletName,
                trailingSlashRedirect,
                exposals
        };
    }

    public WebConfig() {
        super(parameterKeys);
    }

    public String getUriDecoding() {
        return getString(uriDecoding);
    }

    public WebConfig setUriDecoding(String uriDecoding) {
        putValue(WebConfig.uriDecoding, uriDecoding);
        return this;
    }

    public String getDefaultServletName() {
        return getString(defaultServletName);
    }

    public WebConfig setDefaultServletName(String defaultServletName) {
        putValue(WebConfig.defaultServletName, defaultServletName);
        return this;
    }

    public boolean isTrailingSlashRedirect() {
        return getBoolean(trailingSlashRedirect, false);
    }

    public WebConfig setTrailingSlashRedirect(boolean trailingSlashRedirect) {
        putValue(WebConfig.trailingSlashRedirect, trailingSlashRedirect);
        return this;
    }

    public ExposalsConfig getExposalsConfig() {
        return getParameters(exposals);
    }

    public ExposalsConfig newExposalsConfig() {
        return newParameters(exposals);
    }

    public ExposalsConfig touchExposalsConfig() {
        return touchParameters(exposals);
    }

}
