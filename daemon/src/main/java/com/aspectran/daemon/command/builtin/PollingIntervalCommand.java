/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
package com.aspectran.daemon.command.builtin;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpressionParser;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.polling.CommandParameters;

public class PollingIntervalCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtin";

    private static final String COMMAND_NAME = "pollingInterval";

    private CommandDescriptor descriptor = new CommandDescriptor();

    public PollingIntervalCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public String execute(CommandParameters parameters) throws Exception {
        long oldPollingInterval = getCommandRegistry().getDaemon().getCommandPoller().getPollingInterval();
        long pollingInterval = 0L;
        ItemRuleList itemRuleList = parameters.getArgumentItemRuleList();
        if (!itemRuleList.isEmpty()) {
            Activity activity = new InstantActivity(getService().getActivityContext());
            ItemEvaluator evaluator = new ItemExpressionParser(activity);
            pollingInterval = evaluator.evaluate(itemRuleList.get(0));
        }
        if (pollingInterval != 0L) {
            getCommandRegistry().getDaemon().getCommandPoller().setPollingInterval(pollingInterval);
            return "The polling interval is changed from " + oldPollingInterval + " to " + pollingInterval;
        } else {
            return "The polling interval is not changed";
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
            return "Releases all resources and exits this application";
        }

    }

}
