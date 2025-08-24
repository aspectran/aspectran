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

/**
 * Config for async task executor.
 *
 * <p>Created: 2024. 8. 24.</p>
 */
public class AsyncConfig extends AbstractParameters {

    private static final ParameterKey corePoolSize;
    private static final ParameterKey maxPoolSize;
    private static final ParameterKey keepAliveSeconds;
    private static final ParameterKey queueCapacity;
    private static final ParameterKey waitForTasksToCompleteOnShutdown;
    private static final ParameterKey enabled;

    private static final ParameterKey[] parameterKeys;

    static {
        corePoolSize = new ParameterKey("corePoolSize", ValueType.INT);
        maxPoolSize = new ParameterKey("maxPoolSize", ValueType.INT);
        keepAliveSeconds = new ParameterKey("keepAliveSeconds", ValueType.INT);
        queueCapacity = new ParameterKey("queueCapacity", ValueType.INT);
        waitForTasksToCompleteOnShutdown = new ParameterKey("waitForTasksToCompleteOnShutdown", ValueType.BOOLEAN);
        enabled = new ParameterKey("enabled", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                queueCapacity,
                waitForTasksToCompleteOnShutdown,
                enabled
        };
    }

    public AsyncConfig() {
        super(parameterKeys);
    }

    public int getCorePoolSize() {
        return getInt(corePoolSize, -1);
    }

    public AsyncConfig setCorePoolSize(int corePoolSize) {
        putValue(AsyncConfig.corePoolSize, corePoolSize);
        return this;
    }

    public int getMaxPoolSize() {
        return getInt(maxPoolSize, -1);
    }

    public AsyncConfig setMaxPoolSize(int maxPoolSize) {
        putValue(AsyncConfig.maxPoolSize, maxPoolSize);
        return this;
    }

    public int getKeepAliveSeconds() {
        return getInt(keepAliveSeconds, -1);
    }

    public AsyncConfig setKeepAliveSeconds(int keepAliveSeconds) {
        putValue(AsyncConfig.keepAliveSeconds, keepAliveSeconds);
        return this;
    }

    public int getQueueCapacity() {
        return getInt(queueCapacity, -1);
    }

    public AsyncConfig setQueueCapacity(int queueCapacity) {
        putValue(AsyncConfig.queueCapacity, queueCapacity);
        return this;
    }

    public boolean isWaitForTasksToCompleteOnShutdown() {
        return getBoolean(waitForTasksToCompleteOnShutdown, false);
    }

    public AsyncConfig setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        putValue(AsyncConfig.waitForTasksToCompleteOnShutdown, waitForTasksToCompleteOnShutdown);
        return this;
    }

    public boolean isEnabled() {
        return getBoolean(enabled, false);
    }

    public AsyncConfig setEnabled(boolean enabled) {
        putValue(AsyncConfig.enabled, enabled);
        return this;
    }

}
