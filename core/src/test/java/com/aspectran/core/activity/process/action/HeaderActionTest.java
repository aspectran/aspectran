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
package com.aspectran.core.activity.process.action;

import com.aspectran.core.activity.InstantActivity;
import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.rule.HeaderActionRule;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.type.ItemType;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test cases for {@link HeaderAction}.
 *
 * <p>Created: 2026. 10. 05</p>
 */
@AspectranTest(
    rules = "/config/activity/annotated-method-invoker-test.xml"
)
class HeaderActionTest {

    @Test
    void testHeaderActionOverridesExistingHeader(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ResponseAdapter responseAdapter = new DefaultResponseAdapter("mockResponse");
        activity.setResponseAdapter(responseAdapter);
        activity.prepare("/test");

        // Pre-set existing header
        responseAdapter.setHeader("Cache-Control", "no-cache");
        assertEquals("no-cache", responseAdapter.getHeader("Cache-Control"));

        // Configure HeaderAction
        HeaderActionRule rule = new HeaderActionRule();
        ItemRule itemRule = new ItemRule();
        itemRule.setName("Cache-Control");
        itemRule.setValue("max-age=3600");
        rule.addHeaderItemRule(itemRule);

        HeaderAction action = new HeaderAction(rule);
        action.execute(activity);

        // Should override existing header value
        assertEquals("max-age=3600", responseAdapter.getHeader("Cache-Control"));
        Collection<String> headers = responseAdapter.getHeaders("Cache-Control");
        assertNotNull(headers);
        assertEquals(List.of("max-age=3600"), new ArrayList<>(headers));
    }

    @Test
    void testHeaderActionWithMultiValues(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        ResponseAdapter responseAdapter = new DefaultResponseAdapter("mockResponse");
        activity.setResponseAdapter(responseAdapter);
        activity.prepare("/test");

        // Pre-set existing header
        responseAdapter.setHeader("X-Custom-Header", "initial");

        // Configure HeaderAction with multiple values for the same header
        HeaderActionRule rule = new HeaderActionRule();

        ItemRule itemRule = new ItemRule();
        itemRule.setName("X-Custom-Header");
        itemRule.setType(ItemType.ARRAY);
        itemRule.addValue("first");
        itemRule.addValue("second");
        rule.addHeaderItemRule(itemRule);

        HeaderAction action = new HeaderAction(rule);
        action.execute(activity);

        Collection<String> headers = responseAdapter.getHeaders("X-Custom-Header");
        assertNotNull(headers);
        // "initial" is replaced by "first" (via setHeader), and "second" is appended (via addHeader)
        assertEquals(List.of("first", "second"), new ArrayList<>(headers));
    }

}
