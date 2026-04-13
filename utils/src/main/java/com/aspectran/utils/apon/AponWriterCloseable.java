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

import org.jspecify.annotations.NonNull;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An {@link AponWriter} variant that implements {@link Closeable} for use with
 * try-with-resources blocks.
 * <p>This class enables automatic resource management of the underlying output
 * stream or file while serializing {@link Parameters} objects into APON format.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * try (AponWriterCloseable writer = new AponWriterCloseable(Paths.get("output.apon"))) {
 *     writer.write(params);
 * }
 * }</pre>
 */
public class AponWriterCloseable extends AponWriter implements Closeable {

    /**
     * Instantiates a new AponWriterCloseable that writes to the specified file.
     * <p>Pretty printing is enabled by default with two-space indentation.</p>
     * @param file the file to write to
     * @throws IOException if an I/O error occurs
     */
    public AponWriterCloseable(@NonNull File file) throws IOException {
        this(file.toPath());
    }

    /**
     * Instantiates a new AponWriterCloseable that writes to the specified file using the given charset.
     * <p>Pretty printing is enabled by default with two-space indentation.</p>
     * @param file the file to write to
     * @param charset the charset to use
     * @throws IOException if an I/O error occurs
     */
    public AponWriterCloseable(@NonNull File file, Charset charset) throws IOException {
        this(file.toPath(), charset);
    }

    /**
     * Instantiates a new AponWriterCloseable that writes to the specified path.
     * <p>Pretty printing is enabled by default with two-space indentation.</p>
     * @param path the path to write to
     * @throws IOException if an I/O error occurs
     */
    public AponWriterCloseable(@NonNull Path path) throws IOException {
        super(Files.newBufferedWriter(path));
    }

    /**
     * Instantiates a new AponWriterCloseable that writes to the specified path using the given charset.
     * <p>Pretty printing is enabled by default with two-space indentation.</p>
     * @param path the path to write to
     * @param charset the charset to use
     * @throws IOException if an I/O error occurs
     */
    public AponWriterCloseable(@NonNull Path path, Charset charset) throws IOException {
        super(Files.newBufferedWriter(path, charset));
    }

    /**
     * Instantiates a new AponWriterCloseable that wraps the given {@link Writer}.
     * <p>Pretty printing is enabled by default with two-space indentation.</p>
     * @param writer the character-output stream to wrap
     */
    public AponWriterCloseable(Writer writer) {
        super(writer);
    }

}
