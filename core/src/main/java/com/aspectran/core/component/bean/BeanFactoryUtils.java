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
package com.aspectran.core.component.bean;

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.ReflectionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * <p>Created: 2024. 1. 12.</p>
 */
public abstract class BeanFactoryUtils {

    @NonNull
    public static Object newInstance(@NonNull Class<?> beanClass, Object[] args, Class<?>[] argTypes) {
        if (beanClass.isInterface()) {
            throw new BeanInstantiationException(beanClass, "Specified class is an interface");
        }
        Constructor<?> constructorToUse;
        try {
            constructorToUse = getMatchConstructor(beanClass, args);
            if (constructorToUse == null) {
                constructorToUse = ClassUtils.findConstructor(beanClass, argTypes);
            }
        } catch (NoSuchMethodException e) {
            throw new BeanInstantiationException(beanClass, "No default constructor found", e);
        }
        return newInstance(constructorToUse, args);
    }

    @NonNull
    public static Object newInstance(Class<?> beanClass) {
        return newInstance(beanClass, MethodUtils.EMPTY_OBJECT_ARRAY, MethodUtils.EMPTY_CLASS_PARAMETERS);
    }

    @NonNull
    private static Object newInstance(@NonNull Constructor<?> ctor, Object[] args) {
        try {
            if (ObjectUtils.isEmpty(args)) {
                return ctor.newInstance(args);
            } else {
                Class<?>[] parameterTypes = ctor.getParameterTypes();
                boolean casting = false;
                for (Class<?> paramType : parameterTypes) {
                    if (paramType.isArray()) {
                        casting = true;
                        break;
                    }
                }
                if (casting) {
                    Object[] newArgs = Arrays.copyOf(args, args.length);
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (newArgs[i] instanceof Object[] arr) {
                            Class<?> paramType = parameterTypes[i];
                            Class<?> componentType = paramType.getComponentType();
                            int len = arr.length;
                            Object[] newArr = (Object[])Array.newInstance(componentType, len);
                            System.arraycopy(arr, 0, newArr, 0, len);
                            newArgs[i] = newArr;
                        }
                    }
                    return ctor.newInstance(newArgs);
                } else {
                    return ctor.newInstance(args);
                }
            }
        } catch (InstantiationException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Is it an abstract class?", e);
        } catch (IllegalAccessException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Has the class definition changed? Is the constructor accessible?", e);
        } catch (IllegalArgumentException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Illegal arguments for constructor " + ctor, e);
        } catch (InvocationTargetException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Constructor threw exception", e.getTargetException());
        } catch (Exception e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Constructor threw exception", e);
        }
    }

    private static Constructor<?> getMatchConstructor(@NonNull Class<?> beanClass, Object[] args) {
        Constructor<?>[] candidates = beanClass.getDeclaredConstructors();
        Constructor<?> constructorToUse = null;
        float bestMatchWeight = Float.MAX_VALUE;
        float matchWeight;
        for (Constructor<?> candidate : candidates) {
            matchWeight = ReflectionUtils.getTypeDifferenceWeight(candidate.getParameterTypes(), args);
            if (matchWeight < bestMatchWeight) {
                constructorToUse = candidate;
                bestMatchWeight = matchWeight;
            }
        }
        return constructorToUse;
    }

}
