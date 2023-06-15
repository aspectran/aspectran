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
public class DefaultShellConsole extends AbstractShellConsole {

    private volatile boolean reading;

    public DefaultShellConsole() {
        this(null);
    }

    public DefaultShellConsole(String encoding) {
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
        return readMultiCommandLine(readRawLine(prompt).trim());
    }

    @Override
    public PromptStringBuilder newPromptStringBuilder() {
        return new DefaultPromptStringBuilder();
    }

    @Override
    public String readLine(PromptStringBuilder promptStringBuilder) {
        String prompt = null;
        String defaultValue = null;
        if (promptStringBuilder != null) {
            prompt = promptStringBuilder.toString();
            defaultValue = promptStringBuilder.getDefaultValue();
        }
        if (defaultValue != null) {
            String prompt2 = "[" + defaultValue + "] ";
            if (prompt != null) {
                prompt += prompt2;
            } else {
                prompt = prompt2;
            }
        }
        String line;
        try {
            reading = true;
            line = readMultiLine(readRawLine(prompt));
        } finally {
            reading = false;
        }
        if (defaultValue != null && StringUtils.isEmpty(line)) {
            return defaultValue;
        } else {
            return line;
        }
    }

    @Override
    public String readPassword(PromptStringBuilder promptStringBuilder) {
        String prompt = null;
        String defaultValue = null;
        if (promptStringBuilder != null) {
            prompt = promptStringBuilder.toString();
            defaultValue = promptStringBuilder.getDefaultValue();
        }
        if (defaultValue != null) {
            String prompt2 = "[" + StringUtils.repeat(MASK_CHAR, 8) + "] ";
            if (prompt != null) {
                prompt += prompt2;
            } else {
                prompt = prompt2;
            }
        }
        String line;
        try {
            reading = true;
            line = readRawPassword(prompt);
        } finally {
            reading = false;
        }
        if (defaultValue != null && StringUtils.isEmpty(line)) {
            return defaultValue;
        } else {
            return line;
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
    public void write(String str) {
        System.out.print(str);
    }

    @Override
    public void write(String format, Object... args) {
        System.out.printf(format, args);
    }

    @Override
    public void writeLine(String str) {
        System.out.println(str);
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
    public void writeError(String str) {
        System.err.println(str);
    }

    @Override
    public void writeError(String format, Object... args) {
        System.err.printf(format + "%n", args);
    }

    @Override
    public void writeAbove(String str) {
        writeLine(str);
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
    public boolean isReading() {
        return reading;
    }

    @Override
    public boolean hasStyle() {
        return false;
    }

    @Override
    public void setStyle(String... styles) {
        // Nothing to do
    }

    @Override
    public void resetStyle() {
        // Nothing to do
    }

    @Override
    public void resetStyle(String... styles) {
        // Nothing to do
    }

}
