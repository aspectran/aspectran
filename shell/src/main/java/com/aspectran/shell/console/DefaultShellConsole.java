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
 * A basic, default implementation of {@link ShellConsole} that uses the standard
 * Java {@link System#in}, {@link System#out}, and {@link System#err} streams.
 * <p>This implementation is suitable for environments where a sophisticated
 * terminal is not available. It does not support advanced features like command
 * history persistence or complex line editing. Styling is handled by a
 * {@link DefaultConsoleStyler}, which is a no-op styler.</p>
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public class DefaultShellConsole extends AbstractShellConsole {

    private final ConsoleStyler consoleStyler = new DefaultConsoleStyler();

    private volatile boolean reading;

    /**
     * Instantiates a new default shell console using the default system encoding.
     */
    public DefaultShellConsole() {
        this(null);
    }

    /**
     * Instantiates a new default shell console with the specified encoding.
     * @param encoding the character encoding for this console
     */
    public DefaultShellConsole(String encoding) {
        super(encoding);
    }

    @Override
    public boolean isInteractive() {
        return false;
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

    /**
     * Returns a {@link PrintWriter} for the console.
     * <p>If a system console is available (i.e., {@code System.console()} is not null),
     * it returns the console's writer. Otherwise, it returns a new {@code PrintWriter}
     * wrapping {@code System.out}.</p>
     * @return a {@link PrintWriter} instance
     */
    @Override
    public PrintWriter getWriter() {
        if (System.console() != null) {
            return System.console().writer();
        } else {
            return new PrintWriter(System.out, true);
        }
    }

    /**
     * This operation is not supported in this implementation.
     */
    @Override
    public void setCommandHistoryFile(String historyFile) {
        // This is a no-op
    }

    /**
     * Always returns an empty list as this implementation does not support history.
     * @return an empty list
     */
    @Override
    @SuppressWarnings({"unchecked"})
    public List<String> getCommandHistory() {
        return Collections.EMPTY_LIST;
    }

    /**
     * This operation is not supported in this implementation.
     */
    @Override
    public void clearCommandHistory() {
        // This is a no-op
    }

    @Override
    public boolean isReading() {
        return reading;
    }

    @Override
    public String readCommand() {
        String prompt = getCommandPrompt();
        String line = assembleMultiLineCommand(readLineFromTerminal(prompt));
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
            line = assembleMultiLineInput(readLineFromTerminal(prompt));
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
            if (prompt != null) {
                write(prompt);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Reads a password from the terminal without echoing characters.
     * <p>Falls back to standard line reading if a system console is not available.</p>
     * @param prompt the prompt to display
     * @return the password as a string
     */
    private String readPasswordFromTerminal(String prompt) {
        if (System.console() != null) {
            return new String(System.console().readPassword(prompt));
        } else {
            return readLineFromTerminal(prompt);
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

    /**
     * {@inheritDoc}
     * <p>In this implementation, it behaves identically to {@link #writeLine(String)}.</p>
     */
    @Override
    public void writeAbove(String str) {
        writeLine(str);
    }

    /**
     * Clears the screen using an ANSI escape sequence.
     * <p>This may not work on all terminals.</p>
     */
    @Override
    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * This operation is not supported in this implementation.
     */
    @Override
    public void clearLine() {
        // This is a no-op
    }

    /**
     * This operation is not supported in this implementation.
     */
    @Override
    public void redrawLine() {
        // This is a no-op
    }

    @Override
    public void flush() {
        System.out.flush();
    }

}
