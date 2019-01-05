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

import com.aspectran.core.util.PBEncryptionUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;

import java.util.Collection;

/**
 * Decrypts the input string using the encryption password.
 */
public class PBDecryptCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "decrypt";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public PBDecryptCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("i").longOpt("input").hasArgs().valueSeparator().desc("The string to encrypt").build());
        addOption(Option.builder("p").longOpt("password").hasArgs().valueSeparator().desc("The password to be used for encryption").build());
        addOption(Option.builder("h").longOpt("help").desc("Display help for this command").build());
    }

    @Override
    public String execute(String[] args) throws Exception {
        ParsedOptions options = parse(args);
        String input = options.getTypedValue("input");
        String password = options.getTypedValue("password");
        if (!StringUtils.hasText(input) || !StringUtils.hasText(password)) {
            printUsage();
            return null;
        }

        String output;
        try {
            output = PBEncryptionUtils.decrypt(input, password);
        } catch (Exception e) {
            getConsole().writeLine("Failed to decrypt string \"" + input + "\" with password \"" + password + "\".");
            getConsole().writeLine("Please make sure that the input string is encrypted with the password you entered.");
            return null;
        }

        getConsole().writeLine("---------------------------------------------");
        getConsole().writeLine("   %1$-11s: %2$s", "input", input);
        getConsole().writeLine("   %1$-11s: %2$s", "password", password);
        getConsole().writeLine("   %1$-11s: %2$s", "algorithm", PBEncryptionUtils.getAlgorithm());
        getConsole().writeLine("   %1$-11s: %2$s", "output", output);
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
            return "Decrypts the input string using the encryption password";
        }

        @Override
        public String getUsage() {
            return "decrypt -i=<INPUT_STRING> -p=<PASSWORD>";
        }

        @Override
        public Collection<Option> getOptions() {
            return options.getOptions();
        }

    }

}
