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
package com.aspectran.web.service;

import com.aspectran.utils.ClassUtils;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A custom {@link java.net.URLClassLoader} for the web service.
 * <p>This class loader is typically used to provide a specific class loading
 * hierarchy for web-related components, ensuring proper isolation and resource
 * loading within the web application context.
 * </p>
 */
public class WebServiceClassLoader extends URLClassLoader {

    /**
     * Constructs a new WebServiceClassLoader.
     * @param parent the parent class loader
     */
    public WebServiceClassLoader(ClassLoader parent) {
        super((parent != null ? parent.getName() : null),
                new URL[] {}, (parent != null ? parent : ClassUtils.getDefaultClassLoader()));
    }

}
