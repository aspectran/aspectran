/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.shell.command.Command;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.ShellService;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Command and option name highlighter.
 *
 * <p>Created: 26/1/2019</p>
 *
 * @since 6.0.0
 */
public class CommandHighlighter implements Highlighter {

    private final Console console;

    public CommandHighlighter(Console console) {
        this.console = console;
    }

    @Override
    public AttributedString highlight(LineReader reader, String buffer) {
        String best = getMatchedCommandName(buffer);
        if (best == null) {
            best = getMatchedTransletName(buffer);
        }
        if (best != null) {
            return new AttributedStringBuilder(buffer.length())
                    .append(best, AttributedStyle.BOLD)
                    .append(buffer.substring(best.length()))
                    .toAttributedString();
        } else {
            return new AttributedString(buffer,
                    AttributedStyle.DEFAULT.foreground(AttributedStyle.RED | AttributedStyle.BRIGHT));
        }
    }

    private String getMatchedCommandName(String buffer) {
        String best = null;
        CommandRegistry commandRegistry = console.getInterpreter().getCommandRegistry();
        if (commandRegistry != null) {
            int len = 0;
            for (Command command : commandRegistry.getAllCommands()) {
                String commandName = command.getDescriptor().getName();
                if (commandName.length() > len) {
                    if (buffer.equals(commandName) || buffer.startsWith(commandName + " ")) {
                        best = commandName;
                        len = commandName.length();
                    }
                }
            }
        }
        return best;
    }

    private String getMatchedTransletName(String buffer) {
        String best = null;
        ShellService service = console.getInterpreter().getService();
        if (service != null && service.getServiceController().isActive()) {
            TransletRuleRegistry transletRuleRegistry = service.getActivityContext().getTransletRuleRegistry();
            int len = 0;
            for (TransletRule transletRule : transletRuleRegistry.getTransletRules()) {
                String transletName = transletRule.getName();
                if (service.isExposable(transletName)) {
                    if (transletRule.getNamePattern() != null) {
                        if (transletRule.getNamePattern().matches(buffer)) {
                            return buffer;
                        }
                    } else if (transletName.length() > len) {
                        if (buffer.equals(transletName) || buffer.startsWith(transletName + " ")) {
                            best = transletName;
                            len = transletName.length();
                        }
                    }
                }
            }
        }
        return best;
    }

}
