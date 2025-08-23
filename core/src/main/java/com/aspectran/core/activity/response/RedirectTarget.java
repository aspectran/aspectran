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
package com.aspectran.core.activity.response;

import com.aspectran.utils.Assert;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * Represents the target of a redirect operation, encapsulating both the internal
 * request name and the external location URL.
 *
 * <p>This class provides a structured way to manage the destination of a redirect,
 * allowing the framework to track the intended next step in the request flow.</p>
 *
 * <p>Created: 2025-05-16</p>
 */
public class RedirectTarget {

    private final String requestName;

    private final String location;

    private RedirectTarget(String requestName, String location) {
        this.requestName = requestName;
        this.location = location;
    }

    /**
     * Returns the internal request name of the redirect target.
     * @return the request name
     */
    public String getRequestName() {
        return requestName;
    }

    /**
     * Returns the external URL location of the redirect target.
     * @return the location URL
     */
    public String getLocation() {
        return location;
    }

    /**
     * Creates a new {@code RedirectTarget} instance.
     * @param requestName the internal request name
     * @param location the external location URL
     * @return a new {@code RedirectTarget} instance
     * @throws IllegalArgumentException if {@code requestName} is {@code null}
     */
    @NonNull
    public static RedirectTarget of(String requestName, String location) {
        Assert.notNull(requestName, "requestName must not be null");
        return new RedirectTarget(requestName, location);
    }

}
