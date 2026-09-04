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
package com.aspectran.netty.server.handler.encoding;

import io.netty.handler.codec.compression.Brotli;
import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.handler.codec.compression.StandardCompressionOptions;
import io.netty.handler.codec.compression.Zstd;
import io.netty.handler.codec.http.HttpContentCompressor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A configurable factory bean for creating selective HTTP content compressor handlers.
 * <p>Supports configuring compression providers (e.g., "gzip", "deflate", "brotli", "zstd", "snappy")
 * and fine-grained predicates (media types, minimum size, user agent) via {@link ContentEncodingPredicates}.</p>
 *
 * <p>Created: 2026-09-03</p>
 */
public class NettyEncodingHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyEncodingHandler.class);

    public static final String GZIP = "gzip";

    public static final String DEFLATE = "deflate";

    public static final String BROTLI = "brotli";

    public static final String ZSTD = "zstd";

    public static final String SNAPPY = "snappy";

    private String[] encodingProviders;

    private ContentEncodingPredicates[] encodingPredicates;

    private int compressionLevel = 6;

    /**
     * Sets the names of the content encoding providers to enable (e.g., "gzip", "deflate", "brotli", "zstd", "snappy").
     * @param encodingProviders an array of provider names
     */
    public void setEncodingProviders(String... encodingProviders) {
        this.encodingProviders = encodingProviders;
    }

    /**
     * Returns the array of enabled encoding provider names.
     * @return the array of provider names
     */
    public String[] getEncodingProviders() {
        return encodingProviders;
    }

    /**
     * Sets the predicates that determine whether a response should be compressed.
     * If multiple predicates are provided, they are combined with a logical OR.
     * @param encodingPredicates an array of {@link ContentEncodingPredicates}
     */
    public void setEncodingPredicates(ContentEncodingPredicates... encodingPredicates) {
        this.encodingPredicates = encodingPredicates;
    }

    /**
     * Returns the compression predicates configured for this handler.
     * @return the array of {@link ContentEncodingPredicates}
     */
    public ContentEncodingPredicates[] getEncodingPredicates() {
        return encodingPredicates;
    }

    /**
     * Sets the compression level (1 = fastest, 9 = best compression, default = 6).
     * @param compressionLevel the compression level
     */
    public void setCompressionLevel(int compressionLevel) {
        if (compressionLevel < 1 || compressionLevel > 9) {
            throw new IllegalArgumentException("compressionLevel: " + compressionLevel + " (expected: 1-9)");
        }
        this.compressionLevel = compressionLevel;
    }

    /**
     * Returns the configured compression level.
     * @return the compression level (1-9)
     */
    public int getCompressionLevel() {
        return compressionLevel;
    }

    /**
     * Creates a new {@link HttpContentCompressor} instance configured with the specified
     * compression options and encoding predicates for a channel.
     * @return a new content compressor instance
     */
    @NonNull
    public HttpContentCompressor createContentCompressor() {
        CompressionOptions[] options = buildCompressionOptions();
        return new SelectiveHttpContentCompressor(encodingPredicates, options);
    }

    private CompressionOptions[] buildCompressionOptions() {
        List<CompressionOptions> optionsList = new ArrayList<>();
        if (encodingProviders != null && encodingProviders.length > 0) {
            for (String provider : encodingProviders) {
                if (GZIP.equalsIgnoreCase(provider)) {
                    optionsList.add(StandardCompressionOptions.gzip(compressionLevel, 15, 8));
                } else if (DEFLATE.equalsIgnoreCase(provider)) {
                    optionsList.add(StandardCompressionOptions.deflate(compressionLevel, 15, 8));
                } else if (BROTLI.equalsIgnoreCase(provider)) {
                    if (Brotli.isAvailable()) {
                        optionsList.add(StandardCompressionOptions.brotli());
                    } else {
                        logger.warn("Brotli compression requested but brotli4j is not available on classpath");
                    }
                } else if (ZSTD.equalsIgnoreCase(provider)) {
                    if (Zstd.isAvailable()) {
                        optionsList.add(StandardCompressionOptions.zstd());
                    } else {
                        logger.warn("Zstd compression requested but zstd-jni is not available on classpath");
                    }
                } else if (SNAPPY.equalsIgnoreCase(provider)) {
                    optionsList.add(StandardCompressionOptions.snappy());
                } else {
                    logger.warn("Unknown encoding provider: {}", provider);
                }
            }
        } else {
            // Default options: gzip, deflate, snappy
            optionsList.add(StandardCompressionOptions.gzip(compressionLevel, 15, 8));
            optionsList.add(StandardCompressionOptions.deflate(compressionLevel, 15, 8));
            optionsList.add(StandardCompressionOptions.snappy());
            if (Brotli.isAvailable()) {
                optionsList.add(StandardCompressionOptions.brotli());
            }
            if (Zstd.isAvailable()) {
                optionsList.add(StandardCompressionOptions.zstd());
            }
        }
        return optionsList.toArray(new CompressionOptions[0]);
    }

}
