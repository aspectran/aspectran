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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ContentResult}.
 */
class ContentResultTest {

    @Test
    void testConstructorsAndProperties() {
        ContentResult cr = new ContentResult();
        assertTrue(cr.isEmpty());
        assertNull(cr.getParent());
        assertNull(cr.getName());

        cr.setName("content1");
        assertEquals("content1", cr.getName());
        cr.setExplicit(true);
        assertTrue(cr.isExplicit());
    }

    @Test
    void testAddAndGetActionResult() {
        ContentResult cr = new ContentResult();
        ActionResult ar1 = new ActionResult("action1", "val1");
        ActionResult ar2 = new ActionResult("action2", "val2");

        cr.addActionResult(ar1);
        cr.addActionResult(ar2);

        assertEquals(2, cr.size());
        assertEquals("val1", cr.getActionResult("action1").getResultValue());
        assertEquals("val2", cr.getActionResult("action2").getResultValue());
        assertNull(cr.getActionResult("unknown"));

        assertArrayEquals(new String[]{"action1", "action2"}, cr.getActionIds());
    }

    @Test
    void testMergeResultValueMaps() {
        ContentResult cr = new ContentResult();

        ActionResult ar1 = new ActionResult("user.name", "Alice");
        ActionResult ar2 = new ActionResult("user.age", 30);

        cr.addActionResult(ar1);
        cr.addActionResult(ar2);

        assertEquals(1, cr.size());
        ActionResult merged = cr.getActionResult("user");
        assertNotNull(merged);
        assertInstanceOf(ResultValueMap.class, merged.getResultValue());

        ResultValueMap map = (ResultValueMap)merged.getResultValue();
        assertEquals("Alice", map.get("name"));
        assertEquals(30, map.get("age"));
    }

}
