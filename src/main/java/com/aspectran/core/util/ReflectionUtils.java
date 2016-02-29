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
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple utility class for working with the reflection API.
 *
 * @since 2.0.0
 * @author Juho.jeong
 */
public class ReflectionUtils {

	/**
	 * Map with primitive wrapper type as key and corresponding primitive
	 * type as value, for example: Integer.class -> int.class.
	 */
	private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new HashMap<Class<?>, Class<?>>(17);

	/**
	 * Map with primitive type as key and corresponding wrapper type as value,
	 * for example: int.class -> Integer.class.
	 * */
	private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap  = new HashMap<Class<?>, Class<?>>(17);

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

		primitiveTypeToWrapperMap.put(boolean.class, Boolean.class);
		primitiveTypeToWrapperMap.put(byte.class, Byte.class);
		primitiveTypeToWrapperMap.put(char.class, Character.class);
		primitiveTypeToWrapperMap.put(short.class, Short.class);
		primitiveTypeToWrapperMap.put(int.class, Integer.class);
		primitiveTypeToWrapperMap.put(long.class, Long.class);
		primitiveTypeToWrapperMap.put(float.class, Float.class);
		primitiveTypeToWrapperMap.put(double.class, Double.class);
		primitiveTypeToWrapperMap.put(boolean[].class, Boolean[].class);
		primitiveTypeToWrapperMap.put(byte[].class, Byte[].class);
		primitiveTypeToWrapperMap.put(char[].class, Character[].class);
		primitiveTypeToWrapperMap.put(short[].class, Short[].class);
		primitiveTypeToWrapperMap.put(int[].class, Integer[].class);
		primitiveTypeToWrapperMap.put(long[].class, Long[].class);
		primitiveTypeToWrapperMap.put(float[].class, Float[].class);
		primitiveTypeToWrapperMap.put(double[].class, Double[].class);
		primitiveTypeToWrapperMap.put(void.class, Void.TYPE);
	}

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
		} catch(InvocationTargetException e) {
			throw new IllegalStateException("Could not access method: " + e.getMessage());
		} catch(IllegalAccessException e) {
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

	/**
	 * Check if the given class represents a primitive wrapper,
	 * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
	 * @param clazz the class to check
	 * @return whether the given class is a primitive wrapper class
	 */
	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		return primitiveWrapperTypeMap.containsKey(clazz);
	}

	/**
	 * Check if the given class represents a primitive (i.e. boolean, byte,
	 * char, short, int, long, float, or double) or a primitive wrapper
	 * (i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double).
	 * @param clazz the class to check
	 * @return whether the given class is a primitive or primitive wrapper class
	 */
	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
	}

	/**
	 * Check if the given class represents an array of primitives,
	 * i.e. boolean, byte, char, short, int, long, float, or double.
	 * @param clazz the class to check
	 * @return whether the given class is a primitive array class
	 */
	public static boolean isPrimitiveArray(Class<?> clazz) {
		return (clazz.isArray() && clazz.getComponentType().isPrimitive());
	}

	/**
	 * Check if the given class represents an array of primitive wrappers,
	 * i.e. Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
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
	 * @param lhsType the target type
	 * @param rhsType the value type that should be assigned to the target type
	 * @return if the target type is assignable from the value type
	 */
	public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
		if(rhsType == null) {
			if(!lhsType.isPrimitive())
				return true;
		} else {
			if(lhsType.isAssignableFrom(rhsType))
				return true;

			if(rhsType.isPrimitive() && lhsType.equals(getPrimitiveWrapper(rhsType)))
				return true;

			if(lhsType.isPrimitive() && rhsType.equals(getPrimitiveWrapper(lhsType)))
				return true;

			if(lhsType.isArray() && rhsType.isArray()) {
				if(rhsType.getComponentType().isPrimitive() && lhsType.equals(getPrimitiveWrapper(rhsType)))
					return true;

				if(lhsType.getComponentType().isPrimitive() && rhsType.equals(getPrimitiveWrapper(lhsType)))
					return true;
			}
		}

		return false;
	}

	/**
	 * Determine if the given type is assignable from the given value,
	 * assuming setting by reflection. Considers primitive wrapper classes
	 * as assignable to the corresponding primitive types.
	 * @param type	the target type
	 * @param value the value that should be assigned to the type
	 * @return if the type is assignable from the value
	 */
	public static boolean isAssignableValue(Class<?> type, Object value) {
		return (value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive());
	}

	/**
	 * Gets the wrapper object class for the given primitive type class.
	 * For example, passing <code>boolean.class</code> returns <code>Boolean.class</code>
	 * @param primitiveType the primitive type class for which a match is to be found
	 * @return the wrapper type associated with the given primitive
	 * or null if no match is found
	 */
	public static Class<?> getPrimitiveWrapper(Class<?> primitiveType) {
		return primitiveTypeToWrapperMap.get(primitiveType);
	}

	/**
	 * Find a non primitive representation for given primitive class.
	 *
	 * @param clazz the class to find a representation for, not null
	 * @return the original class if it not a primitive. Otherwise the wrapper class. Not null
	 */
	public static Class<?> toNonPrimitiveClass(Class<?> clazz) {
		if(clazz.isPrimitive()) {
			Class<?> primitiveClass = getPrimitiveWrapper(clazz);
			// the above method returns
			if(primitiveClass != null) {
				return primitiveClass;
			} else {
				return clazz;
			}
		} else {
			return clazz;
		}
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
	 * @return the corresponding primitive type if <code>cls</code> is a
	 * wrapper class, <b>null</b> otherwise
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
		for(int i = 0; i < classes.length; i++) {
			convertedClasses[i] = wrapperToPrimitive(classes[i]);
		}
		return convertedClasses;
	}

	/**
	 * Convert a "/"-based resource path to a "."-based fully qualified class name.
	 * @param resourcePath the resource path pointing to a class
	 * @return the corresponding fully qualified class name
	 */
	public static String convertResourcePathToClassName(String resourcePath) {
		return resourcePath.replace('/', '.');
	}

	/**
	 * Convert a "."-based fully qualified class name to a "/"-based resource path.
	 * @param className the fully qualified class name
	 * @return the corresponding resource path, pointing to the class
	 */
	public static String convertClassNameToResourcePath(String className) {
		return className.replace('.', '/');
	}

	/**
	 * Return a path suitable for use with <code>ClassLoader.getResource</code>
	 * (also suitable for use with <code>Class.getResource</code> by prepending a
	 * slash ('/') to the return value. Built by taking the package of the specified
	 * class file, converting all dots ('.') to slashes ('/'), adding a trailing slash
	 * if necesssary, and concatenating the specified resource name to this.
	 * @param clazz	the Class&lt;?&gt; whose package will be used as the base
	 * @param resourceName the resource name to append. A leading slash is optional.
	 * @return the built-up resource path
	 * @see java.lang.ClassLoader#getResource
	 * @see java.lang.Class#getResource
	 */
	public static String addResourcePathToPackagePath(Class<?> clazz, String resourceName) {
		if(!resourceName.startsWith("/")) {
			return classPackageAsResourcePath(clazz) + "/" + resourceName;
		}
		return classPackageAsResourcePath(clazz) + resourceName;
	}

	/**
	 * Given an input class object, return a string which consists of the
	 * class's package name as a pathname, i.e., all dots ('.') are replaced by
	 * slashes ('/'). Neither a leading nor trailing slash is added. The result
	 * could be concatenated with a slash and the name of a resource, and fed
	 * directly to <code>ClassLoader.getResource()</code>. For it to be fed to
	 * <code>Class.getResource</code> instead, a leading slash would also have
	 * to be prepended to the returned value.
	 * @param clazz the input class. A <code>null</code> value or the default
	 * (empty) package will result in an empty string ("") being returned.
	 * @return a path which represents the package name
	 * @see ClassLoader#getResource
	 * @see Class#getResource
	 */
	public static String classPackageAsResourcePath(Class<?> clazz) {
		if(clazz == null) {
			return "";
		}
		String className = clazz.getName();
		int packageEndIndex = className.lastIndexOf('.');
		if(packageEndIndex == -1) {
			return "";
		}
		String packageName = className.substring(0, packageEndIndex);
		return packageName.replace('.', '/');
	}

	/**
	 * Build a String that consists of the names of the classes/interfaces
	 * in the given array.
	 * <p>Basically like <code>AbstractCollection.toString()</code>, but stripping
	 * the "class "/"interface " prefix before every class name.
	 * @param classes a Collection of Class&lt;?&gt; objects (may be <code>null</code>)
	 * @return a String of form "[com.foo.Bar, com.foo.Baz]"
	 * @see java.util.AbstractCollection#toString()
	 */
	public static String classNamesToString(Class<?>[] classes) {
		return classNamesToString(Arrays.asList(classes));
	}

	/**
	 * Build a String that consists of the names of the classes/interfaces
	 * in the given collection.
	 * <p>Basically like <code>AbstractCollection.toString()</code>, but stripping
	 * the "class "/"interface " prefix before every class name.
	 * @param classes a Collection of Class&lt;?&gt; objects (may be <code>null</code>)
	 * @return a String of form "[com.foo.Bar, com.foo.Baz]"
	 * @see java.util.AbstractCollection#toString()
	 */
	public static String classNamesToString(Collection<?> classes) {
		if(classes == null || classes.isEmpty()) {
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for(Iterator<?> it = classes.iterator(); it.hasNext();) {
			Class<?> clazz = (Class<?>)it.next();
			sb.append(clazz.getName());
			if(it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append("]");
		return sb.toString();
	}

	/**
	 * Return all interfaces that the given instance implements as array,
	 * including ones implemented by superclasses.
	 * @param instance the instance to analyse for interfaces
	 * @return all interfaces that the given instance implements as array
	 */
	public static Class<?>[] getAllInterfaces(Object instance) {
		return getAllInterfacesForClass(instance.getClass());
	}

	/**
	 * Return all interfaces that the given class implements as array,
	 * including ones implemented by superclasses.
	 * <p>If the class itself is an interface, it gets returned as sole interface.
	 * @param clazz the class to analyse for interfaces
	 * @return all interfaces that the given object implements as array
	 */
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
		return getAllInterfacesForClass(clazz, null);
	}

	/**
	 * Return all interfaces that the given class implements as array,
	 * including ones implemented by superclasses.
	 * <p>If the class itself is an interface, it gets returned as sole interface.
	 * @param clazz the class to analyse for interfaces
	 * @param classLoader the ClassLoader that the interfaces need to be visible in
	 * (may be <code>null</code> when accepting all declared interfaces)
	 * @return all interfaces that the given object implements as array
	 */
	public static Class<?>[] getAllInterfacesForClass(Class<?> clazz, ClassLoader classLoader) {
		if(clazz.isInterface()) {
			return new Class<?>[] { clazz };
		}
		List<Class<?>> interfaces = new ArrayList<Class<?>>();
		while(clazz != null) {
			for(int i = 0; i < clazz.getInterfaces().length; i++) {
				Class<?> ifc = clazz.getInterfaces()[i];
				if(!interfaces.contains(ifc) && (classLoader == null || isVisible(ifc, classLoader))) {
					interfaces.add(ifc);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces.toArray(new Class[interfaces.size()]);
	}

	/**
	 * Return all interfaces that the given instance implements as Set,
	 * including ones implemented by superclasses.
	 * @param instance the instance to analyse for interfaces
	 * @return all interfaces that the given instance implements as Set
	 */
	public static Set<Class<?>> getAllInterfacesAsSet(Object instance) {
		return getAllInterfacesForClassAsSet(instance.getClass());
	}

	/**
	 * Return all interfaces that the given class implements as Set,
	 * including ones implemented by superclasses.
	 * <p>If the class itself is an interface, it gets returned as sole interface.
	 * @param clazz the class to analyse for interfaces
	 * @return all interfaces that the given object implements as Set
	 */
	public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz) {
		return getAllInterfacesForClassAsSet(clazz, null);
	}

	/**
	 * Return all interfaces that the given class implements as Set,
	 * including ones implemented by superclasses.
	 * <p>If the class itself is an interface, it gets returned as sole interface.
	 * @param clazz the class to analyse for interfaces
	 * @param classLoader the ClassLoader that the interfaces need to be visible in
	 * (may be <code>null</code> when accepting all declared interfaces)
	 * @return all interfaces that the given object implements as Set
	 */
	public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz, ClassLoader classLoader) {
		Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
		if(clazz.isInterface()) {
			interfaces.add(clazz);
			return interfaces;
		}
		while(clazz != null) {
			for(int i = 0; i < clazz.getInterfaces().length; i++) {
				Class<?> ifc = clazz.getInterfaces()[i];
				if(classLoader == null || isVisible(ifc, classLoader)) {
					interfaces.add(ifc);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return interfaces;
	}

	/**
	 * Create a composite interface Class&lt;?&gt; for the given interfaces,
	 * implementing the given interfaces in one single Class.
	 * <p>This implementation builds a JDK proxy class for the given interfaces.
	 * @param interfaces the interfaces to merge
	 * @param classLoader the ClassLoader to create the composite Class&lt;?&gt; in
	 * @return the merged interface as Class
	 * @see java.lang.reflect.Proxy#getProxyClass
	 */
	public static Class<?> createCompositeInterface(Class<?>[] interfaces, ClassLoader classLoader) {
		return Proxy.getProxyClass(classLoader, interfaces);
	}

	/**
	 * Check whether the given class is visible in the given ClassLoader.
	 *
	 * @param clazz the class to check (typically an interface)
	 * @param classLoader the ClassLoader to check against (may be <code>null</code>,
	 * in which case this method will always return <code>true</code>)
	 * @return true, if is visible
	 */
	public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
		if(classLoader == null) {
			return true;
		}
		try {
			Class<?> actualClass = classLoader.loadClass(clazz.getName());
			return (clazz == actualClass);
			// Else: different interface class found...
		} catch(ClassNotFoundException ex) {
			// No interface class found...
			return false;
		}
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
		if(!isAssignableValue(srcClass, destObject))
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
			if(destClass.isPrimitive() && srcClass.equals(getPrimitiveWrapper(destClass)) ||
					srcClass.isPrimitive() && destClass.equals(getPrimitiveWrapper(srcClass))) {
				return 0.5f;
			}
			if(srcClass.isArray() && destClass.isArray()) {
				if(destClass.getComponentType().isPrimitive() && srcClass.equals(getPrimitiveWrapper(destClass)) ||
						srcClass.getComponentType().isPrimitive() && destClass.equals(getPrimitiveWrapper(srcClass))) {
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
