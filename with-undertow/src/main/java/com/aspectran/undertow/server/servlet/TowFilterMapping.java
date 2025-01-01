/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
 * <p>Created: 4/22/24</p>
 */
public class TowFilterMapping {

    private final String target;

    private final DispatcherType[] dispatchers;

    public TowFilterMapping(String target) {
        this(target, DispatcherType.REQUEST);
    }

    public TowFilterMapping(String target, DispatcherType... dispatchers) {
        Assert.notNull(target, "target must not be null");
        Assert.notNull(dispatchers, "dispatchers must not be null");
        this.target = target;
        this.dispatchers = dispatchers;
    }

    public String getTarget() {
        return target;
    }

    public DispatcherType[] getDispatchers() {
        return dispatchers;
    }


}
