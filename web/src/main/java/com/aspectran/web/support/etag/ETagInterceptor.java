/*
 * Copyright (c) 2008-2020 The Aspectran Project
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
import com.aspectran.core.lang.NonNull;
import com.aspectran.core.lang.Nullable;
import com.aspectran.core.util.DigestUtils;
import com.aspectran.core.util.StringUtils;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ETagInterceptor {

    private static final String DIRECTIVE_NO_STORE = "no-store";

    /**
     * Pattern matching ETag multiple field values in headers such as "If-Match", "If-None-Match".
     * @see <a href="https://tools.ietf.org/html/rfc7232#section-2.3">Section 2.3 of RFC 7232</a>
     */
    private static final Pattern ETAG_HEADER_VALUE_PATTERN = Pattern.compile("\\*|\\s*((W\\/)?(\"[^\"]*\"))\\s*,?");

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

    public void intercept(Translet translet) {
        HttpServletResponse response = translet.getResponseAdapter().getAdaptee();
        String cacheControl = response.getHeader(HttpHeaders.CACHE_CONTROL);
        if (cacheControl == null || !cacheControl.contains(DIRECTIVE_NO_STORE)) {
            String eTag = response.getHeader(HttpHeaders.ETAG);
            if (!StringUtils.hasText(eTag)) {
                eTag = generateETagHeaderValue(translet);
                response.setHeader(HttpHeaders.ETAG, eTag);
            }
            boolean notModified = validateIfNoneMatch(translet.getRequestAdapter(), eTag);
            if (notModified) {
                ResponseTemplate responseTemplate = new ResponseTemplate(translet.getResponseAdapter());
                responseTemplate.setStatus(HttpStatus.NOT_MODIFIED.value());
                translet.response(responseTemplate);
            }
        }
    }

    private String generateETagHeaderValue(Translet translet) {
        // length of W/ + " + 0 + 32bits md5 hash + "
        StringBuilder builder = new StringBuilder(37);
        if (isWriteWeakETag()) {
            builder.append("W/");
        }
        builder.append("\"0");
        builder.append(DigestUtils.md5DigestAsHex(tokenFactory.getToken(translet)));
        builder.append('"');
        return builder.toString();
    }

    private boolean validateIfNoneMatch(RequestAdapter requestAdapter,  @Nullable String etag) {
        if (!StringUtils.hasLength(etag)) {
            return false;
        }

        List<String> ifNoneMatch = requestAdapter.getHeaderValues(HttpHeaders.IF_NONE_MATCH);
        if (ifNoneMatch == null || ifNoneMatch.isEmpty()) {
            return false;
        }

        // We will perform this validation...
        etag = padEtagIfNecessary(etag);
        if (etag.startsWith("W/")) {
            etag = etag.substring(2);
        }
        for (String clientETags : ifNoneMatch) {
            Matcher etagMatcher = ETAG_HEADER_VALUE_PATTERN.matcher(clientETags);
            // Compare weak/strong ETags as per https://tools.ietf.org/html/rfc7232#section-2.3
            while (etagMatcher.find()) {
                if (StringUtils.hasLength(etagMatcher.group()) && etag.equals(etagMatcher.group(3))) {
                    return true;
                }
            }
        }

        return false;
    }

    private String padEtagIfNecessary(String etag) {
        if (!StringUtils.hasLength(etag)) {
            return etag;
        }
        if ((etag.startsWith("\"") || etag.startsWith("W/\"")) && etag.endsWith("\"")) {
            return etag;
        }
        return "\"" + etag + "\"";
    }

}
