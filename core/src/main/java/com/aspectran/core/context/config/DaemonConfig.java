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
 * Configuration for running Aspectran as a background daemon.
 *
 * @since 5.1.0
 */
public class DaemonConfig extends AbstractParameters {

    /** The configuration for the command executor. */
    private static final ParameterKey executor;

    /** The configuration for command polling. */
    private static final ParameterKey polling;

    /** The list of commands to execute on startup. */
    private static final ParameterKey commands;

    /** The configuration for the session manager. */
    private static final ParameterKey session;

    /** The configuration for acceptable request patterns. */
    private static final ParameterKey acceptable;

    private static final ParameterKey[] parameterKeys;

    static {
        executor = new ParameterKey("executor", DaemonExecutorConfig.class);
        polling = new ParameterKey("polling", DaemonPollingConfig.class);
        commands = new ParameterKey("commands", ValueType.STRING, true);
        session = new ParameterKey("session", SessionManagerConfig.class);
        acceptable = new ParameterKey("acceptable", AcceptableConfig.class);

        parameterKeys = new ParameterKey[] {
                executor,
                polling,
                commands,
                session,
                acceptable
        };
    }

    /**
     * Instantiates a new DaemonConfig.
     */
    public DaemonConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the configuration for the command executor.
     * @return the {@code DaemonExecutorConfig} instance
     */
    public DaemonExecutorConfig getExecutorConfig() {
        return getParameters(executor);
    }

    /**
     * Creates a new configuration for the command executor.
     * @return the new {@code DaemonExecutorConfig} instance
     */
    public DaemonExecutorConfig newExecutorConfig() {
        return newParameters(executor);
    }

    /**
     * Returns the existing or a new configuration for the command executor.
     * @return a non-null {@code DaemonExecutorConfig} instance
     */
    public DaemonExecutorConfig touchExecutorConfig() {
        return touchParameters(executor);
    }

    /**
     * Returns the configuration for command polling.
     * @return the {@code DaemonPollingConfig} instance
     */
    public DaemonPollingConfig getPollingConfig() {
        return getParameters(polling);
    }

    /**
     * Creates a new configuration for command polling.
     * @return the new {@code DaemonPollingConfig} instance
     */
    public DaemonPollingConfig newPollingConfig() {
        return newParameters(polling);
    }

    /**
     * Returns the existing or a new configuration for command polling.
     * @return a non-null {@code DaemonPollingConfig} instance
     */
    public DaemonPollingConfig touchPollingConfig() {
        return touchParameters(polling);
    }

    /**
     * Returns the list of commands to execute on startup.
     * @return the list of commands
     */
    public String[] getCommands() {
        return getStringArray(commands);
    }

    /**
     * Sets the list of commands to execute on startup.
     * @param commands the list of commands
     * @return this {@code DaemonConfig} instance
     */
    public DaemonConfig setCommands(String[] commands) {
        removeValue(DaemonConfig.commands);
        putValue(DaemonConfig.commands, commands);
        return this;
    }

    /**
     * Adds a command to execute on startup.
     * @param command the command to add
     * @return this {@code DaemonConfig} instance
     */
    public DaemonConfig addCommand(String command) {
        putValue(DaemonConfig.commands, command);
        return this;
    }

    /**
     * Returns the session manager configuration.
     * @return the {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig getSessionManagerConfig() {
        return getParameters(session);
    }

    /**
     * Creates a new session manager configuration.
     * @return the new {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig newSessionManagerConfig() {
        return newParameters(session);
    }

    /**
     * Returns the existing session manager configuration or creates a new one if it does not exist.
     * @return a non-null {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig touchSessionManagerConfig() {
        return touchParameters(session);
    }

    /**
     * Returns the configuration for acceptable request patterns.
     * @return the {@code AcceptableConfig} instance
     */
    public AcceptableConfig getAcceptableConfig() {
        return getParameters(acceptable);
    }

    /**
     * Creates a new configuration for acceptable request patterns.
     * @return the new {@code AcceptableConfig} instance
     */
    public AcceptableConfig newAcceptableConfig() {
        return newParameters(acceptable);
    }

    /**
     * Returns the existing configuration for acceptable request patterns
     * or creates a new one if it does not exist.
     * @return a non-null {@code AcceptableConfig} instance
     */
    public AcceptableConfig touchAcceptableConfig() {
        return touchParameters(acceptable);
    }

}
