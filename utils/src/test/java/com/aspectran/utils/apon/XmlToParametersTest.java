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
package com.aspectran.utils.apon;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-07-08</p>
 */
class XmlToParametersTest {

    @Test
    void convert() throws IOException {
        String xml = """
                <container id="12">
                  <item1>
                    <container id="34">
                      <item id="56">a
                a
                a</item>
                      <item id="78">bbb</item>
                    </container>
                    <container>
                      <item>aaa</item>
                      <item>bbb</item>
                      <item>ccc</item>
                    </container>
                  </item1>
                  <item2>
                    xyz
                </item2>
                  <item3 id="90">
                    xyz
                </item3>
                  <item4>
                    <item5 id="91">
                      xyz
                </item5>
                  </item4>
                </container>""";

        String actual = """
                container: {
                  id: 12
                  item1: {
                    container: [
                      {
                        id: 34
                        item: [
                          {
                            id: 56
                            item: (
                              |a
                              |a
                              |a
                            )
                          }
                          {
                            id: 78
                            item: bbb
                          }
                        ]
                      }
                      {
                        item: [
                          aaa
                          bbb
                          ccc
                        ]
                      }
                    ]
                  }
                  item2: (
                    |
                    |    xyz
                    |
                  )
                  item3: {
                    id: 90
                    item3: (
                      |
                      |    xyz
                      |
                    )
                  }
                  item4: {
                    item5: {
                      id: 91
                      item5: (
                        |
                        |      xyz
                        |
                      )
                    }
                  }
                }
                """;

        Parameters ps = XmlToParameters.from(xml);
        assertEquals(actual, ps.toString());
    }

}
