/*
 * Copyright (c) 2008-2022 The Aspectran Project
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
package com.aspectran.core.util.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * <p>Created: 2019-07-08</p>
 */
class XmlToAponTest {

    @Test
    void testConvert1() throws IOException {
        String xml = "<container id=\"12\">\n" +
                "  <item1>\n" +
                "    <container id=\"34\">\n" +
                "      <item id=\"56\">a\na\na</item>\n" +
                "      <item id=\"78\">bbb</item>\n" +
                "    </container>\n" +
                "    <container>\n" +
                "      <item>aaa</item>\n" +
                "      <item>bbb</item>\n" +
                "      <item>ccc</item>\n" +
                "    </container>\n" +
                "  </item1>\n" +
                "  <item2>\n" +
                "    xyz\n" +
                "  </item2>\n" +
                "  <item3 id=\"90\">\n" +
                "    xyz\n" +
                "  </item3>\n" +
                "  <item4>\n" +
                "    <item5 id=\"91\">\n" +
                "      xyz\n" +
                "    </item5>\n" +
                "  </item4>\n" +
                "</container>";

        Parameters ps = XmlToApon.from(xml);

        System.out.println(ps);
    }

}
