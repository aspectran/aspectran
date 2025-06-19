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
package com.aspectran.web.support.etag;

import com.aspectran.core.activity.Translet;
import com.aspectran.core.activity.response.ResponseTemplate;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.utils.DigestUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Etag interceptor to enable If-None-Match request with ETAG support.
 * If the ETag generated for each request matches the value in the If-None-Match header,
 * an empty response with status code 304 (not modified) is sent to the client.
 *
 * @since 6.9.4
 */
public class ETagInterceptor {

    private static final String DIRECTIVE_NO_STORE = "no-store";

    /**
     * Pattern matching ETag multiple field values in headers such as "If-Match", "If-None-Match".
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section 2.3 of RFC 7232</a>
     */
    private static final Pattern ETAG_HEADER_VALUE_PATTERN = Pattern.compile("\\*|\\s*((W/)?(\"[^\"]*\"))\\s*,?");

    private final ETagTokenFactory tokenFactory;

    private boolean writeWeakETag = false;

    public ETagInterceptor(@NonNull ETagTokenFactory tokenFactory) {
        this.tokenFactory = tokenFactory;
    }

    /**
     * Set whether the ETag value written to the response should be weak, as per RFC 7232.
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">RFC 7232 section 2.3</a>
     */
    public void setWriteWeakETag(boolean writeWeakETag) {
        this.writeWeakETag = writeWeakETag;
    }

    /**
     * Return whether the ETag value written to the response should be weak, as per RFC 7232.
     */
    public boolean isWriteWeakETag() {
        return this.writeWeakETag;
    }

    public void intercept(@NonNull Translet translet) {
        HttpServletResponse response = translet.getResponseAdapter().getAdaptee();
        String cacheControl = response.getHeader(HttpHeaders.CACHE_CONTROL);
        if (cacheControl == null || !cacheControl.contains(DIRECTIVE_NO_STORE)) {
            String token = response.getHeader(HttpHeaders.ETAG);
            if (!StringUtils.hasText(token)) {
                token = generateETagToken(translet, writeWeakETag);
                if (token == null) {
                    return;
                }
                response.setHeader(HttpHeaders.ETAG, token);
            }
            boolean notModified = validateIfNoneMatch(translet.getRequestAdapter(), token);
            if (notModified) {
                ResponseTemplate responseTemplate = new ResponseTemplate(translet.getResponseAdapter());
                responseTemplate.setStatus(HttpStatus.NOT_MODIFIED.value());
                translet.response(responseTemplate);
            }
        }
    }

    protected String generateETagToken(Translet translet, boolean isWeak) {
        byte[] token = tokenFactory.getToken(translet);
        if (token == null || token.length == 0) {
            return null;
        }
        // length of W/ + " + 0 + 32bits md5 hash + "
        StringBuilder builder = new StringBuilder(37);
        if (isWeak) {
            builder.append("W/");
        }
        builder.append("\"0");
        builder.append(DigestUtils.md5DigestAsHex(token));
        builder.append('"');
        return builder.toString();
    }

    private boolean validateIfNoneMatch(@NonNull RequestAdapter requestAdapter, String token) {
        List<String> ifNoneMatch = requestAdapter.getHeaderValues(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch == null || ifNoneMatch.isEmpty()) {
            return false;
        }

        // We will perform this validation...
        token = padETagTokenIfNecessary(token);
        if (token.startsWith("W/")) {
            token = token.substring(2);
        }
        for (String tags : ifNoneMatch) {
            Matcher tokenMatcher = ETAG_HEADER_VALUE_PATTERN.matcher(tags);
            // Compare weak/strong ETags as per https://tools.ietf.org/html/rfc7232#section-2.3
            while (tokenMatcher.find()) {
                if (StringUtils.hasLength(tokenMatcher.group()) && token.equals(tokenMatcher.group(3))) {
                    return true;
                }
            }
        }
        return false;
    }

    @NonNull
    private String padETagTokenIfNecessary(@NonNull String token) {
        if ((token.startsWith("\"") || token.startsWith("W/\"")) && token.endsWith("\"")) {
            return token;
        } else {
            return "\"" + token + "\"";
        }
    }

}
