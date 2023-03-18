/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

/**
 * Thrown to indicate that the request size exceeds the configured maximum.
 *
 * @since 6.3.0
 */
public class SizeLimitExceededException extends RequestParseException {

    /** @serial */
    private static final long serialVersionUID = -6153625356301952978L;

    private final long actual;

    private final long permitted;

    /**
     * Constructs a <code>SizeExceededException</code> with
     * the specified detail message, and actual and permitted sizes.
     *
     * @param msg the detail message
     * @param actual the actual request size
     * @param permitted the maximum permitted request size
     */
    public SizeLimitExceededException(String msg, long actual, long permitted) {
        super(msg);
        this.actual = actual;
        this.permitted = permitted;
    }

    public long getActual() {
        return actual;
    }

    public long getPermitted() {
        return permitted;
    }

}
