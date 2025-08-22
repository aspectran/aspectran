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
package com.aspectran.core.context.config;

import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Contains web-specific configuration settings.
 */
public class WebConfig extends AbstractParameters {

    private static final ParameterKey uriDecoding;
    private static final ParameterKey defaultServletName;
    private static final ParameterKey trailingSlashRedirect;
    private static final ParameterKey legacyHeadHandling;
    private static final ParameterKey acceptable;

    private static final ParameterKey[] parameterKeys;

    static {
        uriDecoding = new ParameterKey("uriDecoding", ValueType.STRING);
        defaultServletName = new ParameterKey("defaultServletName", ValueType.STRING);
        trailingSlashRedirect = new ParameterKey("trailingSlashRedirect", ValueType.BOOLEAN);
        legacyHeadHandling = new ParameterKey("legacyHeadHandling", ValueType.BOOLEAN);
        acceptable = new ParameterKey("acceptable", AcceptableConfig.class);

        parameterKeys = new ParameterKey[] {
                uriDecoding,
                defaultServletName,
                trailingSlashRedirect,
                legacyHeadHandling,
                acceptable
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

    public boolean hasTrailingSlashRedirect() {
        return hasValue(trailingSlashRedirect);
    }

    public boolean isTrailingSlashRedirect() {
        return getBoolean(trailingSlashRedirect, false);
    }

    public WebConfig setTrailingSlashRedirect(boolean trailingSlashRedirect) {
        putValue(WebConfig.trailingSlashRedirect, trailingSlashRedirect);
        return this;
    }

    public boolean hasLegacyHeadHandling() {
        return hasValue(legacyHeadHandling);
    }

    public boolean isLegacyHeadHandling() {
        return getBoolean(legacyHeadHandling, false);
    }

    public WebConfig setLegacyHeadHandling(boolean legacyHeadHandling) {
        putValue(WebConfig.legacyHeadHandling, legacyHeadHandling);
        return this;
    }

    /**
     * Returns the configuration for acceptable request patterns.
     * @return the {@code AcceptableConfig} instance
     */
    public AcceptableConfig getAcceptableConfig() {
        return getParameters(acceptable);
    }

    /**
     * Creates a new configuration for acceptable request patterns.
     * @return the new {@code AcceptableConfig} instance
     */
    public AcceptableConfig newAcceptableConfig() {
        return newParameters(acceptable);
    }

    /**
     * Returns the existing configuration for acceptable request patterns
     * or creates a new one if it does not exist.
     * @return a non-null {@code AcceptableConfig} instance
     */
    public AcceptableConfig touchAcceptableConfig() {
        return touchParameters(acceptable);
    }

}
