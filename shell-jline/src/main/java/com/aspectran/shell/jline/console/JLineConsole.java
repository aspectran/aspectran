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
package com.aspectran.shell.jline.console;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.shell.command.ConsoleTerminatedException;
import com.aspectran.shell.console.AbstractConsole;
import com.aspectran.shell.console.UnclosablePrintWriter;
import org.jline.builtins.Options;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.reader.impl.DefaultParser;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Console I/O implementation that supports JLine.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public class JLineConsole extends AbstractConsole {

    private static final String APP_NAME = "Aspectran Shell";

    private static final Character MASK_CHAR = '*';

    private static final String MULTILINE_DELIMITER = "\\";

    private static final String COMMENT_DELIMITER = "//";

    private static final String encoding = Charset.defaultCharset().name();

    private final Terminal terminal;

    private final LineReader reader;

    private final LineReader commandReader;
    
    private AttributedStyle style;

    public JLineConsole() throws IOException {
        DefaultParser parser = new DefaultParser();

        this.terminal = TerminalBuilder.builder().encoding(encoding).build();
        this.reader = LineReaderBuilder.builder().appName(APP_NAME).parser(parser).terminal(terminal).build();
        this.commandReader = LineReaderBuilder.builder().appName(APP_NAME).parser(parser).terminal(terminal).build();
    }

    @Override
    public String readCommandLine() {
        String prompt = toAnsi(getCommandPrompt());
        return readCommandLine(prompt);
    }

    @Override
    public String readCommandLine(String prompt) {
        try {
            String line = commandReader.readLine(prompt).trim();
            line = readCommandMultiLine(line);
            if (line == null || line.startsWith(COMMENT_DELIMITER)) {
                return null;
            } else {
                return line;
            }
        } catch (UserInterruptException e) {
            if (confirmQuit()) {
                throw new ConsoleTerminatedException();
            } else {
                return null;
            }
        }
    }

    private String readCommandMultiLine(String line) {
        boolean comments = COMMENT_DELIMITER.equals(line);
        boolean continuous = (MULTILINE_DELIMITER.equals(line) || comments);
        if (line == null || continuous) {
            line = commandReader.readLine("> ").trim();
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
            String line = reader.readLine(prompt);
            return readMultiLine(line);
        } catch (UserInterruptException e) {
            throw new ConsoleTerminatedException();
        }
    }

    private String readMultiLine(String line) {
        if (line == null) {
            line = reader.readLine("> ").trim();
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
        try {
            return reader.readLine(prompt, MASK_CHAR);
        } catch (UserInterruptException e) {
            throw new ConsoleTerminatedException();
        }
    }

    @Override
    public String readPassword(String format, Object... args) {
        return readPassword(String.format(format, args));
    }

    @Override
    public void write(String string) {
        if (style != null) {
            AttributedString as = new AttributedString(string, style);
            writeRawText(as.toAnsi(terminal));
        } else {
            writeRawText(toAnsi(string));
        }
    }

    @Override
    public void write(String format, Object... args) {
        write(String.format(format, args));
    }

    @Override
    public void writeLine(String string) {
        write(string);
        writeLine();
    }

    @Override
    public void writeLine(String format, Object... args) {
        write(format, args);
        writeLine();
    }

    @Override
    public void writeLine() {
        writeRawText(Options.NL);
    }

    private void writeRawText(String string) {
        try {
            getWriter().write(string);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public void clearScreen() {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
    }

    @Override
    public void flush() {
        try {
            getWriter().flush();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public Writer getUnclosableWriter() {
        Writer writer = new JLineAnsiStringWriter(terminal, getWriter());
        return new UnclosablePrintWriter(writer);
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public OutputStream getOutput() {
        return terminal.output();
    }

    @Override
    public Writer getWriter() {
        return terminal.writer();
    }

    @Override
    public void setStyle(String... styles) {
        this.style = JLineAnsiStyler.makeStyle(styles);
    }

    @Override
    public void offStyle() {
        this.style = null;
    }

    private String toAnsi(String string) {
        return JLineAnsiStyler.parse(string, terminal);
    }

    @Override
    public boolean isReading() {
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
            String message2 = toAnsi("{{YELLOW}}" + message + "{{off}}");
            reader.printAbove(message2);
        }
        String confirm = toAnsi("{{YELLOW}}Would you like to restart this shell [{{bold}}Y{{bold:off}}/{{bold}}n{{bold:off}}]?{{off}}");
        String yn = readLine(confirm);
        return (yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

    @Override
    public boolean confirmQuit() {
        String confirm = toAnsi("{{YELLOW}}Are you sure you want to quit [{{bold}}Y{{bold:off}}/{{bold}}n{{bold:off}}]?{{off}}");
        String yn = readLine(confirm);
        return (yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

}
