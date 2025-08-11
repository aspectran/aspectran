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
package com.aspectran.shell.jline.console;

import com.aspectran.shell.console.AbstractShellConsole;
import com.aspectran.shell.console.CommandReadFailedException;
import com.aspectran.shell.console.PromptStringBuilder;
import com.aspectran.shell.console.ShellConsoleClosedException;
import com.aspectran.shell.jline.console.JLineConsoleStyler.Style;
import org.jline.reader.EndOfFileException;
import org.jline.reader.UserInterruptException;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Console I/O implementation that supports JLine.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public class JLineShellConsole extends AbstractShellConsole {

    private final JLineTerminal jlineTerminal;

    private final PrintStream output;

    private final PrintWriter writer;

    private final JLineConsoleStyler consoleStyler;

    public JLineShellConsole() throws IOException {
        this(null);
    }

    public JLineShellConsole(String encoding) throws IOException {
        super(encoding);
        this.jlineTerminal = new JLineTerminal(this, encoding);
        this.output = new TerminalPrintStream(jlineTerminal);
        this.writer = new TerminalPrintWriter(jlineTerminal);
        this.consoleStyler = new JLineConsoleStyler(jlineTerminal);
    }

    public JLineTerminal getJlineTerminal() {
        return jlineTerminal;
    }

        @Override
    public JLineConsoleStyler getStyler() {
        return consoleStyler;
    }

    @Override
    public PrintStream getOutput() {
        return output;
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
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
        return new JLinePromptStringBuilder(this);
    }

    @Override
    public String readCommand() {
        try {
            String line = readCommandFromTerminal(getCommandPrompt()).trim();
            jlineTerminal.getCommandCompleter().setLimited(true);
            jlineTerminal.getCommandHighlighter().setLimited(true);
            line = readMultiCommand(line);
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
            return readMultiLine(readLineFromTerminal(prompt, null, defaultValue));
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
            return readLineFromTerminal(prompt, MASK_CHAR, defaultValue);
        } catch (EndOfFileException | UserInterruptException e) {
            return defaultValue;
        }
    }

    @Override
    protected String readCommandFromTerminal(String prompt) {
        return jlineTerminal.getCommandReader().readLine(prompt);
    }

    @Override
    protected String readLineFromTerminal(String prompt) {
        return readLineFromTerminal(prompt, null, null);
    }

    private String readLineFromTerminal(String prompt, Character mask, String defaultValue) {
        // Password masking for dumb terminal doesn't seem to work properly
        Character maskToUse = (jlineTerminal.isDumb() ? null : mask);
        return jlineTerminal.getLineReader().readLine(prompt, maskToUse, defaultValue);
    }

    @Override
    public boolean isReading() {
        return jlineTerminal.isReading();
    }

    @Override
    public void write(String str) {
        jlineTerminal.write(str);
    }

    @Override
    public void write(String format, Object... args) {
        write(String.format(format, args));
    }

    @Override
    public void writeLine(String str) {
        write(str);
        writeLine();
    }

    @Override
    public void writeLine(String format, Object... args) {
        writeLine(String.format(format, args));
    }

    @Override
    public void writeLine() {
        jlineTerminal.writeLine();
    }

    @Override
    public void writeError(String str) {
        Style oldStyle = consoleStyler.getStyle();
        consoleStyler.dangerStyle();
        writeLine(str);
        consoleStyler.setStyle(oldStyle);
    }

    @Override
    public void writeError(String format, Object... args) {
        writeError(String.format(format, args));
    }

    @Override
    public void writeAbove(String str) {
        jlineTerminal.writeAbove(str);
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
    public void flush() {
        jlineTerminal.flush();
    }

}
