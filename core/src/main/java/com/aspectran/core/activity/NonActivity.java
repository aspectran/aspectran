/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.ActivityContext;

/**
 * This is an activity that does nothing, and is mainly used for unit testing.
 */
public class NonActivity extends CoreActivity {

    /**
     * Instantiates a new NonActivity.
     *
     * @param context the activity context
     */
    public NonActivity(ActivityContext context) {
        super(context);
    }

    @Override
    public RequestAdapter getRequestAdapter() {
        if (super.getRequestAdapter() == null) {
            DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter();
            setRequestAdapter(requestAdapter);
        }
        return super.getRequestAdapter();
    }

    @Override
    public <V> V perform(InstantAction<V> instantAction) throws ActivityPerformException {
        throw new UnsupportedOperationException("This activity cannot be performed");
    }

}
