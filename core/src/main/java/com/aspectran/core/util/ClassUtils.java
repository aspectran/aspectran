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

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Miscellaneous class utility methods.
 */
public abstract class ClassUtils {

    /** The package separator character '.' */
    public static final char PACKAGE_SEPARATOR_CHAR = '.';

    /** The ".class" file suffix */
    public static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * Method that can be called to try to create an instantiate of
     * specified type. Instantiation is done using default no-argument
     * constructor.
     *
     * @param <T> the generic type
     * @param cls the class to check
     * @return an instantiated object
     * @throws IllegalArgumentException if instantiation fails for any reason;
     *      except for cases where constructor throws an unchecked exception
     *      (which will be passed as is)
     */
    public static <T> T createInstance(Class<T> cls) {
        Constructor<T> ctor;
        try {
            ctor = findConstructor(cls);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + cls.getName() +
                    " has no default (no arg) constructor");
        }
        try {
            return ctor.newInstance();
        } catch (Exception e) {
            ExceptionUtils.unwrapAndThrowAsIAE(e, "Unable to instantiate class " +
                    cls.getName() + ", problem: " + e.getMessage());
            return null;
        }
    }

    /**
     * Method that can be called to try to create an instantiate of
     * specified type.
     *
     * @param <T> the generic type
     * @param cls the class to check
     * @param args the arguments
     * @return an instantiated object
     * @throws IllegalArgumentException if instantiation fails for any reason;
     *      except for cases where constructor throws an unchecked exception
     *      (which will be passed as is)
     */
    public static <T> T createInstance(Class<T> cls, Object... args) {
        Class[] argTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        Constructor<T> ctor;
        try {
            ctor = findConstructor(cls, argTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + cls.getName() +
                    " has no constructor which can accept the given arguments");
        }
        try {
            return ctor.newInstance(args);
        } catch (Exception e) {
            ExceptionUtils.unwrapAndThrowAsIAE(e, "Unable to instantiate class " + cls.getName()
                    + ", problem: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtain an accessible constructor for the given class and parameters.
     *
     * @param cls the class to check
     * @param parameterTypes the parameter types of the desired constructor
     * @param <T> the generic type
     * @return the constructor reference
     * @throws NoSuchMethodException if no such constructor exists
     */
    public static <T> Constructor<T> findConstructor(Class<T> cls, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Constructor<T> ctor;
        try {
            ctor = cls.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to find constructor of class " + cls.getName() +
                    ", problem: " + e.getMessage(), ExceptionUtils.getRootCause(e));
        }
        // must be public
        if (!Modifier.isPublic(ctor.getModifiers())) {
            throw new IllegalArgumentException("Constructor for " + cls.getName() +
                    " is not accessible (non-public?): not allowed to try modify access via Reflection: can not instantiate type");
        }
        return ctor;
    }

}
