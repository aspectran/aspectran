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

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Defines the core contract for interacting with a shell console.
 * <p>This interface provides a comprehensive API for handling input/output,
 * text styling, command history, and other console-specific functionalities.
 * It serves as the primary abstraction for all console interactions within the
 * Aspectran Shell, allowing for different implementations (e.g., basic system
 * console, JLine-based advanced console).</p>
 *
 * <p>Created: 2017. 3. 5.</p>
 *
 * @since 4.0.0
 */
public interface ShellConsole {

    /** The default command prompt string. */
    String DEFAULT_PROMPT = "Aspectran> ";

    /** The prompt string for multi-line input. */
    String MULTILINE_PROMPT = "> ";

    /** The prompt string for commented multi-line input. */
    String COMMENT_PROMPT = "// ";

    /** The character sequence to indicate that input will continue on the next line. */
    String MULTILINE_DELIMITER = "\\";

    /** The character sequence to indicate the start of a commented line. */
    String COMMENT_DELIMITER = "//";

    /** The character used to mask password input. */
    char MASK_CHAR = '*';

    /**
     * Returns whether the console is in interactive mode.
     * @return {@code true} if the console is interactive, {@code false} otherwise
     */
    boolean isInteractive();

    /**
     * Returns the styler for this console.
     * @return the console styler
     */
    ConsoleStyler getStyler();

    /**
     * Returns the character encoding used by the console.
     * @return the character encoding
     */
    String getEncoding();

    /**
     * Returns the standard output stream for this console.
     * @return the print stream for output
     */
    PrintStream getOutput();

    /**
     * Returns a {@link PrintWriter} for writing formatted text to the console.
     * @return the console's print writer
     */
    PrintWriter getWriter();

    /**
     * Returns the current working directory for the shell.
     * @return the working directory
     */
    File getWorkingDir();

    /**
     * Sets the working directory for the shell.
     * @param workingDir the new working directory
     */
    void setWorkingDir(File workingDir);

    /**
     * Returns the console commander that manages this console.
     * @return the console commander
     */
    ConsoleCommander getCommander();

    /**
     * Sets the console commander.
     * @param runner the console commander
     */
    void setCommander(ConsoleCommander runner);

    /**
     * Sets the file used for persisting command history.
     * @param historyFile the path to the history file
     */
    void setCommandHistoryFile(String historyFile);

    /**
     * Returns a list of commands from the history.
     * @return the command history
     */
    List<String> getCommandHistory();

    /**
     * Clears the command history.
     */
    void clearCommandHistory();

    /**
     * Returns the main command prompt string.
     * @return the command prompt
     */
    String getCommandPrompt();

    /**
     * Sets the main command prompt string.
     * @param commandPrompt the new command prompt
     */
    void setCommandPrompt(String commandPrompt);

    /**
     * Creates a new prompt string builder.
     * @return a new {@link PromptStringBuilder} instance
     */
    PromptStringBuilder newPromptStringBuilder();

    /**
     * Checks if the console is currently in a blocking read operation.
     * @return {@code true} if reading, {@code false} otherwise
     */
    boolean isReading();

    /**
     * Reads a single command from the user, handling multi-line input.
     * @return the command string, or {@code null} if the stream is closed
     */
    String readCommand();

    /**
     * Reads a single line of text from the user.
     * @return the line of text, or {@code null} if the stream is closed
     */
    String readLine();

    /**
     * Reads a single line of text from the user with a custom prompt.
     * @param promptStringBuilder the builder for the prompt to display
     * @return the line of text, or {@code null} if the stream is closed
     */
    String readLine(PromptStringBuilder promptStringBuilder);

    /**
     * Reads a password or other sensitive information without echoing characters to the screen.
     * @return the password as a string, or {@code null} if the stream is closed
     */
    String readPassword();

    /**
     * Reads a password with a custom prompt.
     * @param promptStringBuilder the builder for the prompt to display
     * @return the password as a string, or {@code null} if the stream is closed
     */
    String readPassword(PromptStringBuilder promptStringBuilder);

    /**
     * Writes a string to the console.
     * @param str the string to write
     */
    void write(String str);

    /**
     * Writes a formatted string to the console.
     * @param format a format string
     * @param args arguments referenced by the format specifiers
     */
    void write(String format, Object... args);

    /**
     * Writes a string to the console, followed by a new line.
     * @param str the string to write
     */
    void writeLine(String str);

    /**
     * Writes a formatted string to the console, followed by a new line.
     * @param format a format string
     * @param args arguments referenced by the format specifiers
     */
    void writeLine(String format, Object... args);

    /**
     * Writes a new line to the console.
     */
    void writeLine();

    /**
     * Writes an error message to the console's error stream.
     * @param str the error message to write
     */
    void writeError(String str);

    /**
     * Writes a formatted error message to the console's error stream.
     * @param format a format string
     * @param args arguments referenced by the format specifiers
     */
    void writeError(String format, Object... args);

    /**
     * Writes a string on a new line above the current prompt.
     * <p>(Supported in advanced consoles like JLine)</p>
     * @param str the string to write
     */
    void writeAbove(String str);

    /**
     * Clears the console screen.
     */
    void clearScreen();

    /**
     * Clears the current line.
     */
    void clearLine();

    /**
     * Redraws the current line.
     */
    void redrawLine();

    /**
     * Flushes the console's output buffer.
     */
    void flush();

    /**
     * Prompts the user to confirm a restart action.
     * @return {@code true} if the user confirms, {@code false} otherwise
     */
    boolean confirmRestart();

    /**
     * Prompts the user to confirm a quit action.
     * @return {@code true} if the user confirms, {@code false} otherwise
     */
    boolean confirmQuit();

}
