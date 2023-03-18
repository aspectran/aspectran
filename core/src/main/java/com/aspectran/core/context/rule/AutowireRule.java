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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.core.util.ToStringBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * The Class AutowireRule.
 * 
 * <p>Created: 2016. 2. 24.</p>
 * 
 * @since 2.0.0
 */
public class AutowireRule implements BeanReferenceable {

    private static final BeanRefererType BEAN_REFERER_TYPE = BeanRefererType.AUTOWIRE_RULE;

    private AutowireTargetType targetType;

    private Object target;

    private AutowireTargetRule[] autowireTargetRules;

    private boolean required;

    public AutowireTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(AutowireTargetType targetType) {
        this.targetType = targetType;
    }

    @SuppressWarnings("unchecked")
    public <T> T getTarget() {
        return (T)target;
    }

    public void setTarget(Constructor<?> constructor) {
        this.target = constructor;
    }

    public void setTarget(Field field) {
        this.target = field;
    }

    public void setTarget(Method method) {
        this.target = method;
    }

    public AutowireTargetRule[] getAutowireTargetRules() {
        return autowireTargetRules;
    }

    public void setAutowireTargetRules(AutowireTargetRule... autowireTargetRules) {
        if (autowireTargetRules == null || autowireTargetRules.length == 0) {
            throw new IllegalArgumentException("autowireTargetRules must not be null or empty");
        }
        this.autowireTargetRules = autowireTargetRules;
    }

    public void clearAutowireTargetRules() {
        this.autowireTargetRules = null;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public BeanRefererType getBeanRefererType() {
        return BEAN_REFERER_TYPE;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("targetType", targetType);
        tsb.append("target", target);
        tsb.append("targetRules", autowireTargetRules);
        tsb.append("required", required);
        return tsb.toString();
    }

    public static AutowireTargetRule[] getAutowireTargetRules(AutowireRule autowireRule) {
        if (autowireRule != null && autowireRule.getAutowireTargetRules() != null) {
            return autowireRule.getAutowireTargetRules();
        } else {
            return null;
        }
    }

    public static AutowireTargetRule getAutowireTargetRule(AutowireRule autowireRule) {
        if (autowireRule != null && autowireRule.getAutowireTargetRules() != null) {
            return autowireRule.getAutowireTargetRules()[0];
        } else {
            return null;
        }
    }

}
