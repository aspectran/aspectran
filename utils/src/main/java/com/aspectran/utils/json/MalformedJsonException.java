/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
import java.io.Serial;

/**
 * <p>This class is a clone of com.google.gson.stream.MalformedJsonException</p>
 *
 * Thrown when a reader encounters malformed JSON. Some syntax errors can be
 * ignored by calling {@link JsonReader#setLenient(boolean)}.
 */
public final class MalformedJsonException extends IOException {

    @Serial
    private static final long serialVersionUID = 7310479345877902705L;

    public MalformedJsonException(String msg) {
        super(msg);
    }

    public MalformedJsonException(Throwable cause) {
        super(cause);
    }

    public MalformedJsonException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
