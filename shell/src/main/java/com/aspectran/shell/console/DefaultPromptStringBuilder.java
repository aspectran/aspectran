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
package com.aspectran.shell.console;

public class DefaultPromptStringBuilder implements PromptStringBuilder {

    private final StringBuilder buffer;

    private String defaultValue;

    public DefaultPromptStringBuilder() {
        this.buffer = new StringBuilder();
    }

    public DefaultPromptStringBuilder(String str) {
        if (str == null) {
            throw new IllegalArgumentException("str must not be null");
        }
        this.buffer = new StringBuilder(str);
    }

    @Override
    public PromptStringBuilder setStyle(String... styles) {
        return this;
    }

    @Override
    public PromptStringBuilder resetStyle(String... styles) {
        return this;
    }

    @Override
    public PromptStringBuilder resetStyle() {
        return this;
    }

    @Override
    public PromptStringBuilder append(String str) {
        buffer.append(str);
        return this;
    }

    @Override
    public PromptStringBuilder clear() {
        buffer.setLength(0);
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
    public String toString() {
        return buffer.toString();
    }

}
