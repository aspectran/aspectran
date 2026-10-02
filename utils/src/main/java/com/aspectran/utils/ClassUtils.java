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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Miscellaneous class utility methods.
 * <p>Mainly for internal use within the framework.</p>
 */
public class ClassUtils {

    /** The package separator character: {@code '.'}. */
    public static final char PACKAGE_SEPARATOR_CHAR = '.';

    /** The path separator character: {@code '/'}. */
    public static final char PATH_SEPARATOR_CHAR = '/';

    /** The path separator: {@code "/"}. */
    public static final String PATH_SEPARATOR = "/";

    /** The nested class separator character: {@code '$'}. */
    public static final char NESTED_CLASS_SEPARATOR_CHAR = '$';

    /** The ".class" file suffix. */
    public static final String CLASS_FILE_SUFFIX = ".class";

    /** The array suffix: {@code "[]"}. */
    public static final String ARRAY_SUFFIX = "[]";

    /** The internal array prefix: {@code "["}. */
    private static final String INTERNAL_ARRAY_PREFIX = "[";

    /** The internal non-primitive array prefix: {@code "[L"}. */
    private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";

    /** CGLIB or Javassist-generated class separator: {@code "$$"}. */
    public static final String PROXY_CLASS_SEPARATOR = "$$";

    /** Map of common class names. */
    private static final Map<String, Class<?>> commonClassNames = new HashMap<>(64);

    static {
        List<Class<?>> commonClasses = List.of(
                Boolean[].class, Byte[].class, Character[].class, Short[].class,
                Integer[].class, Long[].class, Float[].class, Double[].class,
                Number[].class, Object[].class, Class[].class,
                String.class, String[].class
        );
        for (Class<?> clazz : commonClasses) {
            commonClassNames.put(clazz.getName(), clazz);
        }
    }

    /**
     * This class cannot be instantiated.
     */
    private ClassUtils() {
    }

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
                    " has no default (no-arg) constructor");
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
    public static <T> T createInstance(Class<T> clazz, Object @Nullable ... args) {
        if (args == null || args.length == 0) {
            return createInstance(clazz);
        }
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = (args[i] != null ? args[i].getClass() : null);
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
        Assert.notNull(clazz, "clazz must not be null");
        Constructor<T> ctor = null;
        if (argTypes != null && !hasNull(argTypes)) {
            try {
                ctor = clazz.getDeclaredConstructor(argTypes);
            } catch (NoSuchMethodException e) {
                // fall through to matching search
            }
        }
        if (ctor == null && argTypes != null && argTypes.length > 0) {
            ctor = getMatchingAccessibleConstructor(clazz, argTypes);
        }
        if (ctor == null) {
            if (argTypes == null || argTypes.length == 0) {
                ctor = clazz.getDeclaredConstructor();
            } else {
                throw new NoSuchMethodException("No matching constructor found for class " + clazz.getName());
            }
        }
        // must be public
        if (!Modifier.isPublic(ctor.getModifiers())) {
            throw new IllegalArgumentException("Constructor for " + clazz.getName() +
                    " is not accessible (non-public?): not allowed to try modify access via Reflection: " +
                    "cannot instantiate type");
        }
        return ctor;
    }

    /**
     * Finds a matching accessible constructor for the given parameter types, considering type hierarchy and boxing.
     * @param <T> the generic type of the class
     * @param clazz the class to find the constructor for
     * @param argTypes the parameter types
     * @return the matching constructor, or {@code null} if none found
     */
    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getMatchingAccessibleConstructor(@NonNull Class<T> clazz, Class<?>[] argTypes) {
        int paramSize = (argTypes != null ? argTypes.length : 0);
        Constructor<?> bestMatch = null;
        Constructor<?>[] constructors = clazz.getConstructors();
        float bestMatchWeight = Float.MAX_VALUE;

        for (Constructor<?> ctor : constructors) {
            if (ctor.getParameterCount() == paramSize) {
                Class<?>[] paramTypes = ctor.getParameterTypes();
                boolean paramMatch = true;
                for (int i = 0; i < paramTypes.length; i++) {
                    if (argTypes != null && !TypeUtils.isAssignable(paramTypes[i], argTypes[i])) {
                        paramMatch = false;
                        break;
                    }
                }
                if (paramMatch) {
                    float weight = (argTypes != null ?
                            ReflectionUtils.getTypeDifferenceWeight(paramTypes, argTypes) : 0.0f);
                    if (weight < bestMatchWeight) {
                        bestMatch = ctor;
                        bestMatchWeight = weight;
                    }
                }
            }
        }
        return (Constructor<T>)bestMatch;
    }

    private static boolean hasNull(Object[] array) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the given class is visible in the given {@link ClassLoader}.
     * @param clazz the class to check (typically an interface)
     * @param classLoader the {@code ClassLoader} to check against (may be {@code null},
     *      in which case this method will always return {@code true})
     * @return {@code true} if the given class is visible, {@code false} otherwise
     * @since 6.0.0
     */
    public static boolean isVisible(@NonNull Class<?> clazz, @Nullable ClassLoader classLoader) {
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
        } catch (ClassNotFoundException | LinkageError ex) {
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
     * <p>The class will not be initialized.</p>
     * @param <T> the generic type of the class
     * @param name the fully qualified name of the class to load
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    @NonNull
    public static <T> Class<T> classForName(String name) throws ClassNotFoundException {
        return classForName(name, false, getDefaultClassLoader());
    }

    /**
     * Loads a class by its fully qualified name, using the specified class loader.
     * <p>The class will not be initialized.</p>
     * @param <T> the generic type of the class
     * @param name the fully qualified name of the class to load
     * @param classLoader the class loader to use
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    @NonNull
    public static <T> Class<T> classForName(String name, @Nullable ClassLoader classLoader)
            throws ClassNotFoundException {
        return classForName(name, false, classLoader);
    }

    /**
     * Loads a class by its fully qualified name, using the specified class loader.
     * Supports primitive types (e.g. {@code "int"}), primitive arrays (e.g. {@code "int[]"}),
     * and array syntax (e.g. {@code "java.lang.String[]"}).
     * @param <T> the generic type of the class
     * @param name the fully qualified name of the class to load
     * @param initialize whether the class must be initialized
     * @param classLoader the class loader to use
     * @return the loaded class
     * @throws ClassNotFoundException if the class cannot be found
     */
    @NonNull
    @SuppressWarnings("unchecked")
    public static <T> Class<T> classForName(String name, boolean initialize, @Nullable ClassLoader classLoader)
            throws ClassNotFoundException {
        Assert.notNull(name, "name must not be null");

        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz != null) {
            return (Class<T>)clazz;
        }

        clazz = commonClassNames.get(name);
        if (clazz != null) {
            return (Class<T>)clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = classForName(elementClassName, initialize, classLoader);
            return (Class<T>)Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
            String elementClassName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = classForName(elementClassName, initialize, classLoader);
            return (Class<T>)Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" or "[[Ljava.lang.String;" style arrays
        if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
            String elementClassName = name.substring(INTERNAL_ARRAY_PREFIX.length());
            Class<?> elementClass = classForName(elementClassName, initialize, classLoader);
            return (Class<T>)Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader cl = (classLoader != null ? classLoader : getDefaultClassLoader());
        try {
            return (Class<T>)Class.forName(name, initialize, cl);
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
            if (lastDotIndex != -1) {
                String nestedClassName = name.substring(0, lastDotIndex) +
                        NESTED_CLASS_SEPARATOR_CHAR + name.substring(lastDotIndex + 1);
                try {
                    return (Class<T>)Class.forName(nestedClassName, initialize, cl);
                } catch (ClassNotFoundException ex2) {
                    // ignore to throw the original exception
                }
            }
            throw ex;
        }
    }

    /**
     * Resolves the given class name as a primitive class, if any.
     * @param name the name of the potential primitive class
     * @return the primitive class, or {@code null} if not a primitive
     */
    @Nullable
    public static Class<?> resolvePrimitiveClassName(@Nullable String name) {
        return TypeUtils.resolvePrimitiveType(name);
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
    @NonNull
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
     * Determines whether the {@link Class} identified by the supplied name is present
     * and can be loaded. Will return {@code false} if either the class or
     * one of its dependencies is not present or cannot be loaded.
     * @param className the name of the class to check
     * @return {@code true} if the class is present; {@code false} otherwise
     */
    public static boolean isPresent(String className) {
        return isPresent(className, getDefaultClassLoader());
    }

    /**
     * Determines whether the {@link Class} identified by the supplied name is present
     * and can be loaded using the specified class loader. Will return {@code false}
     * if either the class or one of its dependencies is not present or cannot be loaded.
     * @param className the name of the class to check
     * @param classLoader the class loader to use (may be {@code null} which indicates
     *                    the default class loader)
     * @return {@code true} if the class is present; {@code false} otherwise
     */
    public static boolean isPresent(@Nullable String className, @Nullable ClassLoader classLoader) {
        if (className == null) {
            return false;
        }
        try {
            classForName(className, false, classLoader);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    /**
     * Returns the user-defined class for a given class.
     * <p>For regular classes, it returns the class itself. For CGLIB or Javassist-generated
     * proxy classes, it walks up the inheritance hierarchy to find the original user-defined class.</p>
     * @param clazz the class to check
     * @return the user-defined class
     * @see #PROXY_CLASS_SEPARATOR
     */
    @NonNull
    public static Class<?> getUserClass(@NonNull Class<?> clazz) {
        Assert.notNull(clazz, "clazz must not be null");
        Class<?> current = clazz;
        while (current != Object.class && current.getName().contains(PROXY_CLASS_SEPARATOR)) {
            Class<?> superclass = current.getSuperclass();
            if (superclass == null || superclass == Object.class) {
                break;
            }
            current = superclass;
        }
        return current;
    }

    /**
     * Checks if an annotation is present on the given class or any of its superclasses.
     * @param clazz the class to check
     * @param annotationClass the annotation to look for
     * @return {@code true} if the annotation is present, {@code false} otherwise
     */
    public static boolean isAnnotationPresent(
            @Nullable Class<?> clazz,
            @NonNull Class<? extends Annotation> annotationClass) {
        Assert.notNull(annotationClass, "annotationClass must not be null");
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
        int nameEndIndex = className.indexOf(PROXY_CLASS_SEPARATOR, lastDotIndex + 1);
        if (nameEndIndex == -1) {
            nameEndIndex = className.length();
        }
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        shortName = shortName.replace(NESTED_CLASS_SEPARATOR_CHAR, PACKAGE_SEPARATOR_CHAR);
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

    /**
     * Gets the package name of a class from its fully qualified class name.
     * @param className the fully qualified class name
     * @return the package name, or an empty string if in the default package
     * @throws IllegalArgumentException if className is empty
     */
    @NonNull
    public static String getPackageName(String className) {
        Assert.hasLength(className, "Class name must not be empty");
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR_CHAR);
        return (lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "");
    }

    /**
     * Gets the package name of a class.
     * @param clazz the class
     * @return the package name, or an empty string if in the default package
     */
    @NonNull
    public static String getPackageName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return getPackageName(clazz.getName());
    }

}
