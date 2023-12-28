/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
 * <p>Created: 12/28/23</p>
 */
public class EchoCommand  extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "echo";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public EchoCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        try {
            Object[] args = parameters.getArguments();
            if (args != null && args.length > 0) {
                String msg = args[0].toString();
                return success(debug(msg));
            } else {
                return success(debug(""));
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
        public String getDescription() {
            return "It returns the input message verbatim";
        }

    }

}
