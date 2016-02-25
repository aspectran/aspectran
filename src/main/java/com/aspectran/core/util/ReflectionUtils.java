package com.aspectran.core.util;

import java.lang.reflect.Field;
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
		} catch(IllegalAccessException ex) {
			throw new IllegalStateException("Could not access field: " + ex.getMessage());
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
	 * @param method
	 *            the method to make accessible
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

}
