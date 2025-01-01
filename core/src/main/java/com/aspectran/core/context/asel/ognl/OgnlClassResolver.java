/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
 * Custom ognl {@code ClassResolver}.
 *
 * <p>Created: 2021/02/07</p>
 */
public class OgnlClassResolver implements ClassResolver {

    private final ConcurrentHashMap<String, Class<?>> classes = new ConcurrentHashMap<>(101);

    public OgnlClassResolver() {
        super();
    }

    @SuppressWarnings("unchecked")
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

    protected Class<?> toClassForName(String className) throws ClassNotFoundException {
        return ClassUtils.getDefaultClassLoader().loadClass(className);
    }

}
