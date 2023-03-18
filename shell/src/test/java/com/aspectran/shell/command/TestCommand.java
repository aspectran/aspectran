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
package com.aspectran.shell.command;

import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;

public class TestCommand extends AbstractCommand {

    private static final String NAMESPACE = "test";

    private static final String COMMAND_NAME = "test";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public TestCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("i")
                .longName("input")
                .valueName("input_string")
                .withEqualSign()
                .required()
                .desc("The string to encrypt")
                .build());
        addOption(Option.builder("p")
                .longName("password")
                .valueName("password")
                .withEqualSign()
                .desc("The password to be used for encryption")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display this help")
                .build());
        addOption(Option.builder("D")
                .withEqualSign()
                .valueName("description")
                .desc("Dkey=value")
                .build());
        addOption(Option.builder("X").desc("XYZ").build());
        addOption(Option.builder("Y").desc("XYZ").build());
        addOption(Option.builder("Z").desc("XYZ").build());
    }

    @Override
    public void execute(ParsedOptions options, ShellConsole console) throws Exception {
        String input = options.getTypedValue("input");
        String password = options.getTypedValue("password");
        String D = options.getValue("D");

        console.writeLine("---------------------------------------------");
        console.writeLine("   %1$-11s: %2$s", "input", input);
        console.writeLine("   %1$-11s: %2$s", "password", password);
        console.writeLine("   %1$-11s: %2$s", "D", D);
        console.writeLine("   %1$-11s: %2$s", "X", options.hasOption("X"));
        console.writeLine("   %1$-11s: %2$s", "Y", options.hasOption("Y"));
        console.writeLine("   %1$-11s: %2$s", "Z", options.hasOption("X"));
        console.writeLine("---------------------------------------------");
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
            return "The test command";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
