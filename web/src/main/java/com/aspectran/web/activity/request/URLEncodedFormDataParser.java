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

import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.util.LinkedMultiValueMap;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.StringUtils;

import java.net.URLDecoder;

/**
 * Support for HTTP request methods like PUT/PATCH/DELETE.
 *
 * <p>The Servlet spec requires form data to be available for HTTP POST but
 * not for HTTP PUT or PATCH requests. This parser intercepts HTTP PUT and PATCH
 * requests where content type is {@code 'application/x-www-form-urlencoded'},
 * reads form encoded content from the body of the request.
 *
 * @since 2.3.0
 */
public class URLEncodedFormDataParser {

    public static void parse(RequestAdapter requestAdapter) {
        try {
            String body = requestAdapter.getBody();
            if (body != null && !body.isEmpty()) {
                String encoding = requestAdapter.getEncoding();
                String[] pairs = StringUtils.tokenize(body, "&");
                MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
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
                requestAdapter.putAllParameters(parameterMap);
            }
        } catch (Exception e) {
            throw new RequestParseException("Could not parse HTTP " +
                    requestAdapter.getRequestMethod() + " request body", e);
        }
    }

}
