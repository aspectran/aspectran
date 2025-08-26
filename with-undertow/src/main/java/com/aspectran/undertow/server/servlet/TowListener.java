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
package com.aspectran.undertow.server.servlet;

import com.aspectran.utils.ClassUtils;
import io.undertow.servlet.api.ListenerInfo;

import java.util.EventListener;

/**
 * Represents a listener that can be added to a deployment.
 *
 * <p>Created: 2021-02-12</p>
 */
public class TowListener extends ListenerInfo {

    /**
     * Creates a new listener with the specified listener class name.
     * @param listenerClass the listener class name
     * @throws ClassNotFoundException if the class is not found
     */
    public TowListener(String listenerClass) throws ClassNotFoundException {
        this(ClassUtils.loadClass(listenerClass));
    }

    /**
     * Creates a new listener with the specified listener class.
     * @param listenerClass the listener class
     */
    public TowListener(Class<? extends EventListener> listenerClass) {
        super(listenerClass);
    }

}
