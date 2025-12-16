/*
 * Copyright (c) 2008-present The Aspectran Project
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
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandLineParser;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.TransletCommandLine;
import com.aspectran.shell.command.option.Arguments;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.service.ShellService;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.apon.AponWriter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Built-in command that executes or inspects translets available to the shell.
 * <p>
 * Supports listing and describing translets (optionally filtering by keywords or request method)
 * and executing a specified translet using command-line parameters.
 * </p>
 * <p>Command name: "translet" (namespace: "builtins").</p>
 */
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
                .desc("Lists all translets or filters them by keywords")
                .build());
        addOption(Option.builder("la")
                .longName("list-all")
                .hasValues()
                .optionalValue()
                .valueName("keywords")
                .desc("Lists all translets, including non-exposed ones, or filters them by keywords")
                .build());
        addOption(Option.builder("d")
                .longName("detail")
                .hasValues()
                .optionalValue()
                .valueName("translet_name")
                .desc("Displays detailed information for a specific translet")
                .build());
        addOption(Option.builder("da")
                .longName("detail-all")
                .hasValues()
                .optionalValue()
                .valueName("translet_name")
                .desc("Displays detailed information for a specific translet, including non-exposed ones")
                .build());
        addOption(Option.builder("m")
                .longName("method")
                .hasValue()
                .optionalValue()
                .valueName("request_method")
                .desc("Specifies the request method for the translet (e.g., GET, POST)")
                .build());
        addOption(Option.builder("v")
                .longName("verbose")
                .desc("Displays a description of the translet before execution")
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
    public void execute(@NonNull ParsedOptions options, ShellConsole console) throws Exception {
        ShellService shellService = getActiveShellService();
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
                console.writeError("Invalid request method: " + method);
                return;
            }
            describeTransletRule(shellService, console, transletNames, requestMethod, false);
        } else if (options.hasOption("detail-all")) {
            String[] transletNames = options.getValues("detail-all");
            String method = options.getValue("method");
            MethodType requestMethod = MethodType.resolve(method);
            if (method != null && requestMethod == null) {
                console.writeError("Invalid request method: " + method);
                return;
            }
            describeTransletRule(shellService, console, transletNames, requestMethod, true);
        } else if (options.hasArgs()) {
            CommandLineParser lineParser = new CommandLineParser(options.getFirstArg());
            TransletCommandLine transletCommandLine = new TransletCommandLine(lineParser);
            if (options.hasOption("verbose")) {
                transletCommandLine.setVerbose(true);
            }
            try {
                shellService.translate(transletCommandLine);
            } catch (TransletNotFoundException e) {
                console.writeError("Translet not found: " + e.getTransletName());
            }
        } else {
            printQuickHelp(console);
        }
    }

    private void listTranslets(
            @NonNull ShellService shellService, @NonNull ShellConsole console,
            @Nullable String[] keywords, boolean all) {
        TransletRuleRegistry transletRuleRegistry = shellService.getActivityContext().getTransletRuleRegistry();
        Collection<TransletRule> transletRules = transletRuleRegistry.getTransletRules();
        console.writeLine("-%4s-+-%-59s-+-%-5s-",
                "----", "-----------------------------------------------------------", "-----");
        console.writeLine(" %4s | %-59s | %-5s ", "No.", "Translet Name", "Async");
        console.writeLine("-%4s-+-%-59s-+-%-5s-",
                "----", "-----------------------------------------------------------", "-----");
        int num = 0;
        for (TransletRule transletRule : transletRules) {
            String transletName = transletRule.getName();
            if (!all && !shellService.isRequestAcceptable(transletName)) {
                continue;
            }
            if (keywords != null) {
                boolean exists = false;
                for (String kw : keywords) {
                    if (transletName.toLowerCase().contains(kw.toLowerCase())) {
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
                transletName = StringUtils.join(requestMethods, ",") + " " + transletName;
            }
            console.write("%5d | %-59s |", ++num, transletName);
            if (transletRule.isAsync()) {
                console.getStyler().successStyle();
            }
            console.writeLine(" %-5s ", transletRule.isAsync());
            if (transletRule.isAsync()) {
                console.getStyler().resetStyle();
            }
        }
        if (num == 0) {
            console.writeLine(" %4s   %s", " ", "No translets found to display.");
        }
        console.writeLine("-%4s-+-%-59s-+-%-5s-",
                "----", "-----------------------------------------------------------", "-----");
    }

    private void describeTransletRule(
            @NonNull ShellService shellService, @NonNull ShellConsole console, @Nullable String[] transletNames,
            @Nullable MethodType requestMethod, boolean all) throws IOException {
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
                    if (requestMethod != null) {
                        console.writeError("Translet not found: " + requestMethod + " " + transletName);
                    } else {
                        console.writeError("Translet not found: " + transletName);
                    }
                    return;
                }
                transletRules.add(transletRule);
            }
        }
        int count = 0;
        for (TransletRule transletRule : transletRules) {
            if (!all && !shellService.isRequestAcceptable(transletRule.getName())) {
                continue;
            }

            TransletParameters transletParameters = RulesToParameters.toTransletParameters(transletRule);

            if (count == 0) {
                console.writeLine("----------------------------------------------------------------------------");
            }
            AponWriter aponWriter = new AponWriter(console.getWriter()).nullWritable(false).autoFlush(true);
            aponWriter.write(transletParameters);
            aponWriter.flush();
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
        @NonNull
        public String getDescription() {
            return "Executes or inspects translets";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
