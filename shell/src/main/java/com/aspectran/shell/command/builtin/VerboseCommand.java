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

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;

import java.util.Collection;

public class VerboseCommand extends AbstractCommand {

    private static final Log log = LogFactory.getLog(VerboseCommand.class);

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "verbose";

    private CommandDescriptor descriptor = new CommandDescriptor();

    public VerboseCommand(CommandRegistry registry) {
        super(registry);

        addOption(new Option("on", "Enable verbose output"));
        addOption(new Option("off", "Disable verbose output"));
        addOption(Option.builder("h").longOpt("help").desc("Display this help").build());
    }

    @Override
    public String execute(String[] args) throws Exception {
        ParsedOptions options = parse(args);
        if (options.hasOption("on")) {
            getService().setVerbose(true);
            getConsole().writeLine("Enabled verbose mode");
        } else if (options.hasOption("off")) {
            getService().setVerbose(false);
            getConsole().writeLine("Disabled verbose mode");
        } else {
            printUsage();
        }
        return null;
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
            return "verbose [-h|--help] [-on] [-off]";
        }

        @Override
        public Collection<Option> getOptions() {
            return options.getOptions();
        }

    }

}
