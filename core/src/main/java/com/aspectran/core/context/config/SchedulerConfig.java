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
 * Configuration for the job scheduler.
 */
public class SchedulerConfig extends DefaultParameters {

    /** The delay in seconds before the scheduler starts. */
    private static final ParameterKey startDelaySeconds;

    /** Whether to wait for running jobs to complete on shutdown. */
    private static final ParameterKey waitOnShutdown;

    /** Whether the scheduler is enabled. */
    private static final ParameterKey enabled;

    private static final ParameterKey[] parameterKeys;

    static {
        startDelaySeconds = new ParameterKey("startDelaySeconds", ValueType.INT);
        waitOnShutdown = new ParameterKey("waitOnShutdown", ValueType.BOOLEAN);
        enabled = new ParameterKey("enabled", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                startDelaySeconds,
                waitOnShutdown,
                enabled
        };
    }

    /**
     * Instantiates a new SchedulerConfig.
     */
    public SchedulerConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the delay in seconds before the scheduler starts.
     * @return the start delay in seconds, or -1 if not set
     */
    public int getStartDelaySeconds() {
        return getInt(startDelaySeconds, -1);
    }

    /**
     * Returns whether the start delay is set.
     * @return true if the start delay is set, false otherwise
     */
    public boolean hasStartDelaySeconds() {
        return hasValue(startDelaySeconds);
    }

    /**
     * Sets the delay in seconds before the scheduler starts.
     * @param startDelaySeconds the start delay in seconds
     * @return this {@code SchedulerConfig} instance
     */
    public SchedulerConfig setStartDelaySeconds(int startDelaySeconds) {
        putValue(SchedulerConfig.startDelaySeconds, startDelaySeconds);
        return this;
    }

    /**
     * Returns whether to wait for running jobs to complete on shutdown.
     * @return true to wait, false otherwise
     */
    public boolean isWaitOnShutdown() {
        return getBoolean(waitOnShutdown, false);
    }

    /**
     * Returns whether the wait-on-shutdown flag is set.
     * @return true if the flag is set, false otherwise
     */
    public boolean hasWaitOnShutdown() {
        return hasValue(waitOnShutdown);
    }

    /**
     * Sets whether to wait for running jobs to complete on shutdown.
     * @param waitOnShutdown true to wait, false otherwise
     * @return this {@code SchedulerConfig} instance
     */
    public SchedulerConfig setWaitOnShutdown(boolean waitOnShutdown) {
        putValue(SchedulerConfig.waitOnShutdown, waitOnShutdown);
        return this;
    }

    /**
     * Returns whether the scheduler is enabled.
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return getBoolean(enabled, false);
    }

    /**
     * Sets whether the scheduler is enabled.
     * @param enabled true to enable, false to disable
     * @return this {@code SchedulerConfig} instance
     */
    public SchedulerConfig setEnabled(boolean enabled) {
        putValue(SchedulerConfig.enabled, enabled);
        return this;
    }

}
