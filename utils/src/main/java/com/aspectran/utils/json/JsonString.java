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

/**
 * A wrapper class to prevent a JSON string from being re-escaped when it is
 * part of a larger JSON serialization process.
 * <p>When an instance of {@code JsonString} is passed to a JSON writer,
 * its {@link #toString()} method is called directly, and the content is
 * written as-is, without further escaping.</p>
 *
 * @since 7.3.0
 */
public class JsonString {

    private final String json;

    /**
     * Creates a new JsonString instance with the given JSON content.
     * @param json the JSON string content
     */
    public JsonString(String json) {
        this.json = json;
    }

    /**
     * Returns the raw JSON string content.
     * <p>This method is intended to be called by JSON writers to retrieve the
     * unescaped JSON content.</p>
     * @return the raw JSON string
     */
    @Override
    public String toString() {
        return json;
    }

}
