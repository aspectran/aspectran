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
package com.aspectran.daemon.command.builtins;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.request.parameter.ParameterMap;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.expr.ItemExpression;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.DaemonCommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.command.polling.CommandParameters;

import java.util.Map;

public class TemplateCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "template";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public TemplateCommand(DaemonCommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        String templateName = parameters.getTemplateName();
        if (templateName == null) {
            return failed(error("'template' parameter is not specified"));
        }

        try {
            ItemRuleMap parameterItemRuleMap = parameters.getParameterItemRuleMap();
            ItemRuleMap attributeItemRuleMap = parameters.getAttributeItemRuleMap();

            ParameterMap parameterMap = null;
            Map<String, Object> attributeMap = null;
            if ((parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) ||
                    (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty())) {
                Activity activity = new InstantActivity(getService().getActivityContext());
                ItemEvaluator evaluator = new ItemExpression(activity);
                if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
                    parameterMap = evaluator.evaluateAsParameterMap(parameterItemRuleMap);
                }
                if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
                    attributeMap = evaluator.evaluate(attributeItemRuleMap);
                }
            }

            String result = getService().template(templateName, parameterMap, attributeMap);
            return success(result);
        } catch (Exception e) {
            return failed(e);
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
            return "Executes a template";
        }

    }

}
