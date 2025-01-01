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
package com.aspectran.core.activity.response.transform;

import com.aspectran.core.activity.process.result.ActionResult;
import com.aspectran.core.activity.process.result.ContentResult;
import com.aspectran.core.activity.process.result.ProcessResult;
import com.aspectran.utils.StringifyContext;
import com.aspectran.utils.apon.AponFormat;
import org.junit.jupiter.api.Test;

import javax.xml.transform.TransformerException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-01-12</p>
 */
class XmlTransformResponseTest {

    @Test
    void toXml() throws TransformerException {
        ProcessResult processResult = new ProcessResult();
        processResult.setName("wrap1");
        processResult.setExplicit(true);

        ContentResult contentResult = new ContentResult(processResult);
        contentResult.setName("subwrap");
        contentResult.setExplicit(true);

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
//        r3.setResultValue(null, "value4");
        contentResult.addActionResult(r3);

        ActionResult r4 = new ActionResult();
        r4.setResultValue("action4", new Object[] {LocalDate.parse("2016-08-16"), LocalDateTime.parse("2016-03-04T10:15:30")});
        contentResult.addActionResult(r4);

        StringifyContext stringifyContext = new StringifyContext();
        stringifyContext.setPretty(true);
        stringifyContext.setDateFormat("yyyy-MM-dd");
        stringifyContext.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

        StringWriter writer = new StringWriter();
        XmlTransformResponse.transform(processResult, writer, null, stringifyContext);

        String expected = """
            <?xml version="1.0" encoding="UTF-8"?>
            <wrap1>
              <subwrap>
                <action0>value0</action0>
                <action1>
                  <result1>value1</result1>
                  <result2>value2</result2>
                </action1>
                <action1>value3</action1>
                <action4>
                  <rows>
                    <row>2016-08-16</row>
                    <row>2016-03-04 10:15:30</row>
                  </rows>
                </action4>
              </subwrap>
            </wrap1>
            """;

        expected = expected.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String actual = writer.toString();

        assertEquals(expected, actual);
    }

}
