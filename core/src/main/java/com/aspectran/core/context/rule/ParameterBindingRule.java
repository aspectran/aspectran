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

import com.aspectran.utils.ToStringBuilder;

/**
 * Defines how a single parameter should be bound to a method argument.
 * This rule holds metadata about the parameter, such as its name, type, and whether it is required.
 *
 * <p>Created: 2019. 2. 17.</p>
 *
 * @since 6.0.0
 */
public class ParameterBindingRule {

    private String name;

    private Class<?> type;

    private String format;

    private boolean required;

    /**
     * Gets the name of the parameter.
     * @return the parameter name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the parameter.
     * @param name the parameter name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the type of the parameter.
     * @return the parameter type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Sets the type of the parameter.
     * @param type the parameter type
     */
    public void setType(Class<?> type) {
        this.type = type;
    }

    /**
     * Gets the format pattern for the parameter (e.g., for date/time conversion).
     * @return the format pattern
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format pattern for the parameter.
     * @param format the format pattern
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Returns whether the parameter is required.
     * @return true if the parameter is required, false otherwise
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets whether the parameter is required.
     * @param required true if the parameter is required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder();
        tsb.append("name", name);
        tsb.append("type", type);
        tsb.append("format", format);
        tsb.append("required", required);
        return tsb.toString();
    }

}
