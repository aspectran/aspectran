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
package com.aspectran.daemon.command.builtin;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.activity.process.action.BeanMethodAction;
import com.aspectran.core.context.rule.BeanMethodActionRule;
import com.aspectran.core.context.rule.BeanRule;
import com.aspectran.core.context.rule.IllegalRuleException;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.daemon.command.AbstractCommand;
import com.aspectran.daemon.command.CommandRegistry;
import com.aspectran.daemon.command.polling.CommandParameters;

public class BeanActionCommand extends AbstractCommand {

    private static final String NAMESPACE = "builtins";

    private static final String COMMAND_NAME = "beanAction";

    private final CommandDescriptor descriptor = new CommandDescriptor();

    public BeanActionCommand(CommandRegistry registry) {
        super(registry);
    }

    @Override
    public String execute(CommandParameters parameters) throws Exception {
        String beanName = parameters.getBeanName();
        String methodName = parameters.getMethodName();
        ItemRuleMap argumentItemRuleMap = parameters.getArgumentItemRuleMap();
        ItemRuleMap propertyItemRuleMap = parameters.getPropertyItemRuleMap();

        if (beanName == null) {
            throw new IllegalRuleException("'bean' parameter is not specified");
        }
        if (methodName == null) {
            throw new IllegalRuleException("'method' parameter is not specified");
        }

        BeanMethodActionRule beanMethodActionRule = new BeanMethodActionRule();
        beanMethodActionRule.setBeanId(beanName);
        beanMethodActionRule.setMethodName(methodName);
        beanMethodActionRule.setArgumentItemRuleMap(argumentItemRuleMap);
        beanMethodActionRule.setPropertyItemRuleMap(propertyItemRuleMap);

        if (beanName.startsWith(BeanRule.CLASS_DIRECTIVE_PREFIX)) {
            String className = beanName.substring(BeanRule.CLASS_DIRECTIVE_PREFIX.length());
            Class<?> beanClass = getService().getAspectranClassLoader().loadClass(className);
            beanMethodActionRule.setBeanClass(beanClass);
        }

        Activity activity = new InstantActivity(getService().getActivityContext());
        BeanMethodAction beanAction = new BeanMethodAction(beanMethodActionRule, null);
        Object result = beanAction.execute(activity);
        return (result != null ? result.toString() : null);
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
            return "Execute a method in the bean";
        }

    }

}
