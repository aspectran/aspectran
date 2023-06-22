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
package com.aspectran.shell.console;

import com.aspectran.core.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * Console I/O implementation that supports System Console.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public class DefaultConsole extends AbstractConsole {

    private volatile boolean busy;

    public DefaultConsole() {
        this(null);
    }

    public DefaultConsole(String encoding) {
        super(encoding);
    }

    @Override
    public void setCommandHistoryFile(String historyFile) {
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public List<String> getCommandHistory() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void clearCommandHistory() {
    }

    @Override
    public String readCommandLine() {
        String prompt = getCommandPrompt();
        return readCommandLine(prompt);
    }

    @Override
    public String readCommandLine(String prompt) {
        return readMultiCommandLine(readRawLine(prompt).trim());
    }

    @Override
    public String readLine() {
        return readLine(null);
    }

    @Override
    public String readLine(String prompt) {
        return readLine(prompt, null);
    }

    @Override
    public String readLine(String prompt, String buffer) {
        try {
            busy = true;
            if (prompt == null) {
                prompt = getPrompt();
            }
            if (prompt != null) {
                if (buffer != null) {
                    prompt += "[" + buffer + "]: ";
                }
            } else {
                if (buffer != null) {
                    prompt = "[" + buffer + "]: ";
                }
            }
            String line = readMultiLine(readRawLine(prompt));
            if (buffer != null && StringUtils.isEmpty(line)) {
                return buffer;
            } else {
                return line;
            }
        } finally {
            busy = false;
        }
    }

    @Override
    public String readPassword() {
        return readPassword(null);
    }

    @Override
    public String readPassword(String prompt) {
        if (System.console() != null) {
            return new String(System.console().readPassword(prompt));
        } else {
            return readRawLine(prompt);
        }
    }

    @Override
    public String readPassword(String prompt, String buffer) {
        try {
            busy = true;
            if (prompt == null) {
                prompt = getPrompt();
            }
            if (prompt != null) {
                if (buffer != null) {
                    prompt += "[" + StringUtils.repeat(MASK_CHAR, 8) + "]: ";
                }
            } else {
                if (buffer != null) {
                    prompt = "[" + StringUtils.repeat(MASK_CHAR, 8) + "]: ";
                }
            }
            String line = readRawPassword(prompt);
            if (buffer != null && StringUtils.isEmpty(line)) {
                return buffer;
            } else {
                return line;
            }
        } finally {
            busy = false;
        }
    }

    @Override
    protected String readRawCommandLine(String prompt) {
        return readRawLine(prompt);
    }

    @Override
    protected String readRawLine(String prompt) {
        try {
            String line;
            if (System.console() != null) {
                line = System.console().readLine(prompt);
            } else {
                if (prompt != null) {
                    write(prompt);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                line = reader.readLine();
            }
            return line;
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private String readRawPassword(String prompt) {
        if (System.console() != null) {
            return new String(System.console().readPassword(prompt));
        } else {
            return readRawLine(prompt);
        }
    }

    @Override
    public void write(String string) {
        System.out.print(string);
    }

    @Override
    public void write(String format, Object... args) {
        System.out.printf(format, args);
    }

    @Override
    public void writeLine(String string) {
        System.out.println(string);
    }

    @Override
    public void writeLine(String format, Object... args) {
        System.out.printf(format + "%n", args);
    }

    @Override
    public void writeLine() {
        System.out.println();
    }

    @Override
    public void writeError(String string) {
        System.err.println(string);
    }

    @Override
    public void writeError(String format, Object... args) {
        System.err.printf(format + "%n", args);
    }

    @Override
    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    @Override
    public void clearLine() {
        // Nothing to do
    }

    @Override
    public void redrawLine() {
        // Nothing to do
    }

    @Override
    public String getEncoding() {
        return Charset.defaultCharset().name();
    }

    public OutputStream getOutput() {
        return System.out;
    }

    @Override
    public PrintWriter getWriter() {
        if (System.console() != null) {
            return System.console().writer();
        } else {
            return new PrintWriter(System.out);
        }
    }

    @Override
    public String[] getStyles() {
        return null;
    }

    @Override
    public void setStyle(String... styles) {
        // Nothing to do
    }

    @Override
    public void styleOff() {
        // Nothing to do
    }

    @Override
    public boolean isBusy() {
        return busy;
    }

    @Override
    public boolean confirmRestart() {
        return confirmRestart(null);
    }

    @Override
    public boolean confirmRestart(String message) {
        if (isBusy()) {
            writeLine("Illegal State");
            return false;
        }
        if (message != null) {
            writeLine(message);
        }
        String confirm = "Would you like to restart this shell [Y/n]? ";
        String yn = readLine(confirm);
        return (yn == null || yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

    @Override
    public boolean confirmQuit() {
        String confirm = "Are you sure you want to quit [Y/n]? ";
        String yn = readLine(confirm);
        return (yn == null || yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

}
