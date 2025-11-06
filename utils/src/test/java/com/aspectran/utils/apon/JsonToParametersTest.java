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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-06-29</p>
 */
class JsonToParametersTest {

    @Test
    void testConvertJsonToApon() throws IOException {
        String sb = """
                [
                {
                  "param1": 111,
                  "param2": 222
                }
                ,
                {
                  "param3": 333,
                  "param4": 444
                }
                , null\
                ]
                """;

        String apon = """
                {
                  param1: 111
                  param2: 222
                }
                {
                  param3: 333
                  param4: 444
                }""";

        Parameters ps = JsonToParameters.from(sb, ArrayParameters.class);

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testConvertJsonToApon2() throws IOException {
        String json = """
                {
                    "glossary": {
                        "title": "example glossary",
                \t\t"GlossDiv": {
                            "title": "S",
                \t\t\t"GlossList": {
                                "GlossEntry": {
                                    "ID": "SGML",
                \t\t\t\t\t"SortAs": "SGML",
                \t\t\t\t\t"GlossTerm": "Standard Generalized Markup Language",
                \t\t\t\t\t"Acronym": "SGML",
                \t\t\t\t\t"Abbrev": "ISO 8879:1986",
                \t\t\t\t\t"GlossDef": {
                                        "para": "A meta-markup language, used to create markup languages such as DocBook.",
                \t\t\t\t\t\t"GlossSeeAlso": ["GML", "XML"]
                                    },
                \t\t\t\t\t"GlossSee": "markup"
                                }
                            }
                        }
                    }
                }""";
        String apon = """
                glossary: {
                  title: example glossary
                  GlossDiv: {
                    title: S
                    GlossList: {
                      GlossEntry: {
                        ID: SGML
                        SortAs: SGML
                        GlossTerm: Standard Generalized Markup Language
                        Acronym: SGML
                        Abbrev: ISO 8879:1986
                        GlossDef: {
                          para: A meta-markup language, used to create markup languages such as DocBook.
                          GlossSeeAlso: [
                            GML
                            XML
                          ]
                        }
                        GlossSee: markup
                      }
                    }
                  }
                }""";

        Parameters ps = JsonToParameters.from(json);

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testConvertJsonToApon3() throws IOException {
        String json = "{\"message\": \"11\\n22\"}";

        Parameters messagePayload = JsonToParameters.from(json, MessagePayload.class);
        String result1 = messagePayload.toString();

        MessagePayload messagePayload2 = new MessagePayload();
        AponReader reader = new AponReader(messagePayload.toString());
        reader.read(messagePayload2);
        String result2 = messagePayload.toString();

        assertEquals(result1, result2);
    }

    public static class MessagePayload extends AbstractParameters {

        private static final ParameterKey message;

        private static final ParameterKey[] parameterKeys;

        static {
            message = new ParameterKey("message", ValueType.STRING);
            parameterKeys = new ParameterKey[] { message };
        }

        public MessagePayload() {
            super(parameterKeys);
        }

        public String getContent() {
            return getString(message);
        }

        public void setContent(String content) {
            putValue(MessagePayload.message, content);
        }

    }

    @Test
    void testConvertJsonToArrayOfObjects() throws IOException {
        String json = """
                {
                  "arrayObject": [
                    {
                      "key1": "value1"
                    }
                  ]
                }""";

        String expectedApon = """
                arrayObject: [
                  {
                    key1: value1
                  }
                ]""";

        Parameters parameters = JsonToParameters.from(json);

        // Normalize line endings for comparison
        String actualApon = parameters.toString().trim().replace("\r\n", "\n");
        String normalizedExpectedApon = expectedApon.replace("\r\n", "\n");

        assertEquals(normalizedExpectedApon, actualApon);

        // Further assertions to ensure correct structure
        List<Parameters> noncoveredDetails = parameters.getParametersList("arrayObject");
        assertEquals(1, noncoveredDetails.size());

        Parameters detail = noncoveredDetails.get(0);
        assertEquals("value1", detail.getString("key1"));
    }

}
