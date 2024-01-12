package com.aspectran.core.component.bean;

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.MethodUtils;
import com.aspectran.utils.ReflectionUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
            return ctor.newInstance(args);
        } catch (InstantiationException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Is it an abstract class?", e);
        } catch (IllegalAccessException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Has the class definition changed? Is the constructor accessible?", e);
        } catch (IllegalArgumentException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Illegal arguments for constructor", e);
        } catch (InvocationTargetException e) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Constructor threw exception", e.getTargetException());
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
