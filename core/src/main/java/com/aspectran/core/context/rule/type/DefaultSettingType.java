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

import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Supported default setting types.
 */
public enum DefaultSettingType {

    /**
     * Specifies a prefix that is automatically added to the names of all translets.
     * This is useful for avoiding naming conflicts when importing translets from multiple sources.
     */
    TRANSLET_NAME_PREFIX("transletNamePrefix"),

    /**
     * Specifies a suffix that is automatically added to the names of all translets.
     * This can be used, for example, to distinguish translets based on their origin or type.
     */
    TRANSLET_NAME_SUFFIX("transletNameSuffix"),

    /**
     * Determines whether to verify the syntax and validity of AOP pointcut patterns
     * during application startup. Disabling this can speed up initialization but may
     * lead to runtime errors if patterns are incorrect.
     */
    POINTCUT_PATTERN_VERIFIABLE("pointcutPatternVerifiable"),

    /**
     * Defines the bean ID of the template engine to be used by default for
     * {@code transform} actions when no specific engine is specified.
     * This allows for a centralized template engine configuration.
     */
    DEFAULT_TEMPLATE_ENGINE_BEAN("defaultTemplateEngineBean"),

    /**
     * Defines the bean ID of the scheduler to be used by default for all
     * {@code schedule} rules when no specific scheduler is referenced.
     * This provides a global default for job scheduling.
     */
    DEFAULT_SCHEDULER_BEAN("defaultSchedulerBean");

    private final String alias;

    DefaultSettingType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a {@code DefaultSettingType} with a value represented
     * by the specified {@code String}.
     * @param alias the default setting type as a {@code String}
     * @return a {@code DefaultSettingType}, may be {@code null}
     */
    @Nullable
    public static DefaultSettingType resolve(String alias) {
        if (alias != null) {
            for (DefaultSettingType type : values()) {
                if (type.alias.equals(alias)) {
                    return type;
                }
            }
        }
        return null;
    }

}
