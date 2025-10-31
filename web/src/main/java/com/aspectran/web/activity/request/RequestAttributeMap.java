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
package com.aspectran.web.activity.request;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import jakarta.servlet.ServletRequest;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Wrapper class for getting attributes from a {@link ServletRequest}.
 *
 * <p>Created: 2018. 9. 15.</p>
 *
 * @since 5.2.0
 */
public final class RequestAttributeMap implements Map<String, Object> {

    private ServletRequest request;

    /**
     * Instantiates a new RequestAttributeMap.
     */
    public RequestAttributeMap() {
    }

    /**
     * Instantiates a new RequestAttributeMap.
     * @param request the servlet request
     */
    public RequestAttributeMap(@NonNull ServletRequest request) {
        this.request = request;
    }

    /**
     * Returns the underlying {@link ServletRequest} that this map wraps.
     * @return the servlet request
     */
    public ServletRequest getRequest() {
        return request;
    }

    /**
     * Sets the underlying {@link ServletRequest} to be wrapped by this map.
     * @param request the servlet request
     */
    public void setRequest(@NonNull ServletRequest request) {
        this.request = request;
    }

    /**
     * Checks that the underlying {@link ServletRequest} is not null.
     * @throws IllegalStateException if the request is null
     */
    private void checkState() {
        if (request == null) {
            throw new IllegalStateException("ServletRequest is not specified");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        if (request == null) {
            return 0;
        }
        return Collections.list(request.getAttributeNames()).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        if (request == null) {
            return true;
        }
        return !request.getAttributeNames().hasMoreElements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        if (request == null) {
            return false;
        }
        // This is the correct implementation, the previous one was buggy
        // because it could not distinguish between a non-existent key and a key with a null value.
        return Collections.list(request.getAttributeNames()).contains(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        if (request == null) {
            return false;
        }
        Enumeration<String> names = request.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object object = request.getAttribute(name);
            if (Objects.equals(value, object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation retrieves the attribute from the underlying {@link ServletRequest}.
     */
    @Override
    @Nullable
    public Object get(Object key) {
        if (request == null || !(key instanceof String)) {
            return null;
        }
        return request.getAttribute((String)key);
    }

    /**
     * {@inheritDoc}
     * <p>This implementation sets the attribute on the underlying {@link ServletRequest}.
     * @throws IllegalStateException if the underlying request has not been specified
     */
    @Override
    public Object put(String name, Object value) {
        checkState();
        Object old = request.getAttribute(name);
        request.setAttribute(name, value);
        return old;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation removes the attribute from the underlying {@link ServletRequest}.
     * @throws IllegalStateException if the underlying request has not been specified
     */
    @Override
    @Nullable
    public Object remove(@NonNull Object key) {
        checkState();
        if (key instanceof String stringKey) {
            Object old = request.getAttribute(stringKey);
            request.removeAttribute(stringKey);
            return old;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation sets all attributes on the underlying {@link ServletRequest}.
     * @throws IllegalStateException if the underlying request has not been specified
     */
    @Override
    public void putAll(@Nullable Map<? extends String, ?> map) {
        checkState();
        if (map != null) {
            for (Entry<? extends String, ?> entry : map.entrySet()) {
                request.setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>This implementation removes all attributes from the underlying {@link ServletRequest}.
     */
    @Override
    public void clear() {
        if (request != null) {
            for (String name : Collections.list(request.getAttributeNames())) {
                request.removeAttribute(name);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>The returned set is an unmodifiable view of the attribute names in the underlying {@link ServletRequest}.
     */
    @Override
    @NonNull
    public Set<String> keySet() {
        if (request == null) {
            return Collections.emptySet();
        }
        return Set.copyOf(Collections.list(request.getAttributeNames()));
    }

    /**
     * {@inheritDoc}
     * <p>The returned collection is an unmodifiable view of the attribute values in the underlying {@link ServletRequest}.
     */
    @Override
    @NonNull
    public Collection<Object> values() {
        if (request == null) {
            return Collections.emptyList();
        }
        List<Object> list = new ArrayList<>();
        Enumeration<String> names = request.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object value = request.getAttribute(name);
            list.add(value);
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * {@inheritDoc}
     * <p>The returned set is an unmodifiable view of the attribute entries in the underlying {@link ServletRequest}.
     */
    @Override
    @NonNull
    public Set<Entry<String, Object>> entrySet() {
        if (request == null) {
            return Collections.emptySet();
        }
        Set<Entry<String, Object>> entries = new HashSet<>();
        Enumeration<String> names = request.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object value = request.getAttribute(name);
            entries.add(new AbstractMap.SimpleImmutableEntry<>(name, value));
        }
        return Collections.unmodifiableSet(entries);
    }

}
