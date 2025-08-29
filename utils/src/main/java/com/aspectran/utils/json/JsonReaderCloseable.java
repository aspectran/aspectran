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

import java.io.Closeable;
import java.io.Reader;

/**
 * A {@link JsonReader} subclass that implements the {@link Closeable} interface.
 * <p>This allows {@code JsonReaderCloseable} instances to be used in a
 * try-with-resources statement, ensuring that the underlying {@link Reader} is
 * automatically closed when the block is exited.</p>
 */
public class JsonReaderCloseable extends JsonReader implements Closeable {

    /**
     * Creates a new JsonReaderCloseable that reads from the given {@link Reader}.
     * @param reader the {@code Reader} to read from
     */
    public JsonReaderCloseable(Reader reader) {
        super(reader);
    }

}
