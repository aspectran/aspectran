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
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.ParsedOptions;

/**
 * Turns verbose mode on or off.
 */
public class VerboseCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "verbose";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public VerboseCommand(CommandRegistry registry) {
        super(registry);

        Arguments arguments = touchArguments();
        arguments.setTitle("Commands:");
        arguments.put("on", "Enable verbose output");
        arguments.put("off", "Disable verbose output");
    }

    @Override
    public void execute(ParsedOptions options) throws Exception {
        String command = null;
        if (options.hasArgs()) {
            String[] optArgs = options.getArgs();
            if (optArgs.length > 0) {
                command = optArgs[0];
            }
            if ("on".equals(command)) {
                getService().setVerbose(true);
                writeLine("Enabled verbose mode");
            } else if ("off".equals(command)) {
                getService().setVerbose(false);
                writeLine("Disabled verbose mode");
            } else {
                writeError("Unknown command '" + String.join(" ", optArgs) + "'");
                printUsage();
            }
        } else {
            printUsage();
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
            return "Turns verbose mode on or off";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
