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

import com.aspectran.web.support.http.MediaType;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Encapsulates the conditions under which an HTTP response is eligible for content compression.
 * <p>This class allows for declarative configuration of conditions such as
 * minimum content size, compressible MIME types, and excluded user agents.</p>
 *
 * <p>Created: 2026-09-03</p>
 */
public class ContentEncodingPredicates {

    private static final Logger logger = LoggerFactory.getLogger(ContentEncodingPredicates.class);

    public static final long BREAK_EVEN_GZIP_SIZE = 23L;

    private long contentSizeLargerThan = 0L;

    private String[] mediaTypes;

    private List<MediaType> parsedMediaTypes;

    private String[] excludedUserAgents;

    private List<Pattern> excludedUserAgentPatterns;

    /**
     * Sets the minimum content size in bytes for a response to be considered for compression.
     * @param contentSizeLargerThan the minimum content size
     */
    public void setContentSizeLargerThan(long contentSizeLargerThan) {
        this.contentSizeLargerThan = contentSizeLargerThan;
        if (contentSizeLargerThan > 0L && contentSizeLargerThan < BREAK_EVEN_GZIP_SIZE) {
            logger.warn("contentSizeLargerThan of {} is inefficient for short content, " +
                    "break even size is {}", contentSizeLargerThan, BREAK_EVEN_GZIP_SIZE);
        }
    }

    /**
     * Returns the minimum content size in bytes for compression eligibility.
     * @return the minimum content size
     */
    public long getContentSizeLargerThan() {
        return contentSizeLargerThan;
    }

    /**
     * Sets the list of MIME types that are eligible for compression (e.g., "text/html", "application/json").
     * @param mediaTypes an array of compressible MIME types
     */
    public void setMediaTypes(String[] mediaTypes) {
        this.mediaTypes = mediaTypes;
        if (mediaTypes != null && mediaTypes.length > 0) {
            if (mediaTypes.length == 1) {
                this.parsedMediaTypes = Collections.singletonList(MediaType.parseMediaType(mediaTypes[0]));
            } else {
                List<MediaType> list = new ArrayList<>(mediaTypes.length);
                for (String mediaType : mediaTypes) {
                    list.add(MediaType.parseMediaType(mediaType));
                }
                this.parsedMediaTypes = list;
            }
        } else {
            this.parsedMediaTypes = null;
        }
    }

    /**
     * Returns the array of compressible MIME types.
     * @return the array of media types
     */
    public String[] getMediaTypes() {
        return mediaTypes;
    }

    /**
     * Sets a list of regular expressions for User-Agent headers that should be excluded from compression.
     * @param excludedUserAgents an array of User-Agent patterns to exclude
     */
    public void setExcludedUserAgents(String[] excludedUserAgents) {
        this.excludedUserAgents = excludedUserAgents;
        if (excludedUserAgents != null && excludedUserAgents.length > 0) {
            List<Pattern> patterns = new ArrayList<>(excludedUserAgents.length);
            for (String agent : excludedUserAgents) {
                patterns.add(Pattern.compile(agent));
            }
            this.excludedUserAgentPatterns = patterns;
        } else {
            this.excludedUserAgentPatterns = null;
        }
    }

    /**
     * Returns the array of User-Agent patterns excluded from compression.
     * @return the array of excluded User-Agent regex patterns
     */
    public String[] getExcludedUserAgents() {
        return excludedUserAgents;
    }

    /**
     * Determines whether the given HTTP response satisfies all the configured compression predicates.
     * @param response the HTTP response
     * @param userAgent the client's User-Agent string, if present
     * @return {@code true} if all conditions are satisfied; {@code false} otherwise
     */
    public boolean matches(@NonNull HttpResponse response, @Nullable String userAgent) {
        if (contentSizeLargerThan > 0L) {
            String contentLengthStr = response.headers().get(HttpHeaderNames.CONTENT_LENGTH);
            if (contentLengthStr != null) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    if (contentLength < contentSizeLargerThan) {
                        return false;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (parsedMediaTypes != null && !parsedMediaTypes.isEmpty()) {
            String contentTypeStr = response.headers().get(HttpHeaderNames.CONTENT_TYPE);
            if (contentTypeStr == null) {
                return false;
            }
            try {
                MediaType responseMediaType = MediaType.parseMediaType(contentTypeStr);
                boolean matched = false;
                for (MediaType mediaType : parsedMediaTypes) {
                    if (mediaType.isCompatibleWith(responseMediaType)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (excludedUserAgentPatterns != null && userAgent != null) {
            for (Pattern pattern : excludedUserAgentPatterns) {
                if (pattern.matcher(userAgent).find()) {
                    return false;
                }
            }
        }

        return true;
    }

}
