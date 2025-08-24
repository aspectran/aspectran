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

import java.lang.annotation.Annotation;
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

    /** The nested class separator character: {@code '$'}. */
    private static final char NESTED_CLASS_SEPARATOR = '$';

    /** CGLIB or Javassist class separator: {@code "$$"}. */
    public static final String PROXY_CLASS_SEPARATOR = "$$";

    /**
     * Method that can be called to try to create an instance of specified type.
     * Instantiation is done using default no-argument constructor.
     * @param <T> the generic type
     * @param clazz the class to check
     * @return an instantiated object
     * @throws IllegalArgumentException if instantiation fails for any reason;
     *      except for cases where constructor throws an unchecked exception
     *      (which will be passed as is)
     */
    @NonNull
    public static <T> T createInstance(Class<T> clazz) {
        Constructor<T> ctor;
        try {
            ctor = findConstructor(clazz);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + clazz.getName() +
                    " has no default (no arg) constructor");
        }
        try {
            return ctor.newInstance();
        } catch (Exception e) {
            throw ExceptionUtils.unwrapAndThrowAsIAE(e, "Unable to instantiate class " +
                    clazz.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Method that can be called to try to create an instantiate of specified type.
     * @param <T> the generic type
     * @param clazz the class to check
     * @param args the arguments
     * @return an instantiated object
     * @throws IllegalArgumentException if instantiation fails for any reason;
     *      except for cases where constructor throws an unchecked exception
     *      (which will be passed as is)
     */
    @NonNull
    public static <T> T createInstance(Class<T> clazz, @NonNull Object... args) {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        return createInstance(clazz, args, argTypes);
    }

    /**
     * Method that can be called to try to create an instance of specified type.
     * @param <T> the generic type
     * @param clazz the class to check
     * @param args the arguments
     * @param argTypes the argument types of the desired constructor
     * @return an instantiated object
     * @throws IllegalArgumentException if instantiation fails for any reason;
     *      except for cases where constructor throws an unchecked exception
     *      (which will be passed as is)
     */
    @NonNull
    public static <T> T createInstance(Class<T> clazz, Object[] args, Class<?>[] argTypes) {
        Constructor<T> ctor;
        try {
            ctor = findConstructor(clazz, argTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + clazz.getName() +
                    " has no constructor which can accept the given arguments");
        }
        try {
            return ctor.newInstance(args);
        } catch (Exception e) {
            throw ExceptionUtils.unwrapAndThrowAsIAE(e, "Unable to instantiate class " + clazz.getName()
                    + ", problem: " + e.getMessage());
        }
    }

    /**
     * Get an accessible constructor for the given class and parameters.
     * @param clazz the class to check
     * @param argTypes the argument types of the desired constructor
     * @param <T> the generic type
     * @return the constructor reference
     * @throws NoSuchMethodException if no such constructor exists
     */
    @NonNull
    public static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>... argTypes)
            throws NoSuchMethodException {
        Assert.notNull(clazz, "cls must not be null");
        Constructor<T> ctor;
        try {
            ctor = clazz.getDeclaredConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to find constructor of class " + clazz.getName() +
                    ", problem: " + e.getMessage(), ExceptionUtils.getRootCause(e));
        }
        // must be public
        if (!Modifier.isPublic(ctor.getModifiers())) {
            throw new IllegalArgumentException("Constructor for " + clazz.getName() +
                    " is not accessible (non-public?): not allowed to try modify access via Reflection: can not instantiate type");
        }
        return ctor;
    }

    /**
     * Check whether the given class is visible in the given ClassLoader.
     * @param clazz the class to check (typically an interface)
     * @param classLoader the ClassLoader to check against
     *      (maybe {@code null} in which case this method will always return {@code true})
     * @return true if the given class is visible; otherwise false
     * @since 6.0.0
     */
    public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
        if (classLoader == null) {
            return true;
        }
        try {
            if (clazz.getClassLoader() == classLoader) {
                return true;
            }
        } catch (SecurityException ex) {
            // Fall through to loadable check below
        }

        // Visible if same Class can be loaded from given ClassLoader
        return isLoadable(clazz, classLoader);
    }

    /**
     * Check whether the given class is loadable in the given ClassLoader.
     * @param clazz the class to check (typically an interface)
     * @param classLoader the ClassLoader to check against
     * @return true if the given class is loadable; otherwise false
     * @since 6.0.0
     */
    private static boolean isLoadable(@NonNull Class<?> clazz, ClassLoader classLoader) {
        Assert.notNull(classLoader, "classLoader must not be null");
        try {
            return (clazz == classLoader.loadClass(clazz.getName()));
            // Else: different class with same name found
        } catch (ClassNotFoundException ex) {
            // No corresponding class found at all
            return false;
        }
    }

    /**
     * Determine if the supplied {@link Class} is a JVM-generated implementation
     * class for a lambda expression or method reference.
     * <p>This method makes a best-effort attempt at determining this, based on
     * checks that work on modern, mainstream JVMs.
     * @param clazz the class to check
     * @return {@code true} if the class is a lambda implementation class
     */
    public static boolean isLambdaClass(@NonNull Class<?> clazz) {
        return (clazz.isSynthetic() && (clazz.getSuperclass() == Object.class) &&
                (clazz.getInterfaces().length > 0) && clazz.getName().contains("$$Lambda"));
    }

    @NonNull
    public static <T> Class<T> classForName(String name) throws ClassNotFoundException {
        return classForName(name, getDefaultClassLoader());
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Class<T> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
        return (Class<T>)Class.forName(name, true, classLoader);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String name) throws ClassNotFoundException {
        return (Class<T>)getDefaultClassLoader().loadClass(name);
    }

    /**
     * Returns the default class loader within the current context.
     * If there is a context classloader it is returned, otherwise the classloader
     * which loaded the ClassUtils Class is returned.
     * @return the appropriate default classloader which is guaranteed to be non-null
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // ignore
        }
        if (cl == null) {
            cl = ClassUtils.class.getClassLoader();
        }
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    }

    /**
     * Return the user-defined class for the given class: usually simply the given
     * class, but the original class in the case of a CGLIB or Javassist-generated subclass.
     * @param clazz the class to check
     * @return the user-defined class
     * @see #PROXY_CLASS_SEPARATOR
     */
    @NonNull
    public static Class<?> getUserClass(@NonNull Class<?> clazz) {
        if (clazz.getName().contains(PROXY_CLASS_SEPARATOR)) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return superclass;
            }
        }
        return clazz;
    }

    public static boolean isAnnotationPresent(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            if (currentClass.isAnnotationPresent(annotationClass)) {
                return true;
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }

    /**
     * Get the class name without the qualified package name.
     * @param className the className to get the short name for
     * @return the class name of the class without the package name
     * @throws IllegalArgumentException if the className is empty
     */
    @NonNull
    public static String getShortName(String className) {
        Assert.hasLength(className, "Class name must not be empty");
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        int nameEndIndex = className.indexOf(PROXY_CLASS_SEPARATOR);
        if (nameEndIndex == -1) {
            nameEndIndex = className.length();
        }
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        shortName = shortName.replace(NESTED_CLASS_SEPARATOR, PACKAGE_SEPARATOR_CHAR);
        return shortName;
    }

    /**
     * Get the class name without the qualified package name.
     * @param clazz the class to get the short name for
     * @return the class name of the class without the package name
     */
    @NonNull
    public static String getShortName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return getShortName(clazz.getTypeName());
    }

}
