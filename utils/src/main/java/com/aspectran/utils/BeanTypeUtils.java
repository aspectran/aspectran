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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import static com.aspectran.utils.BeanUtils.NO_ARGUMENTS;

/**
 * BeanTypeUtils provides methods that allow simple, reflective access to
 * JavaBeans style properties. Methods are provided for all simple types.
 * This has been decoupled from BeanUtils.
 *
 * <p>Created: 2025-02-19</p>
 */
public abstract class BeanTypeUtils {

    /**
     * Invokes the static method of the specified class to get the bean property value.
     * @param type the class for which to lookup
     * @param name the property name
     * @return the property value (as an Object)
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static Object getProperty(Class<?> type, String name) throws InvocationTargetException {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(type);
            Method method = bd.getGetter(name);
            Object bean = null;
            if (!Modifier.isStatic(method.getModifiers())) {
                bean = ClassUtils.createInstance(type);
            }
            try {
                return method.invoke(bean, NO_ARGUMENTS);
            } catch (Throwable t) {
                throw ExceptionUtils.unwrapThrowable(t);
            }
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            throw new InvocationTargetException(t, "Could not get property '" + name +
                    "' from " + type.getName() + ". Cause: " + t);
        }
    }

    /**
     * Invokes the static method of the specified class to get the bean property value.
     * @param type the class for which to lookup
     * @param name the property name
     * @param value the value to which this property is to be set
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static void setProperty(Class<?> type, String name, Object value) throws InvocationTargetException {
        Object bean = null;
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(type);
            Method method = bd.getSetter(name);
            Object[] params = new Object[] { value };
            if (!Modifier.isStatic(method.getModifiers())) {
                bean = ClassUtils.createInstance(type);
            }
            try {
                method.invoke(bean, params);
            } catch (Throwable t) {
                throw ExceptionUtils.unwrapThrowable(t);
            }
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            try {
                if (bean != null) {
                    MethodUtils.invokeSetter(bean, name, value);
                } else {
                    MethodUtils.invokeStaticMethod(type, name, value);
                }
                return;
            } catch (Throwable tt) {
                //ignore
            }
            throw new InvocationTargetException(t, "Could not set property '" + name +
                    "' to value '" + value + "' for " + type.getName() + ". Cause: " + t);
        }
    }

    /**
     * Returns the class that the getter will return when reading a property value.
     * @param bean the bean to check
     * @param name the name of the property
     * @return the type of the property
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static Class<?> getPropertyTypeForGetter(@NonNull Object bean, String name) throws NoSuchMethodException {
        Class<?> type;
        if (bean instanceof Class<?> beanClass) {
            type = getClassPropertyTypeForGetter(beanClass, name);
        } else if (bean instanceof Map<?, ?> map) {
            type = getPropertyType(map, name);
        } else {
            type = getClassPropertyTypeForGetter(bean.getClass(), name);
        }
        return type;
    }

    /**
     * Returns the class that the getter will return when reading a property value.
     * @param type the class to check
     * @param name the name of the property
     * @return the type of the property
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static Class<?> getClassPropertyTypeForGetter(Class<?> type, @NonNull String name)
            throws NoSuchMethodException {
        if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            while (parser.hasMoreTokens()) {
                name = parser.nextToken();
                type = BeanDescriptor.getInstance(type).getGetterType(name);
            }
        } else {
            type = BeanDescriptor.getInstance(type).getGetterType(name);
        }
        return type;
    }

    /**
     * Returns the class that the setter expects to receive as a parameter when
     * setting a property value.
     * @param bean the bean to check
     * @param name the name of the property
     * @return the type of the property
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static Class<?> getPropertyTypeForSetter(@NonNull Object bean, String name) throws NoSuchMethodException {
        Class<?> type;
        if (bean instanceof Class<?> beanClass) {
            type = getClassPropertyTypeForSetter(beanClass, name);
        } else if (bean instanceof Map<?, ?> map) {
            type = getPropertyType(map, name);
        } else {
            type = getPropertyTypeForSetter(bean.getClass(), name);
        }
        return type;
    }

    /**
     * Returns the class that the setter expects to receive as a parameter when
     * setting a property value.
     * @param type The class to check
     * @param name the name of the property
     * @return the type of the property
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static Class<?> getClassPropertyTypeForSetter(Class<?> type, @NonNull String name)
            throws NoSuchMethodException {
        if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            while (parser.hasMoreTokens()) {
                name = parser.nextToken();
                type = BeanDescriptor.getInstance(type).getSetterType(name);
            }
        } else {
            type = BeanDescriptor.getInstance(type).getSetterType(name);
        }
        return type;
    }

    private static Class<?> getPropertyType(@NonNull Map<?, ?> map, String name) {
        Object value = map.get(name);
        if (value == null && map instanceof Properties props) {
            // Allow for defaults fallback or potentially overridden accessor...
            value = props.getProperty(name);
        }
        Class<?> type;
        if (value == null) {
            type = Object.class;
        } else {
            type = value.getClass();
        }
        return type;
    }

    public static Class<?> getIndexedType(Object bean, @NonNull String indexedName) throws InvocationTargetException {
        try {
            String name = indexedName.substring(0, indexedName.indexOf("["));
            int i = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
            Object obj = (!name.isEmpty() ? BeanUtils.getSimpleProperty(bean, name) : bean);
            Class<?> value;
            if (obj instanceof List<?> list) {
                value = list.get(i).getClass();
            } else if (obj instanceof Object[] arr) {
                value = arr[i].getClass();
            } else if (obj instanceof char[]) {
                value = Character.class;
            } else if (obj instanceof boolean[]) {
                value = Boolean.class;
            } else if (obj instanceof byte[]) {
                value = Byte.class;
            } else if (obj instanceof double[]) {
                value = Double.class;
            } else if (obj instanceof float[]) {
                value = Float.class;
            } else if (obj instanceof int[]) {
                value = Integer.class;
            } else if (obj instanceof long[]) {
                value = Long.class;
            } else if (obj instanceof short[]) {
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

    public static boolean hasReadableProperty(Class<?> type, String name) {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(type);
            bd.getGetter(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasWritableProperty(Class<?> type, String name) {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(type);
            bd.getSetter(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasStaticReadableProperty(Class<?> type, String name) {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(type);
            Method method = bd.getGetter(name);
            return Modifier.isStatic(method.getModifiers());
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasStaticWritableProperty(Class<?> type, String name) {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(type);
            Method method = bd.getSetter(name);
            return Modifier.isStatic(method.getModifiers());
        } catch (Throwable t) {
            return false;
        }
    }

}
