/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.builder.ActivityContextBuilder;
import com.aspectran.core.context.builder.ActivityContextBuilderException;
import com.aspectran.core.context.builder.HybridActivityContextBuilder;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testcase for ActivityDataMap.
 *
 * <p>Created: 2017. 12. 4.</p>
 */
class ActivityDataMapTest {

    @Test
    void testEvaluateAsString() throws ActivityContextBuilderException, ActivityPerformException {
        ActivityContextBuilder builder = new HybridActivityContextBuilder();
        builder.setDebugMode(true);
        ActivityContext context = builder.build();

        ParameterMap parameterMap = new ParameterMap();
        parameterMap.setParameter("param1", "Apple");
        parameterMap.setParameter("param2", "Tomato");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "Strawberry");
        attributes.put("attr2", "Melon");

        InstantActivity activity = new InstantActivity(context);
        activity.setParameterMap(parameterMap);
        activity.setAttributeMap(attributes);
        ActivityDataMap activityDataMap = activity.perform(() -> {
            ActivityDataMap activityDataMap2 = new ActivityDataMap(activity);
            activityDataMap2.put("result1", 1);
            return activityDataMap2;
        });
        assertEquals("Apple", activityDataMap.getParameterWithoutCache("param1"));
        assertEquals("Apple", activityDataMap.get("param1"));
        assertEquals("Tomato", activityDataMap.get("param2"));
        assertEquals("Strawberry", activityDataMap.getAttributeWithoutCache("attr1"));
        assertEquals("Strawberry", activityDataMap.get("attr1"));
        assertEquals("Melon", activityDataMap.get("attr2"));
        assertEquals(1, activityDataMap.get("result1"));
    }

}
