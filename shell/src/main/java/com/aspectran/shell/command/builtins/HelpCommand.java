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
package com.aspectran.shell.command.builtins;

import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.Command;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.HelpFormatter;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.OptionUtils;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Display information about builtin commands.
 */
public class HelpCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "help";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public HelpCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());

        Arguments arguments = touchArguments();
        arguments.put("<commands>", "Target commands to display help");
        arguments.setRequired(false);
    }

    @Override
    public void execute(@NonNull ParsedOptions options, ShellConsole console) throws Exception {
        if (options.hasOption("help")) {
            printHelp(console);
            return;
        }
        String[] targetCommands = null;
        if (options.hasArgs()) {
            targetCommands = options.getArgs();
        }
        if (targetCommands == null) {
            printHelp(null, console);
            if (isServiceAvailable()) {
                getShellService().printHelp();
            }
        } else if (targetCommands.length == 1) {
            Command command = getCommandRegistry().getCommand(targetCommands[0]);
            if (command != null) {
                command.printHelp(console);
            } else {
                console.writeLine("No command mapped to '" + targetCommands[0] +  "'");
            }
        } else {
            console.getStyler().setStyle("bold");
            console.writeLine("Built-in commands:");
            console.getStyler().resetStyle();
            printHelp(targetCommands, console);
        }
    }

    @Override
    public List<Arguments> getArgumentsList() {
        List<Arguments> argumentsList = super.getArgumentsList();
        argumentsList.clear();

        List<Command> list = new LinkedList<>(getCommandRegistry().getAllCommands());
        list.sort(Comparator.comparing(Command::getDescriptor, Comparator.comparing(Descriptor::getName)));

        Arguments arguments = touchArguments();
        arguments.setTitle("Available commands:");
        for (Command command : list) {
            String commandName = command.getDescriptor().getName();
            arguments.put(commandName, "Display help for command " + commandName);
        }

        return argumentsList;
    }

    private void printHelp(String[] targetCommands, ShellConsole console) {
        if (targetCommands == null) {
            console.getStyler().setStyle("bold");
            console.writeLine("Available commands:");
            console.getStyler().resetStyle();
        }
        final int lineWidth = HelpFormatter.DEFAULT_WIDTH;
        final int commandWidth = maxLengthOfCommandName(targetCommands);
        final String lpad = OptionUtils.createPadding(HelpFormatter.DEFAULT_LEFT_PAD);
        final String dpad = OptionUtils.createPadding(HelpFormatter.DEFAULT_DESC_PAD);
        for (Command command : getCommandRegistry().getAllCommands()) {
            String name = command.getDescriptor().getName();
            if (commandWidth == 0 || targetCommands == null || contains(name, targetCommands)) {
                String line = renderCommand(command, lineWidth, commandWidth, lpad, dpad);
                if (line != null) {
                    console.writeLine(line);
                }
            }
        }
    }

    private int maxLengthOfCommandName(String[] filteredCommands) {
        int max = 0;
        for (Command command : getCommandRegistry().getAllCommands()) {
            String commandName = command.getDescriptor().getName();
            if (filteredCommands == null || contains(commandName, filteredCommands)) {
                if (commandName.length() > max) {
                    max = commandName.length();
                }
            }
        }
        return max;
    }

    private boolean contains(String commandName, @NonNull String[] filteredCommands) {
        for (String target : filteredCommands) {
            if (commandName.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private String renderCommand(@NonNull Command command, int lineWidth, int commandWidth,
                                 String leftPad, String descPad) {
        String name = command.getDescriptor().getName();
        String desc = command.getDescriptor().getDescription();

        StringBuilder sb = new StringBuilder();
        sb.append(leftPad).append(name);
        if (name.length() < commandWidth) {
            sb.append(OptionUtils.createPadding(commandWidth - name.length()));
        }
        sb.append(descPad);
        HelpFormatter.renderWrappedText(sb, lineWidth, sb.length(), desc);
        return (sb.length() > 0 ? sb.toString() : null);
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private static class CommandDescriptor implements Descriptor {

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String getName() {
            return COMMAND_NAME;
        }

        @Override
        @NonNull
        public String getDescription() {
            return "Display helpful information or command specific help";
        }

        @Override
        @Nullable
        public String getUsage() {
            return "help [-h] [<commands>]";
        }

    }

}
