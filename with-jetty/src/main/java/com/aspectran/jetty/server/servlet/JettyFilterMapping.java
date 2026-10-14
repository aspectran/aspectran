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
package com.aspectran.jetty.server.servlet;

import jakarta.servlet.DispatcherType;

/**
 * Represents a filter mapping for a Jetty web application.
 *
 * <p>Created: 4/23/24</p>
 */
public class JettyFilterMapping {

    private final String target;

    private final DispatcherType[] dispatchers;

    /**
     * Constructs a {@code JettyFilterMapping} instance for handling requests based on a target URL.
     * @param target the target URL to map the filter to
     */
    public JettyFilterMapping(String target) {
        this(target, DispatcherType.REQUEST);
    }

    /**
     * Constructs a {@code JettyFilterMapping} instance with a specified target and optional dispatcher types.
     * @param target the target URL or servlet name to map the filter to
     * @param dispatchers the dispatcher types this filter will apply to; if not specified, default dispatcher types may apply
     */
    public JettyFilterMapping(String target, DispatcherType... dispatchers) {
        this.target = target;
        this.dispatchers = dispatchers;
    }

    /**
     * Retrieves the target URL or servlet name associated with the filter mapping.
     * @return the target URL or servlet name as a string
     */
    public String getTarget() {
        return target;
    }

    /**
     * Retrieves the dispatcher types associated with the filter mapping.
     * @return the dispatcher types as an array of {@code DispatcherType}
     */
    public DispatcherType[] getDispatchers() {
        return dispatchers;
    }

}
