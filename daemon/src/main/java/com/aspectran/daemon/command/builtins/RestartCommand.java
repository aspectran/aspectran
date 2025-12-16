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
package com.aspectran.daemon.command.builtins;

import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.service.DaemonService;
import org.jspecify.annotations.NonNull;

/**
 * Built-in command that restarts the daemon service to reload resources.
 * <p>Command name: "restart" (namespace: "builtins").</p>
 */
public class RestartCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "restart";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public RestartCommand(CommandRegistry registry) {
        super(registry, true);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        DaemonService daemonService = getDaemonService();

        try {
            info("Restarting the daemon service...");
            daemonService.getServiceLifeCycle().restart();
            return success(info("The daemon service has been restarted successfully."));
        } catch (Exception e) {
            return failed(e);
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
            return "Restarts the daemon service to reload all resources";
        }

    }

}
