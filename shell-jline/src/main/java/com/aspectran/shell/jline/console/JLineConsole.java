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
package com.aspectran.shell.jline.console;

import com.aspectran.shell.command.ConsoleTerminatedException;
import com.aspectran.shell.console.AbstractConsole;
import org.jline.reader.EndOfFileException;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Console I/O implementation that supports JLine.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public class JLineConsole extends AbstractConsole {

    private static final String APP_NAME = "Aspectran Shell";

    private final Terminal terminal;

    private final LineReader reader;

    private final LineReader commandReader;

    private final CommandCompleter commandCompleter;

    private final CommandHighlighter commandHighlighter;

    private final History commandHistory;

    private final boolean dumb;

    private AttributedStyle attributedStyle;

    private String[] styles;

    public JLineConsole() throws IOException {
        this(null);
    }

    public JLineConsole(String encoding) throws IOException {
        super(encoding);

        this.terminal = TerminalBuilder.builder().encoding(getEncoding()).build();
        this.commandCompleter = new CommandCompleter(this);
        this.commandHighlighter = new CommandHighlighter(this);
        this.commandHistory = new DefaultHistory();

        this.reader = LineReaderBuilder.builder()
                .appName(APP_NAME)
                .terminal(terminal)
                .build();
        this.reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        this.reader.unsetOpt(LineReader.Option.INSERT_TAB);

        this.commandReader = LineReaderBuilder.builder()
                .appName(APP_NAME)
                .completer(commandCompleter)
                .highlighter(commandHighlighter)
                .history(commandHistory)
                .terminal(terminal)
                .build();
        this.commandReader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        this.commandReader.unsetOpt(LineReader.Option.INSERT_TAB);

        this.dumb = (Terminal.TYPE_DUMB.equals(terminal.getType()) ||
                Terminal.TYPE_DUMB_COLOR.equals(terminal.getType()));
    }

    @Override
    public String readCommandLine() {
        String prompt = toAnsi(getCommandPrompt());
        return readCommandLine(prompt);
    }

    @Override
    public String readCommandLine(String prompt) {
        try {
            String line = readRawCommandLine(prompt).trim();
            commandCompleter.setLimited(true);
            commandHighlighter.setLimited(true);
            line = readMultiCommandLine(line);
            commandCompleter.setLimited(false);
            commandHighlighter.setLimited(false);
            return line;
        } catch (EndOfFileException e) {
            throw new ConsoleTerminatedException();
        } catch (UserInterruptException e) {
            if (confirmQuit()) {
                throw new ConsoleTerminatedException();
            } else {
                return null;
            }
        }
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
            if (prompt == null) {
                prompt = getPrompt();
            }
            return readMultiLine(readRawLine(prompt, null, buffer));
        } catch (EndOfFileException | UserInterruptException e) {
            throw new ConsoleTerminatedException();
        }
    }

    @Override
    public String readPassword() {
        return readPassword(null);
    }

    @Override
    public String readPassword(String prompt) {
        return readPassword(prompt, null);
    }

    @Override
    public String readPassword(String prompt, String buffer) {
        try {
            if (prompt == null) {
                prompt = getPrompt();
            }
            return readRawLine(prompt, MASK_CHAR, buffer);
        } catch (EndOfFileException | UserInterruptException e) {
            throw new ConsoleTerminatedException();
        }
    }

    @Override
    protected String readRawCommandLine(String prompt) {
        return commandReader.readLine(prompt);
    }

    @Override
    protected String readRawLine(String prompt) {
        return readRawLine(prompt, null, null);
    }

    private String readRawLine(String prompt, Character mask, String buffer) {
        return reader.readLine(prompt, mask, buffer);
    }

    @Override
    public void write(String string) {
        if (attributedStyle != null) {
            AttributedString as = new AttributedString(string, attributedStyle);
            getWriter().print(as.toAnsi(terminal));
        } else {
            getWriter().print(toAnsi(string));
        }
    }

    @Override
    public void write(String format, Object... args) {
        write(String.format(format, args));
    }

    @Override
    public void writeLine(String string) {
        write(string);
        getWriter().println();
        getWriter().flush();
    }

    @Override
    public void writeLine(String format, Object... args) {
        writeLine(String.format(format, args));
    }

    @Override
    public void writeLine() {
        getWriter().println();
    }

    @Override
    public void writeError(String string) {
        String[] oldStyles = getStyles();
        setStyle("red");
        writeLine(string);
        if (oldStyles != null) {
            setStyle(oldStyles);
        } else {
            styleOff();
        }
    }

    @Override
    public void writeError(String format, Object... args) {
        writeError(String.format(format, args));
    }

    @Override
    public void appendPrompt(String string) {
        if (attributedStyle != null) {
            AttributedString as = new AttributedString(string, attributedStyle);
            super.appendPrompt(as.toAnsi(terminal));
        } else {
            super.appendPrompt(toAnsi(string));
        }
    }

    @Override
    public void clearScreen() {
        if (commandReader.isReading()) {
            commandReader.callWidget(LineReader.CLEAR_SCREEN);
            commandReader.callWidget(LineReader.REDRAW_LINE);
            commandReader.callWidget(LineReader.REDISPLAY);
        } else if (reader.isReading()) {
            reader.callWidget(LineReader.CLEAR_SCREEN);
            reader.callWidget(LineReader.REDRAW_LINE);
            reader.callWidget(LineReader.REDISPLAY);
        } else {
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
        }
    }

    @Override
    public void clearLine() {
        if (commandReader.isReading()) {
            commandReader.callWidget(LineReader.CLEAR);
        } else if (reader.isReading()) {
            reader.callWidget(LineReader.CLEAR);
        }
        if (dumb) {
            getWriter().print("\r");
            getWriter().flush();
        }
    }

    @Override
    public void redrawLine() {
        if (commandReader.isReading()) {
            commandReader.callWidget(LineReader.REDRAW_LINE);
            commandReader.callWidget(LineReader.REDISPLAY);
        } else if (reader.isReading()) {
            reader.callWidget(LineReader.REDRAW_LINE);
            reader.callWidget(LineReader.REDISPLAY);
        }
    }

    @Override
    public OutputStream getOutput() {
        return terminal.output();
    }

    @Override
    public PrintWriter getWriter() {
        return terminal.writer();
    }

    @Override
    public String[] getStyles() {
        return styles;
    }

    @Override
    public void setStyle(String... styles) {
        this.styles = styles;
        this.attributedStyle = JLineAnsiStyler.makeStyle(styles);
    }

    @Override
    public void styleOff() {
        this.styles = null;
        this.attributedStyle = null;
    }

    private String toAnsi(String string) {
        return JLineAnsiStyler.parse(string, terminal);
    }

    @Override
    public boolean isBusy() {
        return reader.isReading();
    }

    @Override
    public boolean confirmRestart() {
        return confirmRestart(null);
    }

    @Override
    public boolean confirmRestart(String message) {
        if (reader.isReading()) {
            reader.printAbove("Illegal State");
            return false;
        }
        if (message != null) {
            String message2 = toAnsi("{{YELLOW}}" + message + "{{reset}}");
            reader.printAbove(message2);
        }
        String confirm = toAnsi("{{YELLOW}}Would you like to restart this shell [Y/n]?{{reset}} ");
        String yn = readLine(confirm);
        return (yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

    @Override
    public boolean confirmQuit() {
        String confirm = toAnsi("{{YELLOW}}Are you sure you want to quit [Y/n]?{{reset}} ");
        String yn = readLine(confirm);
        return (yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

    @Override
    public List<String> getCommandHistory() {
        List<String> result = new ArrayList<>(commandHistory.size());
        commandHistory.forEach(e -> result.add(e.line()));
        return result;
    }

    @Override
    public void clearCommandHistory() {
        try {
            commandHistory.purge();
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public void setCommandHistoryFile(String historyFile) {
        commandReader.setVariable(LineReader.HISTORY_FILE, historyFile);
        commandHistory.attach(commandReader);
    }

}
