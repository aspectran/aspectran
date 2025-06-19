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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * BeanUtils provides methods that allow simple, reflective access to
 * JavaBeans style properties. Methods are provided for object types.
 *
 * <p>Created: 2008. 04. 22 PM 3:47:15</p>
 */
public abstract class BeanUtils {

    /** An empty immutable {@code Object} array. */
    static final Object[] NO_ARGUMENTS = new Object[0];

    /**
     * Return the value of the specified property of the specified bean,
     * no matter which property reference format is used, with no type
     * conversions.
     * @param bean the bean whose property is to be extracted
     * @param name possibly indexed and/or nested name of the property to be extracted
     * @return the property value (as an Object)
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static Object getProperty(Object bean, @NonNull String name) throws InvocationTargetException {
        Object value;
        if (bean instanceof Properties props) {
            value = getProperty(props, name);
            if (value != null) {
                return value;
            }
        }
        if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            value = bean;
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
     * @param bean the bean whose property is to be extracted
     * @param name the name of the property to be extracted that does not allow nesting
     * @return the property value (as an Object)
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static Object getSimpleProperty(Object bean, @NonNull String name) throws InvocationTargetException {
        try {
            Object value;
            if (name.contains("[")) {
                value = getIndexedProperty(bean, name);
            } else if (bean instanceof Properties props) {
                value = getProperty(props, name);
            } else if (bean instanceof Map<?, ?> map) {
                value = map.get(name);
            } else {
                BeanDescriptor bd = BeanDescriptor.getInstance(bean.getClass());
                Method method = bd.getGetter(name);
                try {
                    value = method.invoke(bean, NO_ARGUMENTS);
                } catch (Throwable t) {
                    throw ExceptionUtils.unwrapThrowable(t);
                }
            }
            return value;
        } catch (InvocationTargetException e) {
            throw e;
        } catch (Throwable t) {
            if (bean == null) {
                throw new InvocationTargetException(t, "Could not get property '" + name +
                        "' from null reference. Cause: " + t);
            } else {
                throw new InvocationTargetException(t, "Could not get property '" + name +
                        "' from " + bean.getClass().getName() + ". Cause: " + t);
            }
        }
    }

    private static Object getProperty(@NonNull Properties props, @NonNull String name) {
        Object value = props.get(name);
        if (value == null) {
            // Allow for defaults fallback or potentially overridden accessor...
            value = props.getProperty(name);
        }
        return value;
    }

    /**
     * Sets the value of the specified property of the specified bean.
     * @param bean the bean whose property is to be modified
     * @param name possibly indexed and/or nested name of the property to be modified
     * @param value the value to which this property is to be set
     * @throws InvocationTargetException if the property accessor method throws an exception
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static void setProperty(Object bean, @NonNull String name, Object value)
            throws InvocationTargetException, NoSuchMethodException {
        if (bean instanceof Properties props) {
            props.put(name, value);
        } else if (name.contains(".")) {
            StringTokenizer parser = new StringTokenizer(name, ".");
            String newName = parser.nextToken();
            Object child = bean;
            while (parser.hasMoreTokens()) {
                Class<?> type = BeanTypeUtils.getPropertyTypeForSetter(child, newName);
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
                                    + type.getName() + ". Cause: " + e);
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
     * @param bean the bean whose property is to be modified
     * @param name the name of the property to be modified that does not allow nesting
     * @param value the value to which this property is to be set
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static void setSimpleProperty(Object bean, @NonNull String name, Object value) throws InvocationTargetException {
        if (bean instanceof Properties props) {
            props.put(name, value);
            return;
        }
        try {
            if (name.contains("[")) {
                setIndexedProperty(bean, name, value);
            } else {
                if (bean instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>)bean;
                    map.put(name, value);
                } else {
                    BeanDescriptor bd = BeanDescriptor.getInstance(bean.getClass());
                    Method method = bd.getSetter(name);
                    Object[] params = new Object[] { value };
                    try {
                        method.invoke(bean, params);
                    } catch (Throwable t) {
                        throw ExceptionUtils.unwrapThrowable(t);
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
                        value + "' for null reference. Cause: " + t);
            } else {
                throw new InvocationTargetException(t, "Could not set property '" + name + "' to value '" + value +
                        "' for " + bean.getClass().getName() + ". Cause: " + t);
            }
        }
    }

    public static Object getIndexedProperty(Object bean, @NonNull String indexedName) throws InvocationTargetException {
        try {
            String name = indexedName.substring(0, indexedName.indexOf("["));
            int index = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
            Object obj = (!name.isEmpty() ? getSimpleProperty(bean, name) : bean);
            Object value;
            if (obj instanceof List<?> list) {
                value = list.get(index);
            } else if (obj instanceof Object[] arr) {
                value = arr[index];
            } else if (obj instanceof char[] arr) {
                value = arr[index];
            } else if (obj instanceof boolean[] arr) {
                value = arr[index];
            } else if (obj instanceof byte[] arr) {
                value = arr[index];
            } else if (obj instanceof double[] arr) {
                value = arr[index];
            } else if (obj instanceof float[] arr) {
                value = arr[index];
            } else if (obj instanceof int[] arr) {
                value = arr[index];
            } else if (obj instanceof long[] arr) {
                value = arr[index];
            } else if (obj instanceof short[] arr) {
                value = arr[index];
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

    public static void setIndexedProperty(Object bean, @NonNull String indexedName, Object value)
            throws InvocationTargetException {
        try {
            String name = indexedName.substring(0, indexedName.indexOf("["));
            int index = Integer.parseInt(indexedName.substring(indexedName.indexOf("[") + 1, indexedName.indexOf("]")));
            Object obj = getSimpleProperty(bean, name);
            if (obj instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>)obj;
                list.set(index, value);
            } else if (obj instanceof Object[] arr) {
                arr[index] = value;
            } else if (obj instanceof char[] arr) {
                arr[index] = (Character)value;
            } else if (obj instanceof boolean[] arr) {
                arr[index] = (Boolean)value;
            } else if (obj instanceof byte[] arr) {
                arr[index] = (Byte)value;
            } else if (obj instanceof double[] arr) {
                arr[index] = (Double)value;
            } else if (obj instanceof float[] arr) {
                arr[index] = (Float)value;
            } else if (obj instanceof int[] arr) {
                arr[index] = (Integer)value;
            } else if (obj instanceof long[] arr) {
                arr[index] = (Long)value;
            } else if (obj instanceof short[] arr) {
                arr[index] = (Short)value;
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
     * Checks to see if a bean has a readable property be a given name.
     * @param bean the bean to check
     * @param name the property name to check for
     * @return true if the property exists and is readable
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static boolean hasReadableProperty(Object bean, @NonNull String name) throws NoSuchMethodException {
        boolean exists = false;
        if (bean instanceof Map<?, ?>) {
            exists = true; // ((Map)bean).containsKey(propertyName);
        } else {
            if (name.contains(".")) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                Class<?> type = bean.getClass();
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = BeanDescriptor.getInstance(type).getGetterType(name);
                    exists = BeanDescriptor.getInstance(type).hasReadableProperty(name);
                }
            } else {
                exists = BeanDescriptor.getInstance(bean.getClass()).hasReadableProperty(name);
            }
        }
        return exists;
    }

    /**
     * Checks to see if a bean has a writable property be a given name.
     * @param bean the bean to check
     * @param name the property name to check for
     * @return true if the property exists and is writable
     * @throws NoSuchMethodException if an accessor method for this property cannot be found
     */
    public static boolean hasWritableProperty(Object bean, @NonNull String name) throws NoSuchMethodException {
        boolean exists = false;
        if (bean instanceof Map<?, ?>) {
            exists = true; // ((Map)bean).containsKey(propertyName);
        } else {
            if (name.contains(".")) {
                StringTokenizer parser = new StringTokenizer(name, ".");
                Class<?> type = bean.getClass();
                while (parser.hasMoreTokens()) {
                    name = parser.nextToken();
                    type = BeanDescriptor.getInstance(type).getGetterType(name);
                    exists = BeanDescriptor.getInstance(type).hasWritableProperty(name);
                }
            } else {
                exists = BeanDescriptor.getInstance(bean.getClass()).hasWritableProperty(name);
            }
        }
        return exists;
    }

    /**
     * Returns an array of the readable properties exposed by a bean.
     * @param bean the bean
     * @return the readable properties
     */
    public static String[] getReadablePropertyNames(@NonNull Object bean) {
        return BeanDescriptor.getInstance(bean.getClass()).getReadablePropertyNames();
    }

    /**
     * Returns an array of readable properties exposed by the bean,
     * except those specified by NonSerializable.
     * @param bean the bean
     * @return the readable properties without non-serializable
     */
    public static String[] getReadablePropertyNamesWithoutNonSerializable(@NonNull Object bean) {
        return BeanDescriptor.getInstance(bean.getClass()).getReadablePropertyNamesWithoutNonSerializable();
    }

    /**
     * Returns an array of the writable properties exposed by a bean.
     * @param bean the bean
     * @return the properties
     */
    public static String[] getWritablePropertyNames(@NonNull Object bean) {
        return BeanDescriptor.getInstance(bean.getClass()).getWritablePropertyNames();
    }

}
