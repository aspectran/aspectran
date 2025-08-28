/*
 * Copyright (c) 2008-present The Aspectran Project
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

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedCharSequence;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.jline.utils.Colors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            return new AttributedString(input, defaultStyle);
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

    private static AttributedStyle style(@NonNull AttributedStyle currentStyle,
                                         @Nullable AttributedStyle defaultStyle,
                                         @Nullable String... styles) {
        if (defaultStyle == null) {
            defaultStyle = AttributedStyle.DEFAULT;
        }
        if (styles != null) {
            for (String style : styles) {
                switch (style) {
                    case "reset":
                        currentStyle = defaultStyle;
                        break;
                    case "bold":
                        currentStyle = currentStyle.bold();
                        break;
                    case "faint":
                        currentStyle = currentStyle.faint();
                        break;
                    case "bold:off":
                        currentStyle = currentStyle.boldOff().faintOff();
                        break;
                    case "italic":
                        currentStyle = currentStyle.italic();
                        break;
                    case "italic:off":
                        currentStyle = currentStyle.italicOff();
                        break;
                    case "underline":
                        currentStyle = currentStyle.underline();
                        break;
                    case "underline:off":
                        currentStyle = currentStyle.underlineOff();
                        break;
                    case "blink":
                        currentStyle = currentStyle.blink();
                        break;
                    case "blink:off":
                        currentStyle = currentStyle.blinkOff();
                        break;
                    case "inverse":
                        currentStyle = currentStyle.inverse();
                        break;
                    case "inverse:off":
                        currentStyle = currentStyle.inverseOff();
                        break;
                    case "conceal":
                        currentStyle = currentStyle.conceal();
                        break;
                    case "conceal:off":
                        currentStyle = currentStyle.concealOff();
                        break;
                    case "crossedOut":
                        currentStyle = currentStyle.crossedOut();
                        break;
                    case "crossedOut:off":
                        currentStyle = currentStyle.crossedOutOff();
                        break;
                    case "black":
                    case "BLACK":
                    case "fg:black":
                    case "fg:BLACK":
                        currentStyle = currentStyle.foreground(AttributedStyle.BLACK);
                        break;
                    case "red":
                    case "fg:red":
                        currentStyle = currentStyle.foreground(AttributedStyle.RED);
                        break;
                    case "green":
                    case "fg:green":
                        currentStyle = currentStyle.foreground(AttributedStyle.GREEN);
                        break;
                    case "yellow":
                    case "fg:yellow":
                        currentStyle = currentStyle.foreground(AttributedStyle.YELLOW);
                        break;
                    case "blue":
                    case "fg:blue":
                        currentStyle = currentStyle.foreground(AttributedStyle.BLUE);
                        break;
                    case "magenta":
                    case "fg:magenta":
                        currentStyle = currentStyle.foreground(AttributedStyle.MAGENTA);
                        break;
                    case "cyan":
                    case "fg:cyan":
                        currentStyle = currentStyle.foreground(AttributedStyle.CYAN);
                        break;
                    case "GRAY":
                    case "fg:GRAY":
                        currentStyle = currentStyle.foreground(AttributedStyle.WHITE);
                        break;
                    case "gray":
                    case "fg:gray":
                        currentStyle = currentStyle.foreground(AttributedStyle.BLACK | AttributedStyle.BRIGHT);
                        break;
                    case "RED":
                    case "fg:RED":
                        currentStyle = currentStyle.foreground(AttributedStyle.RED | AttributedStyle.BRIGHT);
                        break;
                    case "GREEN":
                    case "fg:GREEN":
                        currentStyle = currentStyle.foreground(AttributedStyle.GREEN | AttributedStyle.BRIGHT);
                        break;
                    case "YELLOW":
                    case "fg:YELLOW":
                        currentStyle = currentStyle.foreground(AttributedStyle.YELLOW | AttributedStyle.BRIGHT);
                        break;
                    case "BLUE":
                    case "fg:BLUE":
                        currentStyle = currentStyle.foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT);
                        break;
                    case "MAGENTA":
                    case "fg:MAGENTA":
                        currentStyle = currentStyle.foreground(AttributedStyle.MAGENTA | AttributedStyle.BRIGHT);
                        break;
                    case "CYAN":
                    case "fg:CYAN":
                        currentStyle = currentStyle.foreground(AttributedStyle.CYAN | AttributedStyle.BRIGHT);
                        break;
                    case "WHITE":
                    case "white":
                    case "fg:WHITE":
                    case "fg:white":
                        currentStyle = currentStyle.foreground(AttributedStyle.WHITE | AttributedStyle.BRIGHT);
                        break;
                    case "fg:off":
                        currentStyle = currentStyle.foregroundOff();
                        break;
                    case "bg:black":
                    case "bg:BLACK":
                        currentStyle = currentStyle.background(AttributedStyle.BLACK);
                        break;
                    case "bg:red":
                        currentStyle = currentStyle.background(AttributedStyle.RED);
                        break;
                    case "bg:green":
                        currentStyle = currentStyle.background(AttributedStyle.GREEN);
                        break;
                    case "bg:yellow":
                        currentStyle = currentStyle.background(AttributedStyle.YELLOW);
                        break;
                    case "bg:blue":
                        currentStyle = currentStyle.background(AttributedStyle.BLUE);
                        break;
                    case "bg:magenta":
                        currentStyle = currentStyle.background(AttributedStyle.MAGENTA);
                        break;
                    case "bg:cyan":
                        currentStyle = currentStyle.background(AttributedStyle.CYAN);
                        break;
                    case "bg:GRAY":
                        currentStyle = currentStyle.background(AttributedStyle.WHITE);
                        break;
                    case "bg:gray":
                        currentStyle = currentStyle.background(AttributedStyle.BLACK | AttributedStyle.BRIGHT);
                        break;
                    case "bg:RED":
                        currentStyle = currentStyle.background(AttributedStyle.RED | AttributedStyle.BRIGHT);
                        break;
                    case "bg:GREEN":
                        currentStyle = currentStyle.background(AttributedStyle.GREEN | AttributedStyle.BRIGHT);
                        break;
                    case "bg:YELLOW":
                        currentStyle = currentStyle.background(AttributedStyle.YELLOW | AttributedStyle.BRIGHT);
                        break;
                    case "bg:BLUE":
                        currentStyle = currentStyle.background(AttributedStyle.BLUE | AttributedStyle.BRIGHT);
                        break;
                    case "bg:MAGENTA":
                        currentStyle = currentStyle.background(AttributedStyle.MAGENTA | AttributedStyle.BRIGHT);
                        break;
                    case "bg:CYAN":
                        currentStyle = currentStyle.background(AttributedStyle.CYAN | AttributedStyle.BRIGHT);
                        break;
                    case "bg:WHITE":
                    case "bg:white":
                        currentStyle = currentStyle.background(AttributedStyle.WHITE | AttributedStyle.BRIGHT);
                        break;
                    case "bg:off":
                        currentStyle = currentStyle.backgroundOff();
                        break;
                    default:
                        int color = -1;
                        if (style.startsWith("bg:")) {
                            try {
                                color = Integer.parseInt(style.substring(3));
                            } catch (NumberFormatException ignored) {
                                try {
                                    color = Colors.rgbColor(style.toLowerCase());
                                } catch (IllegalArgumentException e) {
                                    logger.warn("Unable to parse color from string \"{}\"", style, e);
                                }
                            }
                            currentStyle = currentStyle.background(color);
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
                                } catch (IllegalArgumentException e) {
                                    logger.warn("Unable to parse color from string \"{}\"", style, e);
                                }
                            }
                            currentStyle = currentStyle.foreground(color);
                        }
                        if (color == -1) {
                            logger.warn("Unknown color code \"{}\"", style);
                        }
                        break;
                }
            }
        }
        return currentStyle;
    }

}
