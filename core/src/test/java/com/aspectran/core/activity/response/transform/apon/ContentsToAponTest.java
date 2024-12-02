/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.activity.response.transform.apon;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.utils.apon.AponFormat;
import com.aspectran.utils.apon.Parameters;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentsToAponTest {

    @Test
    void testAssemble0() {
        ProcessResult processResult = new ProcessResult();
        ContentResult contentResult = new ContentResult(processResult, 4);

        ActionResult actionResult1 = new ActionResult();
        actionResult1.setResultValue("action1.result1", "value1");
        contentResult.addActionResult(actionResult1);

        ActionResult actionResult2 = new ActionResult();
        actionResult2.setResultValue("action1.result2", "value2");
        contentResult.addActionResult(actionResult2);

        Parameters ps = new ContentsToAponConverter().toParameters(processResult);

        String apon = "action1: {\n  result1: value1\n  result2: value2\n}";

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testAssemble1() {
        ProcessResult processResult = new ProcessResult();
        ContentResult contentResult = new ContentResult(processResult, 4);

        ActionResult r0 = new ActionResult();
        r0.setResultValue("action0", "value0");
        contentResult.addActionResult(r0);

        ActionResult r1 = new ActionResult();
        r1.setResultValue("action1.result1", "value1");
        contentResult.addActionResult(r1);

        ActionResult r2 = new ActionResult();
        r2.setResultValue("action1.result2", "value2");
        contentResult.addActionResult(r2);

        ActionResult r3 = new ActionResult();
        r3.setResultValue("action1", "value3");
        contentResult.addActionResult(r3);

        Parameters ps = new ContentsToAponConverter().toParameters(processResult);

        String apon = "action0: value0\naction1: value3";

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testAssemble2() {
        ProcessResult processResult = new ProcessResult();
        ContentResult contentResult = new ContentResult(processResult, 4);
        contentResult.setName("content1");

        ActionResult actionResult0 = new ActionResult();
        actionResult0.setResultValue("action0", "value0");
        contentResult.addActionResult(actionResult0);

        ActionResult actionResult1 = new ActionResult();
        actionResult1.setResultValue("action1.result1", "value1");
        contentResult.addActionResult(actionResult1);

        ActionResult actionResult2 = new ActionResult();
        actionResult2.setResultValue("action1.result2", "value2");
        contentResult.addActionResult(actionResult2);

        ActionResult actionResult3 = new ActionResult();
        actionResult3.setResultValue("action3", "value3");
        contentResult.addActionResult(actionResult3);

        Parameters ps = new ContentsToAponConverter().toParameters(processResult);

        String apon = "content1: {\n  action0: value0\n" + "  action1: {\n" + "    result1: value1\n" + "    result2: value2\n" + "  }\n" + "  action3: value3\n" + "}";

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testAssemble3() {
        ProcessResult processResult = new ProcessResult();
        ContentResult contentResult = new ContentResult(processResult, 4);
        contentResult.setName("content1");

        ActionResult r0 = new ActionResult();
        r0.setResultValue("action0", "value0");
        contentResult.addActionResult(r0);

        ActionResult r1 = new ActionResult();
        r1.setResultValue("action1.result1", "value1");
        contentResult.addActionResult(r1);

        ActionResult r2 = new ActionResult();
        r2.setResultValue("action1.result2", "value2");
        contentResult.addActionResult(r2);

        ActionResult r3 = new ActionResult();
        r3.setResultValue("action3", "value3");
        contentResult.addActionResult(r3);

        ContentResult contentResult2 = new ContentResult(processResult, 4);
        contentResult2.setName("content2");

        ActionResult r10 = new ActionResult();
        r10.setResultValue("action0", "value0");
        contentResult2.addActionResult(r10);

        ActionResult r11 = new ActionResult();
        r11.setResultValue("action1.result1", "value1");
        contentResult2.addActionResult(r11);

        ActionResult r12 = new ActionResult();
        r12.setResultValue("action1.result2", "value2");
        contentResult2.addActionResult(r12);

        ActionResult r13 = new ActionResult();
        r13.setResultValue("action3", "value3");
        contentResult2.addActionResult(r13);

        Parameters ps = new ContentsToAponConverter().toParameters(processResult);

        String apon = "content1: {\n  action0: value0\n" + "  action1: {\n" + "    result1: value1\n" + "    result2: value2\n" + "  }\n" + "  action3: value3\n" + "}\n" + "content2: {\n" + "  action0: value0\n" + "  action1: {\n" + "    result1: value1\n" + "    result2: value2\n" + "  }\n" + "  action3: value3\n" + "}";

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testAssemble4() {
        ProcessResult processResult = new ProcessResult();
        ContentResult contentResult = new ContentResult(processResult, 4);

        ActionResult actionResult1 = new ActionResult();

        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");

        actionResult1.setResultValue(null, map);
        contentResult.addActionResult(actionResult1);

        Parameters ps = new ContentsToAponConverter().toParameters(processResult);

        String apon = "key1: value1\nkey2: value2\nkey3: value3";

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testAssemble5() {
        ProcessResult processResult = new ProcessResult();
        ContentResult contentResult = new ContentResult(processResult, 3);
        contentResult.setName("content1");

        ActionResult actionResult1 = new ActionResult();

        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");

        actionResult1.setResultValue(null, map);
        contentResult.addActionResult(actionResult1);

        ActionResult actionResult2 = new ActionResult();
        actionResult2.setResultValue("action1.result2", "value2");
        contentResult.addActionResult(actionResult2);

        ActionResult actionResult3 = new ActionResult();
        actionResult3.setResultValue("action2", "value3");
        contentResult.addActionResult(actionResult3);

        Parameters ps = new ContentsToAponConverter().toParameters(processResult);

        String apon = "content1: {\n  key1: value1\n  key2: value2\n  key3: value3\n  action1: {\n    result2: value2\n  }\n  action2: value3\n}";

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

}
