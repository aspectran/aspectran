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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.jspecify.annotations.Nullable;

/**
 * An {@link HttpContentCompressor} that selectively compresses HTTP responses based on
 * configured {@link ContentEncodingPredicates} (such as media types, minimum size, and user agent).
 *
 * <p>Created: 2026-09-03</p>
 */
public class SelectiveHttpContentCompressor extends HttpContentCompressor {

    private final ContentEncodingPredicates[] encodingPredicates;

    private String currentUserAgent;

    /**
     * Creates a new selective HTTP content compressor.
     * @param encodingPredicates an optional array of predicates to filter compressible responses
     * @param compressionOptions variable compression options to apply when encoding
     */
    public SelectiveHttpContentCompressor(
            @Nullable ContentEncodingPredicates[] encodingPredicates,
            CompressionOptions... compressionOptions) {
        super(compressionOptions != null && compressionOptions.length > 0
                ? compressionOptions
                : new CompressionOptions[0]);
        this.encodingPredicates = encodingPredicates;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest request) {
            this.currentUserAgent = request.headers().get(HttpHeaderNames.USER_AGENT);
        }
        super.channelRead(ctx, msg);
    }

    @Override
    protected Result beginEncode(HttpResponse headers, String acceptEncoding) throws Exception {
        if (encodingPredicates != null && encodingPredicates.length > 0) {
            boolean matched = false;
            for (ContentEncodingPredicates predicate : encodingPredicates) {
                if (predicate.matches(headers, currentUserAgent)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return null;
            }
        }
        return super.beginEncode(headers, acceptEncoding);
    }

}
