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
package com.aspectran.core.context.asel.ognl;

import com.aspectran.utils.ClassUtils;
import ognl.ClassResolver;
import ognl.OgnlContext;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A custom OGNL {@link ClassResolver} that resolves class names within an expression.
 * <p>This implementation uses a cache to store resolved classes, improving performance
 * by avoiding repeated lookups for the same class name.</p>
 *
 * <p>Created: 2021/02/07</p>
 */
public class OgnlClassResolver implements ClassResolver {

    private final ConcurrentHashMap<String, Class<?>> classes = new ConcurrentHashMap<>(101);

    /**
     * Resolves a class by its name from the cache or by loading it.
     * <p>This implementation first checks a local cache for the class. If not found,
     * it attempts to load the class using the default class loader. For class names
     * without a package qualifier, a {@code ClassNotFoundException} is suppressed,
     * allowing OGNL to attempt resolution in the default package (e.g., {@code java.lang}).</p>
     * @param className the name of the class to resolve
     * @param context the current OGNL context (unused)
     * @return the resolved {@link Class} object, or {@code null} if not found and the name is unqualified
     * @throws ClassNotFoundException if the class cannot be found and the name is qualified
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> Class<T> classForName(String className, OgnlContext context) throws ClassNotFoundException {
        Class<?> result = classes.get(className);
        if (result != null) {
            return (Class<T>)result;
        }
        try {
            result = toClassForName(className);
        } catch (ClassNotFoundException e) {
            if (className.indexOf('.') > -1) {
                throw e;
            }
        }
        classes.putIfAbsent(className, result);
        return (Class<T>)result;
    }

    /**
     * Loads a class using the default class loader.
     * <p>This method can be overridden by subclasses to provide custom class loading logic.</p>
     * @param className the name of the class to load
     * @return the loaded {@link Class} object
     * @throws ClassNotFoundException if the class cannot be found
     */
    protected Class<?> toClassForName(String className) throws ClassNotFoundException {
        return ClassUtils.getDefaultClassLoader().loadClass(className);
    }

}
