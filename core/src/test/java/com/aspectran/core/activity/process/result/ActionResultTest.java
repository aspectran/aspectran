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
package com.aspectran.core.activity.process.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test cases for {@link ActionResult}.
 */
class ActionResultTest {

    @Test
    void testSimpleActionResult() {
        ActionResult ar = new ActionResult("action1", "value1");
        assertEquals("action1", ar.getActionId());
        assertEquals("value1", ar.getResultValue());
    }

    @Test
    void testTwoLevelNesting() {
        ActionResult ar = new ActionResult("user.name", "John");
        assertEquals("user", ar.getActionId());
        assertInstanceOf(ResultValueMap.class, ar.getResultValue());

        ResultValueMap map = (ResultValueMap)ar.getResultValue();
        assertEquals("John", map.get("name"));
    }

    @Test
    void testDeepNesting() {
        ActionResult ar = new ActionResult("user.profile.contact.phone", "123-456-7890");
        assertEquals("user", ar.getActionId());
        assertInstanceOf(ResultValueMap.class, ar.getResultValue());

        ResultValueMap userMap = (ResultValueMap)ar.getResultValue();
        assertNotNull(userMap);

        ResultValueMap profileMap = userMap.get("profile", ResultValueMap.class);
        assertNotNull(profileMap);

        ResultValueMap contactMap = profileMap.get("contact", ResultValueMap.class);
        assertNotNull(contactMap);

        assertEquals("123-456-7890", contactMap.get("phone"));
    }

    @Test
    void testNullAndEmptyActionId() {
        ActionResult ar = new ActionResult(null, "value");
        assertNull(ar.getActionId());
        assertEquals("value", ar.getResultValue());

        ar.setResultValue("", "empty");
        assertEquals("", ar.getActionId());
        assertEquals("empty", ar.getResultValue());
    }

}
