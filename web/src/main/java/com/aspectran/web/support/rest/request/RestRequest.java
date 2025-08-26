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
package com.aspectran.web.support.rest.request;

import com.aspectran.utils.MultiValueMap;
import com.aspectran.utils.StringUtils;
import com.aspectran.utils.json.JsonString;
import com.aspectran.web.activity.response.RestResponse;
import com.aspectran.web.support.http.MediaType;
import com.aspectran.web.support.rest.response.FailureResponse;
import com.aspectran.web.support.rest.response.SuccessResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.Method;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Client to perform HTTP requests underlying Apache HttpComponents.
 *
 * @since 8.6.0
 */
public class RestRequest {

    private final CloseableHttpClient httpClient;

    private String method;

    private String url;

    private Map<String, Object> params;

    private MultiValueMap<String, String> headers;

    public RestRequest(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public RestRequest method(String method) {
        this.method = method;
        return this;
    }

    public RestRequest url(String url) {
        this.url = url;
        return this;
    }

    public RestRequest params(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    public RestRequest headers(MultiValueMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public RestRequest get() {
        return method(Method.GET.name());
    }

    public RestRequest post() {
        return method(Method.POST.name());
    }

    public RestRequest put() {
        return method(Method.PUT.name());
    }

    public RestRequest delete() {
        return method(Method.DELETE.name());
    }

    public RestResponse retrieve() throws IOException {
        Charset charset = null;
        if (headers != null) {
            String contentType = headers.getFirst(HttpHeaders.CONTENT_TYPE);
            if (StringUtils.hasLength(contentType)) {
                try {
                    MediaType mediaType = MediaType.parseMediaType(contentType);
                    charset = mediaType.getCharset();
                } catch (Exception e) {
                    // ignored
                }
            }
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        ClassicRequestBuilder requestBuilder = ClassicRequestBuilder
                .create(method)
                .setCharset(charset)
                .setUri(url);

        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String name = entry.getKey();
                Object object = entry.getValue();
                if (object instanceof String[] arr) {
                    for (String value : arr) {
                        requestBuilder.addParameter(name, value);
                    }
                } else {
                    String value = (object != null ? object.toString() : null);
                    requestBuilder.addParameter(name, value);
                }
            }
        }

        if (headers != null) {
            headers.remove(HttpHeaders.HOST);
            headers.remove(HttpHeaders.CONTENT_LENGTH);
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String name = entry.getKey();
                for (String value : entry.getValue()) {
                    requestBuilder.addHeader(name, value);
                }
            }
        }

        ClassicHttpRequest request = requestBuilder.build();

        return httpClient.execute(request, response -> {
            HttpEntity entity = response.getEntity();
            final int statusCode = response.getCode();
            ContentType contentType = ContentType.parse(entity.getContentType());
            String data = EntityUtils.toString(entity).trim();
            if (statusCode == HttpStatus.SC_SUCCESS ||
                    statusCode == HttpStatus.SC_CREATED ||
                    statusCode == HttpStatus.SC_ACCEPTED ||
                    statusCode == HttpStatus.SC_NO_CONTENT) {
                SuccessResponse successResponse = new SuccessResponse();
                successResponse.setStatus(statusCode);
                if (isJsonType(contentType)) {
                    successResponse.setData(new JsonString(data));
                } else {
                    successResponse.setData(data);
                }
                if (statusCode == HttpStatus.SC_CREATED) {
                    Header header = response.getFirstHeader(HttpHeaders.LOCATION);
                    if (header != null) {
                        successResponse.setHeader(HttpHeaders.LOCATION, header.getValue());
                    }
                }
                return successResponse;
            } else {
                FailureResponse failureResponse = new FailureResponse();
                failureResponse.setStatus(statusCode);
                if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    failureResponse.setError("-404", "The requested resource was not found");
                } else {
                    failureResponse.setError("-" + statusCode, data);
                }
                if (isJsonType(getAccept(headers))) {
                    failureResponse.setDefaultContentType(ContentType.APPLICATION_JSON.getMimeType());
                }
                return failureResponse;
            }
        });
    }

    private String getAccept(MultiValueMap<String, String> headers) {
        if (headers != null) {
            return headers.getFirst(HttpHeaders.ACCEPT);
        } else {
            return null;
        }
    }

    private boolean isJsonType(ContentType contentType) {
        if (contentType != null) {
            return isJsonType(contentType.getMimeType());
        } else {
            return false;
        }
    }

    private boolean isJsonType(String mimeType) {
        return (mimeType != null && mimeType.equalsIgnoreCase(ContentType.APPLICATION_JSON.getMimeType()));
    }

}
