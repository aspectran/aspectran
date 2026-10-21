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
import com.aspectran.core.context.rule.MethodActionRule;
import com.aspectran.core.context.rule.ParameterBindingRule;
import com.aspectran.core.context.rule.type.ActionType;
import com.aspectran.utils.ToStringBuilder;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * An action that executes a method on a bean, where the target method is identified
 * by specific Aspectran annotations.
 *
 * <p>This allows for a convention-over-configuration approach, where actions can be
 * discovered and executed based on annotations rather than explicit XML rules.
 * It leverages {@link ActionMethodInvoker} to handle parameter binding and method invocation.</p>
 *
 * <p>Created: 2016. 2. 9.</p>
 */
public class MethodAction implements Executable {

    private final MethodActionRule methodActionRule;

    /**
     * Instantiates a new MethodAction.
     * @param methodActionRule the rule that defines this method action
     */
    public MethodAction(@NonNull MethodActionRule methodActionRule) {
        this.methodActionRule = methodActionRule;
    }

    /**
     * Executes the action method.
     * @param activity the current activity
     * @return the result of the method invocation
     * @throws ActionExecutionException if an error occurs during method invocation
     */
    @Override
    public Object execute(@NonNull Activity activity) throws Exception {
        Object bean = resolveBean(activity);
        Method method = methodActionRule.getMethod();
        ParameterBindingRule[] parameterBindingRules = methodActionRule.getParameterBindingRules();
        return ActionMethodInvoker.invoke(activity, bean, method, parameterBindingRules);
    }

    /**
     * Resolves the bean instance on which the action method will be invoked.
     * If the method is static, no bean is needed and this method returns {@code null}.
     * Otherwise, it retrieves a bean instance corresponding to the method's declaring class.
     * @param activity the current activity
     * @return the resolved bean instance, or {@code null} if the method is static
     * @throws ActionExecutionException if a required bean instance cannot be resolved
     */
    protected Object resolveBean(@NonNull Activity activity) throws ActionExecutionException {
        // A static method does not require a bean instance.
        Method method = methodActionRule.getMethod();
        if (Modifier.isStatic(method.getModifiers())) {
            return null;
        }
        return activity.getBean(methodActionRule.getBeanClass());
    }

    /**
     * Returns the rule that defines this method action.
     * @return the method action rule
     */
    public MethodActionRule getMethodActionRule() {
        return methodActionRule;
    }

    @Override
    public String getActionId() {
        return methodActionRule.getActionId();
    }

    @Override
    public ActionType getActionType() {
        return ActionType.METHOD;
    }

    @Override
    public void toString(@NonNull ToStringBuilder tsb) {
        if (getActionId() != null) {
            tsb.append("id", getActionId());
        }
        tsb.append(getActionType().toString(), methodActionRule.getMethod());
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        toString(tsb);
        return tsb.toString();
    }

}
