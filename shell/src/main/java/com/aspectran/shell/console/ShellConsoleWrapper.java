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
package com.aspectran.shell.console;

import com.aspectran.shell.command.CommandInterpreter;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

public class ShellConsoleWrapper implements ShellConsole {

    private final ShellConsole console;

    private PrintWriter writer;

    public ShellConsoleWrapper(ShellConsole console) {
        this.console = console;
    }

    @Override
    public String getEncoding() {
        return console.getEncoding();
    }

    @Override
    public String getCommandPrompt() {
        return console.getCommandPrompt();
    }

    @Override
    public void setCommandPrompt(String commandPrompt) {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getWorkingDir() {
        return console.getWorkingDir();
    }

    @Override
    public void setWorkingDir(File workingDir) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CommandInterpreter getInterpreter() {
        return console.getInterpreter();
    }

    @Override
    public void setInterpreter(CommandInterpreter interpreter) {
        throw new UnsupportedOperationException();
    }

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
    public String readCommandLine() {
        return console.readCommandLine();
    }

    @Override
    public String readCommandLine(String prompt) {
        return console.readCommandLine(prompt);
    }

    @Override
    public String readLine() {
        return console.readLine();
    }

    @Override
    public String readLine(String prompt) {
        return console.readLine(prompt);
    }

    @Override
    public String readLine(String prompt, String buffer) {
        return console.readLine(prompt, buffer);
    }

    @Override
    public String readPassword() {
        return console.readPassword();
    }

    @Override
    public String readPassword(String prompt) {
        return console.readPassword(prompt);
    }

    @Override
    public String readPassword(String prompt, String buffer) {
        return console.readPassword(prompt, buffer);
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
    public void appendPrompt(String str) {
        console.appendPrompt(str);
    }

    @Override
    public void clearPrompt() {
        console.clearPrompt();
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
    public OutputStream getOutput() {
        return console.getOutput();
    }

    @Override
    public PrintWriter getWriter() {
        if (writer != null) {
            return writer;
        } else {
            return console.getWriter();
        }
    }

    public void setWriter(PrintWriter writer) {
        this.writer = writer;
    }

    @Override
    public boolean isBusy() {
        return console.isBusy();
    }

    @Override
    public boolean hasStyle() {
        return false;
    }

    @Override
    public void setStyle(String... styles) {
        console.setStyle(styles);
    }

    @Override
    public void clearStyle() {
        console.clearStyle();
    }

    @Override
    public boolean confirmRestart() {
        return console.confirmRestart();
    }

    @Override
    public boolean confirmRestart(String message) {
        return console.confirmRestart(message);
    }

    @Override
    public boolean confirmQuit() {
        return console.confirmQuit();
    }

}
