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

/**
 * @since 6.3.0
 */
public class EmbedConfig extends AbstractParameters {

    private static final ParameterKey session;
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

    public EmbedConfig() {
        super(parameterKeys);
    }

    public SessionManagerConfig getSessionManagerConfig() {
        return getParameters(session);
    }

    public SessionManagerConfig newSessionManagerConfig() {
        return newParameters(session);
    }

    public SessionManagerConfig touchSessionManagerConfig() {
        return touchParameters(session);
    }

    public AcceptableConfig getAcceptableConfig() {
        return getParameters(acceptable);
    }

    public AcceptableConfig newAcceptableConfig() {
        return newParameters(acceptable);
    }

    public AcceptableConfig touchAcceptableConfig() {
        return touchParameters(acceptable);
    }

}
