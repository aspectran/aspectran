/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;

/**
 * BeanUtils provides methods that allow simple, reflective access to
 * JavaBeans style properties.  Methods are provided for all simple types as
 * well as object types.
 * <p/>
 * Examples:
 * <p/>
 * StaticBeanProbe.setObject(object, propertyName, value);
 * <P>
 * Object value = StaticBeanProbe.getObject(object, propertyName);
 * 
 * <p>
 * Created: 2008. 04. 22 오후 3:47:15
 * </p>
 */
public class BeanUtils {

	private static final Object[] NO_ARGUMENTS = new Object[0];
	
	//private static Map<Class<?>, ClassDescriptor> cache = Collections.synchronizedMap(new HashMap<Class<?>, ClassDescriptor>());
	private static Map<Class<?>, ClassDescriptor> cache = Collections.synchronizedMap(new WeakHashMap<Class<?>, ClassDescriptor>());

	/**
	 * Returns an array of the readable properties exposed by a bean
	 * 
	 * @param object The bean
	 * @return The properties
	 */
	public static String[] getReadablePropertyNames(Object object) {
		return getClassDescriptor(object.getClass()).getReadablePropertyNames();
	}

	/**
	 * Returns an array of the writeable properties exposed by a bean
	 * 
	 * @param object The bean
	 * @return The properties
	 */
	public static String[] getWriteablePropertyNames(Object object) {
		return getClassDescriptor(object.getClass()).getWriteablePropertyNames();
	}

	/**
	 * Returns the class that the setter expects to receive as a parameter when
	 * setting a property value.
	 * 
	 * @param object The bean to check
	 * @param name The name of the property
	 * @return The type of the property
	 */
	public static Class<?> getPropertyTypeForSetter(Object object, String name) throws NoSuchMethodException {
		Class<?> type = object.getClass();

		if(object instanceof Class<?>) {
			type = getClassPropertyTypeForSetter((Class<?>)object, name);
		} else if(object instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>)object;
			Object value = map.get(name);
			if(value == null) {
				type = Object.class;
			} else {
				type = value.getClass();
			}
		} else {
			if(name.indexOf('.') > -1) {
				StringTokenizer parser = new StringTokenizer(name, ".");
				while(parser.hasMoreTokens()) {
					name = parser.nextToken();
					type = getClassDescriptor(type).getSetterType(name);
				}
			} else {
				type = getClassDescriptor(type).getSetterType(name);
			}
		}

		return type;
	}

	/**
	 * Returns the class that the getter will return when reading a property
	 * value.
	 * 
	 * @param object The bean to check
	 * @param name The name of the property
	 * @return The type of the property
	 */
	public static Class<?> getPropertyTypeForGetter(Object object, String name) throws NoSuchMethodException {
		Class<?> type = object.getClass();

		if(object instanceof Class<?>) {
			type = getClassPropertyTypeForGetter((Class<?>)object, name);
		} else if(object instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>)object;
			Object value = map.get(name);
			if(value == null) {
				type = Object.class;
			} else {
				type = value.getClass();
			}
		} else {
			if(name.indexOf('.') > -1) {
				StringTokenizer parser = new StringTokenizer(name, ".");
				while(parser.hasMoreTokens()) {
					name = parser.nextToken();
					type = getClassDescriptor(type).getGetterType(name);
				}
			} else {
				type = getClassDescriptor(type).getGetterType(name);
			}
		}

		return type;
	}

	/**
	 * Returns the class that the getter will return when reading a property
	 * value.
	 * 
	 * @param type The class to check
	 * @param name The name of the property
	 * @return The type of the property
	 */
	private static Class<?> getClassPropertyTypeForGetter(Class<?> type, String name) throws NoSuchMethodException {
		if(name.indexOf('.') > -1) {
			StringTokenizer parser = new StringTokenizer(name, ".");
			while(parser.hasMoreTokens()) {
				name = parser.nextToken();
				type = getClassDescriptor(type).getGetterType(name);
			}
		} else {
			type = getClassDescriptor(type).getGetterType(name);
		}

		return type;
	}

	/**
	 * Returns the class that the setter expects to receive as a parameter when
	 * setting a property value.
	 * 
	 * @param type The class to check
	 * @param name The name of the property
	 * @return The type of the property
	 */
	private static Class<?> getClassPropertyTypeForSetter(Class<?> type, String name) throws NoSuchMethodException {
		if(name.indexOf('.') > -1) {
			StringTokenizer parser = new StringTokenizer(name, ".");
			while(parser.hasMoreTokens()) {
				name = parser.nextToken();
				type = getClassDescriptor(type).getSetterType(name);
			}
		} else {
			type = getClassDescriptor(type).getSetterType(name);
		}

		return type;
	}

	/**
	 * Gets an Object property from a bean
	 * 
	 * @param object The bean
	 * @param name The property name
	 * @return The property value (as an Object)
	 */
	public static Object getObject(Object object, String name) throws InvocationTargetException {
		if(name.indexOf('.') > -1) {
			StringTokenizer parser = new StringTokenizer(name, ".");
			Object value = object;
			while(parser.hasMoreTokens()) {
				value = getProperty(value, parser.nextToken());

				if(value == null) {
					break;
				}
			}
			return value;
		} else {
			return getProperty(object, name);
		}
	}

	/**
	 * Sets the value of a bean property to an Object
	 * 
	 * @param object The bean to change
	 * @param name The name of the property to set
	 * @param value The new value to set
	 */
	public static void setObject(Object object, String name, Object value) throws InvocationTargetException,
			NoSuchMethodException {
		if(name.indexOf('.') > -1) {
			StringTokenizer parser = new StringTokenizer(name, ".");
			String property = parser.nextToken();
			Object child = object;
			
			while(parser.hasMoreTokens()) {
				Class<?> type = getPropertyTypeForSetter(child, property);
				Object parent = child;
				child = getProperty(parent, property);
				
				if(child == null) {
					if(value == null) {
						return; // don't instantiate child path if value is null
					} else {
						try {
							child = type.newInstance();
							setObject(parent, property, child);
						} catch(Exception e) {
							throw new InvocationTargetException(e, "Cannot set value of property '" + name
									+ "' because '" + property + "' is null and cannot be instantiated on instance of "
									+ type.getName() + ". Cause: " + e.toString());
						}
					}
				}
				
				property = parser.nextToken();
			}
			
			setProperty(child, property, value);
		} else {
			setProperty(object, name, value);
		}
	}

	/**
	 * Checks to see if a bean has a writable property be a given name
	 * 
	 * @param object The bean to check
	 * @param propertyName The property to check for
	 * @return True if the property exists and is writable
	 */
	public static boolean hasWritableProperty(Object object, String propertyName) throws NoSuchMethodException {
		boolean hasProperty = false;
		
		if(object instanceof Map<?, ?>) {
			hasProperty = true;// ((Map) object).containsKey(propertyName);
		} else {
			if(propertyName.indexOf('.') > -1) {
				StringTokenizer parser = new StringTokenizer(propertyName, ".");
				Class<?> type = object.getClass();
				
				while(parser.hasMoreTokens()) {
					propertyName = parser.nextToken();
					type = getClassDescriptor(type).getGetterType(propertyName);
					hasProperty = getClassDescriptor(type).hasWritableProperty(propertyName);
				}
			} else {
				hasProperty = getClassDescriptor(object.getClass()).hasWritableProperty(propertyName);
			}
		}
		
		return hasProperty;
	}

	/**
	 * Checks to see if a bean has a readable property be a given name
	 * 
	 * @param object The bean to check
	 * @param propertyName The property to check for
	 * @return True if the property exists and is readable
	 */
	public static boolean hasReadableProperty(Object object, String propertyName) throws NoSuchMethodException {
		boolean hasProperty = false;
		
		if(object instanceof Map<?, ?>) {
			hasProperty = true; // ((Map) object).containsKey(propertyName);
		} else {
			if(propertyName.indexOf('.') > -1) {
				StringTokenizer parser = new StringTokenizer(propertyName, ".");
				Class<?> type = object.getClass();
				
				while(parser.hasMoreTokens()) {
					propertyName = parser.nextToken();
					type = getClassDescriptor(type).getGetterType(propertyName);
					hasProperty = getClassDescriptor(type).hasReadableProperty(propertyName);
				}
			} else {
				hasProperty = getClassDescriptor(object.getClass()).hasReadableProperty(propertyName);
			}
		}
		
		return hasProperty;
	}

	@SuppressWarnings("unused")
	private static Object getProperty(Object object, String name) throws InvocationTargetException {
		ClassDescriptor classCache = getClassDescriptor(object.getClass());
		
		try {
			Object value = null;
			if(name.indexOf("[") > -1) {
				value = getIndexedProperty(object, name);
			} else {
				if(object instanceof Map<?, ?>) {
					value = ((Map<?, ?>)object).get(name);
				} else {
					Method method = classCache.getGetter(name);
					
					if(method == null) {
						throw new NoSuchMethodException("No GET method for property " + name + " on instance of "
								+ object.getClass().getName());
					}
					
					try {
						value = method.invoke(object, NO_ARGUMENTS);
					} catch(Throwable t) {
						throw unwrapThrowable(t);
					}
				}
			}
			return value;
		} catch(InvocationTargetException e) {
			throw e;
		} catch(Throwable t) {
			if(object == null) {
				throw new InvocationTargetException(t, "Could not get property '" + name
						+ "' from null reference. Cause: " + t.toString());
			} else {
				throw new InvocationTargetException(t, "Could not get property '" + name + "' from "
						+ object.getClass().getName() + ". Cause: " + t.toString());
			}
		}
	}

	@SuppressWarnings("unused")
	private static void setProperty(Object object, String name, Object value) throws InvocationTargetException {
		ClassDescriptor classCache = getClassDescriptor(object.getClass());
		
		try {
			if(name.indexOf("[") > -1) {
				setIndexedProperty(object, name, value);
			} else {
				if(object instanceof Map<?, ?>) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>)object;
					map.put(name, value);
				} else {
					Method method = classCache.getSetter(name);
					
					if(method == null) {
						throw new NoSuchMethodException("No SET method for property " + name + " on instance of "
								+ object.getClass().getName());
					}
					
					Object[] params = new Object[1];
					params[0] = value;
					
					try {
						method.invoke(object, params);
					} catch(Throwable t) {
						throw unwrapThrowable(t);
					}
				}
			}
		} catch(InvocationTargetException e) {
			throw e;
		} catch(Throwable t) {
			if(object == null) {
				throw new InvocationTargetException(t, "Could not set property '" + name
						+ "' for null reference. Cause: " + t.toString());
			} else {
				throw new InvocationTargetException(t, "Could not set property '" + name + "' for "
						+ object.getClass().getName() + ". Cause: " + t.toString());
			}
		}
	}

	public static Object getIndexedProperty(Object object, String indexedName) throws InvocationTargetException {

		try {
			String name = indexedName.substring(0, indexedName.indexOf("["));
			int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
			Object list = null;

			if(StringUtils.isEmpty(name)) {
				list = object;
			} else {
				list = getProperty(object, name);
			}

			Object value = null;

			if(list instanceof List<?>) {
				value = ((List<?>)list).get(i);
			} else if(list instanceof Object[]) {
				value = ((Object[])list)[i];
			} else if(list instanceof char[]) {
				value = new Character(((char[])list)[i]);
			} else if(list instanceof boolean[]) {
				value = new Boolean(((boolean[])list)[i]);
			} else if(list instanceof byte[]) {
				value = new Byte(((byte[])list)[i]);
			} else if(list instanceof double[]) {
				value = new Double(((double[])list)[i]);
			} else if(list instanceof float[]) {
				value = new Float(((float[])list)[i]);
			} else if(list instanceof int[]) {
				value = new Integer(((int[])list)[i]);
			} else if(list instanceof long[]) {
				value = new Long(((long[])list)[i]);
			} else if(list instanceof short[]) {
				value = new Short(((short[])list)[i]);
			} else {
				throw new IllegalArgumentException("The '" + name + "' property of the " + object.getClass().getName()
						+ " class is not a List or Array.");
			}

			return value;
		} catch(InvocationTargetException e) {
			throw e;
		} catch(Exception e) {
			throw new InvocationTargetException(e, "Error getting ordinal list from JavaBean. Cause: " + e);
		}
	}

	public static Class<?> getIndexedType(Object object, String indexedName) throws InvocationTargetException {

		try {
			String name = indexedName.substring(0, indexedName.indexOf("["));
			int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
			Object list = null;

			if(!StringUtils.isEmpty(name)) {
				list = getProperty(object, name);
			} else {
				list = object;
			}

			Class<?> value = null;

			if(list instanceof List<?>) {
				value = ((List<?>)list).get(i).getClass();
			} else if(list instanceof Object[]) {
				value = ((Object[])list)[i].getClass();
			} else if(list instanceof char[]) {
				value = Character.class;
			} else if(list instanceof boolean[]) {
				value = Boolean.class;
			} else if(list instanceof byte[]) {
				value = Byte.class;
			} else if(list instanceof double[]) {
				value = Double.class;
			} else if(list instanceof float[]) {
				value = Float.class;
			} else if(list instanceof int[]) {
				value = Integer.class;
			} else if(list instanceof long[]) {
				value = Long.class;
			} else if(list instanceof short[]) {
				value = Short.class;
			} else {
				throw new IllegalArgumentException("The '" + name + "' property of the " + object.getClass().getName()
						+ " class is not a List or Array.");
			}

			return value;
		} catch(InvocationTargetException e) {
			throw e;
		} catch(Exception e) {
			throw new InvocationTargetException(e, "Error getting ordinal list from JavaBean. Cause: " + e);
		}
	}

	public static void setIndexedProperty(Object object, String indexedName, Object value)
			throws InvocationTargetException {
		try {
			String name = indexedName.substring(0, indexedName.indexOf("["));
			int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
			Object list = getProperty(object, name);

			if(list instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<Object> l = (List<Object>)list;
				l.set(i, value);
			} else if(list instanceof Object[]) {
				((Object[])list)[i] = value;
			} else if(list instanceof char[]) {
				((char[])list)[i] = ((Character)value).charValue();
			} else if(list instanceof boolean[]) {
				((boolean[])list)[i] = ((Boolean)value).booleanValue();
			} else if(list instanceof byte[]) {
				((byte[])list)[i] = ((Byte)value).byteValue();
			} else if(list instanceof double[]) {
				((double[])list)[i] = ((Double)value).doubleValue();
			} else if(list instanceof float[]) {
				((float[])list)[i] = ((Float)value).floatValue();
			} else if(list instanceof int[]) {
				((int[])list)[i] = ((Integer)value).intValue();
			} else if(list instanceof long[]) {
				((long[])list)[i] = ((Long)value).longValue();
			} else if(list instanceof short[]) {
				((short[])list)[i] = ((Short)value).shortValue();
			} else {
				throw new IllegalArgumentException("The '" + name + "' property of the " + object.getClass().getName()
						+ " class is not a List or Array.");
			}
		} catch(InvocationTargetException e) {
			throw e;
		} catch(Exception e) {
			throw new InvocationTargetException(e, "Error getting ordinal value from JavaBean. Cause: " + e);
		}
	}
	
	/**
	 * Examines a Throwable object and gets it's root cause
	 * 
	 * @param t - the exception to examine
	 * @return The root cause
	 */
	public static Throwable unwrapThrowable(Throwable t) {
		Throwable t2 = t;
		
		while(true) {
			if(t2 instanceof InvocationTargetException) {
				t2 = ((InvocationTargetException)t).getTargetException();
			} else if(t instanceof UndeclaredThrowableException) {
				t2 = ((UndeclaredThrowableException)t).getUndeclaredThrowable();
			} else {
				return t2;
			}
		}
	}
	
	/**
	 * Gets an instance of ClassDescriptor for the specified class.
	 * 
	 * @param clazz The class for which to lookup the ClassDescriptor cache.
	 * @return The ClassDescriptor cache for the class
	 */
	public static ClassDescriptor getClassDescriptor(Class<?> clazz) {
		synchronized(clazz) {
			ClassDescriptor descriptor = cache.get(clazz);
			
			if(descriptor == null) {
				descriptor = new ClassDescriptor(clazz);
				cache.put(clazz, descriptor);
			}
			
			return descriptor;
		}
	}
	
	/**
	 * This class represents a cached set of class definition information that
	 * allows for easy mapping between property names and getter/setter methods.
	 */
	private static class ClassDescriptor {
		
		private static final String[] EMPTY_STRING_ARRAY = new String[0];

		private String className;

		private String[] readablePropertyNames = EMPTY_STRING_ARRAY;

		private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;

		private Map<String, Method> setMethods = new HashMap<String, Method>();

		private Map<String, Method> getMethods = new HashMap<String, Method>();

		private Map<String, Class<?>> setTypes = new HashMap<String, Class<?>>();

		private Map<String, Class<?>> getTypes = new HashMap<String, Class<?>>();

		private ClassDescriptor(Class<?> clazz) {
			className = clazz.getName();
			addMethods(clazz);
			Class<?> superClass = clazz.getSuperclass();

			while(superClass != null) {
				addMethods(superClass);
				superClass = superClass.getSuperclass();
			}

			readablePropertyNames = (String[])getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
			writeablePropertyNames = (String[])setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
		}

		private void addMethods(Class<?> cls) {
			Method[] methods = cls.getMethods();

			for(int i = 0; i < methods.length; i++) {
				String name = methods[i].getName();
				System.out.println("************* " + name);
				if(name.equals("getClass")) {
					// ignore
				} else if(name.startsWith("set") && name.length() > 3) {
					if(methods[i].getParameterTypes().length == 1) {
						name = dropCase(name);
						setMethods.put(name, methods[i]);
						setTypes.put(name, methods[i].getParameterTypes()[0]);
					}
				} else if(name.startsWith("get") && name.length() > 3) {
					if(methods[i].getParameterTypes().length == 0) {
						name = dropCase(name);
						getMethods.put(name, methods[i]);
						getTypes.put(name, methods[i].getReturnType());
					}
				} else if(name.startsWith("is") && name.length() > 2) {
					if(methods[i].getParameterTypes().length == 0) {
						name = dropCase(name);
						getMethods.put(name, methods[i]);
						getTypes.put(name, methods[i].getReturnType());
					}
				}
			}
		}

		private static String dropCase(String name) {
			if(name.startsWith("is")) {
				name = name.substring(2);
			} else if(name.startsWith("get") || name.startsWith("set")) {
				name = name.substring(3);
			} else {
				throw new IllegalArgumentException("Error parsing property name '" + name
						+ "'.  Didn't start with 'is', 'get' or 'set'.");
			}

			if(name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
				name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
			}

			return name;
		}

		/**
		 * Gets the name of the class the instance provides information for
		 * 
		 * @return The class name
		 */
		@SuppressWarnings("unused")
		public String getClassName() {
			return className;
		}

		/**
		 * Gets the setter for a property as a Method object
		 * 
		 * @param propertyName - the property
		 * @return The Method
		 */
		public Method getSetter(String propertyName) throws NoSuchMethodException {
			Method method = (Method)setMethods.get(propertyName);
			if(method == null) {
				throw new NoSuchMethodException("There is no WRITEABLE property named '" + propertyName + "' in class '"
						+ className + "'");
			}
			return method;
		}

		/**
		 * Gets the getter for a property as a Method object
		 * 
		 * @param propertyName - the property
		 * @return The Method
		 */
		public Method getGetter(String propertyName) throws NoSuchMethodException {
			Method method = (Method)getMethods.get(propertyName);
			if(method == null) {
				throw new NoSuchMethodException("There is no READABLE property named '" + propertyName + "' in class '"
						+ className + "'");
			}
			return method;
		}

		/**
		 * Gets the type for a property setter
		 * 
		 * @param propertyName - the name of the property
		 * @return The Class of the propery setter
		 */
		public Class<?> getSetterType(String propertyName) throws NoSuchMethodException {
			Class<?> clazz = (Class<?>)setTypes.get(propertyName);
			if(clazz == null) {
				throw new NoSuchMethodException("There is no WRITEABLE property named '" + propertyName + "' in class '"
						+ className + "'");
			}
			return clazz;
		}

		/**
		 * Gets the type for a property getter
		 * 
		 * @param propertyName - the name of the property
		 * @return The Class of the propery getter
		 */
		public Class<?> getGetterType(String propertyName) throws NoSuchMethodException {
			Class<?> clazz = (Class<?>)getTypes.get(propertyName);
			if(clazz == null) {
				throw new NoSuchMethodException("There is no READABLE property named '" + propertyName + "' in class '"
						+ className + "'");
			}
			return clazz;
		}

		/**
		 * Gets an array of the readable properties for an object
		 * 
		 * @return The array
		 */
		public String[] getReadablePropertyNames() {
			return readablePropertyNames;
		}

		/**
		 * Gets an array of the writeable properties for an object
		 * 
		 * @return The array
		 */
		public String[] getWriteablePropertyNames() {
			return writeablePropertyNames;
		}

		/**
		 * Check to see if a class has a writeable property by name
		 * 
		 * @param propertyName - the name of the property to check
		 * @return True if the object has a writeable property by the name
		 */
		public boolean hasWritableProperty(String propertyName) {
			return setMethods.keySet().contains(propertyName);
		}

		/**
		 * Check to see if a class has a readable property by name
		 * 
		 * @param propertyName - the name of the property to check
		 * @return True if the object has a readable property by the name
		 */
		public boolean hasReadableProperty(String propertyName) {
			return getMethods.keySet().contains(propertyName);
		}
	}
}
