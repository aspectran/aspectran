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
package com.aspectran.utils.json;

import java.io.IOException;

/**
 * Interface for custom JSON serializers.
 * @param <T> the type of the object to serialize
 */
@FunctionalInterface
public interface JsonSerializer<T> {

    /**
     * Serializes the given object using the provided JsonWriter.
     * @param object the object to serialize
     * @param writer the JsonWriter to use for serialization
     * @throws IOException if an I/O error occurs
     */
    void serialize(T object, JsonWriter writer) throws IOException;

}
