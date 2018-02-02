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
package com.aspectran.core.context.rule.type;

/**
 * Supported Appender types.
 * 
 * <p>Created: 2008. 04. 25 AM 16:47:38</p>
 */
public enum AppenderType {

    FILE("file"),
    RESOURCE("resource"),
    URL("url"),
    PARAMETERS("parameters");

    private final String alias;

    AppenderType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns an {@code AppenderType} with a value represented
     * by the specified {@code String}.
     *
     * @param alias the appender type as a {@code String}
     * @return an {@code AppenderType}, may be {@code null}
     */
    public static AppenderType resolve(String alias) {
        for (AppenderType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
