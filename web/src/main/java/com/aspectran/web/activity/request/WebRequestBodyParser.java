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

import com.aspectran.core.activity.request.RequestBodyParser;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.LinkedMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.JsonToApon;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.XmlToApon;
import com.aspectran.web.support.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

/**
 * Provides convenient methods to parse the request body.
 *
 * @since 6.2.0
 */
public class WebRequestBodyParser {

    private static final String DEFAULT_ENCODING = "ISO-8859-1";

    private static final int BUFFER_SIZE = 1024;

    private WebRequestBodyParser() {
    }

    @NonNull
    public static String parseBody(InputStream inputStream, String encoding)
            throws IOException, SizeLimitExceededException {
        return parseBody(inputStream, encoding, 0L);
    }

    @NonNull
    public static String parseBody(InputStream inputStream, String encoding, long maxSize)
            throws IOException, SizeLimitExceededException {
        StringBuilder sb = new StringBuilder();
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        InputStreamReader reader = new InputStreamReader(inputStream, encoding);
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead;
        long bytesTotal = 0L;
        while ((bytesRead = reader.read(buffer)) != -1) {
            if (maxSize > 0L) {
                bytesTotal += bytesRead;
                if (bytesTotal > maxSize) {
                    throw new SizeLimitExceededException("Maximum request size exceeded; actual: " +
                            bytesTotal + "; permitted: " + maxSize,
                            bytesTotal, maxSize);
                }
            }
            sb.append(buffer, 0, bytesRead);
        }
        return sb.toString();
    }

    @Nullable
    public static MultiValueMap<String, String> parseURLEncoded(String body, String encoding)
            throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(body)) {
            return null;
        }
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        String[] pairs = StringUtils.tokenize(body, "&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx == -1) {
                String name = URLDecoder.decode(pair, encoding);
                multiValueMap.add(name, null);
            } else {
                String name = URLDecoder.decode(pair.substring(0, idx), encoding);
                String value = URLDecoder.decode(pair.substring(idx + 1), encoding);
                multiValueMap.add(name, value);
            }
        }
        return multiValueMap;
    }

    public static void parseURLEncodedFormData(RequestAdapter requestAdapter) throws RequestParseException {
        try {
            String body = requestAdapter.getBody();
            String encoding = requestAdapter.getEncoding();
            MultiValueMap<String, String> multiValueMap = parseURLEncoded(body, encoding);
            if (multiValueMap != null) {
                requestAdapter.putAllParameters(multiValueMap);
                requestAdapter.setBody(null);
            }
        } catch (Exception e) {
            throw new RequestParseException("Could not parse HTTP " + requestAdapter.getRequestMethod() +
                    " request body", e);
        }
    }

    @Nullable
    public static <T extends Parameters> T parseURLEncodedAsParameters(
            RequestAdapter requestAdapter, Class<T> requiredType) throws RequestParseException {
        try {
            String encoding = requestAdapter.getEncoding();
            if (encoding == null) {
                encoding = DEFAULT_ENCODING;
            }
            MultiValueMap<String, String> multiValueMap = parseURLEncoded(requestAdapter.getBody(), encoding);
            if (multiValueMap != null && !multiValueMap.isEmpty()) {
                T parameters = ClassUtils.createInstance(requiredType);
                for (Map.Entry<String, List<String>> entry : multiValueMap.entrySet()) {
                    String name = entry.getKey();
                    for (String value : entry.getValue()) {
                        parameters.putValue(name, value);
                    }
                }
                return parameters;
            }
        } catch (Exception e) {
            throw new RequestParseException("Failed to parse URL-encoded form request body to required type [" +
                    requiredType.getName() + "]", e);
        }
        return null;
    }

    public static <T extends Parameters> T parseBodyAsParameters(
            RequestAdapter requestAdapter, @Nullable MediaType mediaType, Class<T> requiredType)
            throws RequestParseException {
        if (mediaType == null) {
            return null;
        }
        if (isURLEncodedForm(mediaType)) {
            return parseURLEncodedAsParameters(requestAdapter, requiredType);
        } else if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mediaType)) {
            try {
                return JsonToApon.from(requestAdapter.getBody(), requiredType);
            } catch (IOException e) {
                throw new RequestParseException("Failed to parse request body of JSON format to required type [" +
                    requiredType.getName() + "]", e);
            }
        } else if (MediaType.APPLICATION_APON.equalsTypeAndSubtype(mediaType)) {
            return RequestBodyParser.parseBodyAsParameters(requestAdapter.getBody(), requiredType);
        } else if (MediaType.APPLICATION_XML.equalsTypeAndSubtype(mediaType)) {
            try {
                return XmlToApon.from(requestAdapter.getBody(), requiredType);
            } catch (IOException e) {
                throw new RequestParseException("Failed to parse request body of XML format to required type [" +
                        requiredType.getName() + "]", e);
            }
        } else {
            return null;
        }
    }

    public static boolean isMultipartForm(MethodType requestMethod, MediaType mediaType) {
        return MethodType.POST.equals(requestMethod) &&
            MediaType.MULTIPART_FORM_DATA.equalsTypeAndSubtype(mediaType);
    }

    public static boolean isURLEncodedForm(MediaType mediaType) {
        return MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(mediaType);
    }

}
