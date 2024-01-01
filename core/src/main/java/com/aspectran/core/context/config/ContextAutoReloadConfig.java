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

import com.aspectran.core.context.rule.type.AutoReloadType;
import com.aspectran.utils.apon.AbstractParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;

public class ContextAutoReloadConfig extends AbstractParameters {

    /**
     * The reload mode, which is either "hard" or "soft".
     */
    private static final ParameterKey reloadMode;

    /**
     * The interval in seconds between scanning the specified resources for file changes.
     * If file changes are detected, the activity context is reloaded.
     */
    private static final ParameterKey scanIntervalSeconds;

    /**
     *  Defaults to {@code false}, which disables automatic reloading.
     */
    private static final ParameterKey enabled;

    private static final ParameterKey[] parameterKeys;

    static {
        reloadMode = new ParameterKey("reloadMode", ValueType.STRING);
        scanIntervalSeconds = new ParameterKey("scanIntervalSeconds", ValueType.INT);
        enabled = new ParameterKey("enabled", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                reloadMode,
                scanIntervalSeconds,
                enabled
        };
    }

    public ContextAutoReloadConfig() {
        super(parameterKeys);
    }

    public String getReloadMode() {
        return getString(reloadMode);
    }

    public ContextAutoReloadConfig setReloadMode(AutoReloadType autoReloadType) {
        putValue(reloadMode, autoReloadType.toString());
        return this;
    }

    public int getScanIntervalSeconds() {
        return getInt(scanIntervalSeconds, -1);
    }

    public ContextAutoReloadConfig setScanIntervalSeconds(int scanIntervalSeconds) {
        putValue(ContextAutoReloadConfig.scanIntervalSeconds, scanIntervalSeconds);
        return this;
    }

    public boolean isEnabled() {
        return getBoolean(enabled, false);
    }

    public ContextAutoReloadConfig setEnabled(boolean enabled) {
        putValue(ContextAutoReloadConfig.enabled, enabled);
        return this;
    }

}
