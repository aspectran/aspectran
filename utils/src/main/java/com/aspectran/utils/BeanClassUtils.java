/*
 * Copyright (c) 2008-2025 The Aspectran Project
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static com.aspectran.utils.BeanUtils.NO_ARGUMENTS;

/**
 * BeanClassUtils provides methods that allow simple, reflective access to
 * JavaBeans style properties. Methods are provided for all simple types.
 * This has been decoupled from BeanUtils.
 *
 * <p>Created: 2025-02-19</p>
 */
public abstract class BeanClassUtils {

    /**
     * Invokes the static method of the specified class to get the bean property value.
     * @param beanClass the class for which to lookup
     * @param name the property name
     * @return the property value (as an Object)
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static Object getProperty(Class<?> beanClass, String name) throws InvocationTargetException {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(beanClass);
            Method method = bd.getGetter(name);
            Object bean = null;
            if (!Modifier.isStatic(method.getModifiers())) {
                bean = ClassUtils.createInstance(beanClass);
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
                    "' from " + beanClass.getName() + ". Cause: " + t);
        }
    }

    /**
     * Invokes the static method of the specified class to get the bean property value.
     * @param beanClass the class for which to lookup
     * @param name the property name
     * @param value the value to which this property is to be set
     * @throws InvocationTargetException if the property accessor method throws an exception
     */
    public static void setProperty(Class<?> beanClass, String name, Object value) throws InvocationTargetException {
        Object bean = null;
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(beanClass);
            Method method = bd.getSetter(name);
            Object[] params = new Object[] { value };
            if (!Modifier.isStatic(method.getModifiers())) {
                bean = ClassUtils.createInstance(beanClass);
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
                    MethodUtils.invokeStaticMethod(beanClass, name, value);
                }
                return;
            } catch (Throwable tt) {
                //ignore
            }
            throw new InvocationTargetException(t, "Could not set property '" + name + "' to value '" + value +
                    "' for " + beanClass.getName() + ". Cause: " + t);
        }
    }

    public static boolean hasReadableProperty(Class<?> beanClass, String name) {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(beanClass);
            bd.getGetter(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasWritableProperty(Class<?> beanClass, String name) {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(beanClass);
            bd.getSetter(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasStaticReadableProperty(Class<?> beanClass, String name) {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(beanClass);
            Method method = bd.getGetter(name);
            return Modifier.isStatic(method.getModifiers());
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasStaticWritableProperty(Class<?> beanClass, String name) {
        try {
            BeanDescriptor bd = BeanDescriptor.getInstance(beanClass);
            Method method = bd.getSetter(name);
            return Modifier.isStatic(method.getModifiers());
        } catch (Throwable t) {
            return false;
        }
    }

}
