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

import com.aspectran.core.context.rule.type.AutoReloadType;
import com.aspectran.utils.apon.DefaultParameters;
import com.aspectran.utils.apon.ParameterKey;
import com.aspectran.utils.apon.ValueType;
import org.jspecify.annotations.NonNull;

/**
 * Configuration for the automatic context reloading feature.
 * <p>This allows the application context to be automatically reloaded
 * when configuration files are modified.</p>
 */
public class ContextAutoReloadConfig extends DefaultParameters {

    /** The reload mode, which can be "hard" or "soft". */
    private static final ParameterKey reloadMode;

    /** The interval in seconds for scanning for file changes. */
    private static final ParameterKey scanIntervalSeconds;

    /** Whether to enable automatic reloading. Defaults to {@code false}. */
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

    /**
     * Instantiates a new ContextAutoReloadConfig.
     */
    public ContextAutoReloadConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the reload mode.
     * @return the reload mode ("hard" or "soft")
     */
    public String getReloadMode() {
        return getString(reloadMode);
    }

    /**
     * Sets the reload mode.
     * @param autoReloadType the reload mode type
     * @return this {@code ContextAutoReloadConfig} instance
     */
    public ContextAutoReloadConfig setReloadMode(@NonNull AutoReloadType autoReloadType) {
        putValue(reloadMode, autoReloadType.toString());
        return this;
    }

    /**
     * Returns the interval in seconds for scanning file changes.
     * @return the scan interval in seconds, or -1 if not set
     */
    public int getScanIntervalSeconds() {
        return getInt(scanIntervalSeconds, -1);
    }

    /**
     * Sets the interval in seconds for scanning file changes.
     * @param scanIntervalSeconds the scan interval in seconds
     * @return this {@code ContextAutoReloadConfig} instance
     */
    public ContextAutoReloadConfig setScanIntervalSeconds(int scanIntervalSeconds) {
        putValue(ContextAutoReloadConfig.scanIntervalSeconds, scanIntervalSeconds);
        return this;
    }

    /**
     * Returns whether automatic reloading is enabled.
     * @return true if enabled, otherwise false
     */
    public boolean isEnabled() {
        return getBoolean(enabled, false);
    }

    /**
     * Sets whether to enable automatic reloading.
     * @param enabled true to enable, false to disable
     * @return this {@code ContextAutoReloadConfig} instance
     */
    public ContextAutoReloadConfig setEnabled(boolean enabled) {
        putValue(ContextAutoReloadConfig.enabled, enabled);
        return this;
    }

}
