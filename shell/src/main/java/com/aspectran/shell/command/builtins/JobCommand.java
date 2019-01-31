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

import com.aspectran.core.context.rule.ScheduleRule;
import com.aspectran.core.context.rule.ScheduledJobRule;
import com.aspectran.shell.command.AbstractCommand;
import com.aspectran.shell.command.CommandRegistry;
import com.aspectran.shell.command.option.Option;
import com.aspectran.shell.command.option.ParsedOptions;
import com.aspectran.shell.console.Console;
import com.aspectran.shell.service.ShellService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
                .valueName("jobNames")
                .desc("Print list of all scheduled jobs or those filtered by the given name")
                .build());
        addOption(Option.builder("enable")
                .hasValues()
                .valueName("jobNames")
                .desc("Enable a scheduled job with a given name")
                .build());
        addOption(Option.builder("disable")
                .hasValues()
                .valueName("jobNames")
                .desc("Disable a scheduled job with a given name")
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
            listScheduledJobs(service, console, keywords);
        } else if (options.hasOption("enable")) {
            String[] jobNames = options.getValues("enable");
            updateJobActiveState(service, console, jobNames, false);
        } else if (options.hasOption("disable")) {
            String[] jobNames = options.getValues("disable");
            updateJobActiveState(service, console, jobNames, true);
        } else if (options.hasOption("help")) {
            printUsage(console);
        } else {
            printUsage(console);
            if (!options.hasOption("help")) {
                console.writeLine("Registered scheduled jobs:");
                listScheduledJobs(service, console, null);
            }
        }
    }

    private void updateJobActiveState(ShellService service, Console console, String[] jobNames, boolean disabled) {
        Collection<ScheduleRule> scheduleRules = service.getActivityContext().getScheduleRuleRegistry().getScheduleRules();
        List<ScheduledJobRule> scheduledJobRules = new ArrayList<>();
        for (ScheduleRule scheduleRule : scheduleRules) {
            for (ScheduledJobRule jobRule : scheduleRule.getScheduledJobRuleList()) {
                for (String jobName : jobNames) {
                    if (jobRule.getTransletName().equals(jobName)) {
                        scheduledJobRules.add(jobRule);
                    }
                }
            }
        }
        if (scheduledJobRules.isEmpty()) {
            console.writeError("No scheduled jobs with the name: '" + Arrays.toString(jobNames));
            return;
        }
        for (ScheduledJobRule jobRule : scheduledJobRules) {
            if (disabled) {
                if (jobRule.isDisabled()) {
                    console.writeLine("Scheduled job '" + jobRule.getTransletName() + "' is already inactive.");
                } else {
                    jobRule.setDisabled(true);
                    console.writeLine("Scheduled job '" + jobRule.getTransletName() + "' is now inactive.");
                }
            } else {
                if (!jobRule.isDisabled()) {
                    console.writeLine("Scheduled job '" + jobRule.getTransletName() + "' is already active.");
                } else {
                    jobRule.setDisabled(false);
                    console.writeLine("Scheduled job '" + jobRule.getTransletName() + "' is now active.");
                }
            }
        }
    }

    private void listScheduledJobs(ShellService service, Console console, String[] keywords) {
        Collection<ScheduleRule> scheduleRules = service.getActivityContext().getScheduleRuleRegistry().getScheduleRules();
        console.writeLine("-%4s-+-%-20s-+-%-33s-+-%-8s-", "----", "--------------------",
                "---------------------------------", "--------");
        console.writeLine(" %4s | %-20s | %-33s | %-8s ", "No.", "Schedule ID", "Job Name", "Enabled");
        console.writeLine("-%4s-+-%-20s-+-%-33s-+-%-8s-", "----", "--------------------",
                "---------------------------------", "--------");
        int num = 0;
        for (ScheduleRule scheduleRule : scheduleRules) {
            for (ScheduledJobRule jobRule : scheduleRule.getScheduledJobRuleList()) {
                if (keywords != null) {
                    boolean exists = false;
                    for (String keyw : keywords) {
                        if (jobRule.getTransletName().toLowerCase().contains(keyw.toLowerCase())) {
                            exists = true;
                        }
                    }
                    if (!exists) {
                        continue;
                    }
                }
                console.write("%5d | %-20s | %-33s |", ++num, scheduleRule.getId(), jobRule.getTransletName());
                if (jobRule.isDisabled()) {
                    console.setStyle("RED");
                } else {
                    console.setStyle("BLUE");
                }
                console.writeLine(" %-8s ", !jobRule.isDisabled());
                console.styleOff();
                console.writeLine("-%4s-+-%-20s-+-%-33s-+-%-8s-", "----", "--------------------",
                        "---------------------------------", "--------");
            }
        }
        if (num == 0) {
            console.writeLine("%33s %s", " ", "No Data");
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
            return "Show scheduled jobs, or disable or enable them";
        }

        @Override
        public String getUsage() {
            return null;
        }

    }

}
