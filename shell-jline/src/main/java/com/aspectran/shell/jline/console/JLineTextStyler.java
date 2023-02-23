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
        new TextStyleTokenHandler() {
            @Override
            public void character(char c) {
                asb.append(c);
            }
            @Override
            public void style(String... styles) {
                asb.style(JLineTextStyler.style(asb.style(), defaultStyle, styles));
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

    public static AttributedStyle style(String... styles) {
        return style(AttributedStyle.DEFAULT, styles);
    }

    public static AttributedStyle style(AttributedStyle baseStyle, String... styles) {
        return style(baseStyle, baseStyle, styles);
    }

    private static AttributedStyle style(@NonNull AttributedStyle baseStyle,
                                         @Nullable AttributedStyle defaultStyle,
                                         @Nullable String... styles) {
        if (defaultStyle == null) {
            defaultStyle = AttributedStyle.DEFAULT;
        }
        if (styles != null) {
            for (String style : styles) {
                switch (style) {
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
                        baseStyle = baseStyle.foreground(AttributedStyle.BLACK);
                        break;
                    case "red":
                    case "fg:red":
                        baseStyle = baseStyle.foreground(AttributedStyle.RED);
                        break;
                    case "green":
                    case "fg:green":
                        baseStyle = baseStyle.foreground(AttributedStyle.GREEN);
                        break;
                    case "yellow":
                    case "fg:yellow":
                        baseStyle = baseStyle.foreground(AttributedStyle.YELLOW);
                        break;
                    case "blue":
                    case "fg:blue":
                        baseStyle = baseStyle.foreground(AttributedStyle.BLUE);
                        break;
                    case "magenta":
                    case "fg:magenta":
                        baseStyle = baseStyle.foreground(AttributedStyle.MAGENTA);
                        break;
                    case "cyan":
                    case "fg:cyan":
                        baseStyle = baseStyle.foreground(AttributedStyle.CYAN);
                        break;
                    case "GRAY":
                    case "fg:GRAY":
                        baseStyle = baseStyle.foreground(AttributedStyle.WHITE);
                        break;
                    case "gray":
                    case "fg:gray":
                        baseStyle = baseStyle.foreground(AttributedStyle.BLACK | AttributedStyle.BRIGHT);
                        break;
                    case "RED":
                    case "fg:RED":
                        baseStyle = baseStyle.foreground(AttributedStyle.RED | AttributedStyle.BRIGHT);
                        break;
                    case "GREEN":
                    case "fg:GREEN":
                        baseStyle = baseStyle.foreground(AttributedStyle.GREEN | AttributedStyle.BRIGHT);
                        break;
                    case "YELLOW":
                    case "fg:YELLOW":
                        baseStyle = baseStyle.foreground(AttributedStyle.YELLOW | AttributedStyle.BRIGHT);
                        break;
                    case "BLUE":
                    case "fg:BLUE":
                        baseStyle = baseStyle.foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT);
                        break;
                    case "MAGENTA":
                    case "fg:MAGENTA":
                        baseStyle = baseStyle.foreground(AttributedStyle.MAGENTA | AttributedStyle.BRIGHT);
                        break;
                    case "CYAN":
                    case "fg:CYAN":
                        baseStyle = baseStyle.foreground(AttributedStyle.CYAN | AttributedStyle.BRIGHT);
                        break;
                    case "WHITE":
                    case "white":
                    case "fg:WHITE":
                    case "fg:white":
                        baseStyle = baseStyle.foreground(AttributedStyle.WHITE | AttributedStyle.BRIGHT);
                        break;
                    case "bg:black":
                    case "bg:BLACK":
                        baseStyle = baseStyle.background(AttributedStyle.BLACK);
                        break;
                    case "bg:red":
                        baseStyle = baseStyle.background(AttributedStyle.RED);
                        break;
                    case "bg:green":
                        baseStyle = baseStyle.background(AttributedStyle.GREEN);
                        break;
                    case "bg:yellow":
                        baseStyle = baseStyle.background(AttributedStyle.YELLOW);
                        break;
                    case "bg:blue":
                        baseStyle = baseStyle.background(AttributedStyle.BLUE);
                        break;
                    case "bg:magenta":
                        baseStyle = baseStyle.background(AttributedStyle.MAGENTA);
                        break;
                    case "bg:cyan":
                        baseStyle = baseStyle.background(AttributedStyle.CYAN);
                        break;
                    case "bg:GRAY":
                        baseStyle = baseStyle.background(AttributedStyle.WHITE);
                        break;
                    case "bg:gray":
                        baseStyle = baseStyle.background(AttributedStyle.BLACK | AttributedStyle.BRIGHT);
                        break;
                    case "bg:RED":
                        baseStyle = baseStyle.background(AttributedStyle.RED | AttributedStyle.BRIGHT);
                        break;
                    case "bg:GREEN":
                        baseStyle = baseStyle.background(AttributedStyle.GREEN | AttributedStyle.BRIGHT);
                        break;
                    case "bg:YELLOW":
                        baseStyle = baseStyle.background(AttributedStyle.YELLOW | AttributedStyle.BRIGHT);
                        break;
                    case "bg:BLUE":
                        baseStyle = baseStyle.background(AttributedStyle.BLUE | AttributedStyle.BRIGHT);
                        break;
                    case "bg:MAGENTA":
                        baseStyle = baseStyle.background(AttributedStyle.MAGENTA | AttributedStyle.BRIGHT);
                        break;
                    case "bg:CYAN":
                        baseStyle = baseStyle.background(AttributedStyle.CYAN | AttributedStyle.BRIGHT);
                        break;
                    case "bg:WHITE":
                    case "bg:white":
                        baseStyle = baseStyle.background(AttributedStyle.WHITE | AttributedStyle.BRIGHT);
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
                                    logger.warn("Unable to parse color from string \"" + style + "\"", e);
                                }
                            }
                            baseStyle = baseStyle.background(color);
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
                                    logger.warn("Unable to parse color from string \"" + style + "\"", e);
                                }
                            }
                            baseStyle = baseStyle.foreground(color);
                        }
                        if (color == -1) {
                            logger.warn("Unknown color code \"" + style + "\"");
                        }
                        break;
                }
            }
        }
        return baseStyle;
    }

}
