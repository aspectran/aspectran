/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
    private static final ParameterKey requeuable;
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

    public boolean isRequeuable() {
        return getBoolean(requeuable, false);
    }

    public DaemonPollerConfig setRequeuable(boolean requeuable) {
        putValue(DaemonPollerConfig.requeuable, requeuable);
        return this;
    }

    public String getIncoming() {
        return getString(incoming);
    }

    public String getIncoming(String defaultIncoming) {
        return getString(incoming, defaultIncoming);
    }

    public DaemonPollerConfig setIncoming(String incoming) {
        putValue(DaemonPollerConfig.incoming, incoming);
        return this;
    }

}
