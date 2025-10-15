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
 * A wrapper for a {@link ShellConsole} that can override the output writer.
 * <p>This class follows the decorator pattern, delegating most calls to the
 * underlying console instance. Its primary purpose is to allow for temporary
 * output redirection by providing a custom {@link PrintWriter}. Methods that
 * would modify the state of the wrapped console (e.g., {@code setWorkingDir})
 * are unsupported and will throw an {@link UnsupportedOperationException}.</p>
 *
 * <p>Created: 2017. 3. 9.</p>
 */
public class ShellConsoleWrapper implements ShellConsole {

    private final ShellConsole console;

    private PrintWriter writer;

    /**
     * Instantiates a new shell console wrapper.
     * @param console the console to wrap
     */
    public ShellConsoleWrapper(ShellConsole console) {
        this.console = console;
    }

    @Override
    public ConsoleStyler getStyler() {
        return console.getStyler();
    }

    @Override
    public String getEncoding() {
        return console.getEncoding();
    }

    @Override
    public PrintStream getOutput() {
        return console.getOutput();
    }

    /**
     * Returns the overridden writer if set; otherwise, returns the writer from the wrapped console.
     * @return the active print writer
     */
    @Override
    public PrintWriter getWriter() {
        if (writer != null) {
            return writer;
        } else {
            return console.getWriter();
        }
    }

    /**
     * Sets a custom writer to override the default console writer.
     * @param writer the writer to use for output
     */
    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public File getWorkingDir() {
        return console.getWorkingDir();
    }

    /**
     * This operation is not supported on a wrapped console.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setWorkingDir(File workingDir) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCommandPrompt() {
        return console.getCommandPrompt();
    }

    /**
     * This operation is not supported on a wrapped console.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCommandPrompt(String commandPrompt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public PromptStringBuilder newPromptStringBuilder() {
        return console.newPromptStringBuilder();
    }

    @Override
    public ConsoleCommander getCommander() {
        return console.getCommander();
    }

    /**
     * This operation is not supported on a wrapped console.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCommander(ConsoleCommander runner) {
        throw new UnsupportedOperationException();
    }

    /**
     * This operation is not supported on a wrapped console.
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setCommandHistoryFile(String historyFile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getCommandHistory() {
        return console.getCommandHistory();
    }

    @Override
    public void clearCommandHistory() {
        console.clearCommandHistory();
    }

    @Override
    public String readCommand() {
        return console.readCommand();
    }

    @Override
    public String readLine() {
        return console.readLine();
    }

    @Override
    public String readLine(PromptStringBuilder promptStringBuilder) {
        return console.readLine(promptStringBuilder);
    }

    @Override
    public String readPassword() {
        return console.readPassword();
    }

    @Override
    public String readPassword(PromptStringBuilder promptStringBuilder) {
        return console.readPassword(promptStringBuilder);
    }

    @Override
    public boolean isReading() {
        return console.isReading();
    }

    @Override
    public boolean isInteractive() {
        return console.isInteractive();
    }

    @Override
    public void write(String str) {
        console.write(str);
    }

    @Override
    public void write(String format, Object... args) {
        console.write(format, args);
    }

    @Override
    public void writeLine(String str) {
        if (writer != null) {
            writer.println(str);
        } else {
            console.writeLine(str);
        }
    }

    @Override
    public void writeLine(String format, Object... args) {
        if (writer != null) {
            writer.println(String.format(format, args));
        } else {
            console.writeLine(format, args);
        }
    }

    @Override
    public void writeLine() {
        if (writer != null) {
            writer.println();
        } else {
            console.writeLine();
        }
    }

    @Override
    public void writeError(String str) {
        console.writeError(str);
    }

    @Override
    public void writeError(String format, Object... args) {
        console.writeError(format, args);
    }

    @Override
    public void writeAbove(String str) {
        console.writeAbove(str);
    }

    @Override
    public void clearScreen() {
        console.clearScreen();
    }

    @Override
    public void clearLine() {
        console.clearLine();
    }

    @Override
    public void redrawLine() {
        console.redrawLine();
    }

    @Override
    public boolean confirmRestart() {
        return console.confirmRestart();
    }

    @Override
    public boolean confirmQuit() {
        return console.confirmQuit();
    }

    @Override
    public void flush() {
        console.flush();
    }

}
