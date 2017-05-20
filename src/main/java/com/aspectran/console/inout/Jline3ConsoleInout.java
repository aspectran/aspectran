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
package com.aspectran.console.inout;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.jline.builtins.Options;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

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
            String prompt = toAnsi(getCommandPrompt(), "green");
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
        String confirm = toAnsi("Are you sure you want to quit [Y/n]?", "bold", "yellow");
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
        try {
            if (style != null) {
                AttributedString as = new AttributedString(string, style);
                getWriter().write(as.toAnsi(terminal));
            } else {
                getWriter().write(string);
            }
        } catch (IOException e) {
            throw new IOError(e);
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
        write(Options.NL);
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
        this.style = makeStyle(styles);
    }

    @Override
    public void clearStyle() {
        this.style = null;
    }

    private AttributedStyle makeStyle(String... styles) {
        AttributedStyle attributedStyle = AttributedStyle.DEFAULT;
        for(String style : styles) {
            switch (style) {
                case "bold":
                    attributedStyle = attributedStyle.bold();
                    break;
                case "faint":
                    attributedStyle = attributedStyle.faint();
                    break;
                case "bold:off":
                    attributedStyle = attributedStyle.boldOff().faintOff();
                    break;
                case "italic":
                    attributedStyle = attributedStyle.italic();
                    break;
                case "italic:off":
                    attributedStyle = attributedStyle.italicOff();
                    break;
                case "underline":
                    attributedStyle = attributedStyle.underline();
                    break;
                case "underline:off":
                    attributedStyle = attributedStyle.underlineOff();
                    break;
                case "blink":
                    attributedStyle = attributedStyle.blink();
                    break;
                case "blink:off":
                    attributedStyle = attributedStyle.blinkOff();
                    break;
                case "inverse":
                    attributedStyle = attributedStyle.inverse();
                    break;
                case "inverse:off":
                    attributedStyle = attributedStyle.inverseOff();
                    break;
                case "conceal":
                    attributedStyle = attributedStyle.conceal();
                    break;
                case "conceal:off":
                    attributedStyle = attributedStyle.concealOff();
                    break;
                case "crossedOut":
                    attributedStyle = attributedStyle.crossedOut();
                    break;
                case "crossedOut:off":
                    attributedStyle = attributedStyle.crossedOutOff();
                    break;
                case "black":
                case "fg:black":
                    attributedStyle = attributedStyle.foreground(0);
                    break;
                case "red":
                case "fg:red":
                    attributedStyle = attributedStyle.foreground(1);
                    break;
                case "green":
                case "fg:green":
                    attributedStyle = attributedStyle.foreground(2);
                    break;
                case "yellow":
                case "fg:yellow":
                    attributedStyle = attributedStyle.foreground(3);
                    break;
                case "blue":
                case "fg:blue":
                    attributedStyle = attributedStyle.foreground(4);
                    break;
                case "magenta":
                case "fg:magenta":
                    attributedStyle = attributedStyle.foreground(5);
                    break;
                case "cyan":
                case "fg:cyan":
                    attributedStyle = attributedStyle.foreground(6);
                    break;
                case "white":
                case "fg:white":
                    attributedStyle = attributedStyle.foreground(7);
                    break;
                case "black:light":
                case "fg:black:light":
                    attributedStyle = attributedStyle.foreground(8);
                    break;
                case "red:light":
                case "fg:red:light":
                    attributedStyle = attributedStyle.foreground(9);
                    break;
                case "green:light":
                case "fg:green:light":
                    attributedStyle = attributedStyle.foreground(10);
                    break;
                case "yellow:light":
                case "fg:yellow:light":
                    attributedStyle = attributedStyle.foreground(11);
                    break;
                case "blue:light":
                case "fg:blue:light":
                    attributedStyle = attributedStyle.foreground(12);
                    break;
                case "magenta:light":
                case "fg:magenta:light":
                    attributedStyle = attributedStyle.foreground(13);
                    break;
                case "cyan:light":
                case "fg:cyan:light":
                    attributedStyle = attributedStyle.foreground(14);
                    break;
                case "white:light":
                case "fg:white:light":
                    attributedStyle = attributedStyle.foreground(15);
                    break;
                case "fg:off":
                    attributedStyle = attributedStyle.foregroundOff();
                    break;
                case "bg:black":
                    attributedStyle = attributedStyle.background(0);
                    break;
                case "bg:red":
                    attributedStyle = attributedStyle.background(1);
                    break;
                case "bg:green":
                    attributedStyle = attributedStyle.background(2);
                    break;
                case "bg:yellow":
                    attributedStyle = attributedStyle.background(3);
                    break;
                case "bg:blue":
                    attributedStyle = attributedStyle.background(4);
                    break;
                case "bg:magenta":
                    attributedStyle = attributedStyle.background(5);
                    break;
                case "bg:cyan":
                    attributedStyle = attributedStyle.background(6);
                    break;
                case "bg:white":
                    attributedStyle = attributedStyle.background(7);
                    break;
                case "bg:black:light":
                    attributedStyle = attributedStyle.background(8);
                    break;
                case "bg:red:light":
                    attributedStyle = attributedStyle.background(9);
                    break;
                case "bg:green:light":
                    attributedStyle = attributedStyle.background(10);
                    break;
                case "bg:yellow:light":
                    attributedStyle = attributedStyle.background(11);
                    break;
                case "bg:blue:light":
                    attributedStyle = attributedStyle.background(12);
                    break;
                case "bg:magenta:light":
                    attributedStyle = attributedStyle.background(13);
                    break;
                case "bg:cyan:light":
                    attributedStyle = attributedStyle.background(14);
                    break;
                case "bg:white:light":
                    attributedStyle = attributedStyle.background(15);
                    break;
                case "bg:off":
                    attributedStyle = attributedStyle.backgroundOff();
                    break;
                default:
                    // rgb:123:123:123
                    if (style.startsWith("rgb:")) {
                        String[] arr = StringUtils.split(style.substring(4), ':');
                        int col = rgbColor(arr);
                        if (col > -1) {
                            attributedStyle = attributedStyle.foreground(col);
                        }
                    }
                    // fg:rgb:123:123:123
                    if (style.startsWith("fg:rgb:")) {
                        String[] arr = StringUtils.split(style.substring(7), ':');
                        int col = rgbColor(arr);
                        if (col > -1) {
                            attributedStyle = attributedStyle.foreground(col);
                        }
                    }
                    // bg:rgb:123:123:123
                    if (style.startsWith("bg:rgb:")) {
                        String[] arr = StringUtils.split(style.substring(7), ':');
                        int col = rgbColor(arr);
                        if (col > -1) {
                            attributedStyle = attributedStyle.background(col);
                        }
                    }
                    break;
            }
        }
        return attributedStyle;
    }

    private String toAnsi(String string, String... styles) {
        AttributedString as = new AttributedString(string, makeStyle(styles));
        return as.toAnsi();
    }

    private int rgbColor(String[] rgb) {
        if (rgb.length == 3) {
            try {
                int r = Integer.parseInt(rgb[0]);
                int b = Integer.parseInt(rgb[1]);
                int g = Integer.parseInt(rgb[2]);
                // convert to 256 colors
                return (16 + (r >> 3) * 36 + (g >> 3) * 6 + (b >> 3));
            } catch (NumberFormatException e) {
                log.warn("Parsing RGB color failed: " + Arrays.toString(rgb), e);
            }
        }
        return -1;
    }

}
