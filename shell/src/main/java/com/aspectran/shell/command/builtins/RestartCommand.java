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
import com.aspectran.shell.command.ShellCommandExecutionException;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Built-in command that restarts the shell service to reload resources.
 * <p>Command name: "restart" (namespace: "builtins").</p>
 */
public class RestartCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "restart";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public RestartCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());
    }

    @Override
    public void execute(@NonNull ParsedOptions options, ShellConsole console) throws Exception {
        if (options.hasOption("help")) {
            printHelp(console);
        } else if (console.confirmRestart()) {
            if (!getShellService().getServiceLifeCycle().isActive()) {
                console.writeError("Oops! This shell is currently not functioning properly. Please fix the problem.");
                return;
            }
            console.writeLine("Restarting...");
            try {
                getShellService().getServiceLifeCycle().restart();
            } catch (Exception e) {
                throw new ShellCommandExecutionException("Shell restart failed! Please fix the problem.", e);
            }
        }
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
            return "Restarts the shell to reload all resources";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
