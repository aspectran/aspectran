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
import com.aspectran.core.activity.process.action.BeanMethodAction;
import com.aspectran.core.context.rule.BeanMethodActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.CommandResult;
import com.aspectran.daemon.command.polling.CommandParameters;
import com.aspectran.daemon.service.DaemonService;

public class BeanMethodActionCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "beanMethodAction";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public BeanMethodActionCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public CommandResult execute(CommandParameters parameters) {
        DaemonService service = getService();

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

            BeanMethodActionRule beanMethodActionRule = new BeanMethodActionRule();
            beanMethodActionRule.setBeanId(beanName);
            beanMethodActionRule.setMethodName(methodName);
            beanMethodActionRule.setArgumentItemRuleMap(argumentItemRuleMap);
            beanMethodActionRule.setPropertyItemRuleMap(propertyItemRuleMap);

            if (beanName.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
                String className = beanName.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
                Class<?> beanClass = service.getAspectranClassLoader().loadClass(className);
                beanMethodActionRule.setBeanClass(beanClass);
            }

            Activity activity = new InstantActivity(service.getActivityContext());
            BeanMethodAction beanMethodAction = new BeanMethodAction(beanMethodActionRule);
            Object result = beanMethodAction.execute(activity);
            return success(result != null ? result.toString() : null);
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
            return "Executes a method on the specified bean";
        }

    }

}
