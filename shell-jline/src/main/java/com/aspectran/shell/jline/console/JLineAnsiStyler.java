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

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.console.AnsiStyleHandler;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Colors;

/**
 * Utilities to handle ANSI escape sequences with JLine.
 *
 * <p>Created: 2017. 5. 21.</p>
 *
 * @since 4.1.0
 */
public class JLineAnsiStyler {

    private static final Log log = LogFactory.getLog(JLineAnsiStyler.class);

    public static String parse(String input) {
        return parse(input, null);
    }

    public static String parse(String input, Terminal terminal) {
        if (input == null || !input.contains("{{") || !input.contains("}}")) {
            return input;
        }

        final AttributedStringBuilder asb = new AttributedStringBuilder(input.length());
        AnsiStyleHandler handler = new AnsiStyleHandler() {
            @Override
            public void character(char c) {
                asb.append(c);
            }
            @Override
            public void attribute(String... attrs) {
                asb.style(makeStyle(asb.style(), attrs));
            }
        };
        handler.handle(input);
        return asb.toAnsi(terminal);
    }

    public static AttributedStyle makeStyle(String... styles) {
        AttributedStyle attributedStyle = AttributedStyle.DEFAULT;
        return makeStyle(attributedStyle, styles);
    }

    public static AttributedStyle makeStyle(AttributedStyle as, String... styles) {
        for (String style : styles) {
            switch (style) {
                case "off":
                    as = AttributedStyle.DEFAULT;
                    break;
                case "bold":
                    as = as.bold();
                    break;
                case "faint":
                    as = as.faint();
                    break;
                case "bold:off":
                    as = as.boldOff().faintOff();
                    break;
                case "italic":
                    as = as.italic();
                    break;
                case "italic:off":
                    as = as.italicOff();
                    break;
                case "underline":
                    as = as.underline();
                    break;
                case "underline:off":
                    as = as.underlineOff();
                    break;
                case "blink":
                    as = as.blink();
                    break;
                case "blink:off":
                    as = as.blinkOff();
                    break;
                case "inverse":
                    as = as.inverse();
                    break;
                case "inverse:off":
                    as = as.inverseOff();
                    break;
                case "conceal":
                    as = as.conceal();
                    break;
                case "conceal:off":
                    as = as.concealOff();
                    break;
                case "crossedOut":
                    as = as.crossedOut();
                    break;
                case "crossedOut:off":
                    as = as.crossedOutOff();
                    break;
                case "black":
                case "BLACK":
                case "fg:black":
                case "fg:BLACK":
                    as = as.foreground(0);
                    break;
                case "red":
                case "fg:red":
                    as = as.foreground(1);
                    break;
                case "green":
                case "fg:green":
                    as = as.foreground(2);
                    break;
                case "yellow":
                case "fg:yellow":
                    as = as.foreground(3);
                    break;
                case "blue":
                case "fg:blue":
                    as = as.foreground(4);
                    break;
                case "magenta":
                case "fg:magenta":
                    as = as.foreground(5);
                    break;
                case "cyan":
                case "fg:cyan":
                    as = as.foreground(6);
                    break;
                case "GRAY":
                case "fg:GRAY":
                    as = as.foreground(7);
                    break;
                case "gray":
                case "fg:gray":
                    as = as.foreground(8);
                    break;
                case "RED":
                case "fg:RED":
                    as = as.foreground(9);
                    break;
                case "GREEN":
                case "fg:GREEN":
                    as = as.foreground(10);
                    break;
                case "YELLOW":
                case "fg:YELLOW":
                    as = as.foreground(11);
                    break;
                case "BLUE":
                case "fg:BLUE":
                    as = as.foreground(12);
                    break;
                case "MAGENTA":
                case "fg:MAGENTA":
                    as = as.foreground(13);
                    break;
                case "CYAN":
                case "fg:CYAN":
                    as = as.foreground(14);
                    break;
                case "WHITE":
                case "white":
                case "fg:WHITE":
                case "fg:white":
                    as = as.foreground(15);
                    break;
                case "fg:off":
                    as = as.foregroundOff();
                    break;
                case "bg:black":
                case "bg:BLACK":
                    as = as.background(0);
                    break;
                case "bg:red":
                    as = as.background(1);
                    break;
                case "bg:green":
                    as = as.background(2);
                    break;
                case "bg:yellow":
                    as = as.background(3);
                    break;
                case "bg:blue":
                    as = as.background(4);
                    break;
                case "bg:magenta":
                    as = as.background(5);
                    break;
                case "bg:cyan":
                    as = as.background(6);
                    break;
                case "bg:GRAY":
                    as = as.background(7);
                    break;
                case "bg:gray":
                    as = as.background(8);
                    break;
                case "bg:RED":
                    as = as.background(9);
                    break;
                case "bg:GREEN":
                    as = as.background(10);
                    break;
                case "bg:YELLOW":
                    as = as.background(11);
                    break;
                case "bg:BLUE":
                    as = as.background(12);
                    break;
                case "bg:MAGENTA":
                    as = as.background(13);
                    break;
                case "bg:CYAN":
                    as = as.background(14);
                    break;
                case "bg:WHITE":
                case "bg:white":
                    as = as.background(15);
                    break;
                case "bg:off":
                    as = as.backgroundOff();
                    break;
                default:
                    int color = -1;
                    if (style.startsWith("bg:")) {
                        try {
                            color = Integer.parseInt(style.substring(3));
                        } catch (NumberFormatException ignored) {
                            try {
                                color = Colors.rgbColor(style.toLowerCase());
                            } catch (Throwable e) {
                                log.warn("Parsing RGB color failed: " + style, e);
                            }
                        }
                        as = as.background(color);
                    } else {
                        try {
                            if (style.startsWith("fb:")) {
                                color = Integer.parseInt(style.substring(3));
                            } else {
                                color = Integer.parseInt(style);
                            }
                        } catch (NumberFormatException ignored) {
                            try {
                                color = Colors.rgbColor(style.toLowerCase());
                            } catch (Throwable e) {
                                log.warn("Parsing RGB color failed: " + style, e);
                            }
                        }
                        as = as.foreground(color);
                    }
                    if (color == -1) {
                        log.warn("Unknown color code \"" + style + "\"");
                    }
                    break;
            }
        }
        return as;
    }

}
