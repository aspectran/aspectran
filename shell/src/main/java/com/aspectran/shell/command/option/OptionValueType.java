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
package com.aspectran.shell.command.option;

/**
 * Supported Option value types.
 */
public enum OptionValueType {

    STRING("string", String.class),
    INT("int", Integer.class),
    LONG("long", Long.class),
    FLOAT("float", Float.class),
    DOUBLE("double", Double.class),
    BOOLEAN("boolean", Boolean.class),
    FILE("file", java.io.File.class);

    private final String alias;

    private final Class<?> classType;

    OptionValueType(String alias, Class<?> classType) {
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
     * Returns an {@code OptionValueType} with a value represented
     * by the specified {@code String}.
     *
     * @param alias the item value type as a {@code String}
     * @return an {@code OptionValueType}, may be {@code null}
     */
    public static OptionValueType resolve(String alias) {
        for (OptionValueType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

}
