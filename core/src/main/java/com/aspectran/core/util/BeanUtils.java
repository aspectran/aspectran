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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * BeanUtils provides methods that allow simple, reflective access to
 * JavaBeans style properties.  Methods are provided for all simple types as
 * well as object types.
 * 
 * <p>Created: 2008. 04. 22 PM 3:47:15</p>
 */
public class BeanUtils {

    /** An empty immutable {@code Object} array. */
    private static final Object[] NO_ARGUMENTS = new Object[0];

    /**
     * Invokes the static method of the specified class to get the bean property value.
     *
     * @param beanClass the class for which to lookup
     * @param name the property name
     * @return the property value (as an Object)
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static Object getProperty(Class<?> beanClass, String name) throws InvocationTargetException {
        try {
            BeanDescriptor bd = getBeanDescriptor(beanClass);
            Method method = bd.getGetter(name);
            Object bean = null;
            if (!Modifier.isStatic(method.getModifiers())) {
                bean = ClassUtils.createInstance(beanClass);
            }
            try {
                return method.invoke(bean, NO_ARGUMENTS);
            } catch (Throwable t) {
                throw unwrapThrowable(t);
            }
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            throw new InvocationTargetException(t, "Could not get property '" + name +
                    "' from " + beanClass.getName() + ". Cause: " + t.toString());
        }
    }

    /**
     * Invokes the static method of the specified class to get the bean property value.
     *
     * @param beanClass the class for which to lookup
     * @param name the property name
     * @param value the value to which this property is to be set
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static void setProperty(Class<?> beanClass, String name, Object value) throws InvocationTargetException {
        Object bean = null;
        try {
            BeanDescriptor bd = getBeanDescriptor(beanClass);
            Method method = bd.getSetter(name);
            Object[] params = new Object[] { value };
            if (!Modifier.isStatic(method.getModifiers())) {
                bean = ClassUtils.createInstance(beanClass);
            }
            try {
                method.invoke(bean, params);
            } catch (Throwable t) {
                throw unwrapThrowable(t);
            }
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            try {
                if (bean != null) {
                    MethodUtils.invokeSetter(bean, name, value);
                } else {
                    MethodUtils.invokeStaticMethod(beanClass, name, value);
                }
                return;
            } catch (Throwable tt) {
                //ignore
            }
            throw new InvocationTargetException(t, "Could not set property '" + name + "' to value '" + value +
                    "' for " + beanClass.getName() + ". Cause: " + t.toString());
        }
    }

    public static boolean hasReadableProperty(Class<?> beanClass, String name) {
        try {
            BeanDescriptor bd = getBeanDescriptor(beanClass);
            bd.getGetter(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasWritableProperty(Class<?> beanClass, String name) {
        try {
            BeanDescriptor bd = getBeanDescriptor(beanClass);
            bd.getSetter(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasStaticReadableProperty(Class<?> beanClass, String name) {
        try {
            BeanDescriptor bd = getBeanDescriptor(beanClass);
            Method method = bd.getGetter(name);
            return Modifier.isStatic(method.getModifiers());
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasStaticWritableProperty(Class<?> beanClass, String name) {
        try {
            BeanDescriptor bd = getBeanDescriptor(beanClass);
            Method method = bd.getSetter(name);
            return Modifier.isStatic(method.getModifiers());
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * Return the value of the specified property of the specified bean,
     * no matter which property reference format is used, with no type
     * conversions.
     *
     * @param bean the bean whose property is to be extracted
     * @param name possibly indexed and/or nested name of the property to be extracted
     * @return the property value (as an Object)
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static Object getProperty(Object bean, String name) throws InvocationTargetException {
        if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            Object value = bean;
            while (parser.hasMoreTokens()) {
                value = getSimpleProperty(value, parser.nextToken());
                if (value == null) {
                    break;
                }
            }
            return value;
        } else {
            return getSimpleProperty(bean, name);
        }
    }

    /**
     * Return the value of the specified property of the specified bean,
     * no matter which property reference format is used, with no type
     * conversions.
     *
     * @param bean the bean whose property is to be extracted
     * @param name the name of the property to be extracted that does not allow nesting
     * @return the property value (as an Object)
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static Object getSimpleProperty(Object bean, String name) throws InvocationTargetException {
        try {
            Object value;
            if (name.contains("[")) {
                value = getIndexedProperty(bean, name);
            } else {
                if (bean instanceof Map<?, ?>) {
                    value = ((Map<?, ?>)bean).get(name);
                } else {
                    BeanDescriptor bd = getBeanDescriptor(bean.getClass());
                    Method method = bd.getGetter(name);
                    try {
                        value = method.invoke(bean, NO_ARGUMENTS);
                    } catch (Throwable t) {
                        throw unwrapThrowable(t);
                    }
                }
            }
            return value;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            if (bean == null) {
                throw new InvocationTargetException(t, "Could not get property '" + name +
                        "' from null reference. Cause: " + t.toString());
            } else {
                throw new InvocationTargetException(t, "Could not get property '" + name +
                        "' from " + bean.getClass().getName() + ". Cause: " + t.toString());
            }
        }
    }

    /**
     * Sets the value of the specified property of the specified bean.
     *
     * @param bean the bean whose property is to be modified
     * @param name possibly indexed and/or nested name of the property to be modified
     * @param value the value to which this property is to be set
     * @throws InvocationTargetException if the property accessor method throws an exception
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static void setProperty(Object bean, String name, Object value)
            throws InvocationTargetException, NoSuchMethodException {
        if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            String newName = parser.nextToken();
            Object child = bean;
            while (parser.hasMoreTokens()) {
                Class<?> type = getPropertyTypeForSetter(child, newName);
                Object parent = child;
                child = getSimpleProperty(parent, newName);
                if (child == null) {
                    if (value == null) {
                        return; // don't instantiate child path if value is null
                    } else {
                        try {
                            child = ClassUtils.createInstance(type);
                            setProperty(parent, newName, child);
                        } catch (Exception e) {
                            throw new InvocationTargetException(e, "Cannot set value of property '" + name
                                    + "' because '" + newName + "' is null and cannot be instantiated on instance of "
                                    + type.getName() + ". Cause: " + e.toString());
                        }
                    }
                }
                newName = parser.nextToken();
            }
            setSimpleProperty(child, newName, value);
        } else {
            setSimpleProperty(bean, name, value);
        }
    }

    /**
     * Sets the value of the specified property of the specified bean.
     *
     * @param bean the bean whose property is to be modified
     * @param name the name of the property to be modified that does not allow nesting
     * @param value the value to which this property is to be set
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static void setSimpleProperty(Object bean, String name, Object value) throws InvocationTargetException {
        try {
            if (name.contains("[")) {
                setIndexedProperty(bean, name, value);
            } else {
                if (bean instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>)bean;
                    map.put(name, value);
                } else {
                    BeanDescriptor bd = getBeanDescriptor(bean.getClass());
                    Method method = bd.getSetter(name);
                    Object[] params = new Object[] { value };
                    try {
                        method.invoke(bean, params);
                    } catch (Throwable t) {
                        throw unwrapThrowable(t);
                    }
                }
            }
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            try {
                MethodUtils.invokeSetter(bean, name, value);
                return;
            } catch (Throwable tt) {
                //ignore
            }
            if (bean == null) {
                throw new InvocationTargetException(t, "Could not set property '" + name + "' to value '" +
                        value + "' for null reference. Cause: " + t.toString());
            } else {
                throw new InvocationTargetException(t, "Could not set property '" + name + "' to value '" + value +
                        "' for " + bean.getClass().getName() + ". Cause: " + t.toString());
            }
        }
    }

    public static Object getIndexedProperty(Object bean, String indexedName) throws InvocationTargetException {
        try {
            String name = indexedName.substring(0, indexedName.indexOf("["));
            int index = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
            Object list = (name.length() > 0 ? getSimpleProperty(bean, name) : bean);
            Object value;

            if (list instanceof List<?>) {
                value = ((List<?>)list).get(index);
            } else if (list instanceof Object[]) {
                value = ((Object[])list)[index];
            } else if (list instanceof char[]) {
                value = ((char[])list)[index];
            } else if (list instanceof boolean[]) {
                value = ((boolean[])list)[index];
            } else if (list instanceof byte[]) {
                value = ((byte[])list)[index];
            } else if (list instanceof double[]) {
                value = ((double[])list)[index];
            } else if (list instanceof float[]) {
                value = ((float[]) list)[index];
            } else if (list instanceof int[]) {
                value = ((int[])list)[index];
            } else if (list instanceof long[]) {
                value = ((long[])list)[index];
            } else if (list instanceof short[]) {
                value = ((short[])list)[index];
            } else {
                throw new IllegalArgumentException("The '" + name + "' property of the " +
                        bean.getClass().getName() + " class is not a List or Array");
            }

            return value;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e, "Error getting ordinal list from JavaBean. Cause: " + e);
        }
    }

    public static Class<?> getIndexedType(Object bean, String indexedName) throws InvocationTargetException {
        try {
            String name = indexedName.substring(0, indexedName.indexOf("["));
            int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
            Object list = (name.length() > 0 ? getSimpleProperty(bean, name) : bean);
            Class<?> value;

            if (list instanceof List<?>) {
                value = ((List<?>)list).get(i).getClass();
            } else if (list instanceof Object[]) {
                value = ((Object[])list)[i].getClass();
            } else if (list instanceof char[]) {
                value = Character.class;
            } else if (list instanceof boolean[]) {
                value = Boolean.class;
            } else if (list instanceof byte[]) {
                value = Byte.class;
            } else if (list instanceof double[]) {
                value = Double.class;
            } else if (list instanceof float[]) {
                value = Float.class;
            } else if (list instanceof int[]) {
                value = Integer.class;
            } else if (list instanceof long[]) {
                value = Long.class;
            } else if (list instanceof short[]) {
                value = Short.class;
            } else {
                throw new IllegalArgumentException("The '" + name + "' property of the " +
                        bean.getClass().getName() + " class is not a List or Array");
            }

            return value;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e, "Error getting ordinal list from JavaBean. Cause: " + e);
        }
    }

    public static void setIndexedProperty(Object bean, String indexedName, Object value) throws InvocationTargetException {
        try {
            String name = indexedName.substring(0, indexedName.indexOf("["));
            int index = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
            Object list = getSimpleProperty(bean, name);

            if (list instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Object> l = (List<Object>)list;
                l.set(index, value);
            } else if (list instanceof Object[]) {
                ((Object[])list)[index] = value;
            } else if (list instanceof char[]) {
                ((char[])list)[index] = (Character)value;
            } else if (list instanceof boolean[]) {
                ((boolean[])list)[index] = (Boolean)value;
            } else if (list instanceof byte[]) {
                ((byte[])list)[index] = (Byte)value;
            } else if (list instanceof double[]) {
                ((double[])list)[index] = (Double)value;
            } else if (list instanceof float[]) {
                ((float[])list)[index] = (Float)value;
            } else if (list instanceof int[]) {
                ((int[])list)[index] = (Integer)value;
            } else if (list instanceof long[]) {
                ((long[])list)[index] = (Long)value;
            } else if (list instanceof short[]) {
                ((short[])list)[index] = (Short)value;
            } else {
                throw new IllegalArgumentException("The '" + name + "' property of the " +
                        bean.getClass().getName() + " class is not a List or Array");
            }
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Exception e) {
            throw new InvocationTargetException(e, "Error getting ordinal value from JavaBean. Cause: " + e);
        }
    }

    /**
     * Checks to see if a bean has a writable property be a given name.
     *
     * @param bean the bean to check
     * @param name the property name to check for
     * @return true if the property exists and is writable
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static boolean hasWritableProperty(Object bean, String name) throws NoSuchMethodException {
        boolean exists = false;
        if (bean instanceof Map<?, ?>) {
            exists = true; // ((Map)bean).containsKey(propertyName);
        } else {
            if (name.contains(".")) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                Class<?> type = bean.getClass();
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = getBeanDescriptor(type).getGetterType(name);
                    exists = getBeanDescriptor(type).hasWritableProperty(name);
                }
            } else {
                exists = getBeanDescriptor(bean.getClass()).hasWritableProperty(name);
            }
        }
        return exists;
    }

    /**
     * Checks to see if a bean has a readable property be a given name.
     *
     * @param bean the bean to check
     * @param name the property name to check for
     * @return true if the property exists and is readable
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static boolean hasReadableProperty(Object bean, String name) throws NoSuchMethodException {
        boolean exists = false;
        if (bean instanceof Map<?, ?>) {
            exists = true; // ((Map)bean).containsKey(propertyName);
        } else {
            if (name.contains(".")) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                Class<?> type = bean.getClass();
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = getBeanDescriptor(type).getGetterType(name);
                    exists = getBeanDescriptor(type).hasReadableProperty(name);
                }
            } else {
                exists = getBeanDescriptor(bean.getClass()).hasReadableProperty(name);
            }
        }
        return exists;
    }

    /**
     * Examines a Throwable object and gets it's root cause
     *
     * @param t the exception to examine
     * @return the root cause
     */
    private static Throwable unwrapThrowable(Throwable t) {
        Throwable t2 = t;
        while (true) {
            if (t2 instanceof InvocationTargetException) {
                t2 = ((InvocationTargetException)t2).getTargetException();
            } else if (t2 instanceof UndeclaredThrowableException) {
                t2 = ((UndeclaredThrowableException)t2).getUndeclaredThrowable();
            } else {
                return t2;
            }
        }
    }

    /**
     * Returns the class that the setter expects to receive as a parameter when
     * setting a property value.
     *
     * @param bean the bean to check
     * @param name the name of the property
     * @return the type of the property
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static Class<?> getPropertyTypeForSetter(Object bean, String name) throws NoSuchMethodException {
        Class<?> type = bean.getClass();
        if (bean instanceof Class<?>) {
            type = getClassPropertyTypeForSetter((Class<?>)bean, name);
        } else if (bean instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>)bean;
            Object value = map.get(name);
            if (value == null) {
                type = Object.class;
            } else {
                type = value.getClass();
            }
        } else {
            if (name.contains(".")) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = getBeanDescriptor(type).getSetterType(name);
                }
            } else {
                type = getBeanDescriptor(type).getSetterType(name);
            }
        }
        return type;
    }

    /**
     * Returns the class that the getter will return when reading a property value.
     *
     * @param bean the bean to check
     * @param name the name of the property
     * @return the type of the property
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static Class<?> getPropertyTypeForGetter(Object bean, String name) throws NoSuchMethodException {
        Class<?> type = bean.getClass();
        if (bean instanceof Class<?>) {
            type = getClassPropertyTypeForGetter((Class<?>)bean, name);
        } else if (bean instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>)bean;
            Object value = map.get(name);
            if (value == null) {
                type = Object.class;
            } else {
                type = value.getClass();
            }
        } else {
            if (name.contains(".")) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = getBeanDescriptor(type).getGetterType(name);
                }
            } else {
                type = getBeanDescriptor(type).getGetterType(name);
            }
        }
        return type;
    }

    /**
     * Returns the class that the getter will return when reading a property value.
     *
     * @param type the class to check
     * @param name the name of the property
     * @return the type of the property
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static Class<?> getClassPropertyTypeForGetter(Class<?> type, String name) throws NoSuchMethodException {
        if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            while (parser.hasMoreTokens()) {
                name = parser.nextToken();
                type = getBeanDescriptor(type).getGetterType(name);
            }
        } else {
            type = getBeanDescriptor(type).getGetterType(name);
        }
        return type;
    }

    /**
     * Returns the class that the setter expects to receive as a parameter when
     * setting a property value.
     *
     * @param type The class to check
     * @param name the name of the property
     * @return the type of the property
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static Class<?> getClassPropertyTypeForSetter(Class<?> type, String name) throws NoSuchMethodException {
        if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            while (parser.hasMoreTokens()) {
                name = parser.nextToken();
                type = getBeanDescriptor(type).getSetterType(name);
            }
        } else {
            type = getBeanDescriptor(type).getSetterType(name);
        }
        return type;
    }

    /**
     * Returns an array of the readable properties exposed by a bean.
     *
     * @param bean the bean
     * @return the readable properties
     */
    public static String[] getReadablePropertyNames(Object bean) {
        return getBeanDescriptor(bean.getClass()).getReadablePropertyNames();
    }

    /**
     * Returns an array of readable properties exposed by the bean,
     * except those specified by NonSerializable.
     *
     * @param bean the bean
     * @return the readable properties without non serializable
     */
    public static String[] getReadablePropertyNamesWithoutNonSerializable(Object bean) {
        return getBeanDescriptor(bean.getClass()).getReadablePropertyNamesWithoutNonSerializable();
    }

    /**
     * Returns an array of the writable properties exposed by a bean.
     *
     * @param bean the bean
     * @return the properties
     */
    public static String[] getWritablePropertyNames(Object bean) {
        return getBeanDescriptor(bean.getClass()).getWritablePropertyNames();
    }

    /**
     * Gets an instance of BeanDescriptor for the specified class.
     *
     * @param beanClass the class for which to lookup the ClassDescriptor cache.
     * @return the ClassDescriptor cache for the class
     */
    private static BeanDescriptor getBeanDescriptor(Class<?> beanClass) {
        return BeanDescriptor.getInstance(beanClass);
    }

}
