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
package com.aspectran.core.util;

import com.aspectran.core.lang.NonNull;

import java.io.Writer;

/**
 * A character stream that collects its output in a string builder,
 * which can then be used to construct a string.
 * <p>Closing a StringWriter has no effect. The methods in this class can be called
 * after the stream has been closed without generating an IOException.</p>
 */
public class OutputStringWriter extends Writer {

    private StringBuilder buffer;

    private int initialSize;

    /**
     * Create a new string writer using the default initial string-builder size.
     */
    public OutputStringWriter() {
    }

    /**
     * Create a new string writer using the specified initial string-builder size.
     *
     * @param initialSize the number of char values that will fit into this buffer
     *         before it is automatically expanded
     */
    public OutputStringWriter(int initialSize) {
        this.initialSize = initialSize;
    }

    @Override
    public void write(int c) {
        touchBuffer().append((char)c);
    }

    @Override
    public void write(@NonNull char[] cbuf, int off, int len) {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        touchBuffer().append(cbuf, off, len);
    }

    @Override
    public void write(@NonNull String str) {
        touchBuffer().append(str);
    }

    @Override
    public void write(@NonNull String str, int off, int len)  {
        touchBuffer().append(str, off, off + len);
    }

    @Override
    public OutputStringWriter append(CharSequence csq) {
        if (csq == null) {
            write("null");
        } else {
            write(csq.toString());
        }
        return this;
    }

    @Override
    public OutputStringWriter append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    @Override
    public OutputStringWriter append(char c) {
        write(c);
        return this;
    }

    @Override
    public String toString() {
        return (buffer != null ? touchBuffer().toString() : StringUtils.EMPTY);
    }

    @Override
    public void flush() {
        // Nothing to do
    }

    @Override
    public void close() {
        // Nothing to do
    }

    public boolean isDirty() {
        return (buffer != null);
    }

    private StringBuilder touchBuffer() {
        if (buffer == null) {
            if (initialSize > 0) {
                buffer = new StringBuilder(initialSize);
            } else {
                buffer = new StringBuilder();
            }
        }
        return buffer;
    }

}
