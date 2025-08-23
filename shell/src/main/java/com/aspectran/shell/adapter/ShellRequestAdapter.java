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
 * The request adapter for the interactive shell environment.
 * <p>This adapter represents a command-line invocation as a request. It is created
 * by a {@link com.aspectran.shell.activity.ShellActivity} and holds the
 * request method (e.g., GET, POST) parsed from the command. Parameters from the
 * command line are subsequently added to this adapter.
 * </p>
 *
 * @author Juho Jeong
 * @since 2017. 3. 4.
 */
public class ShellRequestAdapter extends DefaultRequestAdapter {

    /**
     * Creates a new {@code ShellRequestAdapter} for the specified request method.
     * @param requestMethod the request method parsed from the shell command
     */
    public ShellRequestAdapter(MethodType requestMethod) {
        super(requestMethod);
    }

}
