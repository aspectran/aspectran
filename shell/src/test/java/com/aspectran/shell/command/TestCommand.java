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
package com.aspectran.shell.command;

import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;

import java.util.Collection;

public class TestCommand extends AbstractCommand {

    private static final String NAMESPACE = "test";

    private static final String COMMAND_NAME = "test";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public TestCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("i").longOpt("input").hasArgs().valueSeparator().desc("The string to encrypt").build());
        addOption(Option.builder("p").longOpt("password").hasArgs().valueSeparator().desc("The password to be used for encryption").build());
        addOption(Option.builder("h").longOpt("help").desc("Display this help").build());
        addOption(Option.builder("D").hasArgs().desc("Dkey=value").build());
        addOption(Option.builder("X").desc("XYZ").build());
        addOption(Option.builder("Y").desc("XYZ").build());
        addOption(Option.builder("Z").desc("XYZ").build());
    }

    @Override
    public String execute(String[] args) throws Exception {
        ParsedOptions options = parse(args);
        String input = options.getTypedValue("input");
        String password = options.getTypedValue("password");
        String[] D = options.getValues("D");

        getConsole().writeLine("---------------------------------------------");
        getConsole().writeLine("   %1$-11s: %2$s", "input", input);
        getConsole().writeLine("   %1$-11s: %2$s", "password", password);
        getConsole().writeLine("   %1$-11s: %2$s", "D", StringUtils.joinCommaDelimitedList(D));
        getConsole().writeLine("   %1$-11s: %2$s", "X", options.hasOption("X"));
        getConsole().writeLine("   %1$-11s: %2$s", "Y", options.hasOption("Y"));
        getConsole().writeLine("   %1$-11s: %2$s", "Z", options.hasOption("X"));
        getConsole().writeLine("---------------------------------------------");
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
            return "The test command";
        }

        @Override
        public String getUsage() {
            return null;
        }

        @Override
        public Collection<Option> getOptions() {
            return options.getOptions();
        }

    }

}
