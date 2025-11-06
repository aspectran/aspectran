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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for AponReaderCloseable.
 */
class AponReaderCloseableTest {

    /**
     * Tests that AponReaderCloseable works correctly within a try-with-resources block.
     */
    @Test
    void testSuccessfulReadWithTryWithResources() throws AponParseException {
        String apon = """
            aspectran: {
                settings: {
                    transletNameSuffix: .job
                }
                bean: {
                    id: *
                    scan: test.**.*Schedule
                    mask: test.**.*
                    scope: singleton
                }
            }
            """;

        StringReader reader = new StringReader(apon);
        try (AponReaderCloseable aponReader = new AponReaderCloseable(reader)) {
            aponReader.read();
        }
    }

    /**
     * Verifies that the underlying reader is actually closed when the AponReaderCloseable is closed.
     */
    @Test
    void testReaderIsClosed() throws AponParseException {
        String apon = "name: value";
        CloseTrackingStringReader trackingReader = new CloseTrackingStringReader(apon);

        try (AponReaderCloseable aponReader = new AponReaderCloseable(trackingReader)) {
            aponReader.read();
        }

        assertTrue(trackingReader.isClosed(), "The underlying reader should have been closed");
    }

    /**
     * A helper StringReader that tracks whether it has been closed.
     */
    private static class CloseTrackingStringReader extends StringReader {
        private boolean closed = false;

        public CloseTrackingStringReader(String s) {
            super(s);
        }

        @Override
        public void close() {
            super.close();
            this.closed = true;
        }

        public boolean isClosed() {
            return closed;
        }
    }

    /**
     * Tests that attempting to read from a closed reader throws an exception.
     */
    @Test
    void testReadAfterCloseThrowsException() throws AponParseException {
        String apon = "name: value";
        AponReaderCloseable aponReader = new AponReaderCloseable(new StringReader(apon));
        aponReader.read();
        aponReader.close();

        assertThrows(IOException.class, aponReader::read,
                "Reading from a closed reader should throw IOException");
    }

}
