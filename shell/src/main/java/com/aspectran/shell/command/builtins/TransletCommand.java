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
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.converter.RuleToParamsConverter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandLineParser;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.ShellService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class TransletCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "translet";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public TransletCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("l")
                .longName("list")
                .hasValues()
                .optionalValue()
                .valueName("keywords")
                .desc("Print list of all translets or those filtered by given keywords")
                .build());
        addOption(Option.builder("d")
                .longName("detail")
                .hasValues()
                .optionalValue()
                .valueName("translet_name")
                .desc("Print detailed information for the translet")
                .build());
        addOption(Option.builder("m")
                .longName("method")
                .hasValue()
                .optionalValue()
                .valueName("request_method")
                .desc("Specifies the request method for the translet\n(GET, PUT, POST, DELETE)")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());

        Arguments arguments = touchArguments();
        arguments.put("<translet_name>", "Name of the Translet to execute");
        arguments.setRequired(false);
    }

    @Override
    public void execute(ParsedOptions options, Console console) throws Exception {
        ShellService service = getService();
        if (!options.hasOptions() && options.hasArgs()) {
            CommandLineParser lineParser = new CommandLineParser(options.getFirstArg());
            TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
            try {
                service.translate(transletCommandLine, console);
            } catch (TransletNotFoundException e) {
                console.writeError("No translet mapped to '" + e.getTransletName() + "'");
            }
        } else if (options.hasOption("list")) {
            String[] keywords = options.getValues("list");
            listTranslets(service, console, keywords);
        } else if (options.hasOption("detail")) {
            String[] transletNames = options.getValues("detail");
            String method = options.getValue("method");
            MethodType requestMethod = MethodType.resolve(method);
            if (method != null && requestMethod == null) {
                console.writeError("No request method type for '" + method + "'");
                return;
            }
            detailTransletRule(service, console, transletNames, requestMethod);
        } else {
            printHelp(console);
        }
    }

    private void listTranslets(ShellService service, Console console, String[] keywords) {
        TransletRuleRegistry transletRuleRegistry = service.getActivityContext().getTransletRuleRegistry();
        Collection<TransletRule> transletRules = transletRuleRegistry.getTransletRules();
        console.writeLine("-%4s-+-%-67s-", "----", "-------------------------------------------------------------------");
        console.writeLine(" %4s | %-67s ", "No.", "Translet Name");
        console.writeLine("-%4s-+-%-67s-", "----", "-------------------------------------------------------------------");
        int num = 0;
        for (TransletRule transletRule : transletRules) {
            String transletName = transletRule.getName();
            if (keywords != null) {
                boolean exists = false;
                for (String keyw : keywords) {
                    if (transletName.toLowerCase().contains(keyw.toLowerCase())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    continue;
                }
            }
            MethodType[] requestMethods = transletRule.getAllowedMethods();
            if (requestMethods != null) {
                transletName = StringUtils.toDelimitedString(requestMethods, ",") + " " + transletName;
            }
            console.writeLine("%5d | %s", ++num, transletName);
        }
        if (num == 0) {
            console.writeLine("%33s %s", " ", "No Data");
        }
        console.writeLine("-%4s-+-%-67s-", "----", "-------------------------------------------------------------------");
    }

    private void detailTransletRule(ShellService service, Console console, String[] transletNames, MethodType requestMethod)
            throws IOException {
        TransletRuleRegistry transletRuleRegistry = service.getActivityContext().getTransletRuleRegistry();
        Collection<TransletRule> transletRules;
        if (transletNames == null || transletNames.length == 0) {
            transletRules = transletRuleRegistry.getTransletRules();
        } else {
            transletRules = new ArrayList<>();
            for (String transletName : transletNames) {
                TransletRule transletRule;
                if (requestMethod != null) {
                    transletRule = transletRuleRegistry.getTransletRule(transletName, requestMethod);
                } else {
                    transletRule = transletRuleRegistry.getTransletRule(transletName);
                }
                if (transletRule == null) {
                    try {
                        int num = Integer.parseInt(transletName) - 1;
                        transletRule = transletRuleRegistry.getTransletRules().toArray(new TransletRule[0])[num];
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (transletRule == null) {
                    console.writeError("Unknown translet: " + transletName);
                    return;
                }
                transletRules.add(transletRule);
            }
        }
        int count = 0;
        for (TransletRule transletRule : transletRules) {
            Parameters transletParameters = RuleToParamsConverter.toTransletParameters(transletRule);

            if (count == 0) {
                console.writeLine("----------------------------------------------------------------------------");
            }
            AponWriter aponWriter = new AponWriter(console.getWriter(), true);
            aponWriter.setIndentString("  ");
            aponWriter.write(transletParameters);
            console.writeLine("----------------------------------------------------------------------------");
            count++;
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
