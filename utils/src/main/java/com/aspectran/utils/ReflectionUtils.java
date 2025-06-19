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
import com.aspectran.utils.annotation.jsr305.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.aspectran.utils.MethodUtils.EMPTY_OBJECT_ARRAY;

/**
 * Simple utility class for working with the reflection API.
 *
 * @since 2.0.0
 */
public abstract class ReflectionUtils {

    /**
     * Algorithm that judges the match between the declared parameter types of
     * a candidate method and a specific list of arguments that this method is
     * supposed to be invoked with.
     * @param paramTypes the parameter types to match
     * @param destArgs the arguments to match
     * @return the accumulated weight for all arguments
     */
    public static float getTypeDifferenceWeight(@NonNull Class<?>[] paramTypes, @NonNull Object[] destArgs) {
        if (paramTypes.length != destArgs.length) {
            return Float.MAX_VALUE;
        }

        float weight = 0.0f;
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> srcClass = paramTypes[i];
            Object destArg = destArgs[i];
            weight += getTypeDifferenceWeight(srcClass, destArg);
            if (weight == Float.MAX_VALUE) {
                break;
            }
        }
        return weight;
    }

    /**
     * Algorithm that judges the match between the declared parameter types of
     * a candidate method and a specific list of arguments that this method is
     * supposed to be invoked with.
     * @param paramType the parameter type to match
     * @param destArg the argument to match
     * @return the type difference weight
     */
    public static float getTypeDifferenceWeight(Class<?> paramType, Object destArg) {
        if (!TypeUtils.isAssignableValue(paramType, destArg)) {
            return Float.MAX_VALUE;
        }
        return getTypeDifferenceWeight(paramType, (destArg != null ? destArg.getClass() : null));
    }

    /**
     * Returns the sum of the object transformation cost for each class in the source
     * argument list.
     * @param srcArgs the source arguments
     * @param destArgs the destination arguments
     * @return the accumulated weight for all arguments
     */
    public static float getTypeDifferenceWeight(@NonNull Class<?>[] srcArgs, @NonNull Class<?>[] destArgs) {
        float weight = 0.0f;
        for (int i = 0; i < srcArgs.length; i++) {
            Class<?> srcClass = srcArgs[i];
            Class<?> destClass = destArgs[i];
            weight += getTypeDifferenceWeight(srcClass, destClass);
            if (weight == Float.MAX_VALUE) {
                break;
            }
        }
        return weight;
    }

    /**
     * Gets the number of steps required needed to turn the source class into the
     * destination class. This represents the number of steps in the object hierarchy
     * graph.
     * @param srcClass the source class
     * @param destClass the destination class
     * @return the cost of transforming an object
     */
    public static float getTypeDifferenceWeight(Class<?> srcClass, Class<?> destClass) {
        if (srcClass == null) {
            return Float.MAX_VALUE;
        }

        if (destClass != null) {
            if (srcClass.isArray() && destClass.isArray()) {
                srcClass = srcClass.getComponentType();
                destClass = destClass.getComponentType();
            }
            if ((destClass.isPrimitive()
                    && srcClass.equals(TypeUtils.getPrimitiveWrapper(destClass)))
                    || (srcClass.isPrimitive()
                    && destClass.equals(TypeUtils.getPrimitiveWrapper(srcClass)))) {
                return 0.1f;
            }
        }

        float weight = 0.0f;
        while (destClass != null) {
            if (destClass.equals(srcClass)) {
                if (destClass.isInterface()) {
                    // slight penalty for interface match.
                    // we still want an exact match to override an interface match, but
                    // an interface match should override anything where we have to get a
                    // superclass.
                    weight += 0.25f;
                }
                break;
            }
            weight++;
            destClass = destClass.getSuperclass();
        }

        /*
         * If the destination class is null, we've travelled all the way up to
         * an Object match. We'll penalize this by adding 1.5 to the cost.
         */
        if (destClass == null) {
            weight += 1.5f;
        }
        return weight;
    }

    /**
     * Converts an array of objects to an array of their primitive types.
     * @param val an array of objects to be converted, may be {@code null}
     * @return an array of their primitive types
     */
    public static Object toPrimitiveArray(Object val) {
        if (val instanceof Boolean[]) {
            int len = Array.getLength(val);
            boolean[] arr = new boolean[len];
            for (int i = 0; i < len; i++) {
                arr[i] = (Boolean)Array.get(val, i);
            }
            return arr;
        } else if (val instanceof Byte[]) {
            int len = Array.getLength(val);
            byte[] arr = new byte[len];
            for (int i = 0; i < len; i++) {
                arr[i] = (Byte)Array.get(val, i);
            }
            return arr;
        } else if (val instanceof Character[]) {
            int len = Array.getLength(val);
            char[] arr = new char[len];
            for (int i = 0; i < len; i++) {
                arr[i] = (Character)Array.get(val, i);
            }
            return arr;
        } else if (val instanceof Short[]) {
            int len = Array.getLength(val);
            short[] arr = new short[len];
            for (int i = 0; i < len; i++) {
                arr[i] = (Short)Array.get(val, i);
            }
            return arr;
        } else if (val instanceof Integer[]) {
            int len = Array.getLength(val);
            int[] arr = new int[len];
            for (int i = 0; i < len; i++) {
                arr[i] = (Integer)Array.get(val, i);
            }
            return arr;
        } else if (val instanceof Long[]) {
            int len = Array.getLength(val);
            long[] arr = new long[len];
            for (int i = 0; i < len; i++) {
                arr[i] = (Long)Array.get(val, i);
            }
            return arr;
        } else if (val instanceof Float[]) {
            int len = Array.getLength(val);
            float[] arr = new float[len];
            for (int i = 0; i < len; i++) {
                arr[i] = (Float)Array.get(val, i);
            }
            return arr;
        } else if (val instanceof Double[]) {
            int len = Array.getLength(val);
            double[] arr = new double[len];
            for (int i = 0; i < len; i++) {
                arr[i] = (Double)Array.get(val, i);
            }
            return arr;
        } else {
            return null;
        }
    }

    /**
     * Converts an array of objects to an array of the specified component type.
     * @param val an array of objects to be converted
     * @param componentType the {@code Class} object representing the component type of the new array
     * @return an array of the objects with the specified component type
     */
    public static Object toComponentTypeArray(Object val, Class<?> componentType) {
        if (val != null) {
            int len = Array.getLength(val);
            Object arr = Array.newInstance(componentType, len);
            for (int i = 0; i < len; i++) {
                Array.set(arr, i, Array.get(val, i));
            }
            return arr;
        } else {
            return null;
        }
    }

    // Field handling

    /**
     * Get the field represented by the supplied {@link Field field object} on the
     * specified {@link Object target object}. In accordance with {@link Field#get(Object)}
     * semantics, the returned value is automatically wrapped if the underlying field
     * has a primitive type.
     * @param field the field to get
     * @param target the target object from which to get the field
     * @return the field's current value
     */
    public static Object getField(@NonNull Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access field: " + field, e);
        }
    }

    /**
     * Set the field represented by the supplied {@link Field field object} on the
     * specified {@link Object target object} to the specified {@code value}.
     * In accordance with {@link Field#set(Object, Object)} semantics, the new value
     * is automatically unwrapped if the underlying field has a primitive type.
     * @param field the field to set
     * @param target the target object on which to set the field
     * @param value the value to set (may be {@code null})
     */
    public static void setField(@NonNull Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access field: " + field, e);
        }
    }

    // Method handling

    /**
     * Invoke the specified {@link Method} against the supplied target object with no arguments.
     * The target object can be {@code null} when invoking a static {@link Method}.
     * @param method the method to invoke
     * @param target the target object to invoke the method on
     * @return the invocation result, if any
     * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
     */
    @Nullable
    public static Object invokeMethod(Method method, @Nullable Object target) {
        return invokeMethod(method, target, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Invoke the specified {@link Method} against the supplied target object with the
     * supplied arguments. The target object can be {@code null} when invoking a
     * static {@link Method}.
     * @param method the method to invoke
     * @param target the target object to invoke the method on
     * @param args the invocation arguments (may be {@code null})
     * @return the invocation result, if any
     */
    @Nullable
    public static Object invokeMethod(@NonNull Method method, @Nullable Object target, Object... args) {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalStateException("Could not access method: " + method,
                ExceptionUtils.getRootCause(e));
        }
    }

}
