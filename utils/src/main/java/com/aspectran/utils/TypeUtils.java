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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for type inspection, particularly with regard to primitives and wrappers.
 */
public abstract class TypeUtils {

    /**
     * A map with primitive wrapper types as keys and corresponding primitive types as values.
     * For example: {@code Integer.class -> int.class}.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<>(32);

    /**
     * A map with primitive types as keys and corresponding wrapper types as values.
     * For example: {@code int.class -> Integer.class}.
     */
    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap  = new HashMap<>(32);

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Boolean[].class, boolean[].class);
        primitiveWrapperTypeMap.put(Byte[].class, byte[].class);
        primitiveWrapperTypeMap.put(Character[].class, char[].class);
        primitiveWrapperTypeMap.put(Short[].class, short[].class);
        primitiveWrapperTypeMap.put(Integer[].class, int[].class);
        primitiveWrapperTypeMap.put(Long[].class, long[].class);
        primitiveWrapperTypeMap.put(Float[].class, float[].class);
        primitiveWrapperTypeMap.put(Double[].class, double[].class);
        primitiveWrapperTypeMap.put(Void.TYPE, void.class);

        for (Map.Entry<Class<?>, Class<?>> e : primitiveWrapperTypeMap.entrySet()) {
            primitiveTypeToWrapperMap.put(e.getValue(), e.getKey());
        }
    }

    /**
     * Checks if the given class is a primitive wrapper type.
     * (i.e., {@link Boolean}, {@link Byte}, {@link Character}, {@link Short},
     * {@link Integer}, {@link Long}, {@link Float}, or {@link Double}).
     * @param clazz the class to check
     * @return {@code true} if the given class is a primitive wrapper class, {@code false} otherwise
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        return primitiveWrapperTypeMap.containsKey(clazz);
    }

    /**
     * Checks if the given class represents an array of primitives.
     * (i.e., {@code boolean[]}, {@code byte[]}, {@code char[]}, etc.)
     * @param clazz the class to check
     * @return {@code true} if the given class is a primitive array class, {@code false} otherwise
     */
    public static boolean isPrimitiveArray(@NonNull Class<?> clazz) {
        return (clazz.isArray() && clazz.getComponentType().isPrimitive());
    }

    /**
     * Checks if the given class represents an array of primitive wrappers.
     * (i.e., {@code Boolean[]}, {@code Byte[]}, {@code Character[]}, etc.)
     * @param clazz the class to check
     * @return {@code true} if the given class is a primitive wrapper array class, {@code false} otherwise
     */
    public static boolean isPrimitiveWrapperArray(@NonNull Class<?> clazz) {
        return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
    }

    /**
     * Checks if the right-hand side type can be assigned to the left-hand side type,
     * considering autoboxing. This method is useful for reflection-based assignments.
     * @param lhsType the target type (Left-Hand Side)
     * @param rhsType the value type (Right-Hand Side) that should be assigned to the target type
     * @return {@code true} if {@code rhsType} is assignable to {@code lhsType}, {@code false} otherwise
     */
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        if (rhsType == null) {
            return !lhsType.isPrimitive();
        }
        if (lhsType.isArray() || rhsType.isArray()) {
            if ((lhsType.isArray() && rhsType.isArray())) {
                return isAssignable(lhsType.getComponentType(), rhsType.getComponentType());
            }
        } else {
            if (lhsType.isAssignableFrom(rhsType)) {
                return true;
            }
            if (rhsType.isPrimitive() && !lhsType.isPrimitive() && lhsType.equals(getPrimitiveWrapper(rhsType))) {
                return true;
            }
            if (lhsType.isPrimitive() && !rhsType.isPrimitive() && rhsType.equals(getPrimitiveWrapper(lhsType))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a given value can be assigned to a given type, considering autoboxing.
     * @param type the target type
     * @param value the value that should be assigned to the type
     * @return {@code true} if the value is assignable to the type, {@code false} otherwise
     */
    public static boolean isAssignableValue(Class<?> type, Object value) {
        if (value == null) {
            return !type.isPrimitive();
        }
        Class<?> valueType = value.getClass();
        if (type.isArray() || valueType.isArray()) {
            if ((type.isArray() && valueType.isArray())) {
                int len = Array.getLength(value);
                if (len == 0) {
                    return true;
                } else {
                    Object first = Array.get(value, 0);
                    return isAssignableValue(type.getComponentType(), first);
                }
            }
        } else {
            if (type.isInstance(value)) {
                return true;
            }
            if (valueType.isPrimitive() && !type.isPrimitive() && type.equals(getPrimitiveWrapper(valueType))) {
                return true;
            }
            if (type.isPrimitive() && !valueType.isPrimitive() && valueType.equals(getPrimitiveWrapper(type))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the wrapper class for a given primitive type class.
     * For example, passing {@code boolean.class} returns {@code Boolean.class}.
     * @param primitiveType the primitive type class
     * @return the corresponding wrapper class, or {@code null} if the input is not a primitive type
     */
    public static Class<?> getPrimitiveWrapper(Class<?> primitiveType) {
        return primitiveTypeToWrapperMap.get(primitiveType);
    }

    /**
     * Converts a wrapper class to its corresponding primitive class.
     * <p>For example, passing {@code Integer.class} returns {@code int.class} (i.e., {@code Integer.TYPE}).</p>
     * @param cls the wrapper class to convert (may be {@code null})
     * @return the corresponding primitive type, or {@code null} if the input is not a wrapper class
     */
    public static Class<?> wrapperToPrimitive(Class<?> cls) {
        return primitiveWrapperTypeMap.get(cls);
    }

    /**
     * Converts an array of wrapper {@code Class} objects to an array of their corresponding primitive types.
     * <p>This method invokes {@link #wrapperToPrimitive(Class)} for each element of the input array.</p>
     * @param classes the array of wrapper classes to convert (may be {@code null})
     * @return an array containing the corresponding primitive types. If a class in the input array
     *      is not a wrapper type, the corresponding element in the output array will be {@code null}.
     *      Returns {@code null} for a {@code null} input, and an empty array for an empty input.
     */
    public static Class<?>[] wrappersToPrimitives(Class<?>[] classes) {
        if (classes == null) {
            return null;
        }
        if (classes.length == 0) {
            return classes;
        }

        Class<?>[] convertedClasses = new Class<?>[classes.length];
        for (int i = 0; i < classes.length; i++) {
            convertedClasses[i] = wrapperToPrimitive(classes[i]);
        }
        return convertedClasses;
    }

    /**
     * Returns the default value for a given primitive type.
     * @param type the primitive type class
     * @return the default value, or {@code null} if the type is not a primitive
     */
    @Nullable
    public static Object getPrimitiveDefaultValue(@NonNull Class<?> type) {
        if (boolean.class == type) {
            return false;
        } else if (char.class == type) {
            return '\u0000';
        } else if (byte.class == type) {
            return (byte)0;
        } else if (short.class == type) {
            return (short)0;
        } else if (int.class == type) {
            return 0;
        } else if (long.class == type) {
            return 0L;
        } else if (float.class == type) {
            return 0.0f;
        } else if (double.class == type) {
            return 0.0d;
        } else {
            return null;
        }
    }

}
