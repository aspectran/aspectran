/*
 * Copyright 2008-2017 Juho Jeong
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
import java.util.HashMap;
import java.util.Map;

/**
 * The Class BasicRequestAdapter.
  *
 * @since 2016. 2. 13.
*/
public class BasicRequestAdapter extends AbstractRequestAdapter {

    private String characterEncoding;

    private Map<String, Object> attributeMap = new HashMap<>();

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
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T)attributeMap.get(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        attributeMap.put(name, value);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributeMap.keySet());
    }

    @Override
    public void removeAttribute(String name) {
        attributeMap.remove(name);
    }

}
