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
package com.aspectran.utils;

import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * <p>This class provides utility methods for handling {@link Boolean} objects and boolean primitives.
 * It offers convenient methods for converting strings to booleans and for handling null {@code Boolean} objects.</p>
 *
 * <p>This class tries to handle {@code null} input gracefully.
 * An exception will not be thrown for a {@code null} input.
 * Each method documents its behavior in more detail.</p>
 *
 * @author Juho Jeong
 */
public abstract class BooleanUtils {

    /**
     * Converts a String to a {@code Boolean}.
     * <p>If the string is {@code "true"} (case-insensitive), {@code true} is returned.
     * Otherwise, {@code false} is returned.</p>
     *
     * @param booleanString the String to convert
     * @return the Boolean value of the string, not {@code null}
     */
    @NonNull
    public static Boolean toBooleanObject(String booleanString) {
        return Boolean.valueOf(booleanString);
    }

    /**
     * Converts a String to a {@code Boolean}, returning {@code null} if the string is {@code null}.
     * <p>If the string is {@code "true"} (case-insensitive), {@code true} is returned.
     * Otherwise, {@code false} is returned.</p>
     *
     * @param booleanString the String to convert
     * @return the Boolean value of the string, or {@code null} if the string is {@code null}
     */
    public static Boolean toNullableBooleanObject(String booleanString) {
        return (booleanString != null ? Boolean.valueOf(booleanString) : null);
    }

    /**
     * Converts a {@code Boolean} to a {@code boolean}, handling {@code null} by returning {@code false}.
     *
     * @param bool the boolean to convert
     * @return {@code true} or {@code false}
     */
    public static boolean toBoolean(Boolean bool) {
        return toBoolean(bool, false);
    }

    /**
     * Converts a {@code Boolean} to a {@code boolean}, handling {@code null} by returning a default value.
     *
     * @param bool the boolean to convert
     * @param defaultValue the default value to return if the input is {@code null}
     * @return {@code true} or {@code false}
     */
    public static boolean toBoolean(Boolean bool, boolean defaultValue) {
        return (bool != null ? bool : defaultValue);
    }

    /**
     * Converts a String to a {@code boolean}, handling {@code null} by returning a default value.
     *
     * @param booleanString the string to convert
     * @param defaultValue the default value to return if the string is {@code null}
     * @return {@code true} or {@code false}
     */
    public static boolean toBoolean(String booleanString, boolean defaultValue) {
        return toBoolean(toNullableBooleanObject(booleanString), defaultValue);
    }

}
