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

import com.aspectran.core.activity.request.FileParameter;
import com.aspectran.utils.apon.Parameters;
import org.jspecify.annotations.Nullable;

/**
 * Types of supported item values.
 *
 * <p>Created: 2008. 03. 29 PM 3:47:00</p>
 */
public enum ItemValueType {

    STRING("string", java.lang.String.class),
    INT("int", java.lang.Integer.class),
    LONG("long", java.lang.Long.class),
    FLOAT("float", java.lang.Float.class),
    DOUBLE("double", java.lang.Double.class),
    BOOLEAN("boolean", java.lang.Boolean.class),
    PARAMETERS("parameters", Parameters.class),
    FILE("file", java.io.File.class),
    MULTIPART_FILE("multipartFile", FileParameter.class),
    BEAN("bean", Object.class);

    private final String alias;

    private final Class<?> classType;

    ItemValueType(String alias, Class<?> classType) {
        this.alias = alias;
        this.classType = classType;
    }

    public Class<?> getClassType() {
        return classType;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns an {@code ItemValueType} with a value represented
     * by the specified {@code String}.
     * @param alias the item value type as a {@code String}
     * @return an {@code ItemValueType}, may be {@code null}
     */
    @Nullable
    public static ItemValueType resolve(String alias) {
        if (alias != null) {
            for (ItemValueType type : values()) {
                if (type.alias.equals(alias)) {
                    return type;
                }
            }
        }
        return null;
    }

}
