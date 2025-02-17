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
package com.aspectran.core.activity;

import com.aspectran.core.context.ActivityContext;

/**
 * CoreActivity could only be executed by the framework, but
 * using this InstantProxyActivity could also be executed by user code.
 */
public class InstantProxyActivity extends CoreActivity {

    private final Mode mode;

    /**
     * Instantiates a new InstantActivity.
     * @param context the activity context
     */
    public InstantProxyActivity(ActivityContext context) {
        super(context);
        mode = Mode.PROXY;
    }

    @Override
    public Mode getMode() {
        return mode;
    }

}
