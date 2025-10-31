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
package com.aspectran.utils.io;

import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

/**
 * A custom {@link ObjectInputStream} that uses a specified {@link ClassLoader}
 * (typically the thread context class loader) to resolve classes during deserialization.
 * <p>This is essential in environments where the application classes are not loaded by
 * the system class loader, such as in web containers or plugin-based architectures.</p>
 */
public class CustomObjectInputStream extends ObjectInputStream {

    private final ClassLoader classLoader;

    /**
     * Creates a new CustomObjectInputStream, using the current thread's context class loader.
     * @param inputStream the {@code InputStream} to read from
     * @throws IOException if an I/O error occurs while reading stream header
     */
    public CustomObjectInputStream(InputStream inputStream) throws IOException {
        this(inputStream, ClassUtils.getDefaultClassLoader());
    }

    /**
     * Creates a new CustomObjectInputStream with a specified ClassLoader.
     * @param inputStream the {@code InputStream} to read from
     * @param classLoader the {@code ClassLoader} to use for class resolution
     * @throws IOException if an I/O error occurs while reading stream header
     */
    public CustomObjectInputStream(InputStream inputStream, ClassLoader classLoader) throws IOException {
        super(inputStream);
        this.classLoader = classLoader;
    }

    /**
     * Resolves the class described by the specified stream class descriptor.
     * This implementation attempts to load the class using the custom class loader first,
     * falling back to the default superclass behavior if not found.
     * @param classDesc the {@code ObjectStreamClass} to resolve
     * @return the resolved {@code Class}
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if the class cannot be found
     */
    @Override
    public Class<?> resolveClass(@NonNull ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        try {
            return Class.forName(classDesc.getName(), false, classLoader);
        } catch (ClassNotFoundException e) {
            return super.resolveClass(classDesc);
        }
    }

    /**
     * Resolves the proxy class for the specified array of interface names.
     * This implementation uses the custom class loader to resolve the interface classes.
     * @param interfaces an array of interface names
     * @return the resolved proxy {@code Class}
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if any of the interface classes cannot be found
     */
    @Override
    protected Class<?> resolveProxyClass(@NonNull String[] interfaces) throws IOException, ClassNotFoundException {
        Class<?>[] resolvedInterfaces = new Class<?>[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            resolvedInterfaces[i] = Class.forName(interfaces[i], false, classLoader);
        }
        try {
            @SuppressWarnings("deprecation")
            Class<?> proxyClass = Proxy.getProxyClass(classLoader, resolvedInterfaces);
            return proxyClass;
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

}
