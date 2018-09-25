/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

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

    void write(String format, Object... args);

    void writeLine(String string);

    void writeLine(String format, Object... args);

    void writeLine();

    void clearScreen();

    void flush();

    String getEncoding();

    OutputStream getOutput();

    Writer getWriter();

    Writer getUnclosableWriter() throws UnsupportedEncodingException;

    void setStyle(String... styles);

    void offStyle();

    boolean isReading();

    boolean confirmRestart();

    boolean confirmRestart(String message);

    boolean confirmQuit();

}

