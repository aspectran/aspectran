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
 * Enumeration of APON value types and helpers to resolve them from hints and runtime values.
 * <p>
 * Each constant corresponds to a logical value category in APON and, by convention,
 * maps to a representative Java type:
 * <ul>
 *   <li>STRING – java.lang.String for single-line text</li>
 *   <li>TEXT – java.lang.String for multi-line text (contains a newline)</li>
 *   <li>INT – java.lang.Integer</li>
 *   <li>LONG – java.lang.Long</li>
 *   <li>FLOAT – java.lang.Float</li>
 *   <li>DOUBLE – java.lang.Double</li>
 *   <li>BOOLEAN – java.lang.Boolean</li>
 *   <li>OBJECT – arbitrary object without a specific APON type</li>
 *   <li>VARIABLE – type decided at runtime from the actual value</li>
 *   <li>PARAMETERS – nested parameter block ({@link Parameters})</li>
 * </ul>
 * The lowercase {@code alias} of each constant is what appears in APON text as a type hint,
 * e.g. {@code name(int): 123}.
 * </p>
 */
public enum ValueType {

    /** Single-line string value. */
    STRING("string"),
    /** Multi-line string value (text). */
    TEXT("text"),
    /** 32-bit integer value. */
    INT("int"),
    /** 64-bit integer value. */
    LONG("long"),
    /** 32-bit floating point value. */
    FLOAT("float"),
    /** 64-bit floating point value. */
    DOUBLE("double"),
    /** Boolean value (true/false). */
    BOOLEAN("boolean"),
    /** Arbitrary object without a specific APON type. */
    OBJECT("object"),
    /** Runtime-decided type when no explicit type is declared. */
    VARIABLE("variable"),
    /** Nested parameter block value. */
    PARAMETERS("parameters");

    /** Lowercase alias used in APON text for this type. */
    private final String alias;

    /**
     * Construct a value type with the given APON alias.
     * @param alias the lowercase alias used in APON text
     */
    ValueType(String alias) {
        this.alias = alias;
    }

    /**
     * Return the APON alias (lowercase) for this type.
     */
    @Override
    public String toString() {
        return this.alias;
    }

    /**
     * Resolve a {@link ValueType} from its APON alias string (e.g., "int").
     * @param alias the lowercase alias as it appears in APON text
     * @return the corresponding {@link ValueType}, or {@code null} if unknown
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

    /**
     * Extract and resolve a type hint embedded in a parameter name, e.g.,
     * {@code name(int)} -> {@link #INT}.
     * @param name the parameter name possibly containing a type hint
     * @return the hinted {@link ValueType}, or {@code null} if no valid hint
     */
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

    /**
     * Remove any type hint suffix from the parameter name.
     * For example, {@code stripHint("count(int)") == "count"}.
     * @param name the original parameter name
     * @return the name without a trailing type hint
     */
    @NonNull
    public static String stripHint(@NonNull String name) {
        int hintStartIndex = name.indexOf(AponFormat.ROUND_BRACKET_OPEN);
        if (hintStartIndex > 0) {
            return name.substring(0, hintStartIndex);
        }
        return name;
    }

    /**
     * Infer a {@link ValueType} from a runtime object instance.
     * Strings containing a newline are treated as {@link #TEXT}.
     * @param value the runtime value
     * @return the inferred value type (never {@code null})
     */
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
