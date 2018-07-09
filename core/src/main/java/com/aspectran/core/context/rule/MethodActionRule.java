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
package com.aspectran.core.context.rule;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.process.action.MethodAction;
import com.aspectran.core.util.ToStringBuilder;

import java.lang.reflect.Method;

/**
 * The Class MethodActionRule.
 * 
 * <p>Created: 2016. 2. 10.</p>
 * 
 * @since 2.0.0
 */
public class MethodActionRule {

    private String actionId;

    private Class<?> configBeanClass;

    private Method method;

    private boolean requiresTranslet;

//    private AspectAdviceRule aspectAdviceRule;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public Class<?> getConfigBeanClass() {
        return configBeanClass;
    }

    public void setConfigBeanClass(Class<?> configBeanClass) {
        this.configBeanClass = configBeanClass;
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

//    /**
//     * Gets the aspect advice rule.
//     *
//     * @return the aspect advice rule
//     */
//    public AspectAdviceRule getAspectAdviceRule() {
//        return aspectAdviceRule;
//    }
//
//    /**
//     * Sets the aspect advice rule.
//     *
//     * @param aspectAdviceRule the new aspect advice rule
//     */
//    public void setAspectAdviceRule(AspectAdviceRule aspectAdviceRule) {
//        this.aspectAdviceRule = aspectAdviceRule;
//    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        if (configBeanClass != null) {
            tsb.append("class", configBeanClass.getName());
        }
        tsb.append("method", method);
        //tsb.append("aspectAdviceRule", aspectAdviceRule);
        return tsb.toString();
    }

    /**
     * Returns a new derived instance of MethodActionRule.
     *
     * @param actionClass the action class
     * @param method the method
     * @return the method action rule
     */
    public static MethodActionRule newInstance(Class<?> actionClass, Method method) {
        MethodActionRule methodActionRule = new MethodActionRule();
        methodActionRule.setConfigBeanClass(actionClass);
        methodActionRule.setMethod(method);
        return methodActionRule;
    }

    public static boolean isRequiresTranslet(Method method) {
        if (method.getParameterCount() == 1) {
            Class<?>[] paramTypes = method.getParameterTypes();
            return paramTypes[0].isAssignableFrom(Translet.class);
        } else {
            return false;
        }
    }

    public static MethodAction newMethodAction(Class<?> configBeanClass, Method method) {
        MethodActionRule methodActionRule = new MethodActionRule();
        methodActionRule.setConfigBeanClass(configBeanClass);
        methodActionRule.setMethod(method);
        return new MethodAction(methodActionRule, null);
    }

}
