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
package com.aspectran.utils.apon;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;

/**
 * Defines the type of the parameter value.
 *
 * <p>Created: 2008. 03. 29 PM 3:47:00</p>
 */
public enum ValueType {

    STRING("string"),
    TEXT("text"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    OBJECT("object"),
    VARIABLE("variable"),
    PARAMETERS("parameters");

    private final String alias;

    ValueType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a ValueType with a value represented by the specified String.
     * @param alias the specified String
     * @return the value type of the parameter
     */
    @Nullable
    public static ValueType resolve(String alias) {
        if (alias != null) {
            for (ValueType type : values()) {
                if (type.alias.equals(alias)) {
                    return type;
                }
            }
        }
        return null;
    }

    @Nullable
    public static ValueType resolveByHint(@NonNull String name) {
        int start = name.indexOf(AponFormat.ROUND_BRACKET_OPEN);
        if (start > 0) {
            int end = name.indexOf(AponFormat.ROUND_BRACKET_CLOSE);
            if (end > start) {
                String hintedType = name.substring(start + 1, end);
                return resolve(hintedType);
            }
        }
        return null;
    }

    @NonNull
    public static String stripHint(@NonNull String name) {
        int hintStartIndex = name.indexOf(AponFormat.ROUND_BRACKET_OPEN);
        if (hintStartIndex > 0) {
            return name.substring(0, hintStartIndex);
        }
        return name;
    }

    public static ValueType resolveFrom(Object value) {
        if (value == null) {
            return ValueType.OBJECT;
        }
        ValueType type;
        if (value instanceof CharSequence) {
            if (value.toString().contains(AponFormat.NEW_LINE)) {
                type = ValueType.TEXT;
            } else {
                type = ValueType.STRING;
            }
        } else if (value instanceof Character) {
            type = ValueType.STRING;
        } else if (value instanceof Integer) {
            type = ValueType.INT;
        } else if (value instanceof Long) {
            type = ValueType.LONG;
        } else if (value instanceof Float) {
            type = ValueType.FLOAT;
        } else if (value instanceof Double) {
            type = ValueType.DOUBLE;
        } else if (value instanceof Boolean) {
            type = ValueType.BOOLEAN;
        } else if (value instanceof Parameters) {
            type = ValueType.PARAMETERS;
        } else {
            type = ValueType.OBJECT;
        }
        return type;
    }

}
