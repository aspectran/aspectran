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
 * Configuration for the interactive shell (CLI).
 */
public class ShellConfig extends DefaultParameters {

    /** The style settings for the shell. */
    private static final ParameterKey style;

    /** The welcome message displayed on startup. */
    private static final ParameterKey greetings;

    /** The command prompt string. */
    private static final ParameterKey prompt;

    /** The list of commands to execute on startup. */
    private static final ParameterKey commands;

    /** The configuration for the session manager. */
    private static final ParameterKey session;

    /** The path to the command history file. */
    private static final ParameterKey historyFile;

    /** Whether to enable verbose output. */
    private static final ParameterKey verbose;

    /** The configuration for acceptable request patterns. */
    private static final ParameterKey acceptable;

    private static final ParameterKey[] parameterKeys;

    static {
        style = new ParameterKey("style", ShellStyleConfig.class);
        greetings = new ParameterKey("greetings", ValueType.TEXT);
        prompt = new ParameterKey("prompt", ValueType.STRING);
        commands = new ParameterKey("commands", ValueType.STRING, true);
        session = new ParameterKey("session", SessionManagerConfig.class);
        historyFile = new ParameterKey("historyFile", ValueType.STRING);
        verbose = new ParameterKey("verbose", ValueType.BOOLEAN);
        acceptable = new ParameterKey("acceptable", AcceptableConfig.class);

        parameterKeys = new ParameterKey[] {
                style,
                greetings,
                prompt,
                commands,
                session,
                historyFile,
                verbose,
                acceptable
        };
    }

    /**
     * Instantiates a new ShellConfig.
     */
    public ShellConfig() {
        super(parameterKeys);
    }

    /**
     * Returns the shell style configuration.
     * @return the {@code ShellStyleConfig} instance
     */
    public ShellStyleConfig getShellStyleConfig() {
        return getParameters(style);
    }

    /**
     * Returns the existing shell style configuration or creates a new one if it does not exist.
     * @return a non-null {@code ShellStyleConfig} instance
     */
    public SessionManagerConfig touchShellStyleConfig() {
        return touchParameters(style);
    }

    /**
     * Returns the welcome message.
     * @return the greetings text
     */
    public String getGreetings() {
        return getString(greetings);
    }

    /**
     * Sets the welcome message.
     * @param greetings the greetings text
     * @return this {@code ShellConfig} instance
     */
    public ShellConfig setGreetings(String greetings) {
        putValue(ShellConfig.greetings, greetings);
        return this;
    }

    /**
     * Returns the command prompt string.
     * @return the prompt string
     */
    public String getPrompt() {
        return getString(prompt);
    }

    /**
     * Sets the command prompt string.
     * @param prompt the prompt string
     * @return this {@code ShellConfig} instance
     */
    public ShellConfig setPrompt(String prompt) {
        putValue(ShellConfig.prompt, prompt);
        return this;
    }

    /**
     * Returns the list of commands to execute on startup.
     * @return the list of commands
     */
    public String[] getCommands() {
        if (isAssigned(commands)) {
            return getStringArray(commands);
        } else {
            return null;
        }
    }

    /**
     * Sets the list of commands to execute on startup.
     * @param commands the list of commands
     * @return this {@code ShellConfig} instance
     */
    public ShellConfig setCommands(String[] commands) {
        removeValue(ShellConfig.commands);
        putValue(ShellConfig.commands, commands);
        return this;
    }

    /**
     * Adds a command to execute on startup.
     * @param command the command to add
     * @return this {@code ShellConfig} instance
     */
    public ShellConfig addCommand(String command) {
        putValue(ShellConfig.commands, command);
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
     * Returns the existing session manager configuration or creates a new one if it does not exist.
     * @return a non-null {@code SessionManagerConfig} instance
     */
    public SessionManagerConfig touchSessionManagerConfig() {
        return touchParameters(session);
    }

    /**
     * Returns the path to the command history file.
     * @return the history file path
     */
    public String getHistoryFile() {
        return getString(historyFile);
    }

    /**
     * Sets the path to the command history file.
     * @param historyFile the history file path
     * @return this {@code ShellConfig} instance
     */
    public ShellConfig setHistoryFile(String historyFile) {
        putValue(ShellConfig.historyFile, historyFile);
        return this;
    }

    /**
     * Returns whether verbose output is enabled.
     * @return true if verbose output is enabled, false otherwise
     */
    public boolean isVerbose() {
        return getBoolean(verbose, false);
    }

    /**
     * Sets whether to enable verbose output.
     * @param verbose true to enable verbose output, false otherwise
     * @return this {@code ShellConfig} instance
     */
    public ShellConfig setVerbose(boolean verbose) {
        putValue(ShellConfig.verbose, verbose);
        return this;
    }

    /**
     * Returns the configuration for acceptable request patterns.
     * @return the {@code AcceptableConfig} instance
     */
    public AcceptableConfig getAcceptableConfig() {
        return getParameters(acceptable);
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
