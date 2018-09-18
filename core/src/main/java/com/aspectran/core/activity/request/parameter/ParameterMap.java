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
package com.aspectran.core.activity.request.parameter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class ParameterMap.
 * 
 * <p>Created: 2008. 06. 11 PM 8:55:13</p>
 */
public class ParameterMap extends LinkedHashMap<String, String[]> {

    /** @serial */
    private static final long serialVersionUID = 1709146569240133920L;

    /**
     * Instantiates a new ParameterMap.
     */
    public ParameterMap() {
        super();
    }

    /**
     * Instantiates a new ParameterMap.
     *
     * @param initialCapacity the initial capacity
     */
    public ParameterMap(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Instantiates a new ParameterMap.
     *
     * @param params the other parameter map
     */
    public ParameterMap(Map<String, String[]> params) {
        super(params);
    }

    /**
     * Returns the string value to which the specified name is mapped,
     * or {@code null} if this map contains no mapping for the name.
     *
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
     *
     * @param name the parameter name
     * @return an array of {@code String} objects
     *            containing the parameter's values
     */
    public String[] getParameterValues(String name) {
        return get(name);
    }

    /**
     * Sets the value to the parameter with the given name.
     *
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
     *
     * @param name a {@code String} specifying the name of the parameter
     * @param values an array of {@code String} objects
     *            containing the parameter's values
     * @see #setParameter
     */
    public void setParameterValues(String name, String[] values) {
        put(name, values);
    }

    /**
     * Returns a {@code Collection} of {@code String} objects containing
     * the names of the parameters.
     * If no parameters, the method returns an empty {@code Collection}.
     *
     * @return a {@code Collection} of {@code String} objects, each {@code String}
     *             containing the name of a parameter;
     *             or an empty {@code Collection} if no parameters
     */
    public Collection<String> getParameterNames() {
        return keySet();
    }

    /**
     * Set the given parameters under.
     *
     * @param params the other parameter map
     */
    public void setAll(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            setParameter(entry.getKey(), entry.getValue());
        }
    }

    public Map<String, Object> extractParameters() {
        Map<String, Object> refinedParameters = new HashMap<>();
        return extractParameters(refinedParameters);
    }

    public Map<String, Object> extractParameters(Map<String, Object> targetParameters) {
        if (targetParameters == null) {
            throw new IllegalArgumentException("Argument 'targetParameters' must not be null");
        }
        for (Map.Entry<String, String[]> entry : this.entrySet()) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            if (values.length == 1) {
                targetParameters.put(name, values[0]);
            } else {
                targetParameters.put(name, values);
            }
        }
        return targetParameters;
    }

}
