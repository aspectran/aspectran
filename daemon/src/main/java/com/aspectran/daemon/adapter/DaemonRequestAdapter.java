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
package com.aspectran.daemon.adapter;

import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;

/**
 * The request adapter for the daemon environment.
 * <p>This class is a specialization of {@link DefaultRequestAdapter} used for
 * activities triggered within a daemon, such as scheduled jobs or background tasks.
 * It is typically instantiated by a {@link com.aspectran.daemon.activity.DaemonActivity}.
 * </p>
 *
 * @author Juho Jeong
 * @since 2017. 12. 12.
 */
public class DaemonRequestAdapter extends DefaultRequestAdapter {

    /**
     * Creates a new {@code DaemonRequestAdapter} with the specified request method.
     * @param requestMethod the method of the request (e.g., GET, POST)
     */
    public DaemonRequestAdapter(MethodType requestMethod) {
        super(requestMethod);
    }

}
