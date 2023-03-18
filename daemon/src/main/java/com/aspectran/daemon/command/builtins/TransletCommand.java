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

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.context.expr.ItemEvaluation;
import com.aspectran.core.context.expr.ItemEvaluator;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.util.OutputStringWriter;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.service.DaemonService;

import java.io.Writer;
import java.util.Map;

public class TransletCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "translet";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public TransletCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        DaemonService daemonService = getDaemonService();

        String transletName = parameters.getTransletName();
        if (transletName == null) {
            return failed(error("'translet' parameter is not specified"));
        }

        try {
            ItemEvaluator evaluator = new ItemEvaluation(daemonService.getDefaultActivity());

            ParameterMap parameterMap = null;
            ItemRuleMap parameterItemRuleMap = parameters.getParameterItemRuleMap();
            if (parameterItemRuleMap != null && !parameterItemRuleMap.isEmpty()) {
                parameterMap = evaluator.evaluateAsParameterMap(parameterItemRuleMap);
            }

            Map<String, Object> attributeMap = null;
            ItemRuleMap attributeItemRuleMap = parameters.getAttributeItemRuleMap();
            if (attributeItemRuleMap != null && !attributeItemRuleMap.isEmpty()) {
                attributeMap = evaluator.evaluate(attributeItemRuleMap);
            }

            Translet translet = daemonService.translate(transletName, parameterMap, attributeMap);
            Writer writer = translet.getResponseAdapter().getWriter();
            if (writer instanceof OutputStringWriter && !((OutputStringWriter)writer).isDirty()) {
                return success(null);
            } else {
                return success(writer.toString());
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
            return "Executes a translet";
        }

    }

}
