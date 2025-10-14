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
package com.aspectran.shell.console;

/**
 * Default implementation of {@link PromptStringBuilder} that does not support styling.
 * <p>This class provides a basic, non-styling builder for constructing prompt strings.
 * All styling methods are implemented as no-ops.</p>
 *
 * <p>Created: 2017. 11. 10.</p>
 */
public class DefaultPromptStringBuilder implements PromptStringBuilder {

    private final StringBuilder buffer = new StringBuilder();

    private String defaultValue;

    /**
     * Instantiates a new default prompt string builder.
     */
    public DefaultPromptStringBuilder() {
    }

    @Override
    public PromptStringBuilder append(String str) {
        buffer.append(str);
        return this;
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public PromptStringBuilder setStyle(String... styles) {
        return this;
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public PromptStringBuilder resetStyle(String... styles) {
        return this;
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public PromptStringBuilder resetStyle() {
        return this;
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public PromptStringBuilder secondaryStyle() {
        return this;
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public PromptStringBuilder successStyle() {
        return this;
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public PromptStringBuilder dangerStyle() {
        return this;
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public PromptStringBuilder warningStyle() {
        return this;
    }

    /**
     * This is a no-op method as this class does not support styling.
     */
    @Override
    public PromptStringBuilder infoStyle() {
        return this;
    }

    @Override
    public PromptStringBuilder defaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void clear() {
        buffer.setLength(0);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

}
