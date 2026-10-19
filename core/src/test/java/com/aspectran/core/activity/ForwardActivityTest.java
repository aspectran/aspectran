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

import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.test.AspectranTest;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases verifying the lifecycle, AOP advice execution order,
 * and state propagation during activity forwarding in {@link CoreActivity}.
 *
 * <p>Created: 2026. 10. 19.</p>
 */
@AspectranTest(
    rules = "/config/activity/forward-activity-test-config.xml"
)
class ForwardActivityTest {

    @Test
    void testSingleForwardLifecycle(@NonNull ActivityContext context) throws Exception {
        List<String> events = new ArrayList<>();
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put("events", events);

        InstantActivity activity = new InstantActivity(context);
        activity.setAttributeMap(attributeMap);
        activity.prepare("/step1");
        activity.perform();

        Writer writer = activity.getResponseAdapter().getWriter();
        String output = (writer != null ? writer.toString() : null);

        // Verify the response produced by /step2 with the attribute forwarded from /step1
        assertEquals("Step 2 completed: forwardVal", output);

        // Verify the exact advice and action execution order:
        // 1. Aspect 1 Before
        // 2. Translet 1 Action
        // 3. Aspect 2 Before
        // 4. Translet 2 Action
        // 5. Aspect 2 After  (LIFO / descending order)
        // 6. Aspect 1 After
        // 7. Aspect 2 Finally (LIFO / descending order)
        // 8. Aspect 1 Finally
        List<String> expectedEvents = List.of(
            "aspect1:before",
            "step1:action",
            "aspect2:before",
            "step2:action",
            "aspect2:after",
            "aspect1:after",
            "aspect2:finally",
            "aspect1:finally"
        );
        assertEquals(expectedEvents, events);
    }

    @Test
    void testMultiForwardChainLifecycle(@NonNull ActivityContext context) throws Exception {
        List<String> events = new ArrayList<>();
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put("events", events);

        InstantActivity activity = new InstantActivity(context);
        activity.setAttributeMap(attributeMap);
        activity.prepare("/chain1");
        activity.perform();

        Writer writer = activity.getResponseAdapter().getWriter();
        String output = (writer != null ? writer.toString() : null);
        assertEquals("Chain completed", output);

        // Verify the nested execution chain:
        // Before advices & actions execute in forward order: 1 -> 2 -> 3
        // After and Finally advices execute in unwind order: 3 -> 2 -> 1
        List<String> expectedEvents = List.of(
            "chainAspect1:before",
            "chain1:action",
            "chainAspect2:before",
            "chain2:action",
            "chainAspect3:before",
            "chain3:action",
            "chainAspect3:after",
            "chainAspect2:after",
            "chainAspect1:after",
            "chainAspect3:finally",
            "chainAspect2:finally",
            "chainAspect1:finally"
        );
        assertEquals(expectedEvents, events);
    }

    @Test
    void testForwardWithException(@NonNull ActivityContext context) throws Exception {
        List<String> events = new ArrayList<>();
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put("events", events);

        InstantActivity activity = new InstantActivity(context);
        activity.setAttributeMap(attributeMap);
        activity.prepare("/error-step1");

        assertThrows(ActivityPerformException.class, activity::perform);

        // Verify:
        // 1. Before advices execute until error occurs in /error-step2
        // 2. After advices are completely skipped
        // 3. Thrown advices and Finally advices execute on unwind
        List<String> expectedEvents = List.of(
            "errorAspect1:before",
            "errorStep1:action",
            "errorAspect2:before",
            "errorStep2:action:error",
            "errorAspect1:thrown",
            "errorAspect2:thrown",
            "errorAspect2:finally",
            "errorAspect1:finally"
        );
        assertEquals(expectedEvents, events);
    }

    @Test
    void testProcessResultCarriedOver(@NonNull ActivityContext context) throws Exception {
        InstantActivity activity = new InstantActivity(context);
        activity.prepare("/data-step1");
        activity.perform();

        Writer writer = activity.getResponseAdapter().getWriter();
        String output = (writer != null ? writer.toString() : null);
        assertEquals("value1+value2", output);

        ProcessResult processResult = activity.getTranslet().getProcessResult();
        assertNotNull(processResult);
        assertEquals("value1", processResult.getResultValue("data1"));
        assertEquals("value2", processResult.getResultValue("data2"));
    }

    @Test
    void testForwardWithInstantAction(@NonNull ActivityContext context) throws Exception {
        List<String> events = new ArrayList<>();
        Map<String, Object> attributeMap = new HashMap<>();
        attributeMap.put("events", events);

        InstantActivity activity = new InstantActivity(context);
        activity.setAttributeMap(attributeMap);
        activity.prepare("/step1");

        String result = activity.perform(() -> {
            events.add("instantAction:execute");
            return "instantActionResult";
        });

        assertEquals("instantActionResult", result);

        // InstantAction executes AFTER forward is completely finished, before AFTER advice
        List<String> expectedEvents = List.of(
            "aspect1:before",
            "step1:action",
            "aspect2:before",
            "step2:action",
            "instantAction:execute",
            "aspect2:after",
            "aspect1:after",
            "aspect2:finally",
            "aspect1:finally"
        );
        assertEquals(expectedEvents, events);
    }

    /**
     * Helper bean implementing actions and aspect advice methods for tests.
     */
    public static class ForwardTestHelper {

        // --- Single Forward: Aspect 1 ---
        public void before1(Translet translet) {
            recordEvent(translet, "aspect1:before");
        }

        public void after1(Translet translet) {
            recordEvent(translet, "aspect1:after");
        }

        public void finally1(Translet translet) {
            recordEvent(translet, "aspect1:finally");
        }

        // --- Single Forward: Aspect 2 ---
        public void before2(Translet translet) {
            recordEvent(translet, "aspect2:before");
        }

        public void after2(Translet translet) {
            recordEvent(translet, "aspect2:after");
        }

        public void finally2(Translet translet) {
            recordEvent(translet, "aspect2:finally");
        }

        // --- Single Forward: Actions ---
        public void actionStep1(Translet translet) {
            recordEvent(translet, "step1:action");
        }

        public void actionStep2(Translet translet) {
            recordEvent(translet, "step2:action");
        }

        // --- Multi Chain: Aspect 1 ---
        public void chainBefore1(Translet translet) {
            recordEvent(translet, "chainAspect1:before");
        }

        public void chainAfter1(Translet translet) {
            recordEvent(translet, "chainAspect1:after");
        }

        public void chainFinally1(Translet translet) {
            recordEvent(translet, "chainAspect1:finally");
        }

        // --- Multi Chain: Aspect 2 ---
        public void chainBefore2(Translet translet) {
            recordEvent(translet, "chainAspect2:before");
        }

        public void chainAfter2(Translet translet) {
            recordEvent(translet, "chainAspect2:after");
        }

        public void chainFinally2(Translet translet) {
            recordEvent(translet, "chainAspect2:finally");
        }

        // --- Multi Chain: Aspect 3 ---
        public void chainBefore3(Translet translet) {
            recordEvent(translet, "chainAspect3:before");
        }

        public void chainAfter3(Translet translet) {
            recordEvent(translet, "chainAspect3:after");
        }

        public void chainFinally3(Translet translet) {
            recordEvent(translet, "chainAspect3:finally");
        }

        // --- Multi Chain: Actions ---
        public void chainAction1(Translet translet) {
            recordEvent(translet, "chain1:action");
        }

        public void chainAction2(Translet translet) {
            recordEvent(translet, "chain2:action");
        }

        public void chainAction3(Translet translet) {
            recordEvent(translet, "chain3:action");
        }

        // --- Error: Aspect 1 ---
        public void errorBefore1(Translet translet) {
            recordEvent(translet, "errorAspect1:before");
        }

        public void errorAfter1(Translet translet) {
            recordEvent(translet, "errorAspect1:after");
        }

        public void errorFinally1(Translet translet) {
            recordEvent(translet, "errorAspect1:finally");
        }

        public void errorThrown1(Translet translet) {
            recordEvent(translet, "errorAspect1:thrown");
        }

        // --- Error: Aspect 2 ---
        public void errorBefore2(Translet translet) {
            recordEvent(translet, "errorAspect2:before");
        }

        public void errorAfter2(Translet translet) {
            recordEvent(translet, "errorAspect2:after");
        }

        public void errorFinally2(Translet translet) {
            recordEvent(translet, "errorAspect2:finally");
        }

        public void errorThrown2(Translet translet) {
            recordEvent(translet, "errorAspect2:thrown");
        }

        // --- Error: Actions ---
        public void errorAction1(Translet translet) {
            recordEvent(translet, "errorStep1:action");
        }

        public void errorAction2(Translet translet) {
            recordEvent(translet, "errorStep2:action:error");
            throw new IllegalStateException("Forced error in error-step2");
        }

        // --- ProcessResult Carried Over: Actions ---
        public String produceData1() {
            return "value1";
        }

        public String produceData2() {
            return "value2";
        }

        private void recordEvent(Translet translet, String event) {
            List<String> events = translet.getAttribute("events");
            if (events != null) {
                events.add(event);
            }
        }
    }

}
