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

import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.command.Command;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.ShellService;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.util.regex.Pattern;

/**
 * Command and option name highlighter.
 *
 * <p>Created: 26/1/2019</p>
 *
 * @since 6.0.0
 */
public class CommandHighlighter implements Highlighter {

    private final Console console;

    private boolean limited;

    public CommandHighlighter(Console console) {
        if (console == null) {
            throw new IllegalArgumentException("console must not be null");
        }
        this.console = console;
    }

    public boolean isLimited() {
        return limited;
    }

    public void setLimited(boolean limited) {
        this.limited = limited;
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        if (!isLimited()) {
            String str = StringUtils.trimLeadingWhitespace(buffer);
            String best = getMatchedCommandName(str);
            if (best == null) {
                best = getMatchedTransletName(str);
            }
            if (best != null) {
                String prefix;
                if (str.length() < buffer.length()) {
                    prefix = buffer.substring(0, buffer.length() - str.length());
                } else {
                    prefix = "";
                }
                if (buffer.endsWith(Console.MULTILINE_DELIMITER)) {
                    AttributedStringBuilder asb = new AttributedStringBuilder(buffer.length());
                    asb.append(prefix + best, AttributedStyle.BOLD);
                    asb.append(buffer.substring(prefix.length() + best.length(),
                            buffer.length() - Console.MULTILINE_DELIMITER.length()));
                    asb.append(new AttributedString(buffer.substring(buffer.length() - Console.MULTILINE_DELIMITER.length()),
                            AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN | AttributedStyle.BRIGHT)));
                    return asb.toAttributedString();
                } else {
                    AttributedStringBuilder asb = new AttributedStringBuilder(buffer.length());
                    asb.append(prefix + best, AttributedStyle.BOLD);
                    asb.append(buffer.substring(prefix.length() + best.length()));
                    return asb.toAttributedString();
                }
            }
        }
        if (buffer.startsWith(Console.COMMENT_DELIMITER) && buffer.endsWith(Console.MULTILINE_DELIMITER)) {
            AttributedStringBuilder asb = new AttributedStringBuilder(buffer.length());
            asb.append(new AttributedString(buffer.substring(0, Console.COMMENT_DELIMITER.length()),
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT)));
            asb.append(new AttributedString(buffer.substring(Console.COMMENT_DELIMITER.length(),
                    buffer.length() - Console.MULTILINE_DELIMITER.length())));
            asb.append(new AttributedString(buffer.substring(buffer.length() - Console.MULTILINE_DELIMITER.length()),
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN | AttributedStyle.BRIGHT)));
            return asb.toAttributedString();
        } else if (buffer.startsWith(Console.COMMENT_DELIMITER)) {
            AttributedStringBuilder asb = new AttributedStringBuilder(buffer.length());
            asb.append(new AttributedString(buffer.substring(0, Console.COMMENT_DELIMITER.length()),
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE | AttributedStyle.BRIGHT)));
            asb.append(new AttributedString(buffer.substring(Console.COMMENT_DELIMITER.length())));
            return asb.toAttributedString();
        } else if (buffer.endsWith(Console.MULTILINE_DELIMITER)) {
            AttributedStringBuilder asb = new AttributedStringBuilder(buffer.length());
            asb.append(new AttributedString(buffer.substring(0, buffer.length() - Console.MULTILINE_DELIMITER.length())));
            asb.append(new AttributedString(buffer.substring(buffer.length() - Console.MULTILINE_DELIMITER.length()),
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN | AttributedStyle.BRIGHT)));
            return asb.toAttributedString();
        } else {
            if (isLimited()) {
                return new AttributedString(buffer);
            } else {
                return new AttributedString(buffer,
                        AttributedStyle.DEFAULT.foreground(AttributedStyle.RED | AttributedStyle.BRIGHT));
            }
        }
    }

    @Override
    public void setErrorPattern(Pattern pattern) {
    }

    @Override
    public void setErrorIndex(int i) {
    }

    private String getMatchedCommandName(String buffer) {
        String best = null;
        CommandRegistry commandRegistry = console.getInterpreter().getCommandRegistry();
        if (commandRegistry != null) {
            int len = 0;
            for (Command command : commandRegistry.getAllCommands()) {
                String name = command.getDescriptor().getName();
                if (name.length() > len) {
                    if (buffer.equals(name) || buffer.startsWith(name + " ") ||
                            buffer.startsWith(name + Console.MULTILINE_DELIMITER)) {
                        best = name;
                        len = name.length();
                    }
                }
            }
        }
        return best;
    }

    private String getMatchedTransletName(String buffer) {
        String best = null;
        ShellService shellService = console.getInterpreter().getShellService();
        if (shellService != null && shellService.getServiceController().isActive()) {
            TransletRuleRegistry transletRuleRegistry = shellService.getActivityContext().getTransletRuleRegistry();
            int len = 0;
            for (TransletRule transletRule : transletRuleRegistry.getTransletRules()) {
                String name = transletRule.getName();
                if (shellService.isExposable(name)) {
                    if (transletRule.getNamePattern() != null) {
                        if (transletRule.getNamePattern().matches(buffer)) {
                            return buffer;
                        }
                    } else if (name.length() > len) {
                        if (buffer.equals(name) || buffer.startsWith(name + " ") ||
                                buffer.startsWith(name + Console.MULTILINE_DELIMITER)) {
                            best = name;
                            len = name.length();
                        }
                    }
                }
            }
        }
        return best;
    }

}
