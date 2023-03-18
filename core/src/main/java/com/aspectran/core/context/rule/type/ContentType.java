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
 * Supported content types.
 * 
 * <p>Created: 2008. 03. 26 AM 12:58:38</p>
 */
public enum ContentType {

    APPLICATION_APON("application/apon"),
    APPLICATION_JSON("application/json"),
    APPLICATION_XML("application/xml"),
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html");

    private final String alias;

    ContentType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a {@code ContentType} with a value represented
     * by the specified {@code String}.
     *
     * @param alias the content type as a {@code String}
     * @return a {@code ContentType}, may be {@code null}
     */
    public static ContentType resolve(String alias) {
        for (ContentType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
