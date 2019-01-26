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

import com.aspectran.core.component.translet.TransletNotFoundException;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.util.StringUtils;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandLineParser;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.service.ShellService;

import java.util.Collection;

public class TransletCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "translet";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public TransletCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("l")
                .longName("list")
                .desc("Prints all translets or those filtered by the given name")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());
    }

    @Override
    public void execute(ParsedOptions options) throws Exception {
        ShellService service = getService();
        if (!options.hasOptions() && options.hasArgs()) {
            CommandLineParser lineParser = new CommandLineParser(options.getArgs());
            TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
            try {
                service.translate(transletCommandLine);
                writeLine();
            } catch (TransletNotFoundException e) {
                writeError("No translet mapped to '" + e.getTransletName() + "'");
            }
        } else if (options.hasOption("l")) {
            String[] keywords = options.getArgs();
            listTranslets(service, keywords.length > 0 ? keywords : null);
        } else {
            printUsage();
            if (!options.hasOption("h")) {
                writeLine("Available translets:");
                listTranslets(service, null);
            }
        }
    }

    private void listTranslets(ShellService service, String[] keywords) {
        TransletRuleRegistry transletRuleRegistry = service.getActivityContext().getTransletRuleRegistry();
        Collection<TransletRule> transletRules = transletRuleRegistry.getTransletRules();
        writeLine("-%-20s-+-%-63s-", "--------------------",
                "---------------------------------------------------------------");
        writeLine(" %-20s | %-63s ", "Translet Name", "Description");
        writeLine("-%-20s-+-%-63s-", "--------------------",
                "---------------------------------------------------------------");
        for (TransletRule transletRule : transletRules) {
            String name = transletRule.getName();
            String desc = StringUtils.trimWhitespace(transletRule.getDescription());
            if (service.isExposable(name)) {
                if (keywords != null) {
                    boolean exists = false;
                    for (String keyw : keywords) {
                        if (name.contains(keyw)) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        continue;
                    }
                }
                if (transletRule.getAllowedMethods() != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[");
                    for (int i = 0; i < transletRule.getAllowedMethods().length; i++) {
                        if (i > 0) {
                            sb.append(",");
                        }
                        sb.append(transletRule.getAllowedMethods()[i].toString());
                    }
                    sb.append("] ");
                    writeLine(" %-20s | %-63s ", sb, StringUtils.EMPTY);
                }
                if (desc != null && desc.contains(ActivityContext.LINE_SEPARATOR)) {
                    String[] arr = StringUtils.split(desc, ActivityContext.LINE_SEPARATOR);
                    for (int i = 0; i < arr.length; i++) {
                        writeLine(" %-20s | %-63s ", (i == 0 ? name : StringUtils.EMPTY), arr[i].trim());
                    }
                } else {
                    writeLine(" %-20s | %-63s ", name, StringUtils.nullToEmpty(desc));
                }
                writeLine("-%-20s-+-%-63s-", "--------------------",
                        "---------------------------------------------------------------");
            }
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
            return "Translet run, or you can find them";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
