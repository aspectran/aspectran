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
package com.aspectran.core.component.bean;

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.ReflectionUtils;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Utility methods for instantiating and constructing bean objects.
 * <p>
 * Provides constructor resolution, reflective invocation, and convenience
 * helpers used by the bean factory/registry when creating beans.
 * </p>
 */
public abstract class BeanFactoryUtils {

    /**
     * Instantiate a class using its constructor that matches the given arguments.
     * @param beanClass the class to instantiate
     * @param args the arguments to pass to the constructor
     * @param argTypes the argument types to use for constructor resolution
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     */
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
            throw new BeanInstantiationException(beanClass, "No matching constructor found", e);
        }
        return newInstance(constructorToUse, args);
    }

    /**
     * Convenience method to instantiate a class using its no-arg constructor.
     * @param beanClass the class to instantiate
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     */
    @NonNull
    public static Object newInstance(Class<?> beanClass) {
        return newInstance(beanClass, MethodUtils.EMPTY_OBJECT_ARRAY, MethodUtils.EMPTY_CLASS_PARAMETERS);
    }

    @NonNull
    private static Object newInstance(@NonNull Constructor<?> ctor, Object[] args) {
        try {
            Object[] adaptedArgs = adaptArgumentsForInstantiation(ctor, args);
            return ctor.newInstance(adaptedArgs);
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

    /**
     * Adapts the given arguments to the constructor's parameter types, primarily for handling array type mismatches.
     * For example, converts an {@code Object[]} to a {@code String[]} if the constructor expects the latter.
     * @param ctor the constructor
     * @param args the original arguments
     * @return the adapted arguments, or the original arguments if no adaptation is needed
     */
    private static Object[] adaptArgumentsForInstantiation(@NonNull Constructor<?> ctor, Object[] args) {
        if (ObjectUtils.isEmpty(args)) {
            return args;
        }
        Class<?>[] parameterTypes = ctor.getParameterTypes();
        if (args.length != parameterTypes.length) {
            // Let the reflective newInstance call handle the argument mismatch error
            return args;
        }

        Object[] adaptedArgs = null; // Lazily create
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = args[i];
            Class<?> paramType = parameterTypes[i];

            // If parameter is an array and argument is an Object[], we may need to convert
            if (paramType.isArray() && arg instanceof Object[] arr) {
                // If the argument's array type is not assignable to the parameter's array type
                if (!paramType.isAssignableFrom(arr.getClass())) {
                    if (adaptedArgs == null) {
                        adaptedArgs = Arrays.copyOf(args, args.length);
                    }
                    Class<?> componentType = paramType.getComponentType();
                    Object[] newArr = (Object[])Array.newInstance(componentType, arr.length);
                    System.arraycopy(arr, 0, newArr, 0, arr.length);
                    adaptedArgs[i] = newArr;
                }
            }
        }
        return (adaptedArgs != null ? adaptedArgs : args);
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
