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
package com.aspectran.shell.jline.console;

import com.aspectran.shell.console.AbstractShellConsole;
import com.aspectran.shell.console.CommandReadFailedException;
import com.aspectran.shell.console.PromptStringBuilder;
import com.aspectran.shell.console.ShellConsoleClosedException;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.aspectran.shell.jline.console.JLineTerminal.Style;

/**
 * Console I/O implementation that supports JLine.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public class JLineShellConsole extends AbstractShellConsole {

    private final JLineTerminal jlineTerminal;

    public JLineShellConsole() throws IOException {
        this(null);
    }

    public JLineShellConsole(String encoding) throws IOException {
        super(encoding);
        this.jlineTerminal = new JLineTerminal(this, encoding);
    }

    @Override
    public void setCommandHistoryFile(String historyFile) {
        jlineTerminal.setCommandHistoryFile(historyFile);
    }

    @Override
    public List<String> getCommandHistory() {
        return jlineTerminal.getCommandHistory();
    }

    @Override
    public void clearCommandHistory() {
        jlineTerminal.clearCommandHistory();
    }

    @Override
    public void setCommandPrompt(String commandPrompt) {
        super.setCommandPrompt(jlineTerminal.toAnsi(commandPrompt));
    }

    @Override
    public PromptStringBuilder newPromptStringBuilder() {
        return new JLinePromptStringBuilder(jlineTerminal)
                .resetStyle(getPrimaryStyle());
    }

    @Override
    public String readCommandLine() {
        try {
            String line = readRawCommandLine(getCommandPrompt()).trim();
            jlineTerminal.getCommandCompleter().setLimited(true);
            jlineTerminal.getCommandHighlighter().setLimited(true);
            line = readMultiCommandLine(line);
            jlineTerminal.getCommandCompleter().setLimited(false);
            jlineTerminal.getCommandHighlighter().setLimited(false);
            return line;
        } catch (EndOfFileException e) {
            return null;
        } catch (IllegalStateException e) {
            if (e.getMessage() == null) {
                return null;
            } else {
                throw new CommandReadFailedException(e);
            }
        } catch (UserInterruptException e) {
            if (confirmQuit()) {
                throw new ShellConsoleClosedException();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new CommandReadFailedException(e);
        }
    }

    @Override
    public String readLine(PromptStringBuilder promptStringBuilder) {
        String prompt = null;
        String defaultValue = null;
        if (promptStringBuilder != null) {
            prompt = promptStringBuilder.toString();
            defaultValue = promptStringBuilder.getDefaultValue();
        }
        try {
            return readMultiLine(readRawLine(prompt, null, defaultValue));
        } catch (EndOfFileException | UserInterruptException e) {
            return defaultValue;
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
        try {
            return readRawLine(prompt, MASK_CHAR, defaultValue);
        } catch (EndOfFileException | UserInterruptException e) {
            return defaultValue;
        }
    }

    @Override
    protected String readRawCommandLine(String prompt) {
        return jlineTerminal.getCommandReader().readLine(prompt);
    }

    @Override
    protected String readRawLine(String prompt) {
        return readRawLine(prompt, null, null);
    }

    private String readRawLine(String prompt, Character mask, String defaultValue) {
        return jlineTerminal.getReader().readLine(prompt, mask, defaultValue);
    }

    @Override
    public void write(String str) {
        getWriter().write(jlineTerminal.toAnsi(str));
    }

    @Override
    public void write(String format, Object... args) {
        write(String.format(format, args));
    }

    @Override
    public void writeLine(String str) {
        write(str);
        writeLine();
        getWriter().flush();
    }

    @Override
    public void writeLine(String format, Object... args) {
        writeLine(String.format(format, args));
    }

    @Override
    public void writeLine() {
        write(System.lineSeparator());
    }

    @Override
    public void writeError(String str) {
        setStyle(getDangerStyle());
        writeLine(str);
        resetStyle();
    }

    @Override
    public void writeError(String format, Object... args) {
        writeError(String.format(format, args));
    }

    @Override
    public void writeAbove(String str) {
        if (jlineTerminal.getReader().isReading()) {
            jlineTerminal.getReader().printAbove(jlineTerminal.toAnsi(str));
        } else {
            jlineTerminal.getCommandReader().printAbove(jlineTerminal.toAnsi(str));
        }
    }

    @Override
    public void clearScreen() {
        jlineTerminal.clearScreen();
    }

    @Override
    public void clearLine() {
        jlineTerminal.clearLine();
    }

    @Override
    public void redrawLine() {
        jlineTerminal.redrawLine();
    }

    @Override
    public OutputStream getOutput() {
        return jlineTerminal.getOutput();
    }

    @Override
    public PrintWriter getWriter() {
        return jlineTerminal.getWriter();
    }

    @Override
    public boolean isReading() {
        return jlineTerminal.isReading();
    }

    @Override
    public boolean hasStyle() {
        return jlineTerminal.hasStyle();
    }

    public Style getStyle() {
        return jlineTerminal.getStyle();
    }

    @Override
    public void setStyle(String... styles) {
        jlineTerminal.setStyle(styles);
    }

    @Override
    public void resetStyle() {
        jlineTerminal.resetStyle(getPrimaryStyle());
    }

    @Override
    public void resetStyle(String... styles) {
        if (getPrimaryStyle() != null) {
            ArrayList<String> list = new ArrayList<>(Arrays.asList(getPrimaryStyle()));
            if (styles != null) {
                list.addAll(Arrays.asList(styles));
            }
            jlineTerminal.resetStyle(list.toArray(new String[0]));
        } else {
            jlineTerminal.resetStyle(styles);
        }
    }

}
