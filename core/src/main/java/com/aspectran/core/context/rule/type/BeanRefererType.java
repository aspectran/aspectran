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
package com.aspectran.core.context.rule.type;

import org.jspecify.annotations.Nullable;

/**
 * An enum that categorizes the different ways a bean can be referenced by a rule
 * within the framework.
 *
 * <p>Each constant represents a specific context in which a bean is used, which is
 * useful for introspection and for analyzing dependency relationships.</p>
 *
 * <p>Created: 2016. 2. 20.</p>
 */
public enum BeanRefererType {

    /**
     * Indicates that the bean is referenced as an AOP advice within an aspect rule.
     */
    ASPECT_RULE("aspectRule"),

    /**
     * Indicates that the bean is referenced for autowiring into another bean's properties.
     */
    AUTOWIRE_RULE("autowireRule"),

    /**
     * Indicates that the bean is referenced as an executable action.
     */
    BEAN_METHOD_ACTION_RULE("invokeActionRule"),

    /**
     * Indicates that the bean is referenced by another bean definition, typically as a
     * constructor argument or property value.
     */
    BEAN_RULE("beanRule"),

    /**
     * Indicates that the bean is referenced as a scheduled job.
     */
    SCHEDULE_RULE("scheduleRule"),

    /**
     * Indicates that the bean is referenced as a template engine or processor.
     */
    TEMPLATE_RULE("templateRule"),

    /**
     * Indicates that the bean is referenced within a token in a template or string.
     */
    TOKEN("token");

    private final String alias;

    BeanRefererType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a {@code BeanRefererType} with a value represented
     * by the specified {@code String}.
     * @param alias the bean referrer type as a {@code String}
     * @return a {@code BeanRefererType}, may be {@code null}
     */
    @Nullable
    public static BeanRefererType resolve(String alias) {
        if (alias != null) {
            for (BeanRefererType type : values()) {
                if (type.alias.equals(alias)) {
                    return type;
                }
            }
        }
        return null;
    }

}
