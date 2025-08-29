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
 * <p>Mainly for internal use within the framework.</p>
 */
public abstract class ClassUtils {

    /** The package separator character: {@code '.'}. */
    public static final char PACKAGE_SEPARATOR_CHAR = '.';

    /** The ".class" file suffix. */
    public static final String CLASS_FILE_SUFFIX = ".class";

    /** The nested class separator character: {@code '$'}. */
    private static final char NESTED_CLASS_SEPARATOR = '$';

    /** CGLIB or Javassist-generated class separator: {@code "$$"}. */
    public static final String PROXY_CLASS_SEPARATOR = "$$";

    /**
     * Creates an instance of the specified class using its default (no-argument) constructor.
     * @param <T> the generic type of the class
     * @param clazz the class to instantiate
     * @return a new instance of the class
     * @throws IllegalArgumentException if the class has no accessible default constructor,
     *      or if instantiation fails for any other reason
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
     * Creates an instance of the specified class using a constructor that matches the given arguments.
     * @param <T> the generic type of the class
     * @param clazz the class to instantiate
     * @param args the arguments to pass to the constructor
     * @return a new instance of the class
     * @throws IllegalArgumentException if no matching constructor is found,
     *      or if instantiation fails for any other reason
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
     * Creates an instance of the specified class using a constructor that matches the given arguments and types.
     * @param <T> the generic type of the class
     * @param clazz the class to instantiate
     * @param args the arguments to pass to the constructor
     * @param argTypes the argument types of the desired constructor
     * @return a new instance of the class
     * @throws IllegalArgumentException if no matching constructor is found,
     *      or if instantiation fails for any other reason
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
     * Finds an accessible constructor for the given class and parameter types.
     * The constructor must be public.
     * @param <T> the generic type of the class
     * @param clazz the class to find the constructor for
     * @param argTypes the parameter types of the desired constructor
     * @return the constructor reference
     * @throws NoSuchMethodException if no such public constructor exists
     * @throws IllegalArgumentException if a matching constructor is found but is not public
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
     * Checks whether the given class is visible in the given {@link ClassLoader}.
     * @param clazz the class to check (typically an interface)
     * @param classLoader the {@code ClassLoader} to check against (may be {@code null},
     *      in which case this method will always return {@code true})
     * @return {@code true} if the given class is visible, {@code false} otherwise
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
     * Checks whether the given class is loadable in the given {@link ClassLoader}.
     * @param clazz the class to check
     * @param classLoader the {@code ClassLoader} to check against
     * @return {@code true} if the given class is loadable
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
     * Determines if the supplied {@link Class} is a JVM-generated implementation
     * class for a lambda expression or method reference.
     * @param clazz the class to check
     * @return {@code true} if the class is a lambda implementation class, {@code false} otherwise
     */
    public static boolean isLambdaClass(@NonNull Class<?> clazz) {
        return (clazz.isSynthetic() && (clazz.getSuperclass() == Object.class) &&
                (clazz.getInterfaces().length > 0) && clazz.getName().contains("$$Lambda"));
    }

    /**
     * Loads a class by its fully qualified name, using the default class loader.
     * @param <T> the generic type of the class
     * @param name the fully qualified name of the class to load
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    @NonNull
    public static <T> Class<T> classForName(String name) throws ClassNotFoundException {
        return classForName(name, getDefaultClassLoader());
    }

    /**
     * Loads a class by its fully qualified name, using the specified class loader.
     * @param <T> the generic type of the class
     * @param name the fully qualified name of the class to load
     * @param classLoader the class loader to use
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Class<T> classForName(String name, ClassLoader classLoader) throws ClassNotFoundException {
        return (Class<T>)Class.forName(name, true, classLoader);
    }

    /**
     * Loads a class by its fully qualified name, using the default class loader.
     * @param <T> the generic type of the class
     * @param name the fully qualified name of the class to load
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String name) throws ClassNotFoundException {
        return (Class<T>)getDefaultClassLoader().loadClass(name);
    }

    /**
     * Returns the default class loader to use.
     * <p>This method will try, in order:
     * <ol>
     *     <li>The thread context class loader.</li>
     *     <li>The class loader that loaded this {@code ClassUtils} class.</li>
     *     <li>The system class loader.</li>
     * </ol>
     * @return the default class loader (never {@code null})
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
     * Returns the user-defined class for a given class.
     * <p>For regular classes, it returns the class itself. For CGLIB or Javassist-generated
     * proxy classes, it returns the superclass, which is the original user-defined class.</p>
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

    /**
     * Checks if an annotation is present on the given class or any of its superclasses.
     * @param clazz the class to check
     * @param annotationClass the annotation to look for
     * @return {@code true} if the annotation is present, {@code false} otherwise
     */
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
     * Gets the short name of a class, excluding the package name.
     * <p>For nested classes, the separator is converted from '$' to '.'.
     * For proxy classes, the proxy-specific suffix is removed.</p>
     * @param className the fully qualified class name
     * @return the short class name
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
     * Gets the short name of a class, excluding the package name.
     * @param clazz the class to get the short name for
     * @return the short class name
     */
    @NonNull
    public static String getShortName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return getShortName(clazz.getTypeName());
    }

}
