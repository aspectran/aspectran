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

public class ShellConfig extends AbstractParameters {

    private static final ParameterDefinition prompt;
    private static final ParameterDefinition commands;
    private static final ParameterDefinition greetings;
    private static final ParameterDefinition workingDir;
    private static final ParameterDefinition historyFile;
    private static final ParameterDefinition verbose;
    private static final ParameterDefinition exposals;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        prompt = new ParameterDefinition("prompt", ParameterValueType.STRING);
        commands = new ParameterDefinition("commands", ParameterValueType.STRING, true);
        greetings = new ParameterDefinition("greetings", ParameterValueType.TEXT);
        workingDir = new ParameterDefinition("workingDir", ParameterValueType.STRING);
        historyFile = new ParameterDefinition("historyFile", ParameterValueType.STRING);
        verbose = new ParameterDefinition("verbose", ParameterValueType.BOOLEAN);
        exposals = new ParameterDefinition("exposals", ExposalsConfig.class);

        parameterDefinitions = new ParameterDefinition[] {
                prompt,
                commands,
                greetings,
                workingDir,
                historyFile,
                verbose,
                exposals
        };
    }

    public ShellConfig() {
        super(parameterDefinitions);
    }

    public String getPrompt() {
        return getString(prompt);
    }

    public void setPrompt(String prompt) {
        putValue(ShellConfig.prompt, prompt);
    }

    public String[] getCommands() {
        return getStringArray(commands);
    }

    public void addCommand(String command) {
        putValue(ShellConfig.commands, command);
    }

    public String getGreetings() {
        return getString(greetings);
    }

    public void setGreetings(String greetings) {
        putValue(ShellConfig.greetings, greetings);
    }

    public String getWorkingDir() {
        return getString(workingDir);
    }

    public void setWorkingDir(String workingDir) {
        putValue(ShellConfig.workingDir, workingDir);
    }

    public String getHistoryFile() {
        return getString(historyFile);
    }

    public void setHistoryFile(String historyFile) {
        putValue(ShellConfig.historyFile, historyFile);
    }

    public boolean isVerbose() {
        return getBoolean(verbose, false);
    }

    public void setVerbose(boolean verbose) {
        putValue(ShellConfig.verbose, verbose);
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
