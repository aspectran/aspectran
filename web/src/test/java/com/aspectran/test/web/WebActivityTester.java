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
package com.aspectran.test.web;

import com.aspectran.core.activity.ActivityException;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.web.mock.MockHttpServletRequest;
import com.aspectran.test.web.mock.MockHttpServletResponse;
import com.aspectran.web.activity.WebActivity;
import com.aspectran.web.service.WebService;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * Helper class for testing Aspectran in a virtual web environment.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
public class WebActivityTester {

    private final WebService webService;

    private MockHttpServletRequest lastRequest;

    private MockHttpServletResponse lastResponse;

    public WebActivityTester(WebService webService) {
        this.webService = webService;
    }

    /**
     * Performs a virtual HTTP GET request.
     * @param requestURI the request URI
     * @return the resulting Translet
     */
    public Translet perform(String requestURI) {
        return perform(requestURI, null);
    }

    /**
     * Performs a virtual HTTP request.
     * @param requestURI the request URI
     * @param requestMethod the HTTP method
     * @return the resulting Translet
     */
    public Translet perform(String requestURI, MethodType requestMethod) {
        return perform(requestURI, requestMethod, null);
    }

    /**
     * Performs a virtual HTTP request with parameters.
     * @param requestURI the request URI
     * @param requestMethod the HTTP method
     * @param parameters the request parameters
     * @return the resulting Translet
     */
    public Translet perform(String requestURI, MethodType requestMethod, Map<String, String> parameters) {
        return perform(requestURI, requestMethod, parameters, null, null);
    }

    /**
     * Performs a virtual HTTP request with body and content type.
     * @param requestURI the request URI
     * @param requestMethod the HTTP method
     * @param parameters the request parameters
     * @param body the request body
     * @param contentType the content type
     * @return the resulting Translet
     */
    public Translet perform(
            String requestURI, MethodType requestMethod, Map<String, String> parameters,
            byte[] body, String contentType) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (requestMethod != null) {
            request.setMethod(requestMethod.name());
        }
        request.setRequestURI(requestURI);
        request.setServletContext(webService.getServletContext());
        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                request.setParameter(entry.getKey(), entry.getValue());
            }
        }
        if (body != null) {
            request.setBody(body);
        }
        if (contentType != null) {
            request.setContentType(contentType);
        }
        this.lastRequest = request;

        MockHttpServletResponse response = new MockHttpServletResponse();
        this.lastResponse = response;

        WebActivity activity = new WebActivity(webService, null, null, request, response);
        try {
            activity.prepare(requestURI, requestMethod);
            activity.perform();
            return activity.getTranslet();
        } catch (ActivityException e) {
            throw new RuntimeException("Failed to perform web activity", e);
        }
    }

    /**
     * Returns the response content from the last virtual request.
     * @return the response content
     */
    @Nullable
    public String getWrittenResponse() {
        return (lastResponse != null ? lastResponse.getContentAsString() : null);
    }

    /**
     * Returns the last mock request object.
     * @return the mock request
     */
    @Nullable
    public MockHttpServletRequest getLastRequest() {
        return lastRequest;
    }

    /**
     * Returns the last mock response object.
     * @return the mock response
     */
    @Nullable
    public MockHttpServletResponse getLastResponse() {
        return lastResponse;
    }

}
