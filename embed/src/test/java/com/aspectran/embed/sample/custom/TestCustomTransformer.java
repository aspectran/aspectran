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
package com.aspectran.embed.sample.custom;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.response.transform.CustomTransformer;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.utils.annotation.jsr305.NonNull;

/**
 * <p>Created: 2019-06-16</p>
 */
public class TestCustomTransformer implements CustomTransformer {

    @Override
    public void transform(@NonNull Activity activity) throws Exception {
        String one = activity.getTranslet().getParameter("one");
        String two = activity.getTranslet().getParameter("two");
        ResponseAdapter responseAdapter = activity.getResponseAdapter();
        responseAdapter.getWriter().write(one + two);
    }

}
