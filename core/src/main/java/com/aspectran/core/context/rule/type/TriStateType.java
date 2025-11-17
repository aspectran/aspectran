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
 * The enum for tri-state boolean type.
 */
public enum TriStateType {

    /** true */
    TRUE,

    /** false */
    FALSE,

    /** unset */
    UNSET;

    /**
     * Returns a {@code Boolean} object representing this TriStateType.
     * Returns {@code Boolean.TRUE} if the state is {@code TRUE},
     * {@code Boolean.FALSE} if the state is {@code FALSE},
     * and {@code null} if the state is {@code UNSET}.
     * @return a {@code Boolean} object, or {@code null} if {@code UNSET}
     */
    @Nullable
    public Boolean toBoolean() {
        return switch (this) {
            case TRUE -> Boolean.TRUE;
            case FALSE -> Boolean.FALSE;
            default -> null;
        };
    }

    /**
     * Returns the boolean value of this TriStateType.
     * @param defaultValue the default value to return if this state is {@code UNSET}
     * @return the boolean value
     */
    public boolean booleanValue(boolean defaultValue) {
        if (this == TRUE) {
            return true;
        } else if (this == FALSE) {
            return false;
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns a {@code TriStateType} with a value represented
     * by the specified {@code boolean}.
     * @param bool the boolean value
     * @return a {@code TriStateType}
     */
    public static TriStateType of(boolean bool) {
        return (bool ? TRUE : FALSE);
    }

}
