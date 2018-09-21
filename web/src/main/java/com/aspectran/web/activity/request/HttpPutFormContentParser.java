/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.util.LinkedMultiValueMap;
import com.aspectran.core.util.MultiValueMap;
import com.aspectran.core.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

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
public class HttpPutFormContentParser {

    private static final Charset DEFAULT_CHARSET = Charset.forName(ActivityContext.DEFAULT_ENCODING);

    private static final int BUFFER_SIZE = 4096;

    public static void parse(RequestAdapter requestAdapter) {
        try {
            HttpServletRequest request = requestAdapter.getAdaptee();
            String requestEncoding = requestAdapter.getEncoding();
            Charset charset = (requestEncoding != null ? Charset.forName(requestEncoding) : DEFAULT_CHARSET);
            String body = copyToString(request.getInputStream(), charset);
            String[] pairs = StringUtils.tokenize(body, "&");
            MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>();
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx == -1) {
                    String name = URLDecoder.decode(pair, charset.name());
                    parameterMap.add(name, null);
                } else {
                    String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
                    parameterMap.add(name, value);
                }
            }
            if (!parameterMap.isEmpty()) {
                for (Map.Entry<String, List<String>> entry : parameterMap.entrySet()) {
                    String name = entry.getKey();
                    List<String> list = entry.getValue();
                    String[] values = list.toArray(new String[0]);
                    requestAdapter.setParameter(name, values);
                }
            }
        } catch (Exception e) {
            throw new RequestParseException("Could not parse multipart servlet request", e);
        }
    }

    private static String copyToString(InputStream in, Charset charset) throws IOException {
        InputStreamReader reader = new InputStreamReader(in, charset);
        StringBuilder out = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }

}
