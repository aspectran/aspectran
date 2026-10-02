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
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.Assert;
import com.aspectran.utils.DigestUtils;
import com.aspectran.utils.StringUtils;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A strategy for handling ETag (entity tag) generation and validation to support
 * conditional HTTP requests.
 * <p>This class can be used from an aspect to intercept a request. It checks for
 * the {@code If-None-Match} header and, if it matches the ETag generated for the
 * current resource state, it sends a {@code 304 Not Modified} response.
 * Otherwise, it computes and sets the {@code ETag} header on the response.</p>
 *
 * @since 6.9.4
 */
@NullMarked
public class ETagInterceptor {

    private static final String DIRECTIVE_NO_STORE = "no-store";

    /**
     * Pattern matching ETag multiple field values in headers such as "If-Match", "If-None-Match".
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section 2.3 of RFC 7232</a>
     */
    private static final Pattern ETAG_HEADER_VALUE_PATTERN = Pattern.compile("\\*|\\s*((W/)?(\"[^\"]*\"))\\s*,?");

    private final ETagTokenFactory tokenFactory;

    private boolean writeWeakETag = false;

    /**
     * Instantiates a new ETag interceptor.
     * @param tokenFactory the factory to generate the token for the ETag
     */
    public ETagInterceptor(ETagTokenFactory tokenFactory) {
        Assert.notNull(tokenFactory, "tokenFactory must not be null");
        this.tokenFactory = tokenFactory;
    }

    /**
     * Sets whether the ETag value written to the response should be weak, as per RFC 7232.
     * @param writeWeakETag {@code true} to write a weak ETag, {@code false} for a strong one
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">RFC 7232 section 2.3</a>
     */
    public void setWriteWeakETag(boolean writeWeakETag) {
        this.writeWeakETag = writeWeakETag;
    }

    /**
     * Returns whether the ETag value written to the response should be weak, as per RFC 7232.
     * @return {@code true} if the ETag should be weak, {@code false} otherwise
     */
    public boolean isWriteWeakETag() {
        return this.writeWeakETag;
    }

    /**
     * Intercepts the request to perform ETag validation and generation.
     * @param translet the current translet
     */
    public void intercept(Translet translet) {
        Assert.notNull(translet, "translet must not be null");

        RequestAdapter request = translet.getRequestAdapter();
        ResponseAdapter response = translet.getResponseAdapter();

        if (!isEligibleMethod(request.getRequestMethod()) || !isEligibleResponse(response)) {
            return;
        }

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
            boolean notModified = validateIfNoneMatch(request, token);
            if (notModified) {
                ResponseTemplate responseTemplate = new ResponseTemplate(response);
                responseTemplate.setStatus(HttpStatus.NOT_MODIFIED.value());
                translet.response(responseTemplate);
            }
        }
    }

    /**
     * Checks if the request method is eligible for ETag processing.
     * Only GET and HEAD methods are typically applicable for 304 Not Modified conditional responses.
     * @param method the HTTP request method
     * @return {@code true} if the method is GET or HEAD, {@code false} otherwise
     */
    protected boolean isEligibleMethod(@Nullable MethodType method) {
        return (method == MethodType.GET || method == MethodType.HEAD);
    }

    /**
     * Checks if the response is eligible for ETag header generation.
     * @param response the response adapter
     * @return {@code true} if eligible, {@code false} otherwise
     */
    protected boolean isEligibleResponse(ResponseAdapter response) {
        int status = response.getStatus();
        return (status == 0 || HttpStatus.valueOf(status).is2xxSuccessful());
    }

    /**
     * Generates an ETag token for the current request.
     * @param translet the current translet
     * @param isWeak whether to generate a weak ETag
     * @return the generated ETag string (e.g., "0a1b2c3d..." or W/"0a1b2c3d...")
     */
    @Nullable
    protected String generateETagToken(Translet translet, boolean isWeak) {
        byte[] token = tokenFactory.getToken(translet);
        if (token == null || token.length == 0) {
            return null;
        }
        // length of W/ + " + 0 + 32-char md5 hash + "
        StringBuilder builder = new StringBuilder(37);
        if (isWeak) {
            builder.append("W/");
        }
        builder.append("\"0");
        DigestUtils.appendMd5DigestAsHex(token, builder);
        builder.append('"');
        return builder.toString();
    }

    /**
     * Validates whether the given ETag token matches the client's {@code If-None-Match} header.
     * @param requestAdapter the request adapter
     * @param token the generated or existing ETag token
     * @return {@code true} if matched (indicating 304 Not Modified), {@code false} otherwise
     */
    private boolean validateIfNoneMatch(RequestAdapter requestAdapter, String token) {
        List<String> ifNoneMatch = requestAdapter.getHeaderValues(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch == null || ifNoneMatch.isEmpty()) {
            return false;
        }

        token = ensureQuoted(token);
        if (token.startsWith("W/")) {
            token = token.substring(2);
        }
        for (String tags : ifNoneMatch) {
            Matcher tokenMatcher = ETAG_HEADER_VALUE_PATTERN.matcher(tags);
            // Compare weak/strong ETags as per https://tools.ietf.org/html/rfc7232#section-2.3
            while (tokenMatcher.find()) {
                String match = tokenMatcher.group();
                if (StringUtils.hasLength(match)) {
                    if ("*".equals(match.trim()) || token.equals(tokenMatcher.group(3))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String ensureQuoted(String token) {
        if ((token.startsWith("\"") || token.startsWith("W/\"")) && token.endsWith("\"")) {
            return token;
        } else {
            return "\"" + token + "\"";
        }
    }

}
