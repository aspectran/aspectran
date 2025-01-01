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

public class PollingIntervalCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "pollingInterval";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public PollingIntervalCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        try {
            long oldPollingInterval = getCommandRegistry().getDaemon().getFileCommander().getPollingInterval();
            long newPollingInterval = 0L;

            Object[] args = parameters.getArguments();
            if (args != null && args.length > 0) {
                if (args[0] instanceof Long) {
                    newPollingInterval = (Long)args[0];
                } else {
                    newPollingInterval = Long.parseLong(args[0].toString());
                }
            }

            if (newPollingInterval == 0L) {
                return failed(warn("The polling interval does not change"));
            } else if (newPollingInterval < 0L) {
                return failed(error("The polling interval can not be negative: " + newPollingInterval));
            } else if (newPollingInterval < 1000L) {
                return failed(error("The polling interval must be greater than 1000 ms."));
            } else {
                getCommandRegistry().getDaemon().getFileCommander().setPollingInterval(newPollingInterval);
                return success(info("The polling interval is changed from " + oldPollingInterval +
                        "ms to " + newPollingInterval + " ms"));
            }
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
            return "Specifies in seconds how often the daemon polls for new commands";
        }

    }

}
