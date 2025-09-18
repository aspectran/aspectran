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
 * Configuration for the daemon's polling mechanism.
 *
 * @since 5.1.0
 */
public class DaemonPollingConfig extends AbstractParameters {

    /** The interval at which to poll for new commands. */
    private static final ParameterKey pollingInterval;

    /** The maximum number of threads to use for polling. */
    private static final ParameterKey maxThreads;

    /** Whether to re-queue failed commands. */
    private static final ParameterKey requeuable;

    /** The directory to scan for incoming command files. */
    private static final ParameterKey incoming;

    private static final ParameterKey[] parameterKeys;

    static {
        pollingInterval = new ParameterKey("pollingInterval", ValueType.LONG);
        maxThreads = new ParameterKey("maxThreads", ValueType.INT);
        requeuable = new ParameterKey("requeuable", ValueType.BOOLEAN);
        incoming = new ParameterKey("incoming", ValueType.STRING);

        parameterKeys = new ParameterKey[] {
                pollingInterval,
                maxThreads,
                requeuable,
                incoming
        };
    }

    /**
     * Instantiates a new DaemonPollingConfig.
     */
    public DaemonPollingConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the polling interval in milliseconds.
     * @param defaultPollingInterval the default polling interval
     * @return the polling interval
     */
    public long getPollingInterval(long defaultPollingInterval) {
        return getLong(pollingInterval, defaultPollingInterval);
    }

    /**
     * Sets the polling interval in milliseconds.
     * @param pollingInterval the polling interval
     * @return this {@code DaemonPollingConfig} instance
     */
    public DaemonPollingConfig setPollingInterval(long pollingInterval) {
        putValue(DaemonPollingConfig.pollingInterval, pollingInterval);
        return this;
    }

    /**
     * Returns the maximum number of threads for polling.
     * @param defaultMaxThreads the default maximum number of threads
     * @return the maximum number of threads
     */
    public int getMaxThreads(int defaultMaxThreads) {
        return getInt(maxThreads, defaultMaxThreads);
    }

    /**
     * Sets the maximum number of threads for polling.
     * @param maxThreads the maximum number of threads
     * @return this {@code DaemonPollingConfig} instance
     */
    public DaemonPollingConfig setMaxThreads(int maxThreads) {
        putValue(DaemonPollingConfig.maxThreads, maxThreads);
        return this;
    }

    /**
     * Returns whether failed commands can be re-queued.
     * @return true if requeuable, false otherwise
     */
    public boolean isRequeuable() {
        return getBoolean(requeuable, false);
    }

    /**
     * Sets whether failed commands can be re-queued.
     * @param requeuable true to allow re-queuing, false otherwise
     * @return this {@code DaemonPollingConfig} instance
     */
    public DaemonPollingConfig setRequeuable(boolean requeuable) {
        putValue(DaemonPollingConfig.requeuable, requeuable);
        return this;
    }

    /**
     * Returns the path to the incoming directory.
     * @return the incoming directory path
     */
    public String getIncoming() {
        return getString(incoming);
    }

    /**
     * Returns the path to the incoming directory.
     * @param defaultIncoming the default incoming directory path
     * @return the incoming directory path
     */
    public String getIncoming(String defaultIncoming) {
        return getString(incoming, defaultIncoming);
    }

    /**
     * Sets the path to the incoming directory.
     * @param incoming the incoming directory path
     * @return this {@code DaemonPollingConfig} instance
     */
    public DaemonPollingConfig setIncoming(String incoming) {
        putValue(DaemonPollingConfig.incoming, incoming);
        return this;
    }

}
