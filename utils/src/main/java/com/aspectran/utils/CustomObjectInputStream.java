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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Proxy;

/**
 * For re-inflating serialized objects, this class uses the thread context classloader
 * rather than the JVM's default classloader selection.
 */
public class CustomObjectInputStream extends ObjectInputStream {

    private final ClassLoader classLoader;

    public CustomObjectInputStream(InputStream inputStream) throws IOException {
        this(inputStream, ClassUtils.getDefaultClassLoader());
    }

    public CustomObjectInputStream(InputStream inputStream, ClassLoader classLoader) throws IOException {
        super(inputStream);
        this.classLoader = classLoader;
    }

    @Override
    public Class<?> resolveClass(@NonNull ObjectStreamClass classDesc) throws IOException, ClassNotFoundException {
        try {
            return Class.forName(classDesc.getName(), false, classLoader);
        } catch (ClassNotFoundException e) {
            return super.resolveClass(classDesc);
        }
    }

    @Override
    protected Class<?> resolveProxyClass(@NonNull String[] interfaces) throws ClassNotFoundException {
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
