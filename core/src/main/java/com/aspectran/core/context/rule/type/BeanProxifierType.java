/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
 * Supported bean proxifier types.
 * 
 * <p>Created: 2008. 03. 26 AM 12:58:38</p>
 */
public enum BeanProxifierType {

    CGLIB("cglib"),
    JAVASSIST("javassist"),
    JDK("jdk");

    private final String alias;

    BeanProxifierType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a {@code BeanProxifierType} with a value represented
     * by the specified {@code String}.
     * @param alias the bean proxifier type as a {@code String}
     * @return a {@code BeanProxifierType}, may be {@code null}
     */
    @Nullable
    public static BeanProxifierType resolve(String alias) {
        for (BeanProxifierType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
