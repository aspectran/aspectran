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
import com.aspectran.shell.command.Command;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.HelpFormatter;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.service.ShellService;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

import static com.aspectran.shell.console.ShellConsole.MULTILINE_DELIMITER;

/**
 * Command and option name autocompleter.
 *
 * <p>Created: 17/11/2018</p>
 *
 * @since 5.8.0
 */
public class CommandCompleter implements Completer {

    private final ShellConsole console;

    private boolean limited;

    public CommandCompleter(ShellConsole console) {
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
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        if (!isLimited() && console.getInterpreter() != null) {
            if (line.wordIndex() == 0) {
                makeCommandCandidates(line.word(), candidates);
                makeTransletCandidates(line.word(), candidates);
            } else if (line.wordIndex() > 0) {
                String word = line.words().get(0);
                makeArgumentsCandidates(word, line.word(), candidates);
            }
        }
    }

    private void makeCommandCandidates(String word, List<Candidate> candidates) {
        CommandRegistry commandRegistry = console.getInterpreter().getCommandRegistry();
        if (commandRegistry != null) {
            for (Command command : commandRegistry.getAllCommands()) {
                String name = command.getDescriptor().getName();
                if (word == null || name.startsWith(word) ||
                        (name + MULTILINE_DELIMITER).startsWith(word)) {
                    candidates.add(new Candidate(name, name, command.getDescriptor().getNamespace(),
                            null, null, null, true));
                }
            }
        }
    }

    private void makeArgumentsCandidates(String word, String opt, List<Candidate> candidates) {
        CommandRegistry commandRegistry = console.getInterpreter().getCommandRegistry();
        if (commandRegistry != null) {
            Command command = commandRegistry.getCommand(word);
            if (command != null) {
                for (Option option : command.getOptions().getAllOptions()) {
                    String shortName = null;
                    if (option.getName() != null) {
                        shortName = HelpFormatter.OPTION_PREFIX + option.getName();
                    }
                    String longName = null;
                    if (option.getLongName() != null) {
                        longName = HelpFormatter.LONG_OPTION_PREFIX + option.getLongName();
                    }
                    String dispName;
                    if (shortName != null && longName != null) {
                        dispName = shortName + "," + longName;
                    } else if (shortName != null) {
                        dispName = shortName;
                    } else {
                        dispName = longName;
                    }
                    if (shortName != null && (opt == null || shortName.indexOf(opt) == 0)) {
                        candidates.add(new Candidate(shortName, dispName,
                                command.getOptions().getTitle(), null, null, longName, false));
                    } else if (longName != null && (opt == null || longName.indexOf(opt) == 0)) {
                        candidates.add(new Candidate(longName, dispName,
                                command.getOptions().getTitle(), null, null, null, false));
                    }
                }
                for (Arguments arguments : command.getArgumentsList()) {
                    if (arguments.getTitle() != null) {
                        for (String name : arguments.keySet()) {
                            if (!name.startsWith("<") || !name.endsWith(">")) {
                                candidates.add(new Candidate(name, name,
                                        arguments.getTitle(), null, null, null, false));
                            }
                        }
                    }
                }
            }
        }
    }

    private void makeTransletCandidates(String word, List<Candidate> candidates) {
        ShellService shellService = console.getInterpreter().getShellService();
        if (shellService != null && shellService.getServiceController().isActive()) {
            TransletRuleRegistry transletRuleRegistry = shellService.getActivityContext().getTransletRuleRegistry();
            for (TransletRule transletRule : transletRuleRegistry.getTransletRules()) {
                String name = transletRule.getName();
                String dispName = name;
                if (shellService.isExposable(name)) {
                    if (word == null || name.startsWith(word)) {
                        if (transletRule.hasPathVariables()) {
                            name = transletRule.getNamePattern().toString();
                            name = name.replaceAll(" [*+?] | [*+?]$|[*+?]", " ").trim();
                        }
                        if (!name.isEmpty()) {
                            candidates.add(new Candidate(name, dispName, "translets",
                                    null, null, null, true));
                        }
                    }
                }
            }
        }
    }

}
