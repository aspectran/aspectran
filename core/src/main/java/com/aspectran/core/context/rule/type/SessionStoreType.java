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
 * Supported Session data store types.
 * 
 * <p>Created: 2008. 03. 26 AM 12:58:38</p>
 */
public enum SessionStoreType {

    NONE("none"),
    FILE("file");
    //MONGODB("mongodb"),
    //JDBC("jdbc");

    private final String alias;

    SessionStoreType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns an {@code SessionStoreType} with a value represented
     * by the specified {@code String}.
     *
     * @param alias the session data store type as a {@code String}
     * @return an {@code SessionStoreType}, may be {@code null}
     */
    public static SessionStoreType resolve(String alias) {
        for (SessionStoreType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
