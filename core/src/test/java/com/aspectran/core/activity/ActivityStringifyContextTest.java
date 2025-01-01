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

import com.aspectran.core.activity.request.ParameterMap;
import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import com.aspectran.utils.StringifyContext;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2024. 12. 1.</p>
 */
class ActivityStringifyContextTest {

    @Test
    void testClone() throws ActivityContextBuilderException, ActivityPerformException {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        builder.setDebugMode(true);
        ActivityContext context = builder.build();

        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("param1", "Apple");
        parameterMap.setParameter("param2", "Tomato");

        InstantActivity activity = new InstantActivity(context);
        activity.setParameterMap(parameterMap);

        RequestAdapter requestAdapter = new DefaultRequestAdapter();
        activity.setRequestAdapter(requestAdapter);

        activity.putSetting(ActivityStringifyContext.FORMAT_PRETTY, true);
        activity.getRequestAdapter().setLocale(Locale.ENGLISH);
        StringifyContext stringifyContext1 = activity.getStringifyContext();
        StringifyContext stringifyContext2 = activity.perform(() -> {
            StringifyContext sc = activity.getStringifyContext().clone();
            sc.setIndentSize(2);
            return sc;
        });

        builder.destroy();

        ActivityData activityData = activity.getActivityData();
        assertEquals("Apple", activityData.getParameterWithoutCache("param1"));
        assertEquals("Apple", activityData.get("param1"));
        assertEquals("Tomato", activityData.get("param2"));

        assertEquals("{pretty=true, locale=en}", stringifyContext1.toString());
        assertEquals("{pretty=true, indentSize=2, locale=en}", stringifyContext2.toString());
    }

}
