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
package com.aspectran.core.activity.response.transform.apon;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.core.util.apon.Parameters;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

class ContentsAponAssemblerTest {

    @Test
    void testAssemble0() throws InvocationTargetException {
        ProcessResult processResult = new ProcessResult();
        ContentResult contentResult = new ContentResult(processResult, 4);

        ActionResult actionResult1 = new ActionResult();
        actionResult1.setResultValue("action1.result1", "value1");
        contentResult.addActionResult(actionResult1);

        ActionResult actionResult2 = new ActionResult();
        actionResult2.setResultValue("action1.result2", "value2");
        contentResult.addActionResult(actionResult2);

        Parameters parameters = ContentsAponAssembler.assemble(processResult);

        System.out.println(parameters);
    }

    @Test
    void testAssemble1() throws InvocationTargetException {
        ProcessResult processResult = new ProcessResult();
        ContentResult contentResult = new ContentResult(processResult, 4);

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
        actionResult3.setResultValue("action1", "value3");
        contentResult.addActionResult(actionResult3);

        Parameters parameters = ContentsAponAssembler.assemble(processResult);

        System.out.println(parameters);
    }

    @Test
    void testAssemble2() throws InvocationTargetException {
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

        Parameters parameters = ContentsAponAssembler.assemble(processResult);

        System.out.println(parameters);
    }

    @Test
    void testAssemble3() throws InvocationTargetException {
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

        ContentResult contentResult2 = new ContentResult(processResult, 4);
        contentResult2.setName("content2");

        ActionResult actionResult20 = new ActionResult();
        actionResult20.setResultValue("action0", "value0");
        contentResult2.addActionResult(actionResult20);

        ActionResult actionResult21 = new ActionResult();
        actionResult21.setResultValue("action1.result1", "value1");
        contentResult2.addActionResult(actionResult21);

        ActionResult actionResult22 = new ActionResult();
        actionResult22.setResultValue("action1.result2", "value2");
        contentResult2.addActionResult(actionResult22);

        ActionResult actionResult23 = new ActionResult();
        actionResult23.setResultValue("action3", "value3");
        contentResult2.addActionResult(actionResult23);

        Parameters parameters = ContentsAponAssembler.assemble(processResult);

        System.out.println(parameters);
    }

    @Test
    void testAssemble4() throws InvocationTargetException {
        ProcessResult processResult = new ProcessResult();
        ContentResult contentResult = new ContentResult(processResult, 4);

        ActionResult actionResult1 = new ActionResult();

        Map<String, String> map = new LinkedHashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");

        actionResult1.setResultValue(null, map);
        contentResult.addActionResult(actionResult1);

        Parameters parameters = ContentsAponAssembler.assemble(processResult);

        System.out.println(parameters);
    }

    @Test
    void testAssemble5() throws InvocationTargetException {
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

        Parameters parameters = ContentsAponAssembler.assemble(processResult);

        System.out.println(parameters);
    }

}