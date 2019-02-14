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

    private AutowireRule autowireRule;

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
    }

    public String getMethodName() {
        return (method != null ? method.getName() : null);
    }

    public AutowireRule getAutowireRule() {
        return autowireRule;
    }

    public void setAutowireRule(AutowireRule autowireRule) {
        this.autowireRule = autowireRule;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        if (beanClass != null) {
            tsb.append("class", beanClass.getName());
        }
        tsb.append("method", method);
        tsb.append("autowireRule", autowireRule);
        return tsb.toString();
    }

}
