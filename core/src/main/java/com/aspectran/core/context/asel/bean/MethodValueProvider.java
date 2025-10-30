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
package com.aspectran.core.context.asel.bean;

import com.aspectran.core.activity.Activity;
import com.aspectran.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A {@link ValueProvider} implementation that resolves a value by invoking a bean's method.
 */
public class MethodValueProvider implements ValueProvider {

    private final Method method;

    public MethodValueProvider(Method method) {
        this.method = method;
    }

    @Override
    public Object evaluate(Activity activity) {
        if (Modifier.isStatic(method.getModifiers())) {
            return ReflectionUtils.invokeMethod(method, null);
        } else {
            Object target = activity.getBean(method.getDeclaringClass());
            return ReflectionUtils.invokeMethod(method, target);
        }
    }

    @Override
    public Class<?> getDependentBeanType() {
        return method.getDeclaringClass();
    }

    @Override
    public boolean isRequiresBeanInstance() {
        return !Modifier.isStatic(method.getModifiers());
    }

    public Method getMethod() {
        return method;
    }

}
