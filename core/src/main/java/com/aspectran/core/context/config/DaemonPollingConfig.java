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
 * Represents the configuration for the daemon's file-based command polling feature.
 * This allows the daemon to watch a directory for incoming files, parsing them as
 * commands to be executed. It is used by the {@code FileCommander} to control
 * polling behavior such as frequency, threading, and file paths.
 *
 * @since 5.1.0
 */
public class DaemonPollingConfig extends DefaultParameters {

    /** Specifies the interval, in milliseconds, at which the daemon polls for new command files. */
    private static final ParameterKey pollingInterval;

    /** Defines the maximum number of threads to use for executing polled commands. */
    private static final ParameterKey maxThreads;

    /** Determines whether commands that fail to execute should be automatically re-queued. */
    private static final ParameterKey requeuable;

    /** Specifies the path to the directory that the daemon will scan for incoming command files. */
    private static final ParameterKey incoming;

    /** Specifies whether the file polling mechanism is enabled. */
    private static final ParameterKey enabled;

    private static final ParameterKey[] parameterKeys;

    static {
        pollingInterval = new ParameterKey("pollingInterval", ValueType.LONG);
        maxThreads = new ParameterKey("maxThreads", ValueType.INT);
        requeuable = new ParameterKey("requeuable", ValueType.BOOLEAN);
        incoming = new ParameterKey("incoming", ValueType.STRING);
        enabled = new ParameterKey("enabled", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                pollingInterval,
                maxThreads,
                requeuable,
                incoming,
                enabled
        };
    }

    /**
     * Creates a new {@code DaemonPollingConfig}.
     */
    public DaemonPollingConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the polling interval in milliseconds.
     * This determines how often the daemon checks for new command files.
     * @param defaultPollingInterval the default polling interval if not specified
     * @return the polling interval
     */
    public long getPollingInterval(long defaultPollingInterval) {
        return getLong(pollingInterval, defaultPollingInterval);
    }

    /**
     * Sets the polling interval in milliseconds.
     * @param pollingInterval the polling interval in milliseconds
     * @return this {@code DaemonPollingConfig} instance for method chaining
     */
    public DaemonPollingConfig setPollingInterval(long pollingInterval) {
        putValue(DaemonPollingConfig.pollingInterval, pollingInterval);
        return this;
    }

    /**
     * Returns the maximum number of threads for the command execution pool.
     * @param defaultMaxThreads the default maximum number of threads if not specified
     * @return the maximum number of threads
     */
    public int getMaxThreads(int defaultMaxThreads) {
        return getInt(maxThreads, defaultMaxThreads);
    }

    /**
     * Sets the maximum number of threads for the command execution pool.
     * @param maxThreads the maximum number of threads
     * @return this {@code DaemonPollingConfig} instance for method chaining
     */
    public DaemonPollingConfig setMaxThreads(int maxThreads) {
        putValue(DaemonPollingConfig.maxThreads, maxThreads);
        return this;
    }

    /**
     * Returns whether commands that fail execution should be re-queued.
     * @return {@code true} if failed commands can be re-queued, {@code false} otherwise
     */
    public boolean isRequeuable() {
        return getBoolean(requeuable, false);
    }

    /**
     * Sets whether commands that fail execution should be re-queued.
     * @param requeuable {@code true} to allow re-queuing, {@code false} otherwise
     * @return this {@code DaemonPollingConfig} instance for method chaining
     */
    public DaemonPollingConfig setRequeuable(boolean requeuable) {
        putValue(DaemonPollingConfig.requeuable, requeuable);
        return this;
    }

    /**
     * Returns the path to the directory for incoming command files.
     * @return the incoming directory path, or {@code null} if not set
     */
    public String getIncoming() {
        return getString(incoming);
    }

    /**
     * Returns the path to the directory for incoming command files.
     * @param defaultIncoming the default incoming directory path if not specified
     * @return the incoming directory path
     */
    public String getIncoming(String defaultIncoming) {
        return getString(incoming, defaultIncoming);
    }

    /**
     * Sets the path to the directory for incoming command files.
     * @param incoming the incoming directory path
     * @return this {@code DaemonPollingConfig} instance for method chaining
     */
    public DaemonPollingConfig setIncoming(String incoming) {
        putValue(DaemonPollingConfig.incoming, incoming);
        return this;
    }

    /**
     * Returns whether the file polling feature is enabled.
     * @return {@code true} if file polling is enabled, {@code false} otherwise
     */
    public boolean isEnabled() {
        return getBoolean(enabled, false);
    }

    /**
     * Enables or disables the file polling feature.
     * @param enabled {@code true} to enable, {@code false} to disable
     * @return this {@code DaemonPollingConfig} instance for method chaining
     */
    public DaemonPollingConfig setEnabled(boolean enabled) {
        putValue(DaemonPollingConfig.enabled, enabled);
        return this;
    }

}
