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
package com.aspectran.daemon.command.builtins;

import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.service.DaemonService;

public class PollingIntervalCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "pollingInterval";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public PollingIntervalCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        DaemonService service = getService();

        try {
            long oldPollingInterval = getCommandRegistry().getDaemon().getCommandFilePoller().getPollingInterval();
            long pollingInterval = 0L;

            ItemRuleList itemRuleList = parameters.getArgumentItemRuleList();
            if (!itemRuleList.isEmpty()) {
                ItemEvaluator evaluator = new ItemEvaluation(service.getDefaultActivity());
                pollingInterval = evaluator.evaluate(itemRuleList.get(0));
            }

            if (pollingInterval > 0L) {
                getCommandRegistry().getDaemon().getCommandFilePoller().setPollingInterval(pollingInterval);
                return success(info("The polling interval is changed from " + oldPollingInterval +
                        "ms to " + pollingInterval + " ms"));
            } else if (pollingInterval < 0L) {
                return failed(error("The polling interval can not be negative: " + pollingInterval));
            } else {
                return failed(warn("The polling interval is not changed"));
            }
        } catch (Exception e) {
            return failed(e);
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
            return "Specifies in seconds how often the daemon polls for new commands";
        }

    }

}
