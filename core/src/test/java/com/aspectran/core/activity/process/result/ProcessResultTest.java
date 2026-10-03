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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ProcessResult}.
 */
class ProcessResultTest {

    @Test
    void testConstructorsAndHierarchy() {
        ProcessResult pr = new ProcessResult();
        assertTrue(pr.isEmpty());
        assertNull(pr.lastContentResult());

        ContentResult cr1 = new ContentResult(pr);
        cr1.setName("content1");
        cr1.addActionResult(new ActionResult("act1", "val1"));

        assertEquals(1, pr.size());
        assertSame(cr1, pr.lastContentResult());
        assertSame(cr1, pr.getContentResult("content1"));
        assertNull(pr.getContentResult("nonExisting"));

        ContentResult cr2 = new ContentResult(pr);
        cr2.setName("content2");
        cr2.setExplicit(true);
        assertSame(cr2, pr.getContentResult("content2", true));
        assertNull(pr.getContentResult("content2", false));
    }

    @Test
    void testGetResultValueSimple() {
        ProcessResult pr = new ProcessResult();
        ContentResult cr = new ContentResult(pr);
        cr.addActionResult(new ActionResult("simpleKey", "simpleVal"));

        assertEquals("simpleVal", pr.getResultValue("simpleKey"));
        assertNull(pr.getResultValue("unknown"));
        assertNull(pr.getResultValue(null));
    }

    @Test
    void testGetResultValueTwoLevels() {
        ProcessResult pr = new ProcessResult();
        ContentResult cr = new ContentResult(pr);
        cr.addActionResult(new ActionResult("user.name", "Bob"));

        assertEquals("Bob", pr.getResultValue("user.name"));
        assertNotNull(pr.getResultValue("user"));
        assertNull(pr.getResultValue("user.unknown"));
    }

    @Test
    void testGetResultValueDeepNesting() {
        ProcessResult pr = new ProcessResult();
        ContentResult cr = new ContentResult(pr);
        cr.addActionResult(new ActionResult("company.dept.team.leader", "Alice"));

        assertEquals("Alice", pr.getResultValue("company.dept.team.leader"));
        assertNull(pr.getResultValue("company.dept.team.member"));
        assertNull(pr.getResultValue("company.other.team"));
    }

    @Test
    void testDescribe() {
        ProcessResult pr = new ProcessResult();
        pr.setName("mainProcess");
        ContentResult cr = new ContentResult(pr);
        cr.addActionResult(new ActionResult("a", "b"));

        String desc = pr.describe();
        assertTrue(desc.contains("mainProcess"));
        assertTrue(desc.contains("size=1"));
    }

}
