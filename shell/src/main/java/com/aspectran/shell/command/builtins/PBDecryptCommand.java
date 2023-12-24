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
package com.aspectran.shell.command.builtins;

import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.utils.PBEncryptionUtils;
import com.aspectran.utils.StringUtils;

import java.util.List;

/**
 * Decrypts the input string using the encryption password.
 */
public class PBDecryptCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "decrypt";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public PBDecryptCommand(CommandRegistry registry) {
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

        Arguments arguments = touchArguments();
        arguments.put("<text>", "The string to decrypt");
        arguments.setRequired(true);
    }

    @Override
    public void execute(ParsedOptions options, ShellConsole console) throws Exception {
        if (!options.hasOptions() && !options.hasArgs()) {
            printQuickHelp(console);
            return;
        }
        if (options.hasOption("help")) {
            printHelp(console);
            return;
        }

        String password = options.getValue("password");
        boolean implicitPassword = false;
        if (!StringUtils.hasText(password)) {
            password = PBEncryptionUtils.getPassword();
            implicitPassword = true;
        }

        if (!StringUtils.hasText(password)) {
            console.writeError("A password is required to attempt password-based encryption or decryption.");
            printQuickHelp(console);
            return;
        }

        List<String> inputValues = options.getArgList();
        if (inputValues.isEmpty()) {
            console.writeError("Please enter a string to encrypt.");
            printQuickHelp(console);
            return;
        }

        if (!implicitPassword) {
            console.writeLine("----------------------------------------------------------------------------");
            console.writeLine(" %1$9s : %2$s", "Algorithm", PBEncryptionUtils.getAlgorithm());
            console.writeLine(" %1$9s : %2$s", "Password", password);
        } else {
            console.writeLine("----------------------------------------------------------------------------");
        }
        for (String input : inputValues) {
            String output;
            try {
                output = PBEncryptionUtils.decrypt(input, password);
            } catch (Exception e) {
                console.writeError("Failed to decrypt string \"" + input + "\" with password \"" + password + "\".");
                console.writeError("Please make sure that the input string is encrypted with the password you entered.");
                console.writeLine("----------------------------------------------------------------------------");
                return;
            }
            console.writeLine(" %1$9s : %2$s", "Input", input);
            console.writeLine(" %1$9s : %2$s", "Result", output);
            console.writeLine("----------------------------------------------------------------------------");
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
            return "Decrypts the input string using the encryption password";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
