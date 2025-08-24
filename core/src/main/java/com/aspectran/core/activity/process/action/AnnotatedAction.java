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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.context.rule.AnnotatedActionRule;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.ToStringBuilder;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * An action that executes a method on a bean, where the target method is identified
 * by specific Aspectran annotations.
 *
 * <p>This allows for a convention-over-configuration approach, where actions can be
 * discovered and executed based on annotations rather than explicit XML rules.
 * It leverages {@link AnnotatedMethodInvoker} to handle parameter binding and method invocation.</p>
 *
 * <p>Created: 2016. 2. 9.</p>
 */
public class AnnotatedAction implements Executable {

    private final AnnotatedActionRule annotatedActionRule;

    /**
     * Instantiates a new AnnotatedAction.
     * @param annotatedActionRule the rule that defines this annotated action
     */
    public AnnotatedAction(AnnotatedActionRule annotatedActionRule) {
        this.annotatedActionRule = annotatedActionRule;
    }

    /**
     * Executes the annotated action method.
     * @param activity the current activity
     * @return the result of the method invocation
     * @throws Exception if an error occurs during method invocation
     */
    @Override
    public Object execute(@NonNull Activity activity) throws Exception {
        try {
            Object bean = resolveBean(activity);
            Method method = annotatedActionRule.getMethod();
            ParameterBindingRule[] parameterBindingRules = annotatedActionRule.getParameterBindingRules();
            if (method.getReturnType() == Void.TYPE) {
                AnnotatedMethodInvoker.invoke(activity, bean, method, parameterBindingRules);
                return Void.TYPE;
            } else {
                return AnnotatedMethodInvoker.invoke(activity, bean, method, parameterBindingRules);
            }
        } catch (Exception e) {
            throw new ActionExecutionException(this, e);
        }
    }

    /**
     * Resolves the bean instance on which the annotated method will be invoked.
     * @param activity the current activity
     * @return the resolved bean instance
     * @throws Exception if the bean cannot be resolved
     */
    protected Object resolveBean(@NonNull Activity activity) throws Exception {
        Object bean = null;
        if (!Modifier.isInterface(annotatedActionRule.getBeanClass().getModifiers())) {
            bean = activity.getBean(annotatedActionRule.getBeanClass());
        }
        return bean;
    }

    /**
     * Returns the rule that defines this annotated action.
     * @return the annotated action rule
     */
    public AnnotatedActionRule getAnnotatedActionRule() {
        return annotatedActionRule;
    }

    @Override
    public String getActionId() {
        return annotatedActionRule.getActionId();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.INVOKE_ANNOTATED;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append(getActionType().toString(), annotatedActionRule);
        return tsb.toString();
    }

}
