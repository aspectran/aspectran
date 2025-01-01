/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

public class ShellConfig extends AbstractParameters {

    private static final ParameterKey style;
    private static final ParameterKey greetings;
    private static final ParameterKey prompt;
    private static final ParameterKey commands;
    private static final ParameterKey session;
    private static final ParameterKey historyFile;
    private static final ParameterKey verbose;
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

    public ShellConfig() {
        super(parameterKeys);
    }

    public ShellStyleConfig getShellStyleConfig() {
        return getParameters(style);
    }

    public ShellStyleConfig newShellStyleConfig() {
        return newParameters(style);
    }

    public SessionManagerConfig touchShellStyleConfig() {
        return touchParameters(style);
    }

    public String getGreetings() {
        return getString(greetings);
    }

    public ShellConfig setGreetings(String greetings) {
        putValue(ShellConfig.greetings, greetings);
        return this;
    }

    public String getPrompt() {
        return getString(prompt);
    }

    public ShellConfig setPrompt(String prompt) {
        putValue(ShellConfig.prompt, prompt);
        return this;
    }

    public String[] getCommands() {
        return getStringArray(commands);
    }

    public ShellConfig setCommands(String[] commands) {
        removeValue(ShellConfig.commands);
        putValue(ShellConfig.commands, commands);
        return this;
    }

    public ShellConfig addCommand(String command) {
        putValue(ShellConfig.commands, command);
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

    public String getHistoryFile() {
        return getString(historyFile);
    }

    public ShellConfig setHistoryFile(String historyFile) {
        putValue(ShellConfig.historyFile, historyFile);
        return this;
    }

    public boolean isVerbose() {
        return getBoolean(verbose, false);
    }

    public ShellConfig setVerbose(boolean verbose) {
        putValue(ShellConfig.verbose, verbose);
        return this;
    }

    public AcceptableConfig getAcceptableConfig() {
        return getParameters(acceptable);
    }

    public AcceptableConfig newAcceptableConfig() {
        return newParameters(acceptable);
    }

    public AcceptableConfig touchAcceptableConfig() {
        return touchParameters(acceptable);
    }

}
