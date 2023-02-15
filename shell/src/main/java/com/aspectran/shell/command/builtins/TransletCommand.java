/*
 * Copyright (c) 2008-2022 The Aspectran Project
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

import com.aspectran.core.activity.TransletNotFoundException;
import com.aspectran.core.component.translet.TransletRuleRegistry;
import com.aspectran.core.context.rule.TransletRule;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.core.context.rule.params.TransletParameters;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandLineParser;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
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
        addOption(Option.builder("la")
                .longName("list-all")
                .hasValues()
                .optionalValue()
                .valueName("keywords")
                .desc("Print list of all translets or those filtered by given keywords (Include all translets that are not exposed)")
                .build());
        addOption(Option.builder("d")
                .longName("detail")
                .hasValues()
                .optionalValue()
                .valueName("translet_name")
                .desc("Print detailed information for the translet")
                .build());
        addOption(Option.builder("da")
                .longName("detail-all")
                .hasValues()
                .optionalValue()
                .valueName("translet_name")
                .desc("Print detailed information for the translet (Include all translets that are not exposed)")
                .build());
        addOption(Option.builder("m")
                .longName("method")
                .hasValue()
                .optionalValue()
                .valueName("request_method")
                .desc("Specifies the request method for the translet\n(GET, PUT, POST, DELETE)")
                .build());
        addOption(Option.builder("v")
                .longName("verbose")
                .desc("Display description about the translet")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());

        Arguments arguments = touchArguments();
        arguments.put("<translet_name>", "Name of the translet to execute");
        arguments.setRequired(false);
    }

    @Override
    public void execute(ParsedOptions options, ShellConsole console) throws Exception {
        ShellService shellService = getShellService();
        if (options.hasOption("help")) {
            printHelp(console);
        } else if (options.hasOption("list")) {
            String[] keywords = options.getValues("list");
            listTranslets(shellService, console, keywords, false);
        } else if (options.hasOption("list-all")) {
            String[] keywords = options.getValues("list-all");
            listTranslets(shellService, console, keywords, true);
        } else if (options.hasOption("detail")) {
            String[] transletNames = options.getValues("detail");
            String method = options.getValue("method");
            MethodType requestMethod = MethodType.resolve(method);
            if (method != null && requestMethod == null) {
                console.writeError("No request method type for '" + method + "'");
                return;
            }
            describeTransletRule(shellService, console, transletNames, requestMethod, false);
        } else if (options.hasOption("detail-all")) {
            String[] transletNames = options.getValues("detail-all");
            String method = options.getValue("method");
            MethodType requestMethod = MethodType.resolve(method);
            if (method != null && requestMethod == null) {
                console.writeError("No request method type for '" + method + "'");
                return;
            }
            describeTransletRule(shellService, console, transletNames, requestMethod, true);
        } else if (options.hasArgs()) {
            CommandLineParser lineParser = new CommandLineParser(options.getFirstArg());
            TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
            boolean verbose = shellService.isVerbose();
            try {
                if (options.hasOption("verbose")) {
                    shellService.setVerbose(true);
                }
                shellService.translate(transletCommandLine, console);
            } catch (TransletNotFoundException e) {
                console.writeError("No translet mapped to '" + e.getTransletName() + "'");
            } finally {
                shellService.setVerbose(verbose);
            }
        } else {
            printQuickHelp(console);
        }
    }

    private void listTranslets(ShellService shellService, ShellConsole console, String[] keywords, boolean all) {
        TransletRuleRegistry transletRuleRegistry = shellService.getActivityContext().getTransletRuleRegistry();
        Collection<TransletRule> transletRules = transletRuleRegistry.getTransletRules();
        console.writeLine("-%4s-+-%-67s-", "----", "-------------------------------------------------------------------");
        console.writeLine(" %4s | %-67s ", "No.", "Translet Name");
        console.writeLine("-%4s-+-%-67s-", "----", "-------------------------------------------------------------------");
        int num = 0;
        for (TransletRule transletRule : transletRules) {
            String transletName = transletRule.getName();
            if (!all && !shellService.isExposable(transletName)) {
                continue;
            }
            if (keywords != null) {
                boolean exists = false;
                for (String keyw : keywords) {
                    if (transletName.toLowerCase().contains(keyw.toLowerCase())) {
                        exists = true;
                        break;
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

    private void describeTransletRule(ShellService shellService, ShellConsole console, String[] transletNames,
                                      MethodType requestMethod, boolean all)
            throws IOException {
        TransletRuleRegistry transletRuleRegistry = shellService.getActivityContext().getTransletRuleRegistry();
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
                    MethodType requestMethod2 = null;
                    for (MethodType methodType : MethodType.values()) {
                        if (transletName.startsWith(methodType.name() + " ")) {
                            requestMethod2 = methodType;
                            transletName = transletName.substring(methodType.name().length()).trim();
                        }
                    }
                    if (requestMethod2 != null) {
                        transletRule = transletRuleRegistry.getTransletRule(transletName, requestMethod2);
                    } else {
                        transletRule = transletRuleRegistry.getTransletRule(transletName);
                    }
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
                    if (requestMethod != null) {
                        console.writeError("Unknown translet: " + requestMethod + " " + transletName);
                    } else {
                        console.writeError("Unknown translet: " + transletName);
                    }
                    return;
                }
                transletRules.add(transletRule);
            }
        }
        int count = 0;
        for (TransletRule transletRule : transletRules) {
            if (!all && !shellService.isExposable(transletRule.getName())) {
                continue;
            }

            TransletParameters transletParameters = RulesToParameters.toTransletParameters(transletRule);

            if (count == 0) {
                console.writeLine("----------------------------------------------------------------------------");
            }
            AponWriter aponWriter = new AponWriter(console.getWriter()).nullWritable(false);
            aponWriter.write(transletParameters);
            console.writeLine("----------------------------------------------------------------------------");
            count++;
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
            return "Translet run, or you can find them";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
