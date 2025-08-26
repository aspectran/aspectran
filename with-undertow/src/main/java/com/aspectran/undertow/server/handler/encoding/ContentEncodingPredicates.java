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
package com.aspectran.undertow.server.handler.encoding;

import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.MediaType;
import io.undertow.attribute.RequestHeaderAttribute;
import io.undertow.predicate.Predicate;
import io.undertow.predicate.Predicates;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A factory bean that creates a composite {@link Predicate} for determining
 * whether a response is eligible for content encoding (compression).
 * <p>This class allows for declarative configuration of multiple conditions, such as
 * minimum content size, compressible MIME types, and excluded user agents.</p>
 *
 * <p>Created: 4/6/24</p>
 */
public class ContentEncodingPredicates {

    private static final Logger logger = LoggerFactory.getLogger(ContentEncodingPredicates.class);

    public static final long BREAK_EVEN_GZIP_SIZE = 23L;

    private long contentSizeLargerThan = 0L;

    private String[] mediaTypes;

    private String[] excludedUserAgents;

    /**
     * Sets the minimum content size in bytes for a response to be considered for compression.
     * @param contentSizeLargerThan the minimum content size
     */
    public void setContentSizeLargerThan(long contentSizeLargerThan) {
        this.contentSizeLargerThan = contentSizeLargerThan;
    }

    /**
     * Sets the list of MIME types that are eligible for compression (e.g., "text/html", "application/json").
     * @param mediaTypes an array of compressible MIME types
     */
    public void setMediaTypes(String[] mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    /**
     * Sets a list of regular expressions for User-Agent headers that should be excluded from compression.
     * @param excludedUserAgents an array of User-Agent patterns to exclude
     */
    public void setExcludedUserAgents(String[] excludedUserAgents) {
        this.excludedUserAgents = excludedUserAgents;
    }

    /**
     * Creates a composite {@link Predicate} by combining all configured conditions with a logical AND.
     * @return the final predicate to be used by the {@link EncodingHandlerWrapper}
     */
    @NonNull
    public Predicate createPredicate() {
        List<Predicate> predicates = new ArrayList<>();
        if (contentSizeLargerThan > 0L) {
            if (contentSizeLargerThan < BREAK_EVEN_GZIP_SIZE) {
                logger.warn("contentSizeLargerThan of {} is inefficient for short content, " +
                        "break even is size {}", contentSizeLargerThan, BREAK_EVEN_GZIP_SIZE);
            }
            predicates.add(Predicates.requestLargerThan(contentSizeLargerThan));
        }
        if (mediaTypes != null && mediaTypes.length > 0) {
            predicates.add(new CompressibleMimeTypePredicate(mediaTypes));
        }
        if (excludedUserAgents != null) {
            for (String agent : excludedUserAgents) {
                RequestHeaderAttribute agentHeader = new RequestHeaderAttribute(new HttpString(HttpHeaders.USER_AGENT));
                predicates.add(Predicates.not(Predicates.regex(agentHeader, agent)));
            }
        }
        return Predicates.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * A custom predicate to check if the response content type is among the compressible MIME types.
     */
    private static class CompressibleMimeTypePredicate implements Predicate {

        private final List<MediaType> mediaTypes;

        private CompressibleMimeTypePredicate(@NonNull String[] mediaTypes) {
            if (mediaTypes.length == 1) {
                this.mediaTypes = Collections.singletonList(MediaType.parseMediaType(mediaTypes[0]));
            } else {
                this.mediaTypes = new ArrayList<>(mediaTypes.length);
                for (String mediaType : mediaTypes) {
                    this.mediaTypes.add(MediaType.parseMediaType(mediaType));
                }
            }
        }

        @Override
        public boolean resolve(@NonNull HttpServerExchange exchange) {
            String contentType = exchange.getResponseHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
            if (contentType != null) {
                for (MediaType mediaType : this.mediaTypes) {
                    if (mediaType.isCompatibleWith(MediaType.parseMediaType(contentType))) {
                        return true;
                    }
                }
            }
            return false;
        }

    }

}
