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
package com.aspectran.utils.io;

import java.io.IOException;
import java.io.Serial;

/**
 * Exception thrown by a {@link CountingInputStream} when the stream's read limit is exceeded.
 * This exception provides access to the actual number of bytes read and the configured limit.
 *
 * @since 9.2.3
 */
public class StreamReadLimitExceededException extends IOException {

    @Serial
    private static final long serialVersionUID = 1417803032033166363L;

    private final long count;

    private final long limit;

    /**
     * Constructs a new StreamReadLimitExceededException.
     * @param message the detail message
     * @param count the actual number of bytes read
     * @param limit the configured limit
     */
    public StreamReadLimitExceededException(String message, long count, long limit) {
        super(message);
        this.count = count;
        this.limit = limit;
    }

    /**
     * Returns the actual number of bytes read from the stream when the limit was exceeded.
     * @return the actual byte count
     */
    public long getCount() {
        return count;
    }

    /**
     * Returns the configured read limit, in bytes.
     * @return the read limit
     */
    public long getLimit() {
        return limit;
    }

}
