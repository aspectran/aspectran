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
 * {@code AnnotatedMethodAction} that invokes a method of the bean instance
 * specified by the annotation.
 *
 * <p>Created: 2016. 2. 9.</p>
 *
 * @since 2.0.0
 */
public class AnnotatedAction implements Executable {

    private final AnnotatedActionRule annotatedActionRule;

    /**
     * Instantiates a new AnnotatedMethodAction.
     * @param annotatedActionRule the annotated method action rule
     */
    public AnnotatedAction(AnnotatedActionRule annotatedActionRule) {
        this.annotatedActionRule = annotatedActionRule;
    }

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

    protected Object resolveBean(@NonNull Activity activity) throws Exception {
        Object bean = null;
        if (!Modifier.isInterface(annotatedActionRule.getBeanClass().getModifiers())) {
            bean = activity.getBean(annotatedActionRule.getBeanClass());
        }
        return bean;
    }

    /**
     * Returns the annotated bean method action rule.
     * @return the annotated bean method action rule
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
