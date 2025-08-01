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
 * Supported item types.
 * 
 * <p>Created: 2008. 03. 29 PM 3:47:00</p>
 */
public enum ItemType {

    SINGLE("single"),
    ARRAY("array"),
    LIST("list"),
    MAP("map"),
    SET("set"),
    PROPERTIES("properties");

    private final String alias;

    ItemType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns an {@code ItemType} with a value represented
     * by the specified {@code String}.
     * @param alias the item type as a {@code String}
     * @return an {@code ItemType}, may be {@code null}
     */
    @Nullable
    public static ItemType resolve(String alias) {
        for (ItemType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
