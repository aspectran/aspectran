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
 * Configuration for the daemon's command executor thread pool.
 * @since 5.1.0
 */
public class DaemonExecutorConfig extends AbstractParameters {

    private static final ParameterKey maxThreads;

    private static final ParameterKey[] parameterKeys;

    static {
        maxThreads = new ParameterKey("maxThreads", ValueType.INT);

        parameterKeys = new ParameterKey[] {
                maxThreads
        };
    }

    public DaemonExecutorConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the maximum number of threads in the pool.
     * @param defaultMaxThreads the default value to return if maxThreads is not set
     * @return the maximum number of threads
     */
    public int getMaxThreads(int defaultMaxThreads) {
        return getInt(maxThreads, defaultMaxThreads);
    }

    /**
     * Sets the maximum number of threads in the pool.
     * @param maxThreads the maximum number of threads
     * @return this {@code DaemonExecutorConfig} instance
     */
    public DaemonExecutorConfig setMaxThreads(int maxThreads) {
        putValue(DaemonExecutorConfig.maxThreads, maxThreads);
        return this;
    }

}
