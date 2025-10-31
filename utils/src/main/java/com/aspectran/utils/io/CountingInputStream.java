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

import com.aspectran.utils.annotation.jsr305.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that counts the number of bytes read.
 *
 * @since 9.2.3
 */
public class CountingInputStream extends FilterInputStream {

    private final long limit;

    private long count;

    private long mark = -1;

    public CountingInputStream(InputStream in, long limit) {
        super(in);
        this.limit = limit;
    }

    @Override
    public int read() throws IOException {
        int result = super.read();
        if (result != -1) {
            count++;
            checkLimit();
        }
        return result;
    }

    @Override
    public int read(@NonNull byte[] b, int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result != -1) {
            count += result;
            checkLimit();
        }
        return result;
    }

    @Override
    public long skip(long n) throws IOException {
        long result = super.skip(n);
        if (result > 0) {
            count += result;
            checkLimit();
        }
        return result;
    }

    @Override
    public synchronized void mark(int readlimit) {
        super.mark(readlimit);
        mark = count;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (!markSupported()) {
            throw new IOException("Mark not supported");
        }
        if (mark == -1) {
            throw new IOException("Mark not set");
        }
        super.reset();
        count = mark;
    }

    private void checkLimit() throws StreamReadLimitExceededException {
        if (limit > -1L && count > limit) {
            throw new StreamReadLimitExceededException("Maximum stream read limit exceeded; actual: " +
                    count + "; permitted: " + limit,
                    count, limit);
        }
    }

}
