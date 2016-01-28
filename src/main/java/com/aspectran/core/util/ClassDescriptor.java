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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ReflectPermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class represents a cached set of class definition information that
 * allows for easy mapping between property names and getter/setter methods.
 */
public class ClassDescriptor {

	private static Map<Class<?>, ClassDescriptor> cache = new ConcurrentHashMap<Class<?>, ClassDescriptor>();
	
	private static boolean cacheEnabled = true;
	
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private String[] readablePropertyNames = EMPTY_STRING_ARRAY;

	private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;

	private String[] distinctMethodNames = EMPTY_STRING_ARRAY;

	private String className;

	private Map<String, Field> fields = new HashMap<String, Field>();
	
	private Map<String, Method> setMethods = new HashMap<String, Method>();

	private Map<String, Method> getMethods = new HashMap<String, Method>();
	
	private Map<String, Class<?>> setTypes = new HashMap<String, Class<?>>();

	private Map<String, Class<?>> getTypes = new HashMap<String, Class<?>>();
	
	private ClassDescriptor(Class<?> clazz) {
		className = clazz.getName();

		addFields(clazz);
		
		Method[] methods = getClassMethods(clazz);
		
		addGetMethods(methods);
		addSetMethods(methods);

		readablePropertyNames = (String[])getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
		writeablePropertyNames = (String[])setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
		
		Set<String> nameSet = new HashSet<String>();
		for(Method method : methods) {
			nameSet.add(method.getName());
		}
		
		distinctMethodNames = (String[])nameSet.toArray(new String[nameSet.size()]);
	}

	private void addFields(Class<?> clazz) {
		while(clazz != null) {
            for(Field field : clazz.getDeclaredFields()) {
                if(!fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }

            clazz = clazz.getSuperclass();
        }
	}
	
	private void addGetMethods(Method[] methods) {
		for(int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			String name = method.getName();

			if(name.startsWith("get") && name.length() > 3) {
				if(method.getParameterTypes().length == 0) {
					name = dropCase(name);
					addGetMethod(name, method);
				}
			} else if(name.startsWith("is") && name.length() > 2) {
				if(method.getParameterTypes().length == 0) {
					name = dropCase(name);
					addGetMethod(name, method);
				}
			}
		}
	}
	
	private void addGetMethod(String name, Method method) {
		getMethods.put(name, method);
		getTypes.put(name, method.getReturnType());
	}
	
	private void addSetMethods(Method[] methods) {
		Map<String, List<Method>> conflictingSetters = new HashMap<String, List<Method>>();
		for(int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			String name = method.getName();
			if(name.startsWith("set") && name.length() > 3) {
				if (method.getParameterTypes().length == 1) {
					name = dropCase(name);
					addSetterConflict(conflictingSetters, name, method);
				}
			}
		}
		resolveSetterConflicts(conflictingSetters);
	}

	private void addSetterConflict(Map<String, List<Method>> conflictingSetters, String name, Method method) {
		List<Method> list = conflictingSetters.get(name);
		if(list == null) {
			list = new ArrayList<Method>();
			conflictingSetters.put(name, list);
		}
		list.add(method);
	}

	private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
		for(Iterator<String> propNames = conflictingSetters.keySet().iterator(); propNames.hasNext();) {
			String propName = (String)propNames.next();
			List<Method> setters = conflictingSetters.get(propName);
			Method firstMethod = setters.get(0);
			if(setters.size() == 1) {
				addSetMethod(propName, firstMethod);
			} else {
				Class<?> expectedType = getTypes.get(propName);
				if(expectedType == null) {
					throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
							+ propName + " in class " + firstMethod.getDeclaringClass()
							+ ".  This breaks the JavaBeans "
							+ "specification and can cause unpredicatble results.");
				} else {
					Iterator<Method> methods = setters.iterator();
					Method setter = null;
					while(methods.hasNext()) {
						Method method = methods.next();
						if(method.getParameterTypes().length == 1 && expectedType.equals(method.getParameterTypes()[0])) {
							setter = method;
							break;
						}
					}
					if(setter == null) {
						throw new RuntimeException(
								"Illegal overloaded setter method with ambiguous type for property " + propName
										+ " in class " + firstMethod.getDeclaringClass()
										+ ".  This breaks the JavaBeans "
										+ "specification and can cause unpredicatble results.");
					}
					addSetMethod(propName, setter);
				}
			}
		}
	}

	private void addSetMethod(String name, Method method) {
		setMethods.put(name, method);
		setTypes.put(name, method.getParameterTypes()[0]);
	}
	
	/**
	 * This method returns an array containing all methods declared in this
	 * class and any superclass. We use this method, instead of the simpler
	 * Class.getMethods(), because we want to look for private methods as
	 * well.
	 * @param cls The class
	 * @return An array containing all methods in this class
	 */
	private Method[] getClassMethods(Class<?> cls) {
		Map<String, Method> uniqueMethods = new HashMap<String, Method>();
		Class<?> currentClass = cls;
		
		while(currentClass != null) {
			addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());
	
			// we also need to look for interface methods -
			// because the class may be abstract
			Class<?>[] interfaces = currentClass.getInterfaces();
			for(int i = 0; i < interfaces.length; i++) {
				addUniqueMethods(uniqueMethods, interfaces[i].getMethods());
			}
	
			currentClass = currentClass.getSuperclass();
		}
	
		Collection<?> methods = uniqueMethods.values();
	
		return (Method[])methods.toArray(new Method[methods.size()]);
	}
	
	private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
		for(Method currentMethod : methods) {
			if(!currentMethod.isBridge()) {
				String signature = getSignature(currentMethod);
				// check to see if the method is already known
				// if it is known, then an extended class must have
				// overridden a method
				if(!uniqueMethods.containsKey(signature)) {
					if(canAccessPrivateMethods()) {
						try {
							currentMethod.setAccessible(true);
						} catch(Exception e) {
							// Ignored. This is only a final precaution,
							// nothing we can do.
						}
					}

					uniqueMethods.put(signature, currentMethod);
				}
			}
		}
	}

	private String getSignature(Method method) {
		StringBuffer sb = new StringBuffer();
		sb.append(method.getName());
		Class<?>[] parameters = method.getParameterTypes();

		for (int i = 0; i < parameters.length; i++) {
			if (i == 0) {
				sb.append(':');
			} else {
				sb.append(',');
			}
			sb.append(parameters[i].getName());
		}

		return sb.toString();
	}
	
	private static boolean canAccessPrivateMethods() {
		try {
			SecurityManager securityManager = System.getSecurityManager();
			if (null != securityManager) {
				securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
			}
		} catch (SecurityException e) {
			return false;
		}
		return true;
	}
	
	private static String dropCase(String name) {
		if(name.startsWith("is")) {
			name = name.substring(2);
		} else if(name.startsWith("get") || name.startsWith("set")) {
			name = name.substring(3);
		} else {
			throw new IllegalArgumentException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
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
	public String getClassName() {
		return className;
	}

	/**
	 * Gets the setter for a property as a Method object.
	 *
	 * @param propertyName - the property
	 * @return The Method
	 * @throws NoSuchMethodException the no such method exception
	 */
	public Method getSetter(String propertyName) throws NoSuchMethodException {
		Method method = (Method)setMethods.get(propertyName);
		if(method == null) {
			throw new NoSuchMethodException("There is no WRITEABLE property named '" + propertyName + "' in class '" + className + "'");
		}
		return method;
	}

	/**
	 * Gets the getter for a property as a Method object.
	 *
	 * @param propertyName - the property
	 * @return The Method
	 * @throws NoSuchMethodException the no such method exception
	 */
	public Method getGetter(String propertyName) throws NoSuchMethodException {
		Method method = (Method)getMethods.get(propertyName);
		if(method == null) {
			throw new NoSuchMethodException("There is no READABLE property named '" + propertyName + "' in class '" + className + "'");
		}
		return method;
	}

	/**
	 * Gets the type for a property setter.
	 *
	 * @param propertyName - the name of the property
	 * @return The Class of the propery setter
	 * @throws NoSuchMethodException the no such method exception
	 */
	public Class<?> getSetterType(String propertyName) throws NoSuchMethodException {
		Class<?> clazz = (Class<?>)setTypes.get(propertyName);
		if(clazz == null) {
			throw new NoSuchMethodException("There is no WRITEABLE property named '" + propertyName + "' in class '" + className + "'");
		}
		return clazz;
	}

	/**
	 * Gets the type for a property getter.
	 *
	 * @param propertyName - the name of the property
	 * @return The Class of the propery getter
	 * @throws NoSuchMethodException the no such method exception
	 */
	public Class<?> getGetterType(String propertyName) throws NoSuchMethodException {
		Class<?> clazz = (Class<?>)getTypes.get(propertyName);
		if(clazz == null) {
			throw new NoSuchMethodException("There is no READABLE property named '" + propertyName + "' in class '" + className + "'");
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
	
	/**
	 * Gets the distinct method names.
	 *
	 * @return the distinct method names
	 */
	public String[] getDistinctMethodNames() {
		return distinctMethodNames;
	}

	/**
	 * Gets an instance of ClassDescriptor for the specified class.
	 * @param clazz The class for which to lookup the method cache.
	 * @return The method cache for the class
	 */
	public static ClassDescriptor getInstance(Class<?> clazz) {
		if(cacheEnabled) {
			ClassDescriptor cached = cache.get(clazz);
			if(cached == null) {
				cached = new ClassDescriptor(clazz);
				cache.put(clazz, cached);
			}
			return cached;
		} else {
			return new ClassDescriptor(clazz);
		}
	}

	public static void setCacheEnabled(boolean cacheEnabled) {
		ClassDescriptor.cacheEnabled = cacheEnabled;
	}
	
    /**
     * Clear the ClassDescriptor cache.
     * @return the number of cached ClassDescriptor cleared
     */
    public static synchronized int clearCache() {
        final int size = cache.size();
        cache.clear();
        return size;
    }
	
}
