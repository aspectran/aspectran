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
 * <p>Provides convenience methods for invoking methods, accessing fields,
 * and determining type compatibility for method resolution.</p>
 *
 * @since 2.0.0
 */
public abstract class ReflectionUtils {

    /**
     * Calculates a weight that represents the cost of converting a given set of argument
     * types to a candidate method's parameter types. Lower weights indicate a better match.
     * <p>This is used for method overloading resolution to find the most specific method.</p>
     * @param paramTypes the parameter types of the candidate method
     * @param destArgs the arguments to be passed to the method
     * @return the accumulated weight for all arguments; {@link Float#MAX_VALUE} if not assignable
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
     * Calculates a weight for a single argument-to-parameter conversion.
     * @param paramType the parameter type of the candidate method
     * @param destArg the argument to be passed
     * @return the type difference weight; {@link Float#MAX_VALUE} if not assignable
     */
    public static float getTypeDifferenceWeight(Class<?> paramType, Object destArg) {
        if (!TypeUtils.isAssignableValue(paramType, destArg)) {
            return Float.MAX_VALUE;
        }
        return getTypeDifferenceWeight(paramType, (destArg != null ? destArg.getClass() : null));
    }

    /**
     * Calculates the total weight for converting an array of source classes to an array of destination classes.
     * @param srcArgs the source class types
     * @param destArgs the destination class types
     * @return the accumulated weight for all type conversions
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
     * Gets the number of steps required to turn the source class into the destination class.
     * This represents the number of steps in the object hierarchy graph. A smaller number
     * indicates a more specific match.
     * <ul>
     *     <li>Primitive-to-wrapper conversion has a small penalty (0.1f).</li>
     *     <li>Interface matches have a small penalty (0.25f) to prefer direct class matches.</li>
     *     <li>Superclass matches increment the weight by 1 for each level.</li>
     *     <li>Object match has a large penalty (1.5f).</li>
     * </ul>
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
     * Converts an array of wrapper objects to an array of their corresponding primitive types.
     * For example, an {@code Integer[]} will be converted to an {@code int[]}.
     * @param val an array of wrapper objects to be converted (may be {@code null})
     * @return an array of primitive types, or {@code null} if the input is not a known wrapper array
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
     * Converts an array of objects to a new array of the specified component type.
     * @param val an array of objects to be converted (may be {@code null})
     * @param componentType the {@code Class} object representing the component type of the new array
     * @return a new array with the specified component type, or {@code null} if the input is {@code null}
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
     * Gets the value of a field on a target object. This method handles
     * {@link IllegalAccessException} by throwing an {@link IllegalStateException}.
     * @param field the field to get, must not be null
     * @param target the target object from which to get the field; can be {@code null} for static fields
     * @return the field's current value
     * @throws IllegalStateException if the field is not accessible
     */
    public static Object getField(@NonNull Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access field: " + field, e);
        }
    }

    /**
     * Sets the value of a field on a target object. This method handles
     * {@link IllegalAccessException} by throwing an {@link IllegalStateException}.
     * @param field the field to set, must not be null
     * @param target the target object on which to set the field; can be {@code null} for static fields
     * @param value the value to set (may be {@code null})
     * @throws IllegalStateException if the field is not accessible
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
     * Invokes the specified {@link Method} against the supplied target object with no arguments.
     * The target object can be {@code null} when invoking a static {@link Method}.
     * <p>This method wraps {@link InvocationTargetException} and {@link IllegalAccessException}
     * in an {@link IllegalStateException}.</p>
     * @param method the method to invoke
     * @param target the target object to invoke the method on
     * @return the invocation result, or {@code null} if the method has a void return type
     * @throws IllegalStateException if the method is not accessible or if the underlying method throws an exception
     * @see #invokeMethod(java.lang.reflect.Method, Object, Object[])
     */
    @Nullable
    public static Object invokeMethod(Method method, @Nullable Object target) {
        return invokeMethod(method, target, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Invokes the specified {@link Method} against the supplied target object with the
     * supplied arguments. The target object can be {@code null} when invoking a
     * static {@link Method}.
     * <p>This method wraps {@link InvocationTargetException} and {@link IllegalAccessException}
     * in an {@link IllegalStateException}.</p>
     * @param method the method to invoke
     * @param target the target object to invoke the method on
     * @param args the invocation arguments (may be {@code null})
     * @return the invocation result, or {@code null} if the method has a void return type
     * @throws IllegalStateException if the method is not accessible or if the underlying method throws an exception
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
