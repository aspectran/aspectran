/*
 * Copyright (c) 2008-present The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law alojreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.utils.apon;

import com.aspectran.utils.ResourceUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for reading from and writing to APON format.
 *
 * <p>Created: 2016. 9. 7.</p>
 */
class AponReadWriteTest {

    @TempDir
    Path tempDir;

    /**
     * Tests a full read-write cycle: read from a file, write to a temporary file,
     * read back from the temporary file, and verify data integrity.
     */
    @Test
    void testFileReadWriteCycle() throws IOException {
        // 1. Read from the original resource file
        File inputFile = ResourceUtils.getResourceAsFile("config/apon/apon-test.apon");
        Parameters originalParams = AponReader.read(inputFile);

        // 2. Write to a temporary file
        File outputFile = tempDir.resolve("apon-test-output.apon").toFile();
        try (AponWriterCloseable aponWriter = new AponWriterCloseable(outputFile)) {
            aponWriter.write(originalParams);
        }

        // 3. Read back from the temporary file
        Parameters rereadParams = AponReader.read(outputFile);

        // 4. Verify that the data is the same
        assertEquals(originalParams.toString(), rereadParams.toString());
    }

    /**
     * Tests an in-memory read-write cycle using StringWriter and AponReader.
     */
    @Test
    void testInMemoryReadWriteCycle() throws IOException {
        // 1. Create a Parameters object programmatically
        Parameters originalParams = new VariableParameters();
        originalParams.putValue("name", "test");
        originalParams.putValue("version", 1.0);
        Parameters nestedParams = new VariableParameters();
        nestedParams.putValue("key", "value");
        originalParams.putValue("nested", nestedParams);

        // 2. Write to a StringWriter
        StringWriter stringWriter = new StringWriter();
        try (AponWriterCloseable aponWriter = new AponWriterCloseable(stringWriter)) {
            aponWriter.write(originalParams);
        }
        String aponString = stringWriter.toString();

        // 3. Read back from the string
        Parameters rereadParams = AponReader.read(aponString);

        // 4. Verify that the data is the same
        assertEquals(originalParams.toString(), rereadParams.toString());
    }

}
