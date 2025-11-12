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

import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

/**
 * Contains web-specific configuration settings.
 */
public class WebConfig extends DefaultParameters {

    /** The character encoding for URI decoding. */
    private static final ParameterKey uriDecoding;

    /** The name of the default servlet. */
    private static final ParameterKey defaultServletName;

    /** Whether to redirect requests with a trailing slash. */
    private static final ParameterKey trailingSlashRedirect;

    /** Whether to enable legacy HEAD request handling. */
    private static final ParameterKey legacyHeadHandling;

    /** The configuration for acceptable request patterns. */
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

    /**
     * Instantiates a new WebConfig.
     */
    public WebConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the character encoding for URI decoding.
     * @return the URI decoding
     */
    public String getUriDecoding() {
        return getString(uriDecoding);
    }

    /**
     * Sets the character encoding for URI decoding.
     * @param uriDecoding the URI decoding
     * @return this {@code WebConfig} instance
     */
    public WebConfig setUriDecoding(String uriDecoding) {
        putValue(WebConfig.uriDecoding, uriDecoding);
        return this;
    }

    /**
     * Returns the name of the default servlet.
     * @return the default servlet name
     */
    public String getDefaultServletName() {
        return getString(defaultServletName);
    }

    /**
     * Sets the name of the default servlet.
     * @param defaultServletName the default servlet name
     * @return this {@code WebConfig} instance
     */
    public WebConfig setDefaultServletName(String defaultServletName) {
        putValue(WebConfig.defaultServletName, defaultServletName);
        return this;
    }

    /**
     * Returns whether the trailing slash redirect setting is present.
     * @return true if the setting is present, false otherwise
     */
    public boolean hasTrailingSlashRedirect() {
        return hasValue(trailingSlashRedirect);
    }

    /**
     * Returns whether to redirect requests with a trailing slash.
     * @return true to redirect, false otherwise
     */
    public boolean isTrailingSlashRedirect() {
        return getBoolean(trailingSlashRedirect, false);
    }

    /**
     * Sets whether to redirect requests with a trailing slash.
     * @param trailingSlashRedirect true to redirect, false otherwise
     * @return this {@code WebConfig} instance
     */
    public WebConfig setTrailingSlashRedirect(boolean trailingSlashRedirect) {
        putValue(WebConfig.trailingSlashRedirect, trailingSlashRedirect);
        return this;
    }

    /**
     * Returns whether legacy HEAD request handling is enabled.
     * @return true if enabled, false otherwise
     */
    public boolean hasLegacyHeadHandling() {
        return hasValue(legacyHeadHandling);
    }

    /**
     * Returns whether to use legacy HEAD request handling.
     * @return true for legacy handling, false otherwise
     */
    public boolean isLegacyHeadHandling() {
        return getBoolean(legacyHeadHandling, false);
    }

    /**
     * Sets whether to use legacy HEAD request handling.
     * @param legacyHeadHandling true for legacy handling, false otherwise
     * @return this {@code WebConfig} instance
     */
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
