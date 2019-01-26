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
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * The Interface for Console I/O.
 *
 * <p>Created: 2017. 3. 5.</p>
 *
 * @since 4.0.0
 */
public interface Console {

    String DEFAULT_COMMAND_PROMPT = "Aspectran> ";

    String getCommandPrompt();

    void setCommandPrompt(String commandPrompt);

    String readCommandLine();

    String readCommandLine(String prompt);

    String readLine();

    String readLine(String prompt);

    String readLine(String format, Object... args);

    String readPassword();

    String readPassword(String prompt);

    String readPassword(String format, Object... args);

    void write(String string);

    void writeLine(String string);

    void write(String format, Object... args);

    void writeLine(String format, Object... args);

    void writeLine();

    void writeError(String string);

    void writeError(String format, Object... args);

    void clearScreen();

    void flush();

    OutputStream getOutput();

    PrintWriter getWriter();

    String[] getStyles();

    void setStyle(String... styles);

    void styleOff();

    boolean isReading();

    boolean confirmRestart();

    boolean confirmRestart(String message);

    boolean confirmQuit();

    String getEncoding();

    File getWorkingDir();

    void setWorkingDir(File workingDir);

    CommandInterpreter getInterpreter();

    void setInterpreter(CommandInterpreter interpreter);

}

