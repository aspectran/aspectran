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
package com.aspectran.undertow.server.servlet;

import com.aspectran.utils.Assert;
import jakarta.servlet.DispatcherType;

/**
 * A simple container for filter mapping.
 *
 * <p>Created: 4/22/24</p>
 */
public class TowFilterMapping {

    private final String target;

    private final DispatcherType[] dispatchers;

    /**
     * Creates a new filter mapping with the specified target and
     * the default dispatcher type ({@code REQUEST}).
     * @param target the target of this mapping (a URL pattern or servlet name)
     */
    public TowFilterMapping(String target) {
        this(target, DispatcherType.REQUEST);
    }

    /**
     * Creates a new filter mapping with the specified target and dispatchers.
     * @param target the target of this mapping (a URL pattern or servlet name)
     * @param dispatchers the dispatcher types of this mapping
     */
    public TowFilterMapping(String target, DispatcherType... dispatchers) {
        Assert.notNull(target, "target must not be null");
        Assert.notNull(dispatchers, "dispatchers must not be null");
        this.target = target;
        this.dispatchers = dispatchers;
    }

    /**
     * Returns the target of this mapping (a URL pattern or servlet name).
     * @return the target of this mapping
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns the dispatcher types of this mapping.
     * @return the dispatcher types
     */
    public DispatcherType[] getDispatchers() {
        return dispatchers;
    }

}
