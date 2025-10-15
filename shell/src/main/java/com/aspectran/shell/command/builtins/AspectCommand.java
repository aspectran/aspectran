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

import com.aspectran.core.component.aspect.AspectRuleRegistry;
import com.aspectran.core.context.rule.AspectRule;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.ShellConsole;
import com.aspectran.shell.service.ShellService;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.AponWriter;
import com.aspectran.utils.apon.Parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Built-in command that lists, describes, and toggles Aspect definitions.
 * <p>
 * Supports listing aspects (optionally filtering by keywords), printing detailed rules,
 * and enabling or disabling selected aspects.
 * </p>
 * <p>Command name: "aspect" (namespace: "builtins").</p>
 */
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
                .valueName("keywords")
                .desc("Lists all aspects or filters them by keywords")
                .build());
        addOption(Option.builder("d")
                .longName("detail")
                .hasValues()
                .optionalValue()
                .valueName("aspect_id")
                .desc("Displays detailed information for a specific aspect")
                .build());
        addOption(Option.builder("enable")
                .hasValues()
                .valueName("aspect_id")
                .desc("Enables a disabled aspect")
                .build());
        addOption(Option.builder("disable")
                .hasValues()
                .valueName("aspect_id")
                .desc("Disables an enabled aspect")
                .build());
        addOption(Option.builder("h")
                .longName("help")
                .desc("Display help for this command")
                .build());
    }

    @Override
    public void execute(@NonNull ParsedOptions options, ShellConsole console) throws Exception {
        ShellService shellService = getActiveShellService();
        if (options.hasOption("help")) {
            printHelp(console);
        } else if (options.hasOption("list")) {
            String[] keywords = options.getValues("list");
            listAspects(shellService, console, keywords);
        } else if (options.hasOption("detail")) {
            String[] aspectIds = options.getValues("detail");
            describeAspectRule(shellService, console, aspectIds);
        } else if (options.hasOption("enable")) {
            String[] aspectIds = options.getValues("enable");
            changeAspectActiveState(shellService, console, aspectIds, false);
        } else if (options.hasOption("disable")) {
            String[] aspectIds = options.getValues("disable");
            changeAspectActiveState(shellService, console, aspectIds, true);
        } else {
            printQuickHelp(console);
        }
    }

    private void listAspects(@NonNull ShellService shellService, @NonNull ShellConsole console, String[] keywords) {
        AspectRuleRegistry aspectRuleRegistry = shellService.getActivityContext().getAspectRuleRegistry();
        Collection<AspectRule> aspectRules = aspectRuleRegistry.getAspectRules();
        console.writeLine("-%4s-+-%-46s-+-%-8s-+-%-7s-",
                "----", "----------------------------------------------", "--------", "-------");
        console.writeLine(" %4s | %-46s | %-8s | %-7s ", "No.", "Aspect ID", "Isolated", "Enabled");
        console.writeLine("-%4s-+-%-46s-+-%-8s-+-%-7s-",
                "----", "----------------------------------------------", "--------", "-------");
        int num = 0;
        for (AspectRule aspectRule : aspectRules) {
            if (keywords != null) {
                boolean exists = false;
                for (String keyw : keywords) {
                    if (aspectRule.getId().toLowerCase().contains(keyw.toLowerCase())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    continue;
                }
            }
            console.write("%5d | %-46s ", ++num, aspectRule.getId());
            console.write("|");
            if (aspectRule.isIsolated()) {
                console.getStyler().successStyle();
            }
            console.write(" %-8s ", aspectRule.isIsolated());
            if (aspectRule.isIsolated()) {
                console.getStyler().resetStyle();
            }
            console.write("|");
            if (!aspectRule.isDisabled()) {
                console.getStyler().successStyle();
            }
            console.writeLine(" %-7s ", !aspectRule.isDisabled());
            if (!aspectRule.isDisabled()) {
                console.getStyler().resetStyle();
            }
        }
        if (num == 0) {
            console.writeLine(" %4s   %s", " ", "No aspects found to display.");
        }
        console.writeLine("-%4s-+-%-46s-+-%-8s-+-%-7s-",
                "----", "----------------------------------------------", "--------", "-------");
    }

    private void describeAspectRule(
            @NonNull ShellService shellService, @NonNull ShellConsole console,
            String[] aspectIds) throws IOException {
        AspectRuleRegistry aspectRuleRegistry = shellService.getActivityContext().getAspectRuleRegistry();
        Collection<AspectRule> aspectRules;
        if (aspectIds == null || aspectIds.length == 0) {
            aspectRules = aspectRuleRegistry.getAspectRules();
        } else {
            aspectRules = new ArrayList<>();
            for (String aspectId : aspectIds) {
                AspectRule aspectRule = aspectRuleRegistry.getAspectRule(aspectId);
                if (aspectRule == null) {
                    try {
                        int num = Integer.parseInt(aspectId) - 1;
                        aspectRule = aspectRuleRegistry.getAspectRules().toArray(new AspectRule[0])[num];
                    } catch (Exception e) {
                        // ignore
                    }
                }
                if (aspectRule == null) {
                    console.writeError("Aspect '" + aspectId + "' not found.");
                    return;
                }
                aspectRules.add(aspectRule);
            }
        }
        int count = 0;
        for (AspectRule aspectRule : aspectRules) {
            Parameters aspectParameters = RulesToParameters.toAspectParameters(aspectRule);
            if (count == 0) {
                console.writeLine("----------------------------------------------------------------------------");
            }
            AponWriter aponWriter = new AponWriter(console.getWriter()).nullWritable(false).autoFlush(true);
            aponWriter.write(aspectParameters);
            aponWriter.flush();
            console.writeLine("----------------------------------------------------------------------------");
            count++;
        }
    }

    private void changeAspectActiveState(
            @NonNull ShellService shellService, @NonNull ShellConsole console,
            @NonNull String[] aspectIds, boolean disabled) {
        AspectRuleRegistry aspectRuleRegistry = shellService.getActivityContext().getAspectRuleRegistry();
        List<AspectRule> aspectRules = new ArrayList<>();
        for (String aspectId : aspectIds) {
            AspectRule aspectRule = aspectRuleRegistry.getAspectRule(aspectId);
            if (aspectRule == null) {
                console.writeError("Aspect '" + aspectId + "' not found.");
                return;
            }
            if (aspectRule.isIsolated()) {
                console.writeError("The aspect '" + aspectId + "' is isolated and cannot be enabled or disabled.");
                return;
            }
            aspectRules.add(aspectRule);
        }
        for (AspectRule aspectRule : aspectRules) {
            if (disabled) {
                if (aspectRule.isDisabled()) {
                    console.writeLine("The aspect '" + aspectRule.getId() + "' is already disabled.");
                } else {
                    aspectRule.setDisabled(true);
                    console.writeLine("The aspect '" + aspectRule.getId() + "' has been disabled.");
                }
            } else {
                if (!aspectRule.isDisabled()) {
                    console.writeLine("The aspect '" + aspectRule.getId() + "' is already enabled.");
                } else {
                    aspectRule.setDisabled(false);
                    console.writeLine("The aspect '" + aspectRule.getId() + "' has been enabled.");
                }
            }
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
            return "Shows registered aspects, disables or enables them";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
