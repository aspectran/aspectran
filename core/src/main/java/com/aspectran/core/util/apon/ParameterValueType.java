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
package com.aspectran.core.util.apon;

/**
 * Defines the type of the parameter value.
 * 
 * <p>Created: 2008. 03. 29 PM 3:47:00</p>
 */
public enum ParameterValueType {

    STRING("string"),
    TEXT("text"),
    INT("int"),
    LONG("long"),
    FLOAT("float"),
    DOUBLE("double"),
    BOOLEAN("boolean"),
    VARIABLE("variable"),
    PARAMETERS("parameters");

    private final String alias;

    ParameterValueType(String alias) {
        this.alias = alias;
    }

    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Returns a ParameterValueType with a value represented by the specified String.
     *
     * @param alias the specified String
     * @return the parameter value type
     */
    public static ParameterValueType resolve(String alias) {
        for (ParameterValueType type : values()) {
            if (type.alias.equals(alias)) {
                return type;
            }
        }
        return null;
    }

    public static ParameterValueType resolveByHint(String name) {
        int hintStartIndex = name.indexOf(AponFormat.ROUND_BRACKET_OPEN);
        if (hintStartIndex > 0) {
            int hintEndIndex = name.indexOf(AponFormat.ROUND_BRACKET_CLOSE);
            if (hintEndIndex > hintStartIndex) {
                String typeHint = name.substring(hintStartIndex + 1, hintEndIndex);
                return resolve(typeHint);
            }
        }
        return null;
    }

    public static String stripValueTypeHint(String name) {
        int hintStartIndex = name.indexOf(AponFormat.ROUND_BRACKET_OPEN);
        if (hintStartIndex > 0) {
            return name.substring(0, hintStartIndex);
        }
        return name;
    }

    public static ParameterValueType determineValueType(Object value) {
        ParameterValueType type;
        if (value instanceof String) {
            if (value.toString().indexOf(AponFormat.NEW_LINE_CHAR) == -1) {
                type = ParameterValueType.STRING;
            } else {
                type = ParameterValueType.TEXT;
            }
        } else if (value instanceof Integer) {
            type = ParameterValueType.INT;
        } else if (value instanceof Long) {
            type = ParameterValueType.LONG;
        } else if (value instanceof Float) {
            type = ParameterValueType.FLOAT;
        } else if (value instanceof Double) {
            type = ParameterValueType.DOUBLE;
        } else if (value instanceof Boolean) {
            type = ParameterValueType.BOOLEAN;
        } else if (value instanceof Parameters) {
            type = ParameterValueType.PARAMETERS;
        } else {
            type = ParameterValueType.STRING;
        }
        return type;
    }

}
