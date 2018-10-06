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

import com.aspectran.core.context.ActivityContext;

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

    private static final String MULTILINE_DELIMITER = "\\\\";

    private static final String COMMENT_DELIMITER = "//";

    private volatile boolean reading;

    @Override
    public String readCommandLine() {
        String prompt = getCommandPrompt();
        return readCommandLine(prompt);
    }

    @Override
    public String readCommandLine(String prompt) {
        try {
            String line;
            if (System.console() != null) {
                line = System.console().readLine(prompt).trim();
            } else {
                if (prompt != null) {
                    write(prompt);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                line = reader.readLine().trim();
            }
            line = readCommandMultiLine(line);
            if (line == null || line.startsWith(COMMENT_DELIMITER)) {
                return null;
            } else {
                return line;
            }
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private String readCommandMultiLine(String line) throws IOException {
        boolean comments = COMMENT_DELIMITER.equals(line);
        boolean continuous = (MULTILINE_DELIMITER.equals(line) || comments);
        if (line == null || continuous) {
            if (System.console() != null) {
                line = System.console().readLine("> ").trim();
            } else {
                write("> ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                line = reader.readLine().trim();
            }
        }
        String nextLine = null;
        if (continuous) {
            if (!line.isEmpty()) {
                if (comments) {
                    nextLine = readCommandMultiLine(COMMENT_DELIMITER);
                } else {
                    nextLine = readCommandMultiLine(MULTILINE_DELIMITER);
                }
            } else {
                return null;
            }
        } else if (line.endsWith(MULTILINE_DELIMITER)) {
            line = line.substring(0, line.length() - MULTILINE_DELIMITER.length()).trim();
            nextLine = readCommandMultiLine(null);
        }
        if (comments) {
            if (nextLine != null) {
                return COMMENT_DELIMITER + line + ActivityContext.LINE_SEPARATOR + nextLine;
            } else {
                return COMMENT_DELIMITER + line;
            }
        } else {
            if (nextLine != null && !nextLine.isEmpty()) {
                return line + " " + nextLine;
            } else {
                return line;
            }
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
            String line;
            if (System.console() != null) {
                line = System.console().readLine(prompt);
            } else {
                if (prompt != null) {
                    write(prompt);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                line = reader.readLine();
            }
            return readMultiLine(line);
        } catch (IOException e) {
            throw new IOError(e);
        } finally {
            reading = false;
        }
    }

    private String readMultiLine(String line) throws IOException {
        if (line == null) {
            if (System.console() != null) {
                line = System.console().readLine("> ").trim();
            } else {
                write("> ");
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                line = reader.readLine().trim();
            }
        }
        if (line.endsWith(MULTILINE_DELIMITER)) {
            line = line.substring(0, line.length() - MULTILINE_DELIMITER.length()) +
                    ActivityContext.LINE_SEPARATOR + readMultiLine(null);
        }
        return line;
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
