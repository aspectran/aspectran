/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.lang.NonNull;
import com.aspectran.core.util.ArrayStack;
import com.aspectran.shell.console.ShellConsole;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import org.jline.widget.AutosuggestionWidgets;

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
public class JLineTerminal {

    private static final String TERMINAL_NAME = "Aspectran JLine terminal";

    private final Terminal terminal;

    private final LineReader reader;

    private final LineReader commandReader;

    private final CommandCompleter commandCompleter;

    private final CommandHighlighter commandHighlighter;

    private final History commandHistory;

    private final boolean dumb;

    private final boolean dumbColor;

    private final ArrayStack<Style> styleStack = new ArrayStack<>();

    public JLineTerminal(ShellConsole console) throws IOException {
        this(console, null);
    }

    public JLineTerminal(ShellConsole console, String encoding) throws IOException {
        this.terminal = TerminalBuilder.builder()
                .name(TERMINAL_NAME)
                .encoding(encoding)
                .build();

        this.dumb = Terminal.TYPE_DUMB.equals(terminal.getType());
        this.dumbColor = Terminal.TYPE_DUMB_COLOR.equals(terminal.getType());

        this.reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        this.reader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        this.reader.unsetOpt(LineReader.Option.INSERT_TAB);

        this.commandCompleter = new CommandCompleter(console);
        this.commandHighlighter = new CommandHighlighter(console);
        this.commandHistory = new DefaultHistory();

        this.commandReader = LineReaderBuilder.builder()
                .completer(commandCompleter)
                .highlighter(commandHighlighter)
                .history(commandHistory)
                .terminal(terminal)
                .build();
        this.commandReader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        this.commandReader.unsetOpt(LineReader.Option.INSERT_TAB);

        AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(commandReader);
        autosuggestionWidgets.enable();
    }

    public Terminal getTerminal() {
        return terminal;
    }

    protected boolean isDumb() {
        return dumb;
    }

    protected boolean isDumbColor() {
        return dumbColor;
    }

    public LineReader getReader() {
        return reader;
    }

    public LineReader getCommandReader() {
        return commandReader;
    }

    public CommandCompleter getCommandCompleter() {
        return commandCompleter;
    }

    public CommandHighlighter getCommandHighlighter() {
        return commandHighlighter;
    }

    public void setCommandHistoryFile(String historyFile) {
        commandReader.setVariable(LineReader.HISTORY_FILE, historyFile);
        commandHistory.attach(commandReader);
    }

    public List<String> getCommandHistory() {
        List<String> result = new ArrayList<>(commandHistory.size());
        commandHistory.forEach(e -> result.add(e.line()));
        return result;
    }

    public void clearCommandHistory() {
        try {
            commandHistory.purge();
        } catch (IOException e) {
            // ignore
        }
    }

    public void clearScreen() {
        if (!isDumb()) {
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
    }

    public void clearLine() {
        if (!isDumb()) {
            if (!isDumbColor()) {
                if (commandReader.isReading()) {
                    commandReader.callWidget(LineReader.CLEAR);
                } else if (reader.isReading()) {
                    reader.callWidget(LineReader.CLEAR);
                }
            } else {
                getWriter().print("\r");
                getWriter().flush();
            }
        }
    }

    public void redrawLine() {
        if (!isDumb()) {
            if (commandReader.isReading()) {
                commandReader.callWidget(LineReader.REDRAW_LINE);
                commandReader.callWidget(LineReader.REDISPLAY);
            } else if (reader.isReading()) {
                reader.callWidget(LineReader.REDRAW_LINE);
                reader.callWidget(LineReader.REDISPLAY);
            }
        }
    }

    public OutputStream getOutput() {
        return terminal.output();
    }

    public PrintWriter getWriter() {
        return terminal.writer();
    }
    
    public boolean isBusy() {
        return reader.isReading();
    }

    public String toAnsi(String str) {
        Style style = getStyle();
        AttributedStyle attributedStyle = style != null ? style.getAttributedStyle() : null;
        return JLineTextStyler.parseAsString(attributedStyle, str, terminal);
    }

    public boolean hasStyle() {
        return !styleStack.isEmpty();
    }

    public Style getStyle() {
        return styleStack.isEmpty() ? null : styleStack.peek();
    }

    public void setStyle(String... styles) {
        styleStack.push(new Style(this, styles));
    }

    public void clearStyle() {
        styleStack.pop();
    }

    protected static class Style {

        private final JLineTerminal jlineTerminal;

        private final AttributedStyle attributedStyle;

        private Style(@NonNull JLineTerminal jlineTerminal, @NonNull String... styles) {
            this.jlineTerminal = jlineTerminal;
            if (jlineTerminal.hasStyle()) {
                this.attributedStyle = JLineTextStyler.style(jlineTerminal.getStyle().getAttributedStyle(), styles);
            } else {
                this.attributedStyle = JLineTextStyler.style(styles);
            }
        }

        public AttributedStyle getAttributedStyle() {
            return attributedStyle;
        }

        public String toAnsi(String str) {
            return new AttributedString(jlineTerminal.toAnsi(str), attributedStyle).toAnsi();
        }

    }

}
