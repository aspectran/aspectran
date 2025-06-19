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

import com.aspectran.shell.console.ShellConsole;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import org.jline.widget.AutosuggestionWidgets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
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

    private final LineReader lineReader;

    private final LineReader commandReader;

    private final CommandCompleter commandCompleter;

    private final CommandHighlighter commandHighlighter;

    private final History commandHistory;

    private final boolean colorlessDumb;

    private final boolean coloredDumb;

    private Style style;

    public JLineTerminal(ShellConsole console) throws IOException {
        this(console, null);
    }

    public JLineTerminal(ShellConsole console, String encoding) throws IOException {
        this.terminal = TerminalBuilder.builder()
                .name(TERMINAL_NAME)
                .encoding(encoding)
                .build();

        this.colorlessDumb = Terminal.TYPE_DUMB.equals(terminal.getType());
        this.coloredDumb = Terminal.TYPE_DUMB_COLOR.equals(terminal.getType());

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
        this.commandReader.unsetOpt(LineReader.Option.AUTO_FRESH_LINE);

        AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(commandReader);
        autosuggestionWidgets.enable();

        this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        this.lineReader.setOpt(LineReader.Option.DISABLE_EVENT_EXPANSION);
        this.lineReader.unsetOpt(LineReader.Option.INSERT_TAB);
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public Charset getEncoding() {
        return terminal.encoding();
    }

    protected boolean isDumb() {
        return (isColorlessDumb() || isColoredDumb());
    }

    protected boolean isColorlessDumb() {
        return colorlessDumb;
    }

    protected boolean isColoredDumb() {
        return coloredDumb;
    }

    protected boolean isNormal() {
        return (!colorlessDumb && !coloredDumb);
    }

    public LineReader getLineReader() {
        return lineReader;
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
        if (!isColorlessDumb()) {
            if (commandReader.isReading()) {
                commandReader.callWidget(LineReader.CLEAR_SCREEN);
                commandReader.callWidget(LineReader.REDRAW_LINE);
                commandReader.callWidget(LineReader.REDISPLAY);
            } else if (lineReader.isReading()) {
                lineReader.callWidget(LineReader.CLEAR_SCREEN);
                lineReader.callWidget(LineReader.REDRAW_LINE);
                lineReader.callWidget(LineReader.REDISPLAY);
            } else {
                if (terminal.puts(InfoCmp.Capability.clear_screen)) {
                    terminal.flush();
                }
            }
        }
    }

    public void clearLine() {
        if (isNormal()) {
            if (lineReader.isReading()) {
                lineReader.callWidget(LineReader.CLEAR);
            } else {
                commandReader.callWidget(LineReader.CLEAR);
            }
        } else {
            getWriter().write("\r \r");
            getWriter().flush();
        }
    }

    public void redrawLine() {
        if (!isColorlessDumb()) {
            if (lineReader.isReading()) {
                lineReader.callWidget(LineReader.REDRAW_LINE);
                lineReader.callWidget(LineReader.REDISPLAY);
            } else if (commandReader.isReading()) {
                commandReader.callWidget(LineReader.REDRAW_LINE);
                commandReader.callWidget(LineReader.REDISPLAY);
            }
        }
    }

    public OutputStream getOutput() {
        return terminal.output();
    }

    public PrintWriter getWriter() {
        return terminal.writer();
    }

    public boolean isReading() {
        return commandReader.isReading() || lineReader.isReading();
    }

    public boolean hasStyle() {
        return (style != null);
    }

    protected Style getStyle() {
        return style;
    }

    protected void setStyle(Style style) {
        this.style = style;
    }

    public void applyStyle(String... styles) {
        setStyle(new Style(this.style, styles));
    }

    public String toAnsi(String str) {
        return toAnsi(str, getStyle());
    }

    protected String toAnsi(String str, Style style) {
        AttributedStyle attributedStyle = (style != null ? style.getAttributedStyle() : null);
        return JLineTextStyler.parseAsString(attributedStyle, str, terminal);
    }

    public void write(String str) {
        getWriter().write(toAnsi(str));
    }

    public void writeLine() {
        getWriter().println();
        getWriter().flush();
    }

    public void writeAbove(String str) {
        if (getLineReader().isReading()) {
            getLineReader().printAbove(toAnsi(str));
        } else {
            getCommandReader().printAbove(toAnsi(str));
        }
    }

    public void flush() {
        getWriter().flush();
    }

    protected static class Style {

        private final AttributedStyle attributedStyle;

        protected Style(String... styles) {
            this(null, styles);
        }

        protected Style(Style defaultStyle, String... styles) {
            if (defaultStyle != null) {
                this.attributedStyle = JLineTextStyler.style(defaultStyle.getAttributedStyle(), styles);
            } else {
                this.attributedStyle = JLineTextStyler.style(styles);
            }
        }

        public AttributedStyle getAttributedStyle() {
            return attributedStyle;
        }

    }

}
