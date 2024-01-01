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

/**
 * @since 6.3.0
 */
public class EmbedConfig extends AbstractParameters {

    private static final ParameterKey session;
    private static final ParameterKey exposals;

    private static final ParameterKey[] parameterKeys;

    static {
        session = new ParameterKey("session", SessionManagerConfig.class);
        exposals = new ParameterKey("exposals", ExposalsConfig.class);

        parameterKeys = new ParameterKey[] {
                session,
                exposals
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
