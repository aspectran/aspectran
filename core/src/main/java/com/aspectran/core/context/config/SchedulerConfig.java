/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterKey;
import com.aspectran.core.util.apon.ValueType;

public class SchedulerConfig extends AbstractParameters {

    private static final ParameterKey startDelaySeconds;
    private static final ParameterKey waitOnShutdown;
    private static final ParameterKey enabled;
    private static final ParameterKey exposals;

    private static final ParameterKey[] parameterKeys;

    static {
        startDelaySeconds = new ParameterKey("startDelaySeconds", ValueType.INT);
        waitOnShutdown = new ParameterKey("waitOnShutdown", ValueType.BOOLEAN);
        enabled = new ParameterKey("enabled", ValueType.BOOLEAN);
        exposals = new ParameterKey("exposals", ExposalsConfig.class);

        parameterKeys = new ParameterKey[] {
                startDelaySeconds,
                waitOnShutdown,
                enabled,
                exposals
        };
    }

    public SchedulerConfig() {
        super(parameterKeys);
    }

    public int getStartDelaySeconds() {
        return getInt(startDelaySeconds, -1);
    }

    public SchedulerConfig setStartDelaySeconds(int startDelaySeconds) {
        putValue(SchedulerConfig.startDelaySeconds, startDelaySeconds);
        return this;
    }

    public boolean isWaitOnShutdown() {
        return getBoolean(waitOnShutdown, false);
    }

    public SchedulerConfig setWaitOnShutdown(boolean waitOnShutdown) {
        putValue(SchedulerConfig.waitOnShutdown, waitOnShutdown);
        return this;
    }

    public boolean isEnabled() {
        return getBoolean(enabled, false);
    }

    public SchedulerConfig setEnabled(boolean enabled) {
        putValue(SchedulerConfig.enabled, enabled);
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
