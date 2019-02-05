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
package com.aspectran.shell.console;

import com.aspectran.shell.command.CommandInterpreter;

import java.io.File;
import java.nio.charset.Charset;

/**
 * The Abstract Class for Console I/O.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public abstract class AbstractConsole implements Console {

    private String commandPrompt = DEFAULT_COMMAND_PROMPT;

    private final String encoding;

    private File workingDir;

    private CommandInterpreter interpreter;

    public AbstractConsole(String encoding) {
        if (encoding != null) {
            this.encoding = encoding;
        } else {
            this.encoding = Charset.defaultCharset().name();
        }
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public String getCommandPrompt() {
        return commandPrompt;
    }

    @Override
    public void setCommandPrompt(String commandPrompt) {
        this.commandPrompt = commandPrompt;
    }

    @Override
    public File getWorkingDir() {
        return workingDir;
    }

    @Override
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public CommandInterpreter getInterpreter() {
        return interpreter;
    }

    @Override
    public void setInterpreter(CommandInterpreter interpreter) {
        this.interpreter = interpreter;
    }

}
