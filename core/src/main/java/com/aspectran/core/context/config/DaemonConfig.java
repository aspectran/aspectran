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
 * @since 5.1.0
 */
public class DaemonConfig extends AbstractParameters {

    private static final ParameterKey executor;
    private static final ParameterKey polling;
    private static final ParameterKey commands;
    private static final ParameterKey session;
    private static final ParameterKey exposals;

    private static final ParameterKey[] parameterKeys;

    static {
        executor = new ParameterKey("executor", DaemonExecutorConfig.class);
        polling = new ParameterKey("polling", DaemonPollingConfig.class);
        commands = new ParameterKey("commands", ValueType.STRING, true);
        session = new ParameterKey("session", SessionManagerConfig.class);
        exposals = new ParameterKey("exposals", ExposalsConfig.class);

        parameterKeys = new ParameterKey[] {
                executor,
                polling,
                commands,
                session,
                exposals
        };
    }

    public DaemonConfig() {
        super(parameterKeys);
    }

    public DaemonExecutorConfig getExecutorConfig() {
        return getParameters(executor);
    }

    public DaemonExecutorConfig newExecutorConfig() {
        return newParameters(executor);
    }

    public DaemonExecutorConfig touchExecutorConfig() {
        return touchParameters(executor);
    }

    public DaemonPollingConfig getPollingConfig() {
        return getParameters(polling);
    }

    public DaemonPollingConfig newPollingConfig() {
        return newParameters(polling);
    }

    public DaemonPollingConfig touchPollingConfig() {
        return touchParameters(polling);
    }

    public String[] getCommands() {
        return getStringArray(commands);
    }

    public DaemonConfig setCommands(String[] commands) {
        removeValue(DaemonConfig.commands);
        putValue(DaemonConfig.commands, commands);
        return this;
    }

    public DaemonConfig addCommand(String command) {
        putValue(DaemonConfig.commands, command);
        return this;
    }

    public SessionManagerConfig getSessionManagerConfig() {
        return getParameters(session);
    }

    public SessionManagerConfig newSessionManagerConfig() {
        return newParameters(session);
    }

    public SessionManagerConfig touchSessionManagerConfig() {
        return touchParameters(session);
    }

    public ExposalsConfig getExposalsConfig() {
        return getParameters(exposals);
    }

    public ExposalsConfig newExposalsConfig() {
        return newParameters(exposals);
    }

    public ExposalsConfig touchExposalsConfig() {
        return touchParameters(exposals);
    }

}
