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
package com.aspectran.core.context.rule.type;

/**
 * The enum AppendableFileFormatType.
 * 
 * <p>Created: 2015. 02. 22 AM 4:52:38</p>
 */
public enum AppendableFileFormatType {

    XML("xml"),
    APON("apon");

    private final String alias;

    AppendableFileFormatType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns an {@code AppendableFileFormatType} with a value represented
     * by the specified {@code String}.
     *
     * @param alias the appendable file format type as a {@code String}
     * @return an {@code AppendableFileFormatType}, may be {@code null}
     */
    public static AppendableFileFormatType resolve(String alias) {
        for (AppendableFileFormatType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
