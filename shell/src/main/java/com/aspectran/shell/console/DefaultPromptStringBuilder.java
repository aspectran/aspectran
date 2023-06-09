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

    private final StringBuilder sb;

    private String defaultValue;

    public DefaultPromptStringBuilder() {
        this.sb = new StringBuilder();
    }

    public DefaultPromptStringBuilder(String str) {
        if (str == null) {
            throw new IllegalArgumentException("str must not be null");
        }
        this.sb = new StringBuilder(str);
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
        sb.append(str);
        return this;
    }

    @Override
    public PromptStringBuilder clear() {
        sb.setLength(0);
        return this;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public PromptStringBuilder setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }

}
