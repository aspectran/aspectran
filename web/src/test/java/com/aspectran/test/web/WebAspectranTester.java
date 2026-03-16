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

/**
 * Helper class for testing Aspectran in a virtual web environment.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
public class WebAspectranTester {

    private final WebService webService;

    private MockHttpServletResponse lastResponse;

    public WebAspectranTester(WebService webService) {
        this.webService = webService;
    }

    /**
     * Performs a virtual HTTP GET request.
     * @param requestURI the request URI
     * @return the resulting Translet
     */
    public Translet perform(MethodType methodType, String requestURI) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        if (methodType != null) {
            request.setMethod(methodType.name());
        }
        request.setRequestURI(requestURI);

        MockHttpServletResponse response = new MockHttpServletResponse();
        this.lastResponse = response;

        WebActivity activity = new WebActivity(webService, null, null, request, response);
        try {
            activity.prepare(requestURI);
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
     * Returns the last mock response object.
     * @return the mock response
     */
    @Nullable
    public MockHttpServletResponse getLastResponse() {
        return lastResponse;
    }

}
