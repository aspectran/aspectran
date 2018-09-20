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
package com.aspectran.web.support.multipart.inmemory;

import org.apache.commons.io.output.ThresholdingOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream which will retain data in memory (always).
 */
public class MemoryOutputStream extends ThresholdingOutputStream {

    /**
     * The output stream to which data will be written prior to the threshold being reached.
     */
    private final ByteArrayOutputStream outputStream;

    /**
     * True when close() has been called successfully.
     */
    private boolean closed;

    /**
     * Constructs an instance of this class which will trigger throw
     * UnsupportedOperationException if the specified threshold is reached.
     *
     * @param threshold the number of bytes at which to trigger an event
     */
    public MemoryOutputStream(long threshold) {
        super((int)threshold);
        outputStream = new ByteArrayOutputStream();
    }

    /**
     * Returns the current output stream. This may be memory based or disk based, depending on the
     * current state with respect to the threshold.
     *
     * @return the underlying output stream
     * @exception IOException if an error occurs
     */
    @Override
    protected OutputStream getStream() throws IOException {
        return outputStream;
    }

    /**
     * Not possible in GAE. Will never reach!!
     * If it happens, try changing max upload size setting.
     */
    @Override
    protected void thresholdReached() {
        throw new UnsupportedOperationException("Not possible in GAE. Will never reach!! " +
                "Try changing max upload size setting.");
    }

    /**
     * Determines whether or not the data for this output stream has been retained in memory.
     *
     * @return always {@code true}
     */
    public boolean isInMemory() {
        return true;
    }

    /**
     * Returns the data for this output stream as an array of bytes.
     *
     * @return the data for this output stream, or {@code null} if no such data is available
     */
    public byte[] getData() {
        return outputStream.toByteArray();
    }

    /**
     * Closes underlying output stream, and mark this as closed
     *
     * @exception IOException if an error occurs
     */
    @Override
    public void close() throws IOException {
        super.close();
        closed = true;
    }

    /**
     * Writes the data from this output stream to the specified output stream, after it has been
     * closed.
     *
     * @param out output stream to write to
     * @exception IOException if this stream is not yet closed or an error occurs
     */
    public void writeTo(OutputStream out) throws IOException {
        // we may only need to check if this is closed if we are working with a file
        // but we should force the habit of closing whether we are working with a file or memory.
        if (!closed) {
            throw new IOException("Stream not closed");
        }

        outputStream.writeTo(out);
    }

}