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
package com.aspectran.shell.adapter;

import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;

/**
 * Shell request adapter that supplies request metadata and parameters to the core.
 * <p>
 * Instances are created by {@link com.aspectran.shell.activity.ShellActivity} for the
 * current invocation, using the HTTP-like {@link MethodType} parsed from the command line.
 * Parameters collected by the shell are later injected via {@link #setParameterMap} on the
 * base adapter.
 * </p>
 */
public class ShellRequestAdapter extends DefaultRequestAdapter {

    /**
     * Create a new ShellRequestAdapter for the given request method.
     * @param requestMethod the method to associate (e.g. GET, POST)
     */
    public ShellRequestAdapter(MethodType requestMethod) {
        super(requestMethod);
    }

}
