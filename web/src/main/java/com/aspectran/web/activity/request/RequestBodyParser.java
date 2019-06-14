/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.activity.request.RequestParseException;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.core.util.ClassUtils;
import com.aspectran.core.util.LinkedMultiValueMap;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.apon.Parameters;

import javax.servlet.http.HttpServletRequest;
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
public class RequestBodyParser {

    private static final String MULTIPART_FORM_DATA = "multipart/form-data";

    private static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    private static final String DEFAULT_ENCODING = "iso-8859-1";

    private static final int BUFFER_SIZE = 1024;

    public static void parseBody(RequestAdapter requestAdapter) {
        try {
            StringBuilder sb = new StringBuilder();
            String encoding = requestAdapter.getEncoding();
            if (encoding == null) {
                encoding = DEFAULT_ENCODING;
            }
            InputStream is = ((HttpServletRequest)requestAdapter.getAdaptee()).getInputStream();
            InputStreamReader reader = new InputStreamReader(is, encoding);
            char[] buffer = new char[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, bytesRead);
            }
            requestAdapter.setBody(sb.toString());
        } catch (IOException e) {
            requestAdapter.setBody(null);
        }
    }

    public static void parseURLEncoded(RequestAdapter requestAdapter) {
        try {
            String body = requestAdapter.getBody();
            String encoding = requestAdapter.getEncoding();
            MultiValueMap<String, String> parameterMap = parseURLEncoded(body, encoding);
            if (parameterMap != null) {
                requestAdapter.putAllParameters(parameterMap);
            }
        } catch (Exception e) {
            throw new RequestParseException("Could not parse HTTP " +
                    requestAdapter.getRequestMethod() + " request body", e);
        }
    }

    public static <T extends Parameters> T parseURLEncoded(RequestAdapter requestAdapter, Class<T> requiredType) {
        try {
            String encoding = requestAdapter.getEncoding();
            if (encoding == null) {
                encoding = DEFAULT_ENCODING;
            }
            MultiValueMap<String, String> parameterMap = parseURLEncoded(requestAdapter.getBody(), encoding);
            if (parameterMap != null && !parameterMap.isEmpty()) {
                T parameters = ClassUtils.createInstance(requiredType);
                for (Map.Entry<String, List<String>> entry : parameterMap.entrySet()) {
                    String name = entry.getKey();
                    for (String value : entry.getValue()) {
                        parameters.putValue(name, value);
                    }
                }
                return parameters;
            }
        } catch (Exception e) {
            throw new RequestParseException("Failed to parse request body to required type [" +
                    requiredType.getName() + "]");
        }
        return null;
    }

    public static MultiValueMap<String, String> parseURLEncoded(String body, String encoding)
            throws UnsupportedEncodingException {
        if (body != null && !body.isEmpty()) {
            MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
            String[] pairs = StringUtils.tokenize(body, "&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx == -1) {
                    String name = URLDecoder.decode(pair, encoding);
                    parameterMap.add(name, null);
                } else {
                    String name = URLDecoder.decode(pair.substring(0, idx), encoding);
                    String value = URLDecoder.decode(pair.substring(idx + 1), encoding);
                    parameterMap.add(name, value);
                }
            }
            return parameterMap;
        } else {
            return null;
        }
    }

    public static boolean isMultipartForm(MethodType requestMethod, String contentType) {
        return MethodType.POST.equals(requestMethod) && contentType.startsWith(MULTIPART_FORM_DATA);
    }

    public static boolean isURLEncodedForm(String contentType) {
        return contentType.startsWith(APPLICATION_FORM_URLENCODED);
    }

}
