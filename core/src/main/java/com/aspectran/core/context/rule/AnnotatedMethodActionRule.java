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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.process.action.AnnotatedMethodAction;
import com.aspectran.core.util.ToStringBuilder;

import java.lang.reflect.Method;

/**
 * The Class AnnotatedMethodActionRule.
 * 
 * <p>Created: 2016. 2. 10.</p>
 * 
 * @since 2.0.0
 */
public class AnnotatedMethodActionRule {

    private String actionId;

    private Class<?> beanClass;

    private Method method;

    private boolean requiresTranslet;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
        this.requiresTranslet = isRequiresTranslet(method);
    }

    public String getMethodName() {
        return (method != null ? method.getName() : null);
    }

    public boolean isRequiresTranslet() {
        return requiresTranslet;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        if (beanClass != null) {
            tsb.append("class", beanClass.getName());
        }
        tsb.append("method", method);
        return tsb.toString();
    }

    /**
     * Returns a new derived instance of AnnotatedMethodActionRule.
     *
     * @param actionClass the action class
     * @param method the method
     * @return the annotated method action rule
     */
    public static AnnotatedMethodActionRule newInstance(Class<?> actionClass, Method method) {
        AnnotatedMethodActionRule annotatedMethodActionRule = new AnnotatedMethodActionRule();
        annotatedMethodActionRule.setBeanClass(actionClass);
        annotatedMethodActionRule.setMethod(method);
        return annotatedMethodActionRule;
    }

    public static boolean isRequiresTranslet(Method method) {
        if (method.getParameterCount() == 1) {
            Class<?>[] paramTypes = method.getParameterTypes();
            return paramTypes[0].isAssignableFrom(Translet.class);
        } else {
            return false;
        }
    }

    public static AnnotatedMethodAction newAnnotatedMethodAction(Class<?> beanClass, Method method) {
        AnnotatedMethodActionRule annotatedMethodActionRule = new AnnotatedMethodActionRule();
        annotatedMethodActionRule.setBeanClass(beanClass);
        annotatedMethodActionRule.setMethod(method);
        return new AnnotatedMethodAction(annotatedMethodActionRule, null);
    }

}
