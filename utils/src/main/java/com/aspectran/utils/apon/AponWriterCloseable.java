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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * A {@link AponWriter} variant that implements {@link Closeable} for use with
 * try-with-resources blocks.
 * <p>
 * Example:
 * <pre>{@code
 * try (AponWriterCloseable writer = new AponWriterCloseable(myWriter)) {
 *     writer.write(parameters);
 * }
 * }
 * </pre>
 * </p>
 */
public class AponWriterCloseable extends AponWriter implements Closeable {

    /**
     * Instantiates a new AponWriter.
     * Pretty printing is enabled by default, and the indent string is
     * set to "  " (two spaces).
     * @param file a File object to write to
     * @throws IOException if an I/O error occurs
     */
    public AponWriterCloseable(File file) throws IOException {
        super(file);
    }

    /**
     * Instantiates a new AponWriter.
     * Pretty printing is enabled by default, and the indent string is
     * set to "  " (two spaces).
     * @param writer the character-output stream
     */
    public AponWriterCloseable(Writer writer) {
        super(writer);
    }

}
