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

import java.io.Serial;

/**
 * Thrown to indicate that the request size exceeds the configured maximum.
 * This is typically encountered during file uploads or when processing large
 * request bodies.
 *
 * @since 6.3.0
 */
public class SizeLimitExceededException extends RequestParseException {

    @Serial
    private static final long serialVersionUID = -6153625356301952978L;

    private final long actual;

    private final long permitted;

    /**
     * Constructs a <code>SizeExceededException</code> with
     * the specified detail message, and actual and permitted sizes.
     * @param msg the detail message
     * @param actual the actual request size
     * @param permitted the maximum permitted request size
     */
    public SizeLimitExceededException(String msg, long actual, long permitted) {
        super(msg);
        this.actual = actual;
        this.permitted = permitted;
    }

    /**
     * Returns the actual size of the request.
     * @return the actual size
     */
    public long getActual() {
        return actual;
    }

    /**
     * Returns the maximum permitted size of the request.
     * @return the permitted size
     */
    public long getPermitted() {
        return permitted;
    }

}
