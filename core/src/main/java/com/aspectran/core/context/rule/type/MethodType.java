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
package com.aspectran.core.context.rule.type;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Supported Method types.
 * 
 * <p>Created: 2008. 03. 26 AM 12:58:38</p>
 */
public enum MethodType {

    /**
     * retrieves a representation of a resource without side-effects
     * (nothing changes on the server).
     */
    GET,

    /**
     * creates a resource.
     */
    POST,

    /**
     * (completely) replaces an existing resource.
     */
    PUT,

    /**
     * partial modification of a resource.
     */
    PATCH,

    /**
     * deletes a resource.
     */
    DELETE,

    /**
     * retrieves just the resource meta-information (headers)
     * i.e. same as GET but without the response body - also without side-effects.
     */
    HEAD,

    /**
     * returns the actions supported for specified the resource - also without side-effects.
     */
    OPTIONS,

    TRACE;

    private static final int MAX_COUNT = 8;

    private static final Map<String, MethodType> mappings = new HashMap<>();

    static {
        for (MethodType type : values()) {
            mappings.put(type.name(), type);
        }
    }

    public boolean containsTo(MethodType[] types) {
        for (MethodType type : types) {
            if (equals(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean matches(String type) {
        return name().equals(type);
    }

    /**
     * Returns a {@code MethodType} with a value represented
     * by the specified {@code String}.
     *
     * @param methodType the method type as a {@code String}
     * @return a {@code MethodType}, may be {@code null}
     */
    public static MethodType resolve(String methodType) {
        return (methodType != null ? mappings.get(methodType.toUpperCase()) : null);
    }

    /**
     * Returns an array of {@code MethodType} with a value represented
     * by the specified {@code String}.
     *
     * @param value the method type as a {@code String}
     * @return a {@code MethodType}, may be {@code null}
     */
    public static MethodType[] parse(String value) {
        MethodType[] types = new MethodType[MAX_COUNT];
        int count = 0;

        StringTokenizer st = new StringTokenizer(value, ", ");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (!token.isEmpty()) {
                MethodType type = resolve(token);
                if (type != null) {
                    int ord = type.ordinal();
                    if (types[ord] == null) {
                        types[ord] = type;
                        count++;
                    }
                }
            }
        }

        if (count == 0) {
            return null;
        }

        MethodType[] orderedTypes = new MethodType[count];
        for (int i = 0, seq = 0; i < MAX_COUNT; i++) {
            if (types[i] != null) {
                orderedTypes[seq++] = types[i];
            }
        }
        return orderedTypes;
    }

    /**
     * Converts an array of {@code MethodType} to a comma separated {@code String}.
     *
     * @param types an array of {@code MethodType}
     * @return a comma separated {@code String}
     */
    public static String stringify(MethodType[] types) {
        if (types == null || types.length == 0) {
            return null;
        }

        StringBuilder sb = new StringBuilder(types.length * 7 + 1);
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(types[i]);
        }
        return sb.toString();
    }

}