/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

/**
 * <p>Created: 2017. 12. 12.</p>
 *
 * @since 5.1.0
 */
public class DaemonPollerConfig extends AbstractParameters {

    private static final ParameterDefinition pollingInterval;
    private static final ParameterDefinition maxThreads;
    private static final ParameterDefinition inbound;
    private static final ParameterDefinition requeue;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        pollingInterval = new ParameterDefinition("pollingInterval", ParameterValueType.LONG);
        maxThreads = new ParameterDefinition("maxThreads", ParameterValueType.INT);
        inbound = new ParameterDefinition("inbound", ParameterValueType.STRING);
        requeue = new ParameterDefinition("requeue", ParameterValueType.BOOLEAN);

        parameterDefinitions = new ParameterDefinition[] {
                pollingInterval,
                maxThreads,
                inbound,
                requeue
        };
    }

    public DaemonPollerConfig() {
        super(parameterDefinitions);
    }

    public long getPollingInterval(long defaultPollingInterval) {
        return getLong(pollingInterval, defaultPollingInterval);
    }

    public void setPollingInterval(long pollingInterval) {
        putValue(DaemonPollerConfig.pollingInterval, pollingInterval);
    }

    public int getMaxThreads(int defaultMaxThreads) {
        return getInt(maxThreads, defaultMaxThreads);
    }

    public void setMaxThreads(int maxThreads) {
        putValue(DaemonPollerConfig.maxThreads, maxThreads);
    }

    public String getInboundPath() {
        return getString(inbound);
    }

    public String getInboundPath(String defaultInboundPath) {
        return getString(inbound, defaultInboundPath);
    }

    public void setInboundPath(String inboundPath) {
        putValue(inbound, inboundPath);
    }

    public boolean isRequeue() {
        return getBoolean(requeue, false);
    }

    public void setRequeue(boolean requeue) {
        putValue(DaemonPollerConfig.requeue, requeue);
    }

}
