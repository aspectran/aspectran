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
package com.aspectran.daemon.command.builtins;

import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.process.action.InvokeAction;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.InvokeActionRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandParameters;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.service.DaemonService;
import com.aspectran.utils.annotation.jsr305.NonNull;

public class InvokeActionCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "invokeAction";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public InvokeActionCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        DaemonService daemonService = getDaemonService();

        try {
            String beanName = parameters.getBeanName();
            String methodName = parameters.getMethodName();
            ItemRuleMap argumentItemRuleMap = parameters.getArgumentItemRuleMap();
            ItemRuleMap propertyItemRuleMap = parameters.getPropertyItemRuleMap();

            if (beanName == null) {
                return failed(error("'bean' parameter is not specified"));
            }
            if (methodName == null) {
                return failed(error("'method' parameter is not specified"));
            }

            InvokeActionRule invokeActionRule = new InvokeActionRule();
            invokeActionRule.setBeanId(beanName);
            invokeActionRule.setMethodName(methodName);
            invokeActionRule.setArgumentItemRuleMap(argumentItemRuleMap);
            invokeActionRule.setPropertyItemRuleMap(propertyItemRuleMap);

            if (beanName.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
                String className = beanName.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
                Class<?> beanClass = daemonService.getServiceClassLoader().loadClass(className);
                invokeActionRule.setBeanClass(beanClass);
            }

            InstantActivity activity = new InstantActivity(daemonService.getActivityContext());
            Object result = activity.perform(() -> {
                InvokeAction invokeAction = new InvokeAction(invokeActionRule);
                return invokeAction.execute(activity);
            });
            return success(result != null ? result.toString() : null);
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
        @NonNull
        public String getDescription() {
            return "Executes a method on the specified bean";
        }

    }

}
