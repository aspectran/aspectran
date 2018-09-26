/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Console I/O implementation that supports System Console.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public class DefaultConsole extends AbstractConsole {

    private boolean reading;

    @Override
    public String readCommandLine() {
        String prompt = getCommandPrompt();
        return readCommandLine(prompt);
    }

    @Override
    public String readCommandLine(String prompt) {
        try {
            if (System.console() != null) {
                return System.console().readLine(prompt);
            } else {
                if (prompt != null) {
                    write(prompt);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                return reader.readLine();
            }
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public String readLine() {
        return readLine(null);
    }

    @Override
    public String readLine(String prompt) {
        try {
            reading = true;
            if (System.console() != null) {
                return System.console().readLine(prompt);
            } else {
                if (prompt != null) {
                    write(prompt);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                return reader.readLine();
            }
        } catch (IOException e) {
            throw new IOError(e);
        } finally {
            reading = false;
        }
    }

    @Override
    public String readLine(String format, Object... args) {
        return readLine(String.format(format, args));
    }

    @Override
    public String readPassword() {
        return readPassword(null);
    }

    @Override
    public String readPassword(String prompt) {
        if (System.console() != null) {
            return new String(System.console().readPassword(prompt));
        } else {
            return readLine(prompt);
        }
    }

    @Override
    public String readPassword(String format, Object... args) {
        return readPassword(String.format(format, args));
    }

    @Override
    public void write(String string) {
        System.out.print(string);
    }

    @Override
    public void write(String format, Object... args) {
        System.out.print(String.format(format, args));
    }

    @Override
    public void writeLine(String string) {
        System.out.println(string);
    }

    @Override
    public void writeLine(String format, Object... args) {
        write(format, args);
        System.out.println();
    }

    @Override
    public void writeLine() {
        System.out.println();
    }

    @Override
    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    @Override
    public void flush() {
        System.out.flush();
    }

    @Override
    public String getEncoding() {
        return Charset.defaultCharset().name();
    }

    public OutputStream getOutput() {
        return System.out;
    }

    @Override
    public Writer getWriter() {
        if (System.console() != null) {
            return System.console().writer();
        } else {
            return new PrintWriter(System.out);
        }
    }

    @Override
    public void setStyle(String... styles) {
        // Nothing to do
    }

    @Override
    public void offStyle() {
        // Nothing to do
    }

    private String stripStyle() {
        return null;
    }

    @Override
    public boolean isReading() {
        return reading;
    }

    @Override
    public boolean confirmRestart() {
        return confirmRestart(null);
    }

    @Override
    public boolean confirmRestart(String message) {
        if (isReading()) {
            writeLine("Illegal State");
            return false;
        }
        if (message != null) {
            writeLine(message);
        }
        String confirm = "Would you like to restart this shell [Y/n]?";
        String yn = readLine(confirm);
        return (yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

    @Override
    public boolean confirmQuit() {
        String confirm = "Are you sure you want to quit [Y/n]?";
        String yn = readLine(confirm);
        return (yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

}
