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

import java.util.List;

/**
 * Encrypts the input string using the encryption password.
 */
public class PBEncryptCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "encrypt";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public PBEncryptCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("p")
                .longName("password")
                .valueName("password")
                .withEqualSign()
                .desc("The password to be used for encryption")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());
        skipParsingAtNonOption();
    }

    @Override
    public String execute(ParsedOptions options) throws Exception {
        if (options.hasOption("help")) {
            printUsage();
            return null;
        }

        String password = options.getValue("password");
        boolean implicitPassword = false;
        if (!StringUtils.hasText(password)) {
            password = PBEncryptionUtils.getPassword();
            implicitPassword = true;
        }

        if (!StringUtils.hasText(password)) {
            setStyle("RED");
            writeLine("A password is required to attempt password-based encryption or decryption.");
            offStyle();
            printUsage();
            return null;
        }

        List<String> inputValues = options.getArgList();
        if (inputValues.isEmpty()) {
            setStyle("RED");
            writeLine("Please enter a string to decrypt.");
            offStyle();
            printUsage();
            return null;
        }

        writeLine("--------------------------------------------------------------");
        if (!implicitPassword) {
            writeLine("   %1$-10s: %2$s", "algorithm", PBEncryptionUtils.getAlgorithm());
            writeLine("   %1$-10s: %2$s", "password", password);
        }
        for (String input : inputValues) {
            String output;
            try {
                output = PBEncryptionUtils.encrypt(input, password);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to encrypt input string \"" + input + "\"");
            }

            writeLine("   %1$-10s: %2$s", "input", input);
            writeLine("   %1$-10s: %2$s", "output", output);
            writeLine("--------------------------------------------------------------");
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
            return "Encrypts the input string using the encryption password";
        }

        @Override
        public String getUsage() {
            return "encrypt [-p=<password>] <input_string>";
        }

    }

}
