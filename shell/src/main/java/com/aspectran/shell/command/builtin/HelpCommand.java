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
package com.aspectran.shell.command.builtin;

import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.Command;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.HelpFormatter;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.OptionUtils;
import com.aspectran.shell.command.option.ParsedOptions;

import java.util.Collection;

public class HelpCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "help";

    private String newLine = System.getProperty("line.separator");

    private CommandDescriptor descriptor = new CommandDescriptor();

    public HelpCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public String execute(String[] args) throws Exception {
        ParsedOptions options = parse(args);
        String[] filteredCommands = null;
        if (options.hasArgs()) {
            filteredCommands = options.getArgs();
        }
        if (filteredCommands == null) {
            getService().printHelp();
        } else if (filteredCommands.length == 1) {
            Command command = getCommandRegistry().getCommand(filteredCommands[0]);
            if (command != null) {
                getConsole().writeLine(command.getDescriptor().getDescription());
                command.printUsage();
            } else {
                getConsole().writeLine("No command mapped to '"
                        + filteredCommands[0] +  "'");
            }
        } else {
            getConsole().setStyle("bold");
            getConsole().writeLine("Built-in commands used in this application:");
            getConsole().offStyle();
            printHelp(filteredCommands);
        }
        return null;
    }

    private void printHelp(String[] filteredCommands) {
        final int lineWidth = HelpFormatter.DEFAULT_WIDTH;
        final int commandWidth = maxLengthOfCommandName(filteredCommands);
        final String lpad = OptionUtils.createPadding(HelpFormatter.DEFAULT_LEFT_PAD);
        final String dpad = OptionUtils.createPadding(HelpFormatter.DEFAULT_DESC_PAD);
        for (Command command : getCommandRegistry().getAllCommands()) {
            String name = command.getDescriptor().getName();
            if (commandWidth == 0 || filteredCommands == null || contains(name, filteredCommands)) {
                String line = renderCommand(command, lineWidth, commandWidth, lpad, dpad);
                if (line != null) {
                    getConsole().writeLine(line);
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

        sb.append(OptionUtils.rtrim(text.substring(0, pos))).append(newLine);

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

            sb.append(OptionUtils.rtrim(text.substring(0, pos))).append(newLine);
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
            return "Display information about builtin commands";
        }

        @Override
        public String getUsage() {
            return "help [command [command2 [command3] ...]]";
        }

        @Override
        public Collection<Option> getOptions() {
            return options.getOptions();
        }

    }

}
