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
package com.aspectran.core.activity;

import com.aspectran.core.context.ActivityContext;
import com.aspectran.test.AspectranTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link InstantActivitySupport}.
 *
 * <p>Created: 2026-11-18</p>
 */
@AspectranTest(
    rules = "/config/activity/instant-activity-config.xml"
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InstantActivitySupportTest {

    private SampleSupport sampleSupport;

    @BeforeAll
    void ready(ActivityContext context) {
        sampleSupport = new SampleSupport();
        sampleSupport.setActivityContext(context);
    }

    @Test
    void testExecuteInActivityWithoutCurrentActivity() {
        assertFalse(sampleSupport.hasCurrentActivity());
        String result = sampleSupport.runAction(() -> {
            assertTrue(sampleSupport.hasCurrentActivity());
            return "executed-without-parent";
        });
        assertEquals("executed-without-parent", result);
        assertFalse(sampleSupport.hasCurrentActivity());
    }

    @Test
    void testExecuteInActivityWithCurrentActivity() {
        // Enclose within an existing activity
        String result = sampleSupport.runInstantActivity(() -> {
            assertTrue(sampleSupport.hasCurrentActivity());
            Activity parentActivity = sampleSupport.getCurrentActivity();

            AtomicBoolean executed = new AtomicBoolean(false);
            String innerResult = sampleSupport.runAction(() -> {
                // Must be executing inside the same parent activity
                assertEquals(parentActivity, sampleSupport.getCurrentActivity());
                executed.set(true);
                return "executed-with-parent";
            });

            assertTrue(executed.get());
            return innerResult;
        });
        assertEquals("executed-with-parent", result);
    }

    @Test
    void testExecuteInActivityNullAction() {
        assertThrows(IllegalArgumentException.class, () -> sampleSupport.runAction(null));
    }

    @Test
    void testInstantActivityNullAction() {
        assertThrows(IllegalArgumentException.class, () -> sampleSupport.runInstantActivity(null));
    }

    private static class SampleSupport extends InstantActivitySupport {

        public <V> V runAction(InstantAction<V> action) {
            return executeInActivity(action);
        }

        public <V> V runInstantActivity(InstantAction<V> action) {
            return instantActivity(action);
        }

    }

}
