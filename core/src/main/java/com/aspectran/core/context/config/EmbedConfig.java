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

/**
 * Configuration for running Aspectran in an embedded mode within another application.
 *
 * @since 6.3.0
 */
public class EmbedConfig extends DefaultParameters {

    /** The configuration for the session manager. */
    private static final ParameterKey session;

    /** The configuration for acceptable request patterns. */
    private static final ParameterKey acceptable;

    private static final ParameterKey[] parameterKeys;

    static {
        session = new ParameterKey("session", SessionManagerConfig.class);
        acceptable = new ParameterKey("acceptable", AcceptableConfig.class);

        parameterKeys = new ParameterKey[] {
                session,
                acceptable
        };
    }

    /**
     * Instantiates a new EmbedConfig.
     */
    public EmbedConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the session manager configuration.
     * @return the {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig getSessionManagerConfig() {
        return getParameters(session);
    }

    /**
     * Creates a new session manager configuration.
     * @return the new {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig newSessionManagerConfig() {
        return newParameters(session);
    }

    /**
     * Returns the existing session manager configuration or creates a new one if it does not exist.
     * @return a non-null {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig touchSessionManagerConfig() {
        return touchParameters(session);
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
