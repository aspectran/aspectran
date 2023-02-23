/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

public class ShellConfig extends AbstractParameters {

    private static final ParameterKey greetings;
    private static final ParameterKey styles;
    private static final ParameterKey prompt;
    private static final ParameterKey commands;
    private static final ParameterKey session;
    private static final ParameterKey workingDir;
    private static final ParameterKey historyFile;
    private static final ParameterKey verbose;
    private static final ParameterKey exposals;

    private static final ParameterKey[] parameterKeys;

    static {
        greetings = new ParameterKey("greetings", ValueType.TEXT);
        styles = new ParameterKey("styles", ValueType.STRING, true);
        prompt = new ParameterKey("prompt", ValueType.STRING);
        commands = new ParameterKey("commands", ValueType.STRING, true);
        session = new ParameterKey("session", SessionManagerConfig.class);
        workingDir = new ParameterKey("workingDir", ValueType.STRING);
        historyFile = new ParameterKey("historyFile", ValueType.STRING);
        verbose = new ParameterKey("verbose", ValueType.BOOLEAN);
        exposals = new ParameterKey("exposals", ExposalsConfig.class);

        parameterKeys = new ParameterKey[] {
                greetings,
                styles,
                prompt,
                commands,
                session,
                workingDir,
                historyFile,
                verbose,
                exposals
        };
    }

    public ShellConfig() {
        super(parameterKeys);
    }

    public String[] getStyles() {
        return getStringArray(styles);
    }

    public ShellConfig setStyles(String[] styles) {
        putValue(ShellConfig.styles, styles);
        return this;
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

    public String getWorkingDir() {
        return getString(workingDir);
    }

    public ShellConfig setWorkingDir(String workingDir) {
        putValue(ShellConfig.workingDir, workingDir);
        return this;
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
