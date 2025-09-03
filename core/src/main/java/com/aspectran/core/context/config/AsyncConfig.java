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
 * Config for async task executor.
 *
 * <p>Created: 2024. 8. 24.</p>
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

    public AsyncConfig() {
        super(parameterKeys);
    }

    public boolean isEnabled() {
        return getBoolean(enabled, false);
    }

    public AsyncConfig setEnabled(boolean enabled) {
        putValue(AsyncConfig.enabled, enabled);
        return this;
    }

}
