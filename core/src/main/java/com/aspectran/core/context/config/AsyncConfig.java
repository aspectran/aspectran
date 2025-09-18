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
 * Configuration for the asynchronous task executor.
 * <p>This class allows enabling or disabling the async feature.
 *
 * <p>Created: 2024. 8. 24.</p>
 * @since 9.0.0
 */
public class AsyncConfig extends AbstractParameters {

    private static final ParameterKey enabled;

    private static final ParameterKey[] parameterKeys;

    static {
        enabled = new ParameterKey("enabled", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                enabled
        };
    }

    /**
     * Instantiates a new AsyncConfig.
     */
    public AsyncConfig() {
        super(parameterKeys);
    }

    /**
     * Returns whether the async feature is enabled.
     * @return true if the async feature is enabled, false otherwise
     */
    public boolean isEnabled() {
        return getBoolean(enabled, false);
    }

    /**
     * Sets whether to enable the async feature.
     * @param enabled true to enable, false to disable
     * @return this {@code AsyncConfig} instance
     */
    public AsyncConfig setEnabled(boolean enabled) {
        putValue(AsyncConfig.enabled, enabled);
        return this;
    }

}
