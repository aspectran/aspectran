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
package com.aspectran.core.component.bean.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A wrapper for a method that is annotated with @EventListener.
 *
 * @since 8.6.0
 */
public class ListenerMethod {

    private final Object bean;

    private final Method method;

    ListenerMethod(Object bean, Method method) {
        this.bean = bean;
        this.method = method;
    }

    void invoke(Object event) throws Exception {
        try {
            method.invoke(bean, event);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception)e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Override
    public String toString() {
        return method.toGenericString();
    }

}
