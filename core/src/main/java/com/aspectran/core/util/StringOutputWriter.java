/*
 * Copyright (c) 2008-2018 The Aspectran Project
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

import java.io.Writer;

/**
 * A character stream that collects its output in a string builder,
 * which can then be used to construct a string.
 * <p>Closing a StringWriter has no effect. The methods in this class can be called
 * after the stream has been closed without generating an IOException.</p>
 */
public class StringOutputWriter extends Writer {

    private final StringBuilder buffer;

    /**
     * Create a new string writer using the default initial string-builder size.
     */
    public StringOutputWriter() {
        this.buffer = new StringBuilder();
    }

    /**
     * Create a new string writer using the specified initial string-builder size.
     *
     * @param initialSize the number of char values that will fit into this buffer
     *         before it is automatically expanded
     */
    public StringOutputWriter(int initialSize) {
        this.buffer = new StringBuilder(initialSize);
    }

    @Override
    public void write(int c) {
        buffer.append((char)c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        buffer.append(cbuf, off, len);
    }

    @Override
    public void write(String str) {
        buffer.append(str);
    }

    @Override
    public void write(String str, int off, int len)  {
        buffer.append(str.substring(off, off + len));
    }

    @Override
    public StringOutputWriter append(CharSequence csq) {
        if (csq == null) {
            write("null");
        } else {
            write(csq.toString());
        }
        return this;
    }

    @Override
    public StringOutputWriter append(CharSequence csq, int start, int end) {
        CharSequence cs = (csq == null ? "null" : csq);
        write(cs.subSequence(start, end).toString());
        return this;
    }

    @Override
    public StringOutputWriter append(char c) {
        write(c);
        return this;
    }

    @Override
    public String toString() {
        return buffer.toString();
    }

    @Override
    public void flush() {
        // Nothing to do
    }

    @Override
    public void close() {
        // Nothing to do
    }

}
