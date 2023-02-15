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
package com.aspectran.shell.jline.console;

import com.aspectran.core.lang.NonNull;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.logging.Logger;
import com.aspectran.core.util.logging.LoggerFactory;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedCharSequence;
import org.jline.utils.AttributedString;
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
public class JLineTextStyler {

    private static final Logger logger = LoggerFactory.getLogger(JLineTextStyler.class);

    public static AttributedCharSequence parse(String input) {
        return parse(input, null);
    }

    public static AttributedCharSequence parse(final String input, final AttributedStyle defaultStyle) {
        if (input == null) {
            return AttributedString.EMPTY;
        }
        if (!input.contains("{{") || !input.contains("}}")) {
            return new AttributedString(input);
        }
        final AttributedStringBuilder asb = new AttributedStringBuilder(input.length());
        if (defaultStyle != null) {
            asb.style(defaultStyle);
        }
        new TextStyleAttributeHandler() {
            @Override
            public void character(char c) {
                asb.append(c);
            }
            @Override
            public void attribute(String... attrs) {
                asb.style(style(asb.style(), defaultStyle, attrs));
            }
        }.handle(input);
        return asb;
    }

    public static String parseAsString(String input, Terminal terminal) {
        return parseAsString(null, input, terminal);
    }

    public static String parseAsString(AttributedStyle defaultStyle, String input, Terminal terminal) {
        return parse(input, defaultStyle).toAnsi(terminal);
    }

    public static AttributedStyle style(String... attrs) {
        return style(AttributedStyle.DEFAULT, AttributedStyle.DEFAULT, attrs);
    }

    private static AttributedStyle style(@NonNull AttributedStyle baseStyle,
                                         @Nullable AttributedStyle defaultStyle,
                                         String... attrs) {
        if (defaultStyle == null) {
            defaultStyle = AttributedStyle.DEFAULT;
        }
        for (String attr : attrs) {
            switch (attr) {
                case "reset":
                    baseStyle = defaultStyle;
                    break;
                case "bold":
                    baseStyle = baseStyle.bold();
                    break;
                case "faint":
                    baseStyle = baseStyle.faint();
                    break;
                case "bold:off":
                    baseStyle = baseStyle.boldOff().faintOff();
                    break;
                case "italic":
                    baseStyle = baseStyle.italic();
                    break;
                case "italic:off":
                    baseStyle = baseStyle.italicOff();
                    break;
                case "underline":
                    baseStyle = baseStyle.underline();
                    break;
                case "underline:off":
                    baseStyle = baseStyle.underlineOff();
                    break;
                case "blink":
                    baseStyle = baseStyle.blink();
                    break;
                case "blink:off":
                    baseStyle = baseStyle.blinkOff();
                    break;
                case "inverse":
                    baseStyle = baseStyle.inverse();
                    break;
                case "inverse:off":
                    baseStyle = baseStyle.inverseOff();
                    break;
                case "conceal":
                    baseStyle = baseStyle.conceal();
                    break;
                case "conceal:off":
                    baseStyle = baseStyle.concealOff();
                    break;
                case "crossedOut":
                    baseStyle = baseStyle.crossedOut();
                    break;
                case "crossedOut:off":
                    baseStyle = baseStyle.crossedOutOff();
                    break;
                case "black":
                case "BLACK":
                case "fg:black":
                case "fg:BLACK":
                    baseStyle = baseStyle.foreground(0);
                    break;
                case "red":
                case "fg:red":
                    baseStyle = baseStyle.foreground(1);
                    break;
                case "green":
                case "fg:green":
                    baseStyle = baseStyle.foreground(2);
                    break;
                case "yellow":
                case "fg:yellow":
                    baseStyle = baseStyle.foreground(3);
                    break;
                case "blue":
                case "fg:blue":
                    baseStyle = baseStyle.foreground(4);
                    break;
                case "magenta":
                case "fg:magenta":
                    baseStyle = baseStyle.foreground(5);
                    break;
                case "cyan":
                case "fg:cyan":
                    baseStyle = baseStyle.foreground(6);
                    break;
                case "GRAY":
                case "fg:GRAY":
                    baseStyle = baseStyle.foreground(7);
                    break;
                case "gray":
                case "fg:gray":
                    baseStyle = baseStyle.foreground(8);
                    break;
                case "RED":
                case "fg:RED":
                    baseStyle = baseStyle.foreground(9);
                    break;
                case "GREEN":
                case "fg:GREEN":
                    baseStyle = baseStyle.foreground(10);
                    break;
                case "YELLOW":
                case "fg:YELLOW":
                    baseStyle = baseStyle.foreground(11);
                    break;
                case "BLUE":
                case "fg:BLUE":
                    baseStyle = baseStyle.foreground(12);
                    break;
                case "MAGENTA":
                case "fg:MAGENTA":
                    baseStyle = baseStyle.foreground(13);
                    break;
                case "CYAN":
                case "fg:CYAN":
                    baseStyle = baseStyle.foreground(14);
                    break;
                case "WHITE":
                case "white":
                case "fg:WHITE":
                case "fg:white":
                    baseStyle = baseStyle.foreground(15);
                    break;
                case "fg:reset":
                    baseStyle = defaultStyle.backgroundDefault();
                    break;
                case "bg:black":
                case "bg:BLACK":
                    baseStyle = baseStyle.background(0);
                    break;
                case "bg:red":
                    baseStyle = baseStyle.background(1);
                    break;
                case "bg:green":
                    baseStyle = baseStyle.background(2);
                    break;
                case "bg:yellow":
                    baseStyle = baseStyle.background(3);
                    break;
                case "bg:blue":
                    baseStyle = baseStyle.background(4);
                    break;
                case "bg:magenta":
                    baseStyle = baseStyle.background(5);
                    break;
                case "bg:cyan":
                    baseStyle = baseStyle.background(6);
                    break;
                case "bg:GRAY":
                    baseStyle = baseStyle.background(7);
                    break;
                case "bg:gray":
                    baseStyle = baseStyle.background(8);
                    break;
                case "bg:RED":
                    baseStyle = baseStyle.background(9);
                    break;
                case "bg:GREEN":
                    baseStyle = baseStyle.background(10);
                    break;
                case "bg:YELLOW":
                    baseStyle = baseStyle.background(11);
                    break;
                case "bg:BLUE":
                    baseStyle = baseStyle.background(12);
                    break;
                case "bg:MAGENTA":
                    baseStyle = baseStyle.background(13);
                    break;
                case "bg:CYAN":
                    baseStyle = baseStyle.background(14);
                    break;
                case "bg:WHITE":
                case "bg:white":
                    baseStyle = baseStyle.background(15);
                    break;
                case "bg:reset":
                    baseStyle = defaultStyle.foregroundDefault();
                    break;
                default:
                    int color = -1;
                    if (attr.startsWith("bg:")) {
                        try {
                            color = Integer.parseInt(attr.substring(3));
                        } catch (NumberFormatException ignored) {
                            try {
                                color = Colors.rgbColor(attr.toLowerCase());
                            } catch (Throwable e) {
                                logger.warn("Unable to parse color from string \"" + attr + "\"", e);
                            }
                        }
                        baseStyle = baseStyle.background(color);
                    } else {
                        try {
                            if (attr.startsWith("fb:")) {
                                color = Integer.parseInt(attr.substring(3));
                            } else {
                                color = Integer.parseInt(attr);
                            }
                        } catch (NumberFormatException ignored) {
                            try {
                                color = Colors.rgbColor(attr.toLowerCase());
                            } catch (Throwable e) {
                                logger.warn("Unable to parse color from string \"" + attr + "\"", e);
                            }
                        }
                        baseStyle = baseStyle.foreground(color);
                    }
                    if (color == -1) {
                        logger.warn("Unknown color code \"" + attr + "\"");
                    }
                    break;
            }
        }
        return baseStyle;
    }

}
