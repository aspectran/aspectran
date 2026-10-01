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
public class TypeUtils {

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
        primitiveWrapperTypeMap.put(Void.class, void.class);

        for (Map.Entry<Class<?>, Class<?>> e : primitiveWrapperTypeMap.entrySet()) {
            primitiveTypeToWrapperMap.put(e.getValue(), e.getKey());
        }
    }

    /**
     * This class cannot be instantiated.
     */
    private TypeUtils() {
    }

    /**
     * Checks if the given class is a primitive wrapper type.
     * (i.e., {@link Boolean}, {@link Byte}, {@link Character}, {@link Short},
     * {@link Integer}, {@link Long}, {@link Float}, {@link Double}, or {@link Void}).
     * @param clazz the class to check
     * @return {@code true} if the given class is a primitive wrapper class, {@code false} otherwise
     */
    public static boolean isPrimitiveWrapper(@Nullable Class<?> clazz) {
        return (clazz != null && primitiveWrapperTypeMap.containsKey(clazz));
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
    public static boolean isAssignable(@NonNull Class<?> lhsType, @Nullable Class<?> rhsType) {
        Assert.notNull(lhsType, "lhsType must not be null");
        if (rhsType == null) {
            return !lhsType.isPrimitive();
        }
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        if (lhsType.isArray() && rhsType.isArray()) {
            return isAssignable(lhsType.getComponentType(), rhsType.getComponentType());
        }
        if (rhsType.isPrimitive()) {
            return lhsType.equals(getPrimitiveWrapper(rhsType));
        }
        if (lhsType.isPrimitive()) {
            return rhsType.equals(getPrimitiveWrapper(lhsType));
        }
        return false;
    }

    /**
     * Checks if an array of right-hand side types can be assigned to an array of left-hand side types,
     * considering autoboxing.
     * @param lhsTypes the target types (Left-Hand Side)
     * @param rhsTypes the value types (Right-Hand Side)
     * @return {@code true} if all {@code rhsTypes} are assignable to {@code lhsTypes}, {@code false} otherwise
     */
    public static boolean isAssignable(Class<?> @Nullable [] lhsTypes, Class<?> @Nullable [] rhsTypes) {
        if (lhsTypes == null && rhsTypes == null) {
            return true;
        }
        if (lhsTypes == null || rhsTypes == null || lhsTypes.length != rhsTypes.length) {
            return false;
        }
        for (int i = 0; i < lhsTypes.length; i++) {
            if (!isAssignable(lhsTypes[i], rhsTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a given value can be assigned to a given type, considering autoboxing.
     * @param type the target type
     * @param value the value that should be assigned to the type
     * @return {@code true} if the value is assignable to the type, {@code false} otherwise
     */
    public static boolean isAssignableValue(@NonNull Class<?> type, @Nullable Object value) {
        Assert.notNull(type, "type must not be null");
        if (value == null) {
            return !type.isPrimitive();
        }
        if (type.isInstance(value)) {
            return true;
        }
        Class<?> valueType = value.getClass();
        if (type.isArray() && valueType.isArray()) {
            int len = Array.getLength(value);
            if (len == 0) {
                return isAssignable(type.getComponentType(), valueType.getComponentType());
            } else {
                Object first = Array.get(value, 0);
                return isAssignableValue(type.getComponentType(), first);
            }
        }
        if (type.isPrimitive()) {
            return valueType.equals(getPrimitiveWrapper(type));
        }
        return false;
    }

    /**
     * Checks if an array of values can be assigned to an array of types, considering autoboxing.
     * @param types the target types
     * @param values the values to check
     * @return {@code true} if all values are assignable to the corresponding types, {@code false} otherwise
     */
    public static boolean isAssignableValue(Class<?> @Nullable [] types, Object @Nullable [] values) {
        if (types == null && values == null) {
            return true;
        }
        if (types == null || values == null || types.length != values.length) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            if (!isAssignableValue(types[i], values[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the wrapper class for a given primitive type class.
     * For example, passing {@code boolean.class} returns {@code Boolean.class}.
     * @param primitiveType the primitive type class
     * @return the corresponding wrapper class, or {@code null} if the input is not a primitive type
     */
    @Nullable
    public static Class<?> getPrimitiveWrapper(@Nullable Class<?> primitiveType) {
        return (primitiveType != null ? primitiveTypeToWrapperMap.get(primitiveType) : null);
    }

    /**
     * Converts a wrapper class to its corresponding primitive class.
     * <p>For example, passing {@code Integer.class} returns {@code int.class} (i.e., {@code Integer.TYPE}).</p>
     * @param cls the wrapper class to convert (may be {@code null})
     * @return the corresponding primitive type, or {@code null} if the input is not a wrapper class
     */
    @Nullable
    public static Class<?> wrapperToPrimitive(@Nullable Class<?> cls) {
        return (cls != null ? primitiveWrapperTypeMap.get(cls) : null);
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
