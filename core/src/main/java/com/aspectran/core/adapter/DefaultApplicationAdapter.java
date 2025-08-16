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
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory {@link ApplicationAdapter} implementation backed by a concurrent map.
 * <p>
 * Useful for non-servlet environments or tests where application attributes and
 * base-path resolution are needed without a full container.
 * </p>
 *
 * @since 2016. 3. 26.
 */
public class DefaultApplicationAdapter extends AbstractApplicationAdapter {

    /**
     * Thread-safe storage for application-scoped attributes.
     */
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * Create a new DefaultApplicationAdapter with an optional base path.
     * @param basePath the application base path; may be {@code null}
     */
    public DefaultApplicationAdapter(String basePath) {
        super(basePath);
    }

    /**
     * Return the attribute with the specified name from the internal store.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)attributes.get(name);
    }

    /**
     * Store an attribute in the internal map, replacing any existing value.
     */
    @Override
    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    /**
     * Return an enumeration of all attribute names currently stored.
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    /**
     * Remove the attribute with the specified name, if present.
     */
    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

}
