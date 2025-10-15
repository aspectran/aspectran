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
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Built-in command that prints the command history or clears it.
 * <p>Command name: "history" (namespace: "builtins").</p>
 */
public class HistoryCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "history";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public HistoryCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("c")
                .longName("clear")
                .desc("Clears the entire command history")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());
    }

    @Override
    public void execute(@NonNull ParsedOptions options, ShellConsole console) throws Exception {
        if (options.hasOption("help")) {
            printHelp(console);
        } else if (options.hasOption("clear")) {
            console.clearCommandHistory();
        } else {
            listHistory(console);
        }
    }

    private void listHistory(@NonNull ShellConsole console) {
        console.writeLine("-%4s-+-%-67s-", "----", "-------------------------------------------------------------------");
        console.writeLine(" %4s | %-67s ", "No.", "Command");
        console.writeLine("-%4s-+-%-67s-", "----", "-------------------------------------------------------------------");
        int num = 0;
        for (String line : console.getCommandHistory()) {
            console.writeLine("%5d | %s", ++num, line);
        }
        if (num == 0) {
            console.writeLine("%31s %s", " ", "- No Data -");
        }
        console.writeLine("-%4s-+-%-67s-", "----", "-------------------------------------------------------------------");
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
            return "Manages the command history";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
