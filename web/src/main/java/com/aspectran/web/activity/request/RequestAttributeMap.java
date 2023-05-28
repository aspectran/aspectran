/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.lang.NonNull;
import com.aspectran.core.lang.Nullable;

import javax.servlet.ServletRequest;
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
 * Wrapper class for getting attributes from HttpServletRequest().
 *
 * <p>Created: 2018. 9. 15.</p>
 *
 * @since 5.2.0
 */
public class RequestAttributeMap implements Map<String, Object> {

    private ServletRequest request;

    public RequestAttributeMap(ServletRequest request) {
        setRequest(request);
    }

    public ServletRequest getRequest() {
        return request;
    }

    public void setRequest(ServletRequest request) {
        this.request = request;
    }

    @Override
    public int size() {
        return Collections.list(getRequest().getAttributeNames()).size();
    }

    @Override
    public boolean isEmpty() {
        return !getRequest().getAttributeNames().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object key) {
        return (getRequest().getAttribute((String)key) != null);
    }

    @Override
    public boolean containsValue(Object value) {
        Enumeration<String> names = getRequest().getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object object = getRequest().getAttribute(name);
            if (Objects.equals(value, object)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        return getRequest().getAttribute((String)key);
    }

    @Override
    public Object put(String name, Object value) {
        Object old = getRequest().getAttribute(name);
        getRequest().setAttribute(name, value);
        return old;
    }

    @Override
    public Object remove(@NonNull Object key) {
        Object old = getRequest().getAttribute((String)key);
        getRequest().removeAttribute((String)key);
        return old;
    }

    @Override
    public void putAll(@Nullable Map<? extends String, ?> map) {
        if (map != null) {
            for (Entry<? extends String, ?> entry : map.entrySet()) {
                getRequest().setAttribute(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void clear() {
        for (String name : Collections.list(getRequest().getAttributeNames())) {
            getRequest().removeAttribute(name);
        }
    }

    @Override
    @NonNull
    public Set<String> keySet() {
        return new HashSet<>(Collections.list(getRequest().getAttributeNames()));
    }

    @Override
    @NonNull
    public Collection<Object> values() {
        List<Object> list = new ArrayList<>();
        Enumeration<String> names = getRequest().getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object value = getRequest().getAttribute(name);
            list.add(value);
        }
        return list;
    }

    @Override
    @NonNull
    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> set = new HashSet<>();
        Enumeration<String> names = getRequest().getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Object value = getRequest().getAttribute(name);
            set.add(new AbstractMap.SimpleImmutableEntry<>(name, value));
        }
        return set;
    }

}
