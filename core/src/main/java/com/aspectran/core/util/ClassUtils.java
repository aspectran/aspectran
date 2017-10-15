/*
 * Copyright (c) 2008-2017 The Aspectran Project
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
     * @throws IllegalArgumentException If instantiation fails for any reason;
     *      except for cases where constructor throws an unchecked exception
     *      (which will be passed as is)
     */
    public static <T> T createInstance(Class<T> cls, Class<?>... parameterTypes) {
        Constructor<T> ctor;
        try {
            ctor = findConstructor(cls, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + cls.getName() + " has no default (no arg) constructor");
        }
        try {
            return ctor.newInstance();
        } catch (Exception e) {
            ExceptionUtils.unwrapAndThrowAsIAE(e, "Failed to instantiate class " + cls.getName() + ", problem: " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtain an accessible constructor for the given class and parameters.
     *
     * @param cls the class to check
     * @param parameterTypes the parameter types of the desired constructor
     * @return the constructor reference
     * @throws NoSuchMethodException if no such constructor exists
     */
    public static <T> Constructor<T> findConstructor(Class<T> cls, Class<?>... parameterTypes) throws NoSuchMethodException {
        Constructor<T> ctor;
        try {
            ctor = cls.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to find default constructor of class " + cls.getName() +
                    ", problem: " + e.getMessage(), ExceptionUtils.getRootCause(e));
        }
        // must be public
        if (!Modifier.isPublic(ctor.getModifiers())) {
            throw new IllegalArgumentException("Default constructor for " + cls.getName() + " is not accessible (non-public?): not allowed to try modify access via Reflection: can not instantiate type");
        }
        return ctor;
    }

}
