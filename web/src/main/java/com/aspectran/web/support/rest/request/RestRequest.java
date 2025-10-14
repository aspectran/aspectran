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

import com.aspectran.utils.LinkedCaseInsensitiveMultiValueMap;
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
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * A fluent client for performing HTTP requests, based on Apache HttpComponents.
 * <p>This client is designed to be used with a fluent builder pattern.
 * A request is configured by chaining method calls and then executed by
 * calling the {@link #retrieve()} method.</p>
 *
 * @since 8.6.0
 */
public class RestRequest {

    private final CloseableHttpClient httpClient;

    private String method;

    private String url;

    private MultiValueMap<String, String> headers;

    private Map<String, Object> params;

    private MediaType contentType;

    private String content;

    /**
     * Instantiates a new RestRequest with the given HttpClient.
     * @param httpClient the {@link CloseableHttpClient} to use for executing requests
     */
    public RestRequest(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sets the HTTP method for the request.
     * @param method the HTTP method (e.g., "GET", "POST")
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest method(String method) {
        this.method = method;
        return this;
    }

    /**
     * Sets the target URL for the request.
     * @param url the target URL
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest url(String url) {
        this.url = url;
        return this;
    }

    /**
     * Sets the request parameters. These are typically used for
     * {@code application/x-www-form-urlencoded} requests.
     * <p>This is mutually exclusive with {@link #content(String)}. If a request
     * body is set via {@code content()}, these parameters will be ignored.</p>
     * @param params a map of parameters
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest params(Map<String, Object> params) {
        this.params = params;
        return this;
    }

    /**
     * Sets all headers for the request, overwriting any previously set headers.
     * @param headers a map of headers
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest headers(MultiValueMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Sets the content type of the request body.
     * This should be used in conjunction with {@link #content(String)}.
     * @param contentType the media type of the request body
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest contentType(MediaType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Sets the raw request body.
     * <p>This is mutually exclusive with {@link #params(Map)}. If this is set,
     * any parameters will be ignored.</p>
     * @param content the request body
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest content(String content) {
        this.content = content;
        return this;
    }

    /**
     * A convenience method to add a Bearer Token to the 'Authorization' header.
     * @param token the bearer token
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest bearerToken(String token) {
        addAuthorizationHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return this;
    }

    /**
     * A convenience method to add a Basic Authentication credential to the
     * 'Authorization' header. The credentials will be Base64 encoded.
     * @param username the username
     * @param password the password
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        addAuthorizationHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
        return this;
    }

    /**
     * A convenience method to add a Bearer Token to the non-standard
     * 'X-Authorization' header.
     * @param token the bearer token
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest xBearerToken(String token) {
        addAuthorizationHeader("X-Authorization", "Bearer " + token);
        return this;
    }

    /**
     * A convenience method to add a Basic Authentication credential to the
     * non-standard 'X-Authorization' header. The credentials will be
     * Base64 encoded.
     * @param username the username
     * @param password the password
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest xBasicAuth(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        addAuthorizationHeader("X-Authorization", "Basic " + encodedCredentials);
        return this;
    }

    /**
     * A shortcut for {@code method(Method.GET.name())}.
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest get() {
        return method(Method.GET.name());
    }

    /**
     * A shortcut for {@code method(Method.POST.name())}.
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest post() {
        return method(Method.POST.name());
    }

    /**
     * A shortcut for {@code method(Method.PUT.name())}.
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest put() {
        return method(Method.PUT.name());
    }

    /**
     * A shortcut for {@code method(Method.DELETE.name())}.
     * @return this {@code RestRequest} for fluent chaining
     */
    public RestRequest delete() {
        return method(Method.DELETE.name());
    }

    /**
     * Executes the configured HTTP request.
     * <p>It builds the request with the specified URL, method, headers, and body
     * (or parameters), sends it, and processes the response. It returns a
     * {@link SuccessResponse} for successful status codes (2xx) and a
     * {@link FailureResponse} for error codes (4xx, 5xx).</p>
     * The response body is returned as a string, or as a {@link JsonString}
     * if the response content type is JSON.
     * @return the {@link RestResponse} from the server
     * @throws IOException if an I/O error occurs during the request
     */
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

        if (content != null) {
            ContentType ct = null;
            if (contentType != null) {
                ct = ContentType.parse(contentType.toString());
            }
            requestBuilder.setEntity(content, ct);
        } else if (params != null) {
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
                if (isJsonType(contentType)) {
                    failureResponse.setData(new JsonString(data));
                } else {
                    failureResponse.setData(data);
                }
                if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    failureResponse.setError("-404", "The Requested Resource Was Not Found");
                } else {
                    failureResponse.setError("-" + statusCode, response.getReasonPhrase());
                }
                if (isJsonType(getAccept(headers))) {
                    failureResponse.setDefaultContentType(ContentType.APPLICATION_JSON.getMimeType());
                }
                return failureResponse;
            }
        });
    }

    /**
     * Gets the 'Accept' header from the headers map.
     * @param headers the headers map
     * @return the 'Accept' header value, or null if not found
     */
    private String getAccept(MultiValueMap<String, String> headers) {
        if (headers != null) {
            return headers.getFirst(HttpHeaders.ACCEPT);
        } else {
            return null;
        }
    }

    /**
     * Initializes the headers map if null and sets an authorization header.
     * @param headerName the name of the authorization header
     * @param headerValue the value of the authorization header
     */
    private void addAuthorizationHeader(String headerName, String headerValue) {
        if (this.headers == null) {
            this.headers = new LinkedCaseInsensitiveMultiValueMap<>();
        }
        this.headers.set(headerName, headerValue);
    }

    /**
     * Checks if the given content type is a JSON type.
     * @param contentType the content type
     * @return true if it is a JSON type, false otherwise
     */
    private boolean isJsonType(ContentType contentType) {
        if (contentType != null) {
            return isJsonType(contentType.getMimeType());
        } else {
            return false;
        }
    }

    /**
     * Checks if the given mime type is a JSON type.
     * @param mimeType the mime type string
     * @return true if it is a JSON type, false otherwise
     */
    private boolean isJsonType(String mimeType) {
        return (mimeType != null && mimeType.equalsIgnoreCase(ContentType.APPLICATION_JSON.getMimeType()));
    }

}
