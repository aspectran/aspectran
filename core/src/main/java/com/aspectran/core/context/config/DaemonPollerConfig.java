/*
 * Copyright (c) 2008-2020 The Aspectran Project
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

/**
 * <p>Created: 2017. 12. 12.</p>
 *
 * @since 5.1.0
 */
public class DaemonPollerConfig extends AbstractParameters {

    private static final ParameterKey pollingInterval;
    private static final ParameterKey maxThreads;
    private static final ParameterKey inbound;
    private static final ParameterKey requeue;

    private static final ParameterKey[] parameterKeys;

    static {
        pollingInterval = new ParameterKey("pollingInterval", ValueType.LONG);
        maxThreads = new ParameterKey("maxThreads", ValueType.INT);
        inbound = new ParameterKey("inbound", ValueType.STRING);
        requeue = new ParameterKey("requeue", ValueType.BOOLEAN);

        parameterKeys = new ParameterKey[] {
                pollingInterval,
                maxThreads,
                inbound,
                requeue
        };
    }

    public DaemonPollerConfig() {
        super(parameterKeys);
    }

    public long getPollingInterval(long defaultPollingInterval) {
        return getLong(pollingInterval, defaultPollingInterval);
    }

    public DaemonPollerConfig setPollingInterval(long pollingInterval) {
        putValue(DaemonPollerConfig.pollingInterval, pollingInterval);
        return this;
    }

    public int getMaxThreads(int defaultMaxThreads) {
        return getInt(maxThreads, defaultMaxThreads);
    }

    public DaemonPollerConfig setMaxThreads(int maxThreads) {
        putValue(DaemonPollerConfig.maxThreads, maxThreads);
        return this;
    }

    public String getInboundPath() {
        return getString(inbound);
    }

    public String getInboundPath(String defaultInboundPath) {
        return getString(inbound, defaultInboundPath);
    }

    public DaemonPollerConfig setInboundPath(String inboundPath) {
        putValue(inbound, inboundPath);
        return this;
    }

    public boolean isRequeue() {
        return getBoolean(requeue, false);
    }

    public DaemonPollerConfig setRequeue(boolean requeue) {
        putValue(DaemonPollerConfig.requeue, requeue);
        return this;
    }

}
