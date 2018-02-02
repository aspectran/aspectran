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
 * Supported Scope types.
 * 
 * <p>Created: 2008. 12. 22 PM 2:48:00</p>
 */
public enum ScopeType {

    SINGLETON("singleton"),
    PROTOTYPE("prototype"),
    REQUEST("request"),
    SESSION("session"),
    APPLICATION("application");

    private final String alias;

    ScopeType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a {@code ScopeType} with a value represented
     * by the specified {@code String}.
     *
     * @param alias the scope type as a {@code String}
     * @return a {@code ScopeType}, may be {@code null}
     */
    public static ScopeType resolve(String alias) {
        for (ScopeType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
