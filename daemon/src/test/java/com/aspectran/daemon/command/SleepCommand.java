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
package com.aspectran.daemon.command;

/**
 * A test command that sleeps for a specified duration.
 *
 * <p>Created: 2026. 04. 25.</p>
 */
public class SleepCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "sleep";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public SleepCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        try {
            Object[] args = parameters.getArguments();
            long millis = 5000; // default 5s
            if (args != null && args.length > 0) {
                millis = Long.parseLong(args[0].toString());
            }
            Thread.sleep(millis);
            return success("Slept for " + millis + "ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return failed(e);
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
        public String getDescription() {
            return "Sleeps for a specified duration";
        }

    }

}
