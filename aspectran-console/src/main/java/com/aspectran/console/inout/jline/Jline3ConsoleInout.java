/*
 * Copyright 2008-2017 Juho Jeong
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
package com.aspectran.console.inout.jline;

import com.aspectran.console.inout.AbstractConsoleInout;
import com.aspectran.console.inout.ConsoleTerminatedException;
import com.aspectran.console.inout.UnclosablePrintWriter;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import org.jline.builtins.Options;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Console I/O implementation that supports Jline.
 *
 * <p>Created: 2017. 3. 4.</p>
 */
public class Jline3ConsoleInout extends AbstractConsoleInout {

    private static final Log log = LogFactory.getLog(AbstractConsoleInout.class);

    private static final String APP_NAME = "Aspectran Console";

    private static final Character MASK_CHAR = '*';

    private static final String encoding = Charset.defaultCharset().name();

    private final Terminal terminal;

    private final LineReader reader;

    private final LineReader commandReader;
    
    private AttributedStyle style;

    public Jline3ConsoleInout() throws IOException {
        this.terminal = TerminalBuilder.builder().encoding(encoding).build();
        this.reader = LineReaderBuilder.builder().appName(APP_NAME).terminal(terminal).build();
        this.commandReader = LineReaderBuilder.builder().appName(APP_NAME).terminal(terminal).build();
    }

    @Override
    public String readCommand() {
        try {
            String prompt = toAnsi(getCommandPrompt());
            return commandReader.readLine(prompt);
        } catch (UserInterruptException e) {
            if (confirmQuit()) {
                throw new ConsoleTerminatedException();
            } else {
                return null;
            }
        }
    }

    private boolean confirmQuit() {
        String confirm = toAnsi("{{yellow}}Are you sure you want to quit [{{bold}}Y{{bold:off}}/{{bold}}n{{bold:off}}]?{{off}}");
        String yn = readLine(confirm);
        return (yn.isEmpty() || yn.equalsIgnoreCase("Y"));
    }

    @Override
    public String readLine() {
        try {
            return reader.readLine();
        } catch (UserInterruptException e) {
            throw new ConsoleTerminatedException();
        }
    }

    @Override
    public String readLine(String prompt) {
        try {
            return reader.readLine(prompt);
        } catch (UserInterruptException e) {
            throw new ConsoleTerminatedException();
        }
    }

    @Override
    public String readLine(String format, Object... args) {
        try {
            return reader.readLine(String.format(format, args));
        } catch (UserInterruptException e) {
            throw new ConsoleTerminatedException();
        }
    }

    @Override
    public String readPassword() {
        try {
            return reader.readLine(MASK_CHAR);
        } catch (UserInterruptException e) {
            throw new ConsoleTerminatedException();
        }
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
        try {
            return reader.readLine(String.format(format, args), MASK_CHAR);
        } catch (UserInterruptException e) {
            throw new ConsoleTerminatedException();
        }
    }

    @Override
    public void write(String string) {
        if (style != null) {
            AttributedString as = new AttributedString(string, style);
            writeRawText(as.toAnsi(terminal));
        } else {
            AttributedStringBuilder asb = JlineAnsiStringUtils.parse(string);
            writeRawText(asb.toAnsi(terminal));
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
    public void flush() {
        try {
            getWriter().flush();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    @Override
    public Writer getUnclosableWriter() throws UnsupportedEncodingException {
        Writer writer = new JlineAnsiStringWriter(terminal, getWriter());
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
        this.style = JlineAnsiStringUtils.makeStyle(styles);
    }

    @Override
    public void offStyle() {
        this.style = null;
    }

    private String toAnsi(String string) {
        return JlineAnsiStringUtils.toAnsi(string, terminal);
    }

}
