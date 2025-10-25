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
package com.aspectran.web.activity.request;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.request.RequestBodyParser;
import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.activity.request.SizeLimitExceededException;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.ClassUtils;
import com.aspectran.utils.LinkedMultiValueMap;
import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.annotation.jsr305.NonNull;
import com.aspectran.utils.annotation.jsr305.Nullable;
import com.aspectran.utils.apon.JsonToParameters;
import com.aspectran.utils.apon.Parameters;
import com.aspectran.utils.apon.XmlToParameters;
import com.aspectran.web.adapter.WebRequestAdapter;
import com.aspectran.web.support.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Provides convenient methods to parse the request body.
 *
 * @since 6.2.0
 */
public abstract class WebRequestBodyParser {

    public static final String MULTIPART_FORM_DATA_PARSER_SETTING_NAME = "multipartFormDataParser";

    public static final String MAX_REQUEST_SIZE_SETTING_NAME = "maxRequestSize";

    private static final Charset DEFAULT_ENCODING = StandardCharsets.ISO_8859_1;

    private static final int BUFFER_SIZE = 1024;

    /**
     * Parses a multipart form data request using the configured {@link MultipartFormDataParser}.
     * The name of the parser bean must be specified as a setting in the activity.
     * @param activity the current activity
     * @throws MultipartRequestParseException if the parser is not configured or if parsing fails
     */
    public static void parseMultipartFormData(@NonNull Activity activity) throws MultipartRequestParseException {
        String multipartFormDataParser = activity.getSetting(MULTIPART_FORM_DATA_PARSER_SETTING_NAME);
        if (multipartFormDataParser == null) {
            throw new MultipartRequestParseException("The 'multipartFormDataParser' setting is not specified. " +
                    "This setting is required to parse multipart form data. Please configure it in your " +
                    "aspect rules with the name of the multipart parser bean.");
        }

        MultipartFormDataParser parser = activity.getBean(multipartFormDataParser);
        if (parser == null) {
            throw new MultipartRequestParseException("The multipart form data parser bean named '" +
                    multipartFormDataParser + "' could not be found. Please ensure that a bean with this name " +
                    "is correctly defined in your configuration.");
        }
        parser.parse(activity.getRequestAdapter());
    }

    /**
     * Parses the request body as a string.
     * This method respects the maximum request size limit.
     * @param requestAdapter the web request adapter
     * @return the request body as a string
     * @throws IOException if an I/O error occurs
     * @throws SizeLimitExceededException if the request size exceeds the configured maximum
     */
    @NonNull
    public static String parseBody(WebRequestAdapter requestAdapter) throws IOException, SizeLimitExceededException {
        Charset encoding = determineEncoding(requestAdapter);
        InputStream inputStream = requestAdapter.getInputStream();
        long maxSize = requestAdapter.getMaxRequestSize();
        StringBuilder sb = new StringBuilder();
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

    /**
     * Parses the request body into a {@link Parameters} object of the specified type,
     * based on the request's Content-Type.
     * <p>Supports {@code application/x-www-form-urlencoded}, {@code application/json},
     * {@code application/apon}, and {@code application/xml}.</p>
     * @param requestAdapter the web request adapter
     * @param requiredType the target {@code Parameters} type
     * @param <T> the type of the parameters object
     * @return the parsed {@code Parameters} object, or {@code null} if the content type is not supported
     * @throws RequestParseException if parsing fails
     */
    @Nullable
    public static <T extends Parameters> T parseBodyAsParameters(
            @NonNull WebRequestAdapter requestAdapter, Class<T> requiredType) throws RequestParseException {
        MediaType mediaType = requestAdapter.getMediaType();
        if (mediaType == null) {
            return null;
        }
        if (isURLEncodedForm(mediaType)) {
            return parseURLEncodedBodyAsParameters(requestAdapter, requiredType);
        } else if (MediaType.APPLICATION_JSON.equalsTypeAndSubtype(mediaType)) {
            try {
                return JsonToParameters.from(requestAdapter.getBody(), requiredType);
            } catch (IOException e) {
                throw new RequestParseException("Failed to parse request body of JSON format to required type [" +
                        requiredType.getName() + "]", e);
            }
        } else if (MediaType.APPLICATION_APON.equalsTypeAndSubtype(mediaType)) {
            return RequestBodyParser.parseBodyAsParameters(requestAdapter.getBody(), requiredType);
        } else if (MediaType.APPLICATION_XML.equalsTypeAndSubtype(mediaType)) {
            try {
                return XmlToParameters.from(requestAdapter.getBody(), requiredType);
            } catch (IOException e) {
                throw new RequestParseException("Failed to parse request body of XML format to required type [" +
                        requiredType.getName() + "]", e);
            }
        } else {
            return null;
        }
    }

    /**
     * Parse the URL-encoded Form Data to get the request parameters.
     */
    public static void parseURLEncodedFormData(WebRequestAdapter requestAdapter) throws RequestParseException {
        try {
            String body = requestAdapter.getBody();
            if (StringUtils.isEmpty(body)) {
                return;
            }
            Charset encoding = determineEncoding(requestAdapter);
            MultiValueMap<String, String> multiValueMap = parseURLEncodedBody(body, encoding);
            if (multiValueMap != null) {
                requestAdapter.putAllParameters(multiValueMap);
                requestAdapter.setBody(null);
            }
        } catch (Exception e) {
            throw new RequestParseException("Could not parse HTTP " + requestAdapter.getRequestMethod() +
                    " request body", e);
        }
    }

    /**
     * Returns whether the request is a multipart form data request.
     * @param requestMethod the HTTP request method
     * @param mediaType the media type of the request
     * @return true if the request is multipart, false otherwise
     */
    public static boolean isMultipartForm(MethodType requestMethod, MediaType mediaType) {
        return MethodType.POST.equals(requestMethod) &&
            MediaType.MULTIPART_FORM_DATA.equalsTypeAndSubtype(mediaType);
    }

    /**
     * Returns whether the request's content type is {@code application/x-www-form-urlencoded}.
     * @param mediaType the media type of the request
     * @return true if the request is a URL-encoded form, false otherwise
     */
    public static boolean isURLEncodedForm(MediaType mediaType) {
        return MediaType.APPLICATION_FORM_URLENCODED.equalsTypeAndSubtype(mediaType);
    }

    @Nullable
    private static <T extends Parameters> T parseURLEncodedBodyAsParameters(
            WebRequestAdapter requestAdapter, Class<T> requiredType) throws RequestParseException {
        try {
            Charset encoding = determineEncoding(requestAdapter);
            MultiValueMap<String, String> multiValueMap = parseURLEncodedBody(requestAdapter.getBody(), encoding);
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

    @Nullable
    private static MultiValueMap<String, String> parseURLEncodedBody(String body, Charset encoding) {
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

    @NonNull
    private static Charset determineEncoding(@NonNull WebRequestAdapter requestAdapter) {
        Charset encoding = null;
        if (requestAdapter.getMediaType() != null) {
            encoding = requestAdapter.getMediaType().getCharset();
        }
        if (encoding == null && requestAdapter.getEncoding() != null) {
            encoding = Charset.forName(requestAdapter.getEncoding());
        }
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        return encoding;
    }

}
