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
package com.aspectran.core.activity.response;

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.Serial;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * A specialized {@link LinkedHashMap} for storing and managing multiple {@link Response} objects.
 *
 * <p>This map allows for associating different response configurations with unique names,
 * providing a flexible way to define and select various output strategies within a translet.
 * It maintains the insertion order of responses.</p>
 *
 * <p>Created: 2008. 03. 29 PM 11:50:02</p>
 */
public class ResponseMap extends LinkedHashMap<String, Response> implements Iterable<Response> {

    @Serial
    private static final long serialVersionUID = 2093093144584776388L;

    /**
     * Returns the first {@link Response} object in this map.
     * @return the first {@link Response} object, or {@code null} if the map is empty
     */
    public Response getFirst() {
        if (!isEmpty()) {
            return (Response)values().toArray()[0];
        } else {
            return null;
        }
    }

    /**
     * Returns an iterator over the {@link Response} objects in this map.
     * The elements are returned in insertion order.
     * @return an iterator over the {@link Response} objects
     */
    @Override
    @NonNull
    public Iterator<Response> iterator() {
        return this.values().iterator();
    }

}
