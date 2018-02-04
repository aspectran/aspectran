/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import com.aspectran.core.util.thread.Locker.Lock;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class BasicRequestAdapter.
  *
 * @since 2016. 2. 13.
*/
public class BasicRequestAdapter extends AbstractRequestAdapter {

    private String encoding;

    private Map<String, Object> attributes = new HashMap<>();

    /**
     * Instantiates a new BasicRequestAdapter.
     *
     * @param adaptee the adaptee object
     */
    public BasicRequestAdapter(Object adaptee) {
        super(adaptee);
    }

    /**
     * Instantiates a new BasicRequestAdapter.
     *
     * @param adaptee the adaptee object
     * @param parameterMap the parameter map
     */
    public BasicRequestAdapter(Object adaptee, Map<String, String[]> parameterMap) {
        super(adaptee, parameterMap);
    }

    @Override
    public String getEncoding() {
        return encoding;
    }

    @Override
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return (T)attributes.get(name);
        }
    }

    @Override
    public void setAttribute(String name, Object value) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (value == null) {
                attributes.remove(name);
            } else {
                attributes.put(name, value);
            }
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return Collections.enumeration(attributes.keySet());
        }
    }

    @Override
    public void removeAttribute(String name) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            attributes.remove(name);
        }
    }

    @Override
    public Map<String, Object> getAllAttributes() {
        try (Lock ignored = locker.lockIfNotHeld()) {
            return Collections.unmodifiableMap(attributes);
        }
    }

    @Override
    public void putAllAttributes(Map<String, Object> attributes) {
        try (Lock ignored = locker.lockIfNotHeld()) {
            this.attributes.putAll(attributes);
        }
    }

    @Override
    public void fillAllAttributes(Map<String, Object> targetAttributes) {
        if (targetAttributes == null) {
            throw new IllegalArgumentException("Argument 'targetAttributes' must not be null");
        }
        try (Lock ignored = locker.lockIfNotHeld()) {
            if (attributes != null) {
                targetAttributes.putAll(attributes);
            }
        }
    }

}
