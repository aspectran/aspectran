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
package com.aspectran.utils.apon;

import java.io.IOException;
import java.io.Serial;

/**
 * Base class for exceptions thrown while reading or interpreting APON text.
 * <p>
 * Extends {@link IOException} so callers can uniformly handle I/O and parsing
 * errors produced by {@link AponReader} and related utilities.
 * </p>
 */
public class AponParseException extends IOException {

    @Serial
    private static final long serialVersionUID = -8511680666286307705L;

    public AponParseException() {
        super();
    }

    public AponParseException(String msg) {
        super(msg);
    }

    public AponParseException(Throwable cause) {
        super(cause);
    }

    public AponParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
