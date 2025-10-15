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

import com.aspectran.core.component.schedule.ScheduleRuleRegistry;
import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.core.context.rule.converter.RulesToParameters;
import com.aspectran.core.context.rule.params.ScheduleParameters;
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
import java.util.Arrays;
import java.util.Set;

/**
 * Built-in command that lists, describes, and toggles scheduled jobs.
 * <p>
 * Provides listing with keyword filtering, detailed rule output, and enable/disable controls
 * for jobs defined under schedules.
 * </p>
 * <p>Command name: "job" (namespace: "builtins").</p>
 */
public class JobCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "job";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public JobCommand(CommandRegistry registry) {
        super(registry);

        addOption(Option.builder("l")
                .longName("list")
                .hasValues()
                .optionalValue()
                .valueName("keywords")
                .desc("Lists all scheduled jobs or filters them by keywords")
                .build());
        addOption(Option.builder("d")
                .longName("detail")
                .hasValues()
                .optionalValue()
                .valueName("translet_name")
                .desc("Displays detailed information for a specific job")
                .build());
        addOption(Option.builder("enable")
                .hasValues()
                .valueName("translet_name")
                .desc("Enables a disabled job")
                .build());
        addOption(Option.builder("disable")
                .hasValues()
                .valueName("translet_name")
                .desc("Disables an enabled job")
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
            listScheduledJobs(shellService, console, keywords);
        } else if (options.hasOption("detail")) {
            String[] transletNames = options.getValues("detail");
            describeScheduledJobRule(shellService, console, transletNames);
        } else if (options.hasOption("enable")) {
            String[] transletNames = options.getValues("enable");
            changeJobActiveState(shellService, console, transletNames, false);
        } else if (options.hasOption("disable")) {
            String[] transletNames = options.getValues("disable");
            changeJobActiveState(shellService, console, transletNames, true);
        } else {
            printQuickHelp(console);
        }
    }

    private void listScheduledJobs(
            @NonNull ShellService shellService, @NonNull ShellConsole console, String[] keywords) {
        ScheduleRuleRegistry scheduleRuleRegistry = shellService.getActivityContext().getScheduleRuleRegistry();
        console.writeLine("-%4s-+-%-20s-+-%-34s-+-%-7s-",
                "----", "--------------------", "----------------------------------", "-------");
        console.writeLine(" %4s | %-20s | %-34s | %-7s ", "No.", "Schedule ID", "Job Name", "Enabled");
        console.writeLine("-%4s-+-%-20s-+-%-34s-+-%-7s-",
                "----", "--------------------", "----------------------------------", "-------");
        int num = 0;
        for (ScheduleRule scheduleRule : scheduleRuleRegistry.getScheduleRules()) {
            for (ScheduledJobRule jobRule : scheduleRule.getScheduledJobRuleList()) {
                if (keywords != null) {
                    boolean exists = false;
                    for (String keyw : keywords) {
                        if (jobRule.getTransletName().toLowerCase().contains(keyw.toLowerCase())) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        continue;
                    }
                }
                console.write("%5d | %-20s | %-34s |", ++num, scheduleRule.getId(), jobRule.getTransletName());
                if (!jobRule.isDisabled()) {
                    console.getStyler().successStyle();
                }
                console.writeLine(" %-7s ", !jobRule.isDisabled());
                if (!jobRule.isDisabled()) {
                    console.getStyler().resetStyle();
                }
            }
        }
        if (num == 0) {
            console.writeLine(" %4s   %s", " ", "No scheduled jobs found to display.");
        }
        console.writeLine("-%4s-+-%-20s-+-%-34s-+-%-7s-",
                "----", "--------------------", "----------------------------------", "-------");
    }

    private void describeScheduledJobRule(
            @NonNull ShellService shellService, @NonNull ShellConsole console,
            @Nullable String[] transletNames) throws IOException {
        ScheduleRuleRegistry scheduleRuleRegistry = shellService.getActivityContext().getScheduleRuleRegistry();
        if (transletNames != null && transletNames.length > 0) {
            Set<ScheduledJobRule> scheduledJobRules = scheduleRuleRegistry.getScheduledJobRules(transletNames);
            if (scheduledJobRules.isEmpty()) {
                console.writeError("Job not found: " + Arrays.toString(transletNames));
                return;
            }
            int count = 0;
            for (ScheduledJobRule jobRule : scheduledJobRules) {
                ScheduleParameters scheduleParameters = RulesToParameters.toScheduleParameters(jobRule.getScheduleRule(), jobRule);
                if (count == 0) {
                    console.writeLine("----------------------------------------------------------------------------");
                }
                AponWriter aponWriter = new AponWriter(console.getWriter()).nullWritable(false).autoFlush(true);
                aponWriter.write(scheduleParameters);
                aponWriter.flush();
                console.writeLine("----------------------------------------------------------------------------");
                count++;
            }
            if (count == 0) {
                console.writeError("Job not found: " + Arrays.toString(transletNames));
            }
        } else {
            int count = 0;
            for (ScheduleRule scheduleRule : scheduleRuleRegistry.getScheduleRules()) {
                Parameters scheduleParameters = RulesToParameters.toScheduleParameters(scheduleRule);

                if (count == 0) {
                    console.writeLine("----------------------------------------------------------------------------");
                }
                AponWriter aponWriter = new AponWriter(console.getWriter()).nullWritable(false).autoFlush(true);
                aponWriter.write(scheduleParameters);
                aponWriter.flush();
                console.writeLine("----------------------------------------------------------------------------");
                count++;
            }
        }
    }

    private void changeJobActiveState(
            @NonNull ShellService shellService, @NonNull ShellConsole console,
            @Nullable String[] transletNames, boolean disabled) {
        ScheduleRuleRegistry scheduleRuleRegistry = shellService.getActivityContext().getScheduleRuleRegistry();
        Set<ScheduledJobRule> scheduledJobRules = scheduleRuleRegistry.getScheduledJobRules(transletNames);
        if (scheduledJobRules.isEmpty()) {
            console.writeError("Job not found: " + Arrays.toString(transletNames));
            return;
        }
        for (ScheduledJobRule jobRule : scheduledJobRules) {
            if (disabled) {
                if (jobRule.isDisabled()) {
                    console.writeLine("The job '%s' on schedule '%s' is already disabled.",
                            jobRule.getTransletName(), jobRule.getScheduleRule().getId());
                } else {
                    jobRule.setDisabled(true);
                    console.writeLine("The job '%s' on schedule '%s' has been disabled.",
                            jobRule.getTransletName(), jobRule.getScheduleRule().getId());
                }
            } else {
                if (!jobRule.isDisabled()) {
                    console.writeLine("The job '%s' on schedule '%s' is already enabled.",
                            jobRule.getTransletName(), jobRule.getScheduleRule().getId());
                } else {
                    jobRule.setDisabled(false);
                    console.writeLine("The job '%s' on schedule '%s' has been enabled.",
                            jobRule.getTransletName(), jobRule.getScheduleRule().getId());
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
            return "Lists, describes, and toggles scheduled jobs";
        }

        @Override
        @Nullable
        public String getUsage() {
            return null;
        }

    }

}
