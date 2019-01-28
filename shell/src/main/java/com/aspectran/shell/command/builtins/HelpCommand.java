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
package com.aspectran.shell.command.builtins;

import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.Command;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.HelpFormatter;
import com.aspectran.shell.command.option.OptionUtils;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.Console;

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
    }

    @Override
    public void execute(ParsedOptions options, Console console) throws Exception {
        String[] targetCommands = null;
        if (options.hasArgs()) {
            targetCommands = options.getArgs();
        }
        if (targetCommands == null) {
            printHelp(null, console);
            if (isServiceAvailable()) {
                getService().printHelp();
            }
        } else if (targetCommands.length == 1) {
            Command command = getCommandRegistry().getCommand(targetCommands[0]);
            if (command != null) {
                console.writeLine(command.getDescriptor().getDescription());
                command.printUsage(console);
            } else {
                console.writeLine("No command mapped to '" + targetCommands[0] +  "'");
            }
        } else {
            console.setStyle("bold");
            console.writeLine("Built-in commands:");
            console.styleOff();
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
        arguments.setTitle("Commands:");
        for (Command command : list) {
            String commandName = command.getDescriptor().getName();
            arguments.put(commandName, "Display help for command " + commandName);
        }

        return argumentsList;
    }

    private void printHelp(String[] targetCommands, Console console) {
        if (targetCommands == null) {
            console.setStyle("bold");
            console.writeLine("Available commands:");
            console.styleOff();
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

    private boolean contains(String commandName, String[] filteredCommands) {
        for (String target : filteredCommands) {
            if (commandName.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private String renderCommand(Command command, int lineWidth, int commandWidth, String leftPad, String descPad) {
        String name = command.getDescriptor().getName();
        String desc = command.getDescriptor().getDescription();

        StringBuilder sb = new StringBuilder();
        sb.append(leftPad).append(name);
        if (name.length() < commandWidth) {
            sb.append(OptionUtils.createPadding(commandWidth - name.length()));
        }
        sb.append(descPad);

        int nextLineTabStop = commandWidth + descPad.length();
        renderWrappedText(sb, lineWidth, nextLineTabStop, desc);

        return (sb.length() > 0 ? sb.toString() : null);
    }

    private StringBuilder renderWrappedText(StringBuilder sb, int width, int nextLineTabStop, String text) {
        int pos = OptionUtils.findWrapPos(text, width, 0);
        if (pos == -1) {
            sb.append(OptionUtils.rtrim(text));
            return sb;
        }

        sb.append(OptionUtils.rtrim(text.substring(0, pos))).append(System.lineSeparator());

        if (nextLineTabStop >= width) {
            // stops infinite loop happening
            nextLineTabStop = 1;
        }

        // all following lines must be padded with nextLineTabStop space characters
        String padding = OptionUtils.createPadding(nextLineTabStop);

        while (true) {
            text = padding + text.substring(pos).trim();
            pos = OptionUtils.findWrapPos(text, width, 0);
            if (pos == -1) {
                sb.append(text);
                return sb;
            }
            if (text.length() > width && pos == nextLineTabStop - 1) {
                pos = width;
            }
            sb.append(OptionUtils.rtrim(text.substring(0, pos))).append(System.lineSeparator());
        }
    }

    @Override
    public Descriptor getDescriptor() {
        return descriptor;
    }

    private class CommandDescriptor implements Descriptor {

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public String getName() {
            return COMMAND_NAME;
        }

        @Override
        public String getDescription() {
            return "Display information about all built-in commands";
        }

        @Override
        public String getUsage() {
            return "help [<commands>]";
        }

    }

}
