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

import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.converter.RuleToParamsConverter;
import com.aspectran.core.util.apon.AponWriter;
import com.aspectran.core.util.apon.Parameters;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.ShellService;

import java.io.IOException;
import java.util.Collection;

public class AspectCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "aspect";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public AspectCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("l")
                .longName("list")
                .hasValues()
                .optionalValue()
                .desc("Print list of all aspects or those filtered by the given name")
                .build());
        addOption(Option.builder("d")
                .longName("detail")
                .hasValue()
                .valueName("aspect_id")
                .desc("Print detailed information for the aspect")
                .build());
        addOption(Option.builder("enable")
                .hasValue()
                .valueName("aspect_id")
                .desc("Enable an aspect with a given name")
                .build());
        addOption(Option.builder("disable")
                .hasValue()
                .valueName("aspect_id")
                .desc("Disable an aspect with a given name")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());
    }

    @Override
    public void execute(ParsedOptions options, Console console) throws Exception {
        ShellService service = getService();
        if (options.hasOption("list")) {
            String[] keywords = options.getValues("list");
            listAspects(service, console, keywords);
        } else if (options.hasOption("detail")) {
            String aspectId = options.getValue("detail");
            detailAspectRule(service, console, aspectId);
        } else if (options.hasOption("enable")) {
            String aspectId = options.getValue("enable");
            updateAspectActiveState(service, console, aspectId, false);
        } else if (options.hasOption("disable")) {
            String aspectId = options.getValue("disable");
            updateAspectActiveState(service, console, aspectId, true);
        } else {
            printHelp(console);
        }
    }

    private void updateAspectActiveState(ShellService service, Console console, String aspectId, boolean disabled) {
        AspectRule aspectRule = service.getActivityContext().getAspectRuleRegistry().getAspectRule(aspectId);
        if (aspectRule == null) {
            console.writeError("Aspect '" + aspectId + "' not found.");
            return;
        }
        if (aspectRule.isIsolated()) {
            console.writeError("Can not be disabled or enabled for isolated Aspect '" + aspectId + "'.");
            return;
        }
        if (disabled) {
            if (aspectRule.isDisabled()) {
                console.writeLine("Aspect '" + aspectId + "' is already inactive.");
            } else {
                aspectRule.setDisabled(true);
                console.writeLine("Aspect '" + aspectId + "' is now inactive.");
            }
        } else {
            if (!aspectRule.isDisabled()) {
                console.writeLine("Aspect '" + aspectId + "' is already active.");
            } else {
                aspectRule.setDisabled(false);
                console.writeLine("Aspect '" + aspectId + "' is now active.");
            }
        }
    }

    private void listAspects(ShellService service, Console console, String[] keywords) {
        AspectRuleRegistry aspectRuleRegistry = service.getActivityContext().getAspectRuleRegistry();
        Collection<AspectRule> aspectRules = aspectRuleRegistry.getAspectRules();
        console.writeLine("-%4s-+-%-45s-+-%-8s-+-%-8s-", "----", "---------------------------------------------",
                "--------", "--------");
        console.writeLine(" %4s | %-45s | %-8s | %-8s ", "No.", "Aspect ID", "Isolated", "Enabled");
        console.writeLine("-%4s-+-%-45s-+-%-8s-+-%-8s-", "----", "---------------------------------------------",
                "--------", "--------");
        int num = 0;
        for (AspectRule aspectRule : aspectRules) {
            if (keywords != null) {
                boolean exists = false;
                for (String keyw : keywords) {
                    if (aspectRule.getId().toLowerCase().contains(keyw.toLowerCase())) {
                        exists = true;
                    }
                }
                if (!exists) {
                    continue;
                }
            }
            console.write("%5d | %-45s ", ++num, aspectRule.getId(), aspectRule.isIsolated());
            console.write("|");
            if (aspectRule.isIsolated()) {
                console.setStyle("YELLOW");
            }
            console.write(" %-8s ", aspectRule.isIsolated());
            console.styleOff();
            console.write("|");
            if (!aspectRule.isIsolated()) {
                if (aspectRule.isDisabled()) {
                    console.setStyle("RED");
                } else {
                    console.setStyle("BLUE");
                }
            }
            console.writeLine(" %-8s ", !aspectRule.isDisabled());
            console.styleOff();
            console.writeLine("-%4s-+-%-45s-+-%-8s-+-%-8s-", "----", "---------------------------------------------",
                    "--------", "--------");
        }
        if (num == 0) {
            console.writeLine("%33s %s", " ", "No Data");
        }
    }

    private void detailAspectRule(ShellService service, Console console, String aspectId) throws IOException {
        AspectRuleRegistry aspectRuleRegistry = service.getActivityContext().getAspectRuleRegistry();
        AspectRule aspectRule = aspectRuleRegistry.getAspectRule(aspectId);
        if (aspectRule == null) {
            console.writeError("Unknown aspect: " + aspectId);
            return;
        }

        Parameters aspectParameters = RuleToParamsConverter.toAspectParameters(aspectRule);

        console.writeLine("----------------------------------------------------------------------------");
        AponWriter aponWriter = new AponWriter(console.getWriter(), true);
        aponWriter.setIndentString("  ");
        aponWriter.write(aspectParameters);
        console.writeLine("----------------------------------------------------------------------------");
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
            return "Show registered aspects, or disable or enable them";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
