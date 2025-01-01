/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.daemon.command.builtins;

import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.utils.annotation.jsr305.NonNull;

public class QuitCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "quit";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public QuitCommand(CommandRegistry registry) {
        super(registry, true);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        if (!getCommandRegistry().getDaemon().isWaiting()) {
            return failed(getCommandRegistry().getDaemon().getName() + " does not support the quit command");
        }

        info("Shutting down the " + getCommandRegistry().getDaemon().getName());
        getCommandRegistry().getDaemon().stop();

        return success(info("Goodbye."));
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
            return "Releases all resources and exits this daemon";
        }

    }

}
