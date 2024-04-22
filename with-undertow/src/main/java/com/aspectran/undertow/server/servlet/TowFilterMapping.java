/*
 * Copyright (c) 2008-2024 The Aspectran Project
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

import jakarta.servlet.DispatcherType;

/**
 * <p>Created: 4/22/24</p>
 */
public class TowFilterMapping {

    private final String target;

    private final DispatcherType dispatcher;

    public TowFilterMapping(String target) {
        this(target, DispatcherType.REQUEST);
    }

    public TowFilterMapping(String target, DispatcherType dispatcher) {
        this.target = target;
        this.dispatcher = dispatcher;
    }

    public String getTarget() {
        return target;
    }

    public DispatcherType getDispatcher() {
        return dispatcher;
    }


}
