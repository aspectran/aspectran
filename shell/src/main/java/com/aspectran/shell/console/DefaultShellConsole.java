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
package com.aspectran.shell.console;

import com.aspectran.utils.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
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

    private final ConsoleStyler consoleStyler = new DefaultConsoleStyler();

    private volatile boolean reading;

    public DefaultShellConsole() {
        this(null);
    }

    public DefaultShellConsole(String encoding) {
        super(encoding);
    }

    @Override
    public ConsoleStyler getStyler() {
        return consoleStyler;
    }

    @Override
    public String getEncoding() {
        return Charset.defaultCharset().name();
    }

    @Override
    public PrintStream getOutput() {
        return System.out;
    }

    @Override
    public PrintWriter getWriter() {
        if (System.console() != null) {
            return System.console().writer();
        } else {
            return new PrintWriter(System.out, true);
        }
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
    public String readCommand() {
        String prompt = getCommandPrompt();
        String line = readMultiCommand(readLineFromTerminal(prompt));
        return (line != null ? line.trim() : null);
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
            line = readMultiLine(readLineFromTerminal(prompt));
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
            line = readPasswordFromTerminal(prompt);
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
    protected String readCommandFromTerminal(String prompt) {
        return readLineFromTerminal(prompt);
    }

    @Override
    protected String readLineFromTerminal(String prompt) {
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
            return null;
        }
    }

    private String readPasswordFromTerminal(String prompt) {
        if (System.console() != null) {
            return new String(System.console().readPassword(prompt));
        } else {
            return readLineFromTerminal(prompt);
        }
    }

    @Override
    public boolean isReading() {
        return reading;
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
    public void flush() {
        System.out.flush();
    }

}
