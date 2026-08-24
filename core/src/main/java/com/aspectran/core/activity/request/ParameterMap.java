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
package com.aspectran.core.activity.request;

import com.aspectran.utils.Assert;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A specialized {@link LinkedHashMap} implementation for storing and extracting
 * request parameters.
 * <p>
 * Provides convenient methods for converting multi-valued parameter arrays into
 * a simple key-value map structure, which is often required for template processing
 * or activity execution.
 * </p>
 *
 * <p>Created: 2008. 06. 11 PM 8:55:13</p>
 */
public class ParameterMap extends LinkedHashMap<String, String[]> {

    @Serial
    private static final long serialVersionUID = 1709146569240133920L;

    /**
     * Instantiates a new ParameterMap.
     */
    public ParameterMap() {
        super();
    }

    /**
     * Instantiates a new ParameterMap.
     * @param initialCapacity the initial capacity
     */
    public ParameterMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Instantiates a new ParameterMap.
     * @param params the other parameter map
     */
    public ParameterMap(Map<String, String[]> params) {
        super(params);
    }

    /**
     * Returns the string value to which the specified name is mapped,
     * or {@code null} if this map contains no mapping for the name.
     * @param name the parameter name
     * @return a {@code String} representing the
     *            single value of the parameter
     */
    public String getParameter(String name) {
        String[] values = get(name);
        return (values != null && values.length > 0 ? values[0] : null);
    }

    /**
     * Returns the string values to which the specified name is mapped,
     * or {@code null} if this map contains no mapping for the name.
     * @param name the parameter name
     * @return an array of {@code String} objects
     *            containing the parameter's values
     */
    public String[] getParameterValues(String name) {
        return get(name);
    }

    /**
     * Sets the value to the parameter with the given name.
     * @param name a {@code String} specifying the name of the parameter
     * @param value a {@code String} representing the
     *            single value of the parameter
     * @see #setParameterValues(String, String[])
     */
    public void setParameter(String name, String value) {
        put(name, new String[] { value });
    }

    /**
     * Sets the values to the parameter with the given name.
     * @param name a {@code String} specifying the name of the parameter
     * @param values an array of {@code String} objects
     *            containing the parameter's values
     * @see #setParameter
     */
    public void setParameterValues(String name, String[] values) {
        put(name, values);
    }

    /**
     * Returns a {@code Set} of the names of the parameters contained in this map.
     * @return a set of the names of the parameters; an empty set if the map is empty
     */
    public Set<String> getParameterNames() {
        return keySet();
    }

    /**
     * Adds all entries from the given single-valued map to this multi-valued parameter map.
     * @param params the map containing the parameters to add
     */
    public void setAll(Map<String, String> params) {
        Assert.notNull(params, "params must not be null");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            setParameter(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Returns a new map containing all parameters with single values flattened to {@code String}
     * and multiple values kept as {@code String[]}.
     * @return a new map containing flattened parameters
     */
    public Map<String, Object> toFlatMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        copyTo(map);
        return map;
    }

    /**
     * Copies all parameters into the specified target map, flattening single values to {@code String}
     * and keeping multiple values as {@code String[]}.
     * @param targetMap the map into which parameters should be inserted
     */
    public void copyTo(Map<String, Object> targetMap) {
        Assert.notNull(targetMap, "targetMap must not be null");
        for (Map.Entry<String, String[]> entry : entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            if (values.length == 1) {
                targetMap.put(name, values[0]);
            } else {
                targetMap.put(name, values);
            }
        }
    }

}
