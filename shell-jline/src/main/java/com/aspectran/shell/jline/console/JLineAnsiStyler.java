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

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.console.AnsiStyleHandler;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.Arrays;

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
                case "fg:black":
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
                case "white":
                case "fg:white":
                    as = as.foreground(7);
                    break;
                case "BLACK":
                case "fg:BLACK":
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
                case "fg:WHITE":
                    as = as.foreground(15);
                    break;
                case "fg:off":
                    as = as.foregroundOff();
                    break;
                case "bg:black":
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
                case "bg:white":
                    as = as.background(7);
                    break;
                case "bg:BLACK":
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
                    as = as.background(15);
                    break;
                case "bg:off":
                    as = as.backgroundOff();
                    break;
                default:
                    // rgb:123:123:123
                    if (style.startsWith("rgb:")) {
                        String[] arr = StringUtils.split(style.substring(4), ':');
                        int col = rgbColor(arr);
                        if (col > -1) {
                            as = as.foreground(col);
                        }
                    }
                    // fg:rgb:123:123:123
                    if (style.startsWith("fg:rgb:")) {
                        String[] arr = StringUtils.split(style.substring(7), ':');
                        int col = rgbColor(arr);
                        if (col > -1) {
                            as = as.foreground(col);
                        }
                    }
                    // bg:rgb:123:123:123
                    if (style.startsWith("bg:rgb:")) {
                        String[] arr = StringUtils.split(style.substring(7), ':');
                        int col = rgbColor(arr);
                        if (col > -1) {
                            as = as.background(col);
                        }
                    }
                    break;
            }
        }
        return as;
    }

    private static int rgbColor(String[] rgb) {
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
