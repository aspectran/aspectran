/**
 * Copyright 2008-2016 Juho Jeong
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Simple utility class for working with the reflection API.
 *
 * @since 2.0.0
 * @author Juho.jeong
 */
public class ReflectionUtils {

	/**
	 * Set the field represented by the supplied {@link Field field object} on the
	 * specified {@link Object target object} to the specified {@code value}.
	 * In accordance with {@link Field#set(Object, Object)} semantics, the new value
	 * is automatically unwrapped if the underlying field has a primitive type.
	 * 
	 * @param field the field to set
	 * @param target the target object on which to set the field
	 * @param value the value to set (may be {@code null})
	 */
	public static void setField(Field field, Object target, Object value) {
		try {
			boolean accessibled = makeAccessible(field);
			field.set(target, value);
			if(accessibled)
				field.setAccessible(false);
		} catch(IllegalAccessException e) {
			throw new IllegalStateException("Could not access field: " + e.getMessage());
		}
	}

	/**
	 * Invoke the specified {@link Method} against the supplied target object with the
	 * supplied arguments. The target object can be {@code null} when invoking a
	 * static {@link Method}.
	 *
	 * @param method the method to invoke
	 * @param target the target object to invoke the method on
	 * @param args the invocation arguments (may be {@code null})
	 * @return the invocation result, if any
	 */
	public static Object invokeMethod(Method method, Object target, Object... args) {
		try {
			return method.invoke(target, args);
		} catch(InvocationTargetException | IllegalAccessException e) {
			throw new IllegalStateException("Could not access method: " + e.getMessage());
		}
	}
	
	/**
	 * Make the given field accessible, explicitly setting it accessible if
	 * necessary. The {@code setAccessible(true)} method is only called when
	 * actually necessary, to avoid unnecessary conflicts with a JVM
	 * SecurityManager (if active).
	 *
	 * @param field the field to make accessible
	 * @return true, if successful
	 * @see java.lang.reflect.Field#setAccessible
	 */
	public static boolean makeAccessible(Field field) {
		if((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())
				|| Modifier.isFinal(field.getModifiers())) && !field.isAccessible()) {
			field.setAccessible(true);
			return true;
		}
		return false;
	}

	/**
	 * Make the given method accessible, explicitly setting it accessible if
	 * necessary. The {@code setAccessible(true)} method is only called when
	 * actually necessary, to avoid unnecessary conflicts with a JVM
	 * SecurityManager (if active).
	 *
	 * @param method the method to make accessible
	 * @return true, if successful
	 * @see java.lang.reflect.Method#setAccessible
	 */
	public static boolean makeAccessible(Method method) {
		if((!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
				&& !method.isAccessible()) {
			method.setAccessible(true);
			return true;
		}
		return false;
	}

	public static float getTypeDifferenceWeight(Class<?>[] srcArgs, Object[] destArgs) {
		if(srcArgs.length != destArgs.length)
			return Float.MAX_VALUE;

		float weight = 0.0f;

		for(int i = 0; i < srcArgs.length; i++) {
			Class<?> srcClass = srcArgs[i];
			Object destObject = destArgs[i];
			weight += getTypeDifferenceWeight(srcClass, destObject);

			if(weight == Float.MAX_VALUE)
				break;
		}

		return weight;
	}

	public static float getTypeDifferenceWeight(Class<?> srcClass, Object destObject) {
		if(!ClassUtils.isAssignableValue(srcClass, destObject))
			return Float.MAX_VALUE;

		return getTypeDifferenceWeight(srcClass, destObject.getClass());
	}

	/**
	 * Returns the sum of the object transformation cost for each class in the source
	 * argument list.
	 * @param srcArgs The source arguments
	 * @param destArgs The destination arguments
	 * @return the accumulated weight for all arguments
	 */
	public static float getTypeDifferenceWeight(Class<?>[] srcArgs, Class<?>[] destArgs) {
		float weight = 0.0f;

		for(int i = 0; i < srcArgs.length; i++) {
			Class<?> srcClass = srcArgs[i];
			Class<?> destClass = destArgs[i];
			weight += getTypeDifferenceWeight(srcClass, destClass);
		}

		return weight;
	}

	/**
	 * Gets the number of steps required needed to turn the source class into the
	 * destination class. This represents the number of steps in the object hierarchy
	 * graph.
	 * @param srcClass The source class
	 * @param destClass The destination class
	 * @return The cost of transforming an object
	 */
	public static float getTypeDifferenceWeight(Class<?> srcClass, Class<?> destClass) {
		if(destClass != null) {
			if(destClass.isPrimitive() && srcClass.equals(ClassUtils.getPrimitiveWrapper(destClass)) ||
					srcClass.isPrimitive() && destClass.equals(ClassUtils.getPrimitiveWrapper(srcClass))) {
				return 0.5f;
			}
			if(srcClass.isArray() && destClass.isArray()) {
				if(destClass.getComponentType().isPrimitive() && srcClass.equals(ClassUtils.getPrimitiveWrapper(destClass)) ||
						srcClass.getComponentType().isPrimitive() && destClass.equals(ClassUtils.getPrimitiveWrapper(srcClass))) {
					return 0.75f;
				}
			}
		}

		float weight = 0.0f;

		while(destClass != null && !destClass.equals(srcClass)) {
			if(destClass.isInterface() && destClass.equals(srcClass)) {
				// slight penalty for interface match.
				// we still want an exact match to override an interface match, but
				// an interface match should override anything where we have to get a
				// superclass.
				weight += 0.25f;
				break;
			}
			weight++;
			destClass = destClass.getSuperclass();
		}

		/*
		 * If the destination class is null, we've travelled all the way up to
		 * an Object match. We'll penalize this by adding 1.5 to the cost.
		 */
		if(destClass == null) {
			weight += 1.5f;
		}

		return weight;
	}

	public static Object toPrimitiveArray(Object val) {
		int len = Array.getLength(val);

		if(val instanceof Boolean[]) {
			boolean[] arr = new boolean[len];
			for(int i = 0; i < len; i++) {
				arr[i] = (Boolean)Array.get(val, i);
			}
			return arr;
		} else if(val instanceof Byte[]) {
			byte[] arr = new byte[len];
			for(int i = 0; i < len; i++) {
				arr[i] = (Byte)Array.get(val, i);
			}
			return arr;
		} else if(val instanceof Character[]) {
			char[] arr = new char[len];
			for(int i = 0; i < len; i++) {
				arr[i] = (Character)Array.get(val, i);
			}
			return arr;
		} else if(val instanceof Short[]) {
			short[] arr = new short[len];
			for(int i = 0; i < len; i++) {
				arr[i] = (Short)Array.get(val, i);
			}
			return arr;
		} else if(val instanceof Integer[]) {
			int[] arr = new int[len];
			for(int i = 0; i < len; i++) {
				arr[i] = (Integer)Array.get(val, i);
			}
			return arr;
		} else if(val instanceof Long[]) {
			long[] arr = new long[len];
			for(int i = 0; i < len; i++) {
				arr[i] = (Long)Array.get(val, i);
			}
			return arr;
		} else if(val instanceof Float[]) {
			float[] arr = new float[len];
			for(int i = 0; i < len; i++) {
				arr[i] = (Float)Array.get(val, i);
			}
			return arr;
		} else if(val instanceof Double[]) {
			double[] arr = new double[len];
			for(int i = 0; i < len; i++) {
				arr[i] = (Double)Array.get(val, i);
			}
			return arr;
		}

		return null;
	}

}
