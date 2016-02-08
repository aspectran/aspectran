/**
 * Copyright 2008-2016 Juho Jeong
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
package com.aspectran.core.context.message;

/**
 * <p>Created: 2016. 2. 8.</p>
 */
public class AbstractMessageSource {

    private final ClassLoader classLoader;

    private final String defaultEncoding;

    /**
     * Instantiates a new Resource bundle message source.
     *
     * @param classLoader the <code>ClassLoader</code> to use to load the bundle
     * @param defaultEncoding the default charset
     */
    public AbstractMessageSource(ClassLoader classLoader, String defaultEncoding) {
        this.classLoader = classLoader;
        this.defaultEncoding = defaultEncoding;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public String getDefaultEncoding() {
        return defaultEncoding;
    }

}
