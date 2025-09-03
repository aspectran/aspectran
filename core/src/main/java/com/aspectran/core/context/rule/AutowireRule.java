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
package com.aspectran.core.context.rule;

import com.aspectran.core.context.rule.ability.BeanReferenceable;
import com.aspectran.core.context.rule.type.AutowireTargetType;
import com.aspectran.core.context.rule.type.BeanRefererType;
import com.aspectran.utils.ToStringBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Represents a rule for autowiring dependencies into a bean.
 * It specifies the target (field, method, or constructor) and the required dependencies.
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

    /**
     * Gets the autowire target type.
     * @return the autowire target type
     */
    public AutowireTargetType getTargetType() {
        return targetType;
    }

    /**
     * Sets the autowire target type.
     * @param targetType the autowire target type
     */
    public void setTargetType(AutowireTargetType targetType) {
        this.targetType = targetType;
    }

    /**
     * Gets the autowire target.
     * @param <T> the type of the target
     * @return the autowire target
     */
    @SuppressWarnings("unchecked")
    public <T> T getTarget() {
        return (T)target;
    }

    /**
     * Sets the autowire target with a constructor.
     * @param constructor the target constructor
     */
    public void setTarget(Constructor<?> constructor) {
        this.target = constructor;
    }

    /**
     * Sets the autowire target with a field.
     * @param field the target field
     */
    public void setTarget(Field field) {
        this.target = field;
    }

    /**
     * Sets the autowire target with a method.
     * @param method the target method
     */
    public void setTarget(Method method) {
        this.target = method;
    }

    /**
     * Gets the autowire target rules.
     * @return the autowire target rules
     */
    public AutowireTargetRule[] getAutowireTargetRules() {
        return autowireTargetRules;
    }

    /**
     * Sets the autowire target rules.
     * @param autowireTargetRules the autowire target rules
     */
    public void setAutowireTargetRules(AutowireTargetRule... autowireTargetRules) {
        if (autowireTargetRules == null || autowireTargetRules.length == 0) {
            throw new IllegalArgumentException("autowireTargetRules must not be null or empty");
        }
        this.autowireTargetRules = autowireTargetRules;
    }

    /**
     * Clears the autowire target rules.
     */
    public void clearAutowireTargetRules() {
        this.autowireTargetRules = null;
    }

    /**
     * Returns whether autowiring is required.
     * @return true if autowiring is required, false otherwise
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets whether autowiring is required.
     * @param required true if autowiring is required
     */
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

    /**
     * A static helper method to get the autowire target rules from an AutowireRule.
     * @param autowireRule the autowire rule
     * @return the autowire target rules, or null if not present
     */
    public static AutowireTargetRule[] getAutowireTargetRules(AutowireRule autowireRule) {
        if (autowireRule != null && autowireRule.getAutowireTargetRules() != null) {
            return autowireRule.getAutowireTargetRules();
        } else {
            return null;
        }
    }

    /**
     * A static helper method to get the first autowire target rule from an AutowireRule.
     * @param autowireRule the autowire rule
     * @return the first autowire target rule, or null if not present
     */
    public static AutowireTargetRule getAutowireTargetRule(AutowireRule autowireRule) {
        if (autowireRule != null && autowireRule.getAutowireTargetRules() != null) {
            return autowireRule.getAutowireTargetRules()[0];
        } else {
            return null;
        }
    }

}
