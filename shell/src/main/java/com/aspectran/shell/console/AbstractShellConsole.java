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

import com.aspectran.shell.command.ConsoleCommander;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.io.File;
import java.nio.charset.Charset;

/**
 * Abstract base class for {@link ShellConsole} implementations.
 *
 * <p>This class provides default implementations for common console
 * functionality, such as managing the working directory, command prompt,
 * and multi-line input handling. Subclasses must implement the abstract
 * methods for reading from and writing to the underlying terminal or
 * output stream.</p>
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public abstract class AbstractShellConsole implements ShellConsole {

    private final String encoding;

    private String commandPrompt = DEFAULT_PROMPT;

    private File workingDir;

    private ConsoleCommander consoleCommander;

    /**
     * Instantiates a new abstract shell console.
     * @param encoding the character encoding for this console
     */
    public AbstractShellConsole(String encoding) {
        if (encoding != null) {
            this.encoding = encoding;
        } else {
            this.encoding = Charset.defaultCharset().name();
        }
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public File getWorkingDir() {
        return workingDir;
    }

    @Override
    public void setWorkingDir(File workingDir) {
        this.workingDir = workingDir;
    }

    @Override
    public ConsoleCommander getCommander() {
        return consoleCommander;
    }

    @Override
    public void setCommander(ConsoleCommander consoleCommander) {
        this.consoleCommander = consoleCommander;
    }

    @Override
    public String getCommandPrompt() {
        return commandPrompt;
    }

    @Override
    public void setCommandPrompt(String commandPrompt) {
        this.commandPrompt = commandPrompt;
    }

    @Override
    public String readLine() {
        return readLine(null);
    }

    @Override
    public String readPassword() {
        return readPassword(null);
    }

    /**
     * Assembles a command that can span multiple lines.
     * <p>This method handles multi-line input terminated by a backslash ({@code \})
     * and also supports quoted strings that span multiple lines.</p>
     * @param line the initial line of input
     * @return the complete multi-line command as a single string, or {@code null}
     *      if the initial input is null
     */
    protected String assembleMultiLineCommand(String line) {
        if (line == null) {
            return null;
        }
        if (COMMENT_DELIMITER.equals(line)) {
            String next = readCommandFromTerminal(COMMENT_PROMPT);
            if (next.isEmpty()) {
                return next;
            }
            assembleMultiLineCommand(COMMENT_DELIMITER);
        }
        String quote = searchQuote(line);
        if (quote != null || line.endsWith(MULTILINE_DELIMITER)) {
            String next = readCommandFromTerminal(MULTILINE_PROMPT).trim();
            if (next.startsWith(COMMENT_DELIMITER)) {
                line = assembleMultiLineCommand(line);
            } else if (quote != null) {
                line += System.lineSeparator() + next;
            } else {
                line = line.substring(0, line.length() - MULTILINE_DELIMITER.length()).trim() + " " + next;
            }
        }
        quote = searchQuote(line);
        if (quote != null) {
            return assembleMultiLineCommand(line);
        }
        if (line.endsWith(MULTILINE_DELIMITER)) {
            line = assembleMultiLineCommand(line);
        }
        return line;
    }

    /**
     * Assembles input that can span multiple lines.
     * <p>This method handles multi-line input terminated by a backslash ({@code \}).</p>
     * @param line the initial line of input
     * @return the complete multi-line input as a single string
     */
    protected String assembleMultiLineInput(String line) {
        if (line == null) {
            line = readLineFromTerminal(MULTILINE_PROMPT);
        }
        if (line.endsWith(MULTILINE_DELIMITER)) {
            line = line.substring(0, line.length() - MULTILINE_DELIMITER.length()) +
                    System.lineSeparator() + assembleMultiLineInput(null);
        }
        return line;
    }

    /**
     * Reads a single line of command input directly from the terminal.
     * <p>This method is intended for reading top-level commands from the user.
     * Advanced implementations may use a reader with command-specific features
     * like auto-completion based on registered command names.</p>
     * @param prompt the prompt to display to the user
     * @return the line read from the terminal
     */
    protected abstract String readCommandFromTerminal(String prompt);

    /**
     * Reads a single line of general-purpose text directly from the terminal.
     * <p>This method is intended for reading non-command input, such as parameter
     * values during an interactive procedure. Implementations may use a reader
     * with generic features like file path completion.</p>
     * <p>In the default console implementation, this method's behavior is identical
     * to {@link #readCommandFromTerminal(String)}, but advanced implementations
     * like JLine provide distinct behaviors for each.</p>
     * @param prompt the prompt to display to the user
     * @return the line read from the terminal
     */
    protected abstract String readLineFromTerminal(String prompt);

    /**
     * Searches for an unclosed quote (single or double) in the given line.
     * @param line the line to search
     * @return the type of unclosed quote (" or '), or {@code null} if all quotes are balanced
     */
    @Nullable
    private String searchQuote(@NonNull String line) {
        boolean doubleQuote = false;
        boolean singleQuote = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"' && !singleQuote) {
                doubleQuote = !doubleQuote;
            } else if (c == '\'' && !doubleQuote) {
                singleQuote = !singleQuote;
            }
        }
        if (doubleQuote) {
            return "\"";
        } else if (singleQuote) {
            return "'";
        } else {
            return null;
        }
    }

    @Override
    public boolean confirmRestart() {
        if (!isInteractive()) {
            return true;
        }
        if (checkReadingState()) {
            return false;
        }
        PromptStringBuilder psb = newPromptStringBuilder()
                .warningStyle()
                .append("Are you sure you want to restart this shell [Y/n]? ");
        String yn = readLine(psb);
        return (yn == null || yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

    @Override
    public boolean confirmQuit() {
        if (!isInteractive()) {
            return true;
        }
        if (checkReadingState()) {
            return false;
        }
        PromptStringBuilder psb = newPromptStringBuilder()
                .warningStyle()
                .append("Are you sure you want to quit [Y/n]? ");
        String yn = readLine(psb);
        return (yn == null || yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

    private boolean checkReadingState() {
        if (isReading()) {
            getStyler().dangerStyle();
            writeAbove("Illegal State");
            getStyler().resetStyle();
            return true;
        } else {
            return false;
        }
    }

}
