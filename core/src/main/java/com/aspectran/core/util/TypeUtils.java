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
package com.aspectran.core.util;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility methods focusing on type inspection, particularly with regard to generics.
 */
public abstract class TypeUtils {

    /**
     * Map with primitive wrapper type as key and corresponding primitive
     * type as value, for example: Integer.class -> int.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<>();

    /**
     * Map with primitive type as key and corresponding wrapper type as value,
     * for example: int.class -> Integer.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap  = new HashMap<>();

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

        for (final Map.Entry<Class<?>, Class<?>> e : primitiveWrapperTypeMap.entrySet()) {
            primitiveTypeToWrapperMap.put(e.getValue(), e.getKey());
        }
    }

    /**
     * Check if the given class represents a primitive wrapper,
     * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper class
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        return primitiveWrapperTypeMap.containsKey(clazz);
    }

    /**
     * Check if the given class represents an array of primitives,
     * i.e. boolean, byte, char, short, int, long, float, or double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive array class
     */
    public static boolean isPrimitiveArray(Class<?> clazz) {
        return (clazz.isArray() && clazz.getComponentType().isPrimitive());
    }

    /**
     * Check if the given class represents an array of primitive wrappers,
     * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper array class
     */
    public static boolean isPrimitiveWrapperArray(Class<?> clazz) {
        return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
    }

    /**
     * Check if the right-hand side type may be assigned to the left-hand side
     * type, assuming setting by reflection. Considers primitive wrapper
     * classes as assignable to the corresponding primitive types.
     *
     * @param lhsType the target type
     * @param rhsType the value type that should be assigned to the target type
     * @return if the target type is assignable from the value type
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
     * Determine if the given type is assignable from the given value,
     * assuming setting by reflection. Considers primitive wrapper classes
     * as assignable to the corresponding primitive types.
     *
     * @param type    the target type
     * @param value the value that should be assigned to the type
     * @return if the type is assignable from the value
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
     * Gets the wrapper object class for the given primitive type class.
     * For example, passing <code>boolean.class</code> returns <code>Boolean.class</code>
     *
     * @param primitiveType the primitive type class for which a match is to be found
     * @return the wrapper type associated with the given primitive or null if no match is found
     */
    public static Class<?> getPrimitiveWrapper(Class<?> primitiveType) {
        return primitiveTypeToWrapperMap.get(primitiveType);
    }

    /**
     * <p>Converts the specified wrapper class to its corresponding primitive
     * class.</p>
     *
     * <p>This method is the counter part of <code>primitiveToWrapper()</code>.
     * If the passed in class is a wrapper class for a primitive type, this
     * primitive type will be returned (e.g. <code>Integer.TYPE</code> for
     * <code>Integer.class</code>). For other classes, or if the parameter is
     * <b>null</b>, the return value is <b>null</b>.</p>
     *
     * @param cls the class to convert, may be <b>null</b>
     * @return the corresponding primitive type if <code>cls</code> is a wrapper class, <b>null</b> otherwise
     */
    public static Class<?> wrapperToPrimitive(Class<?> cls) {
        return primitiveWrapperTypeMap.get(cls);
    }

    /**
     * <p>Converts the specified array of wrapper Class objects to an array of
     * its corresponding primitive Class objects.</p>
     *
     * <p>This method invokes <code>wrapperToPrimitive()</code> for each element
     * of the passed in array.</p>
     *
     * @param classes  the class array to convert, may be null or empty
     * @return an array which contains for each given class, the primitive class or
     * <b>null</b> if the original class is not a wrapper class. <code>null</code> if null input.
     * Empty array if an empty array passed in.
     * @see #wrapperToPrimitive(Class)
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

}
