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
import com.aspectran.shell.jline.console.JLineConsoleStyler.Style;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;
import org.jline.widget.AutosuggestionWidgets;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ShellConsole} implementation that uses JLine for advanced terminal
 * interactions. It provides features like command history, tab-completion,
 * syntax highlighting, and styled text output.
 *
 * <p>This class manages two distinct {@link LineReader} instances:
 * <ul>
 *     <li><b>Command Reader:</b> For reading shell commands with full features
 *     like completion and highlighting.</li>
 *     <li><b>Line Reader:</b> For reading simple user input without advanced
 *     features.</li>
 * </ul>
 *
 * <p>It also handles different terminal types, such as "dumb" terminals,
 * to ensure basic functionality is maintained in limited environments.
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

    /**
     * Instantiates a new JLine terminal.
     * @param console the shell console
     * @throws IOException if an I/O error occurs
     */
    public JLineTerminal(ShellConsole console) throws IOException {
        this(console, null);
    }

    /**
     * Instantiates a new JLine terminal.
     * @param console the shell console
     * @param encoding the character encoding
     * @throws IOException if an I/O error occurs
     */
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

    /**
     * Returns the underlying JLine {@link Terminal} instance.
     * @return the terminal
     */
    public Terminal getTerminal() {
        return terminal;
    }

    /**
     * Returns the character encoding of the terminal.
     * @return the charset
     */
    public Charset getEncoding() {
        return terminal.encoding();
    }

    /**
     * Checks if the terminal is a normal, fully-featured terminal.
     * @return true if the terminal is not a "dumb" terminal
     */
    protected boolean isNormal() {
        return (!isColorlessDumb() && !isColoredDumb());
    }

    /**
     * Checks if the terminal is a "dumb" terminal (either with or without color support).
     * @return true if the terminal is a dumb terminal
     */
    protected boolean isDumb() {
        return (isColorlessDumb() || isColoredDumb());
    }

    /**
     * Checks if the terminal is a "dumb" terminal that does not support colors.
     * @return true if the terminal is a colorless dumb terminal
     */
    protected boolean isColorlessDumb() {
        return colorlessDumb;
    }

    /**
     * Checks if the terminal is a "dumb" terminal that supports basic ANSI colors.
     * @return true if the terminal is a colored dumb terminal
     */
    protected boolean isColoredDumb() {
        return coloredDumb;
    }

    /**
     * Checks if either the command reader or the line reader is currently
     * waiting for input.
     * @return true if currently reading input
     */
    public boolean isReading() {
        return (commandReader.isReading() || lineReader.isReading());
    }

    /**
     * Returns the {@link LineReader} for command input, equipped with completion and highlighting.
     * @return the command reader
     */
    public LineReader getCommandReader() {
        return commandReader;
    }

    /**
     * Returns the {@link LineReader} for simple line input.
     * @return the line reader
     */
    public LineReader getLineReader() {
        return lineReader;
    }

    /**
     * Returns the currently active {@link LineReader} if input is being read.
     * @return the currently reading {@code LineReader}, or {@code null} if not reading
     */
    @Nullable
    public LineReader getReadingReader() {
        if (commandReader.isReading()) {
            return commandReader;
        } else if (lineReader.isReading()) {
            return lineReader;
        } else {
            return null;
        }
    }

    /**
     * Returns the command completer.
     * @return the command completer
     */
    public CommandCompleter getCommandCompleter() {
        return commandCompleter;
    }

    /**
     * Returns the command highlighter.
     * @return the command highlighter
     */
    public CommandHighlighter getCommandHighlighter() {
        return commandHighlighter;
    }

    /**
     * Sets the file path for storing command history.
     * @param historyFile the path to the history file
     */
    public void setCommandHistoryFile(String historyFile) {
        commandReader.setVariable(LineReader.HISTORY_FILE, historyFile);
        commandHistory.attach(commandReader);
    }

    /**
     * Returns a list of all entries in the command history.
     * @return the list of command history entries
     */
    public List<String> getCommandHistory() {
        List<String> result = new ArrayList<>(commandHistory.size());
        commandHistory.forEach(e -> result.add(e.line()));
        return result;
    }

    /**
     * Clears all entries from the command history.
     */
    public void clearCommandHistory() {
        try {
            commandHistory.purge();
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Clears the terminal screen.
     * If the line reader is active, it uses the appropriate widget; otherwise,
     * it sends a clear-screen command directly to the terminal.
     */
    public void clearScreen() {
        if (isNormal()) {
            LineReader reader = getReadingReader();
            if (reader != null) {
                reader.callWidget(LineReader.CLEAR_SCREEN);
                reader.callWidget(LineReader.REDRAW_LINE);
                reader.callWidget(LineReader.REDISPLAY);
            } else {
                if (terminal.puts(InfoCmp.Capability.clear_screen)) {
                    terminal.flush();
                }
            }
        } else {
            getWriter().write("\033[H\033[2J");
            getWriter().flush();
        }
    }

    /**
     * Clears the current line of the terminal.
     * This method is designed to be resilient, with fallbacks for different
     * terminal types and execution contexts (e.g., when called outside of a
     * read-line loop).
     */
    public void clearLine() {
        if (isNormal()) {
            LineReader reader = getReadingReader();
            if (reader != null) {
                try {
                    reader.callWidget(LineReader.CLEAR);
                    return; // Widget call succeeded
                } catch (IllegalStateException e) {
                    // Widget call failed (e.g., wrong thread), fallback to terminal command
                }
            }

            // Fallback for when not reading or widget call fails
            if (terminal.puts(InfoCmp.Capability.carriage_return)) {
                if (terminal.puts(InfoCmp.Capability.clr_eol)) {
                    terminal.flush();
                }
            }
        } else {
            // For dumb terminals, overwrite the line with spaces
            int width = terminal.getWidth();
            if (width > 0) {
                getWriter().write("\r" + " ".repeat(width) + "\r");
            } else {
                // Fallback for terminals with undefined width (e.g., 80 columns)
                getWriter().write("\r" +
                        "                                                                                " +
                        "\r");
            }
            getWriter().flush();
        }
    }

    /**
     * Redraws the current line being edited.
     */
    public void redrawLine() {
        if (!isColorlessDumb()) {
            LineReader reader = getReadingReader();
            if (reader != null) {
                reader.callWidget(LineReader.REDRAW_LINE);
                reader.callWidget(LineReader.REDISPLAY);
            }
        }
    }

    /**
     * Returns the terminal's output stream.
     * @return the output stream
     */
    public OutputStream getOutput() {
        return terminal.output();
    }

    /**
     * Returns the terminal's print writer.
     * @return the print writer
     */
    public PrintWriter getWriter() {
        return terminal.writer();
    }

    /**
     * Writes a styled string to the terminal.
     * @param str the string to write
     */
    public void write(String str) {
        getWriter().write(toAnsi(str));
    }

    /**
     * Writes a new line to the terminal.
     */
    public void writeLine() {
        getWriter().println();
        getWriter().flush();
    }

    /**
     * Writes a string above the current input line without disrupting the prompt.
     * If the terminal is not fully capable, it clears the line, writes the
     * message, and redraws the input line.
     * @param str the string to write
     */
    public void writeAbove(String str) {
        if (isNormal()) {
            LineReader reader = getReadingReader();
            if (reader != null) {
                reader.printAbove(toAnsi(str));
                return;
            }
        }
        clearLine();
        write(str);
        writeLine();
        redrawLine();
    }

    /**
     * Flushes the terminal's output writer.
     */
    public void flush() {
        getWriter().flush();
    }

    /**
     * Checks if a style is currently applied.
     * @return true if a style is set
     */
    public boolean hasStyle() {
        return (style != null);
    }

    protected Style getStyle() {
        return style;
    }

    protected void setStyle(Style style) {
        this.style = style;
    }

    /**
     * Applies a set of styles to the current style.
     * @param styles the styles to apply
     */
    public void applyStyle(String... styles) {
        setStyle(new Style(this.style, styles));
    }

    /**
     * Converts a string to its ANSI-escaped representation based on the current style.
     * @param str the string to convert
     * @return the ANSI-escaped string
     */
    public String toAnsi(String str) {
        return toAnsi(str, getStyle());
    }

    protected String toAnsi(String str, Style style) {
        AttributedStyle attributedStyle = (style != null ? style.getAttributedStyle() : null);
        return JLineTextStyler.parseAsString(attributedStyle, str, terminal);
    }

}
