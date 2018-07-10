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

import com.aspectran.core.component.bean.annotation.NonSerializable;
import com.aspectran.core.context.AspectranRuntimeException;

import java.lang.reflect.Method;
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
 * This class represents a cached set of bean property information that
 * allows for easy mapping between property names and getter/setter methods.
 */
public class BeanDescriptor {

    private static volatile boolean cacheEnabled = true;

    private static final Map<Class<?>, BeanDescriptor> cache = new ConcurrentHashMap<>();

    private final String className;

    private final String[] readablePropertyNames;

    private final String[] serializableReadablePropertyNames;

    private final String[] writablePropertyNames;

    private final String[] distinctMethodNames;

    private final Map<String, Method> readMethods = new HashMap<>();

    private final Map<String, Class<?>> readTypes = new HashMap<>();

    private final Map<String, Method> writeMethods = new HashMap<>();

    private final Map<String, Class<?>> writeType = new HashMap<>();

    private BeanDescriptor(Class<?> beanClass) {
        this.className = beanClass.getName();
        Method[] methods = getAllMethods(beanClass);

        Set<String> nonSerializableReadPropertyNames = addReadMethods(methods);
        this.readablePropertyNames = readMethods.keySet().toArray(new String[0]);

        if (!nonSerializableReadPropertyNames.isEmpty()) {
            String[] serializableReadablePropertyNames = new String[readablePropertyNames.length - nonSerializableReadPropertyNames.size()];
            int index = 0;
            for (String name : readablePropertyNames) {
                if (!nonSerializableReadPropertyNames.contains(name)) {
                    serializableReadablePropertyNames[index++] = name;
                }
            }
            this.serializableReadablePropertyNames = (index > 0 ? serializableReadablePropertyNames : null);
        } else {
            this.serializableReadablePropertyNames = this.readablePropertyNames;
        }

        addWriteMethods(methods);
        this.writablePropertyNames = writeMethods.keySet().toArray(new String[0]);

        Set<String> nameSet = new HashSet<>();
        for (Method method : methods) {
            nameSet.add(method.getName());
        }
        this.distinctMethodNames = nameSet.toArray(new String[0]);
    }

    private Set<String> addReadMethods(Method[] methods) {
        Set<String> nonSerializableReadPropertyNames = new HashSet<>();
        for (Method method : methods) {
            if (method.getParameterCount() == 0) {
                String name = method.getName();
                if ((name.startsWith("get") && name.length() > 3) ||
                        (name.startsWith("is") && name.length() > 2)) {
                    name = dropCase(name);
                    addGetMethod(name, method);
                    if (method.isAnnotationPresent(NonSerializable.class)) {
                        nonSerializableReadPropertyNames.add(name);
                    }
                }
            }
        }
        return nonSerializableReadPropertyNames;
    }

    private void addGetMethod(String name, Method method) {
        readMethods.put(name, method);
        readTypes.put(name, method.getReturnType());
    }

    private void addWriteMethods(Method[] methods) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        for (Method method : methods) {
            if (method.getParameterCount() == 1) {
                String name = method.getName();
                if (name.startsWith("set") && name.length() > 3) {
                    name = dropCase(name);
                    addSetterConflict(conflictingSetters, name, method);
                }
            }
        }
        resolveSetterConflicts(conflictingSetters);
    }

    private void addSetterConflict(Map<String, List<Method>> conflictingSetters, String name, Method method) {
        List<Method> list = conflictingSetters.computeIfAbsent(name, k -> new ArrayList<>());
        list.add(method);
    }

    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (String propName : conflictingSetters.keySet()) {
            List<Method> setters = conflictingSetters.get(propName);
            Method firstMethod = setters.get(0);
            if (setters.size() == 1) {
                addWriteMethod(propName, firstMethod);
            } else {
                Class<?> expectedType = readTypes.get(propName);
                if (expectedType == null) {
                    throw new AspectranRuntimeException("Illegal overloaded setter method with ambiguous type for property " +
                            propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                            "specification and can cause unpredictable results.");
                } else {
                    Iterator<Method> methods = setters.iterator();
                    Method setter = null;
                    while (methods.hasNext()) {
                        Method method = methods.next();
                        if (method.getParameterCount() == 1 && expectedType.equals(method.getParameterTypes()[0])) {
                            setter = method;
                            break;
                        }
                    }
                    if (setter == null) {
                        throw new AspectranRuntimeException("Illegal overloaded setter method with ambiguous type for property " +
                                propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                                "specification and can cause unpredictable results.");
                    }
                    addWriteMethod(propName, setter);
                }
            }
        }
    }

    private void addWriteMethod(String name, Method method) {
        writeMethods.put(name, method);
        writeType.put(name, method.getParameterTypes()[0]);
    }

    /**
     * This method returns an array containing all methods exposed in this
     * class and any superclass. In the future, Java is not pleased to have
     * access to private or protected methods.
     *
     * @param beanClass the class
     * @return an array containing all the public methods in this class
     */
    private Method[] getAllMethods(Class<?> beanClass) {
        Map<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = beanClass;

        while (currentClass != null && currentClass != Object.class) {
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());

            // we also need to look for interface methods -
            // because the class may be abstract
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getMethods());
            }

            currentClass = currentClass.getSuperclass();
        }

        Collection<Method> methods = uniqueMethods.values();
        return methods.toArray(new Method[0]);
    }

    private void addUniqueMethods(Map<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            if (!currentMethod.isBridge()) {
                String signature = getSignature(currentMethod);
                if (!uniqueMethods.containsKey(signature)) {
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    private String getSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getName());
        if (method.getParameterCount() > 0) {
            Class<?>[] parameters = method.getParameterTypes();
            for (int i = 0; i < parameters.length; i++) {
                if (i == 0) {
                    sb.append(':');
                } else {
                    sb.append(',');
                }
                sb.append(parameters[i].getName());
            }
        }
        return sb.toString();
    }

    private static String dropCase(String name) {
        if (name.startsWith("is")) {
            name = name.substring(2);
        } else if (name.startsWith("get") || name.startsWith("set")) {
            name = name.substring(3);
        } else {
            throw new IllegalArgumentException("Error parsing property name '" + name +
                    "'; Didn't start with 'is', 'get' or 'set'");
        }
        if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1)))) {
            name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
        }
        return name;
    }

    /**
     * Gets the setter for a property as a Method object.
     *
     * @param name the name of the property
     * @return the setter method
     * @throws NoSuchMethodException when a setter method cannot be found
     */
    public Method getSetter(String name) throws NoSuchMethodException {
        Method method = writeMethods.get(name);
        if (method == null) {
            throw new NoSuchMethodException("There is no WRITABLE property named '" + name +
                    "' in class '" + className + "'");
        }
        return method;
    }

    /**
     * Gets the getter for a property as a Method object.
     *
     * @param name the name of the property
     * @return the getter Method
     * @throws NoSuchMethodException when a getter method cannot be found
     */
    public Method getGetter(String name) throws NoSuchMethodException {
        Method method = readMethods.get(name);
        if (method == null) {
            throw new NoSuchMethodException("There is no READABLE property named '" + name +
                    "' in class '" + className + "'");
        }
        return method;
    }

    /**
     * Gets the type for a property setter.
     *
     * @param name the name of the property
     * @return the Class of the property setter
     * @throws NoSuchMethodException when a getter method cannot be found
     */
    public Class<?> getSetterType(String name) throws NoSuchMethodException {
        Class<?> clazz = writeType.get(name);
        if (clazz == null) {
            throw new NoSuchMethodException("There is no WRITABLE property named '" + name +
                    "' in class '" + className + "'");
        }
        return clazz;
    }

    /**
     * Gets the type for a property getter.
     *
     * @param name the name of the property
     * @return the Class of the property getter
     * @throws NoSuchMethodException when a getter method cannot be found
     */
    public Class<?> getGetterType(String name) throws NoSuchMethodException {
        Class<?> clazz = readTypes.get(name);
        if (clazz == null) {
            throw new NoSuchMethodException("There is no READABLE property named '" + name +
                    "' in class '" + className + "'");
        }
        return clazz;
    }

    /**
     * Gets an array of the readable properties for an object.
     *
     * @return the array
     */
    public String[] getReadablePropertyNames() {
        return readablePropertyNames;
    }

    public String[] getReadablePropertyNamesWithoutNonSerializable() {
        return serializableReadablePropertyNames;
    }

    /**
     * Gets an array of the writable properties for an object.
     *
     * @return the array
     */
    public String[] getWritablePropertyNames() {
        return writablePropertyNames;
    }

    /**
     * Check to see if a class has a writable property by name.
     *
     * @param propertyName the name of the property to check
     * @return true if the object has a writable property by the name
     */
    public boolean hasWritableProperty(String propertyName) {
        return writeMethods.keySet().contains(propertyName);
    }

    /**
     * Check to see if a class has a readable property by name.
     *
     * @param propertyName the name of the property to check
     * @return true if the object has a readable property by the name
     */
    public boolean hasReadableProperty(String propertyName) {
        return readMethods.keySet().contains(propertyName);
    }

    /**
     * Gets the class methods' names that is unique.
     *
     * @return the distinct method names
     */
    public String[] getDistinctMethodNames() {
        return distinctMethodNames;
    }

    /**
     * Gets an instance of ClassDescriptor for the specified class.
     *
     * @param clazz the class for which to lookup the method cache
     * @return the method cache for the class
     */
    public static BeanDescriptor getInstance(Class<?> clazz) {
        if (cacheEnabled) {
            BeanDescriptor cached = cache.get(clazz);
            if (cached == null) {
                cached = new BeanDescriptor(clazz);
                cache.put(clazz, cached);
            }
            return cached;
        } else {
            return new BeanDescriptor(clazz);
        }
    }

    /**
     * You can manually turn off caching enabled by default.
     *
     * @param cacheEnabling if true, turn caching on; otherwise, turn off caching
     */
    public static synchronized void setCacheEnabled(boolean cacheEnabling) {
        cacheEnabled = cacheEnabling;
        if (!cacheEnabled) {
            clearCache();
        }
    }

    /**
     *
     * Clear the ClassDescriptor cache.
     *
     * @return the number of cached ClassDescriptor cleared
     */
    public static synchronized int clearCache() {
        final int size = cache.size();
        cache.clear();
        return size;
    }

}
