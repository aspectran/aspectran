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
package com.aspectran.core.adapter;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple, concrete implementation of {@link ApplicationAdapter} that stores
 * application-scoped attributes in a thread-safe {@link ConcurrentHashMap}.
 *
 * <p>This adapter is useful for non-web environments, such as command-line
 * applications or tests, where a basic application context is required.
 * It extends {@link AbstractApplicationAdapter} to inherit base path resolution
 * capabilities.
 * </p>
 *
 * @author Juho Jeong
 * @since 2016. 3. 26.
 */
public class DefaultApplicationAdapter extends AbstractApplicationAdapter {

    /**
     * Thread-safe storage for application-scoped attributes.
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * Creates a new {@code DefaultApplicationAdapter} with the specified base path.
     * @param basePath the application base path, may be {@code null}
     */
    public DefaultApplicationAdapter(String basePath) {
        super(basePath);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the attribute from the internal map.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)attributes.get(name);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation stores the attribute in the internal map.
     */
    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation returns a set of keys from the internal map.
     */
    @Override
    public Set<String> getAttributeNames() {
        return Collections.unmodifiableSet(attributes.keySet());
    }

    /**
     * {@inheritDoc}
     * <p>This implementation removes the attribute from the internal map.
     */
    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

}
