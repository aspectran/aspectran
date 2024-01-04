/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.web.activity.request;

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.InvalidMediaTypeException;
import com.aspectran.web.support.http.MediaType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Provides convenient methods to parse the request headers.
 *
 * @since 6.2.0
 */
public class RequestHeaderParser {

    /**
     * A singleton list with {@link MediaType#ALL} that is returned from
     * {@link #resolveAcceptContentTypes} when no specific media types are requested.
     */
    public static final List<MediaType> MEDIA_TYPE_ALL_LIST = Collections.singletonList(MediaType.ALL);

    private RequestHeaderParser() {
    }

    @NonNull
    public static List<MediaType> resolveAcceptContentTypes(@NonNull RequestAdapter requestAdapter)
            throws HttpMediaTypeNotAcceptableException {
        Collection<String> acceptHeaderValues = requestAdapter.getHeaderValues(HttpHeaders.ACCEPT);
        if (acceptHeaderValues == null || acceptHeaderValues.isEmpty()) {
            return MEDIA_TYPE_ALL_LIST;
        }

        List<String> headerValues = new ArrayList<>(acceptHeaderValues);
        try {
            List<MediaType> mediaTypes = MediaType.parseMediaTypes(headerValues);
            MediaType.sortBySpecificityAndQuality(mediaTypes);
            if (mediaTypes != null && !mediaTypes.isEmpty()) {
                return mediaTypes;
            } else {
                return MEDIA_TYPE_ALL_LIST;
            }
        } catch (InvalidMediaTypeException e) {
            throw new HttpMediaTypeNotAcceptableException(
                "Could not parse 'Accept' header " + headerValues + ": " + e.getMessage());
        }
    }

}
