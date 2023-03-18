/*
 * Copyright (c) 2008-2023 The Aspectran Project
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019-06-29</p>
 */
class JsonToAponTest {

    @Test
    void testConvertJsonToApon() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append("{\n");
        sb.append("  \"param1\": 111,\n");
        sb.append("  \"param2\": 222\n");
        sb.append("}\n");
        sb.append(",\n");
        sb.append("{\n");
        sb.append("  \"param3\": 333,\n");
        sb.append("  \"param4\": 444\n");
        sb.append("}\n, null");
        sb.append("]\n");

        String apon = "{\n" + "  param1: 111\n" + "  param2: 222\n" + "}\n" + "{\n" + "  param3: 333\n" + "  param4: 444\n" + "}";

        Parameters ps = JsonToApon.from(sb.toString(), new ArrayParameters());

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testConvertJsonToApon2() throws IOException {
        String json = "{\n" + "    \"glossary\": {\n" + "        \"title\": \"example glossary\",\n" + "\t\t\"GlossDiv\": {\n" + "            \"title\": \"S\",\n" + "\t\t\t\"GlossList\": {\n" + "                \"GlossEntry\": {\n" + "                    \"ID\": \"SGML\",\n" + "\t\t\t\t\t\"SortAs\": \"SGML\",\n" + "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" + "\t\t\t\t\t\"Acronym\": \"SGML\",\n" + "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" + "\t\t\t\t\t\"GlossDef\": {\n" + "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" + "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" + "                    },\n" + "\t\t\t\t\t\"GlossSee\": \"markup\"\n" + "                }\n" + "            }\n" + "        }\n" + "    }\n" + "}";
        String apon = "glossary: {\n" + "  title: example glossary\n" + "  GlossDiv: {\n" + "    title: S\n" + "    GlossList: {\n" + "      GlossEntry: {\n" + "        ID: SGML\n" + "        SortAs: SGML\n" + "        GlossTerm: Standard Generalized Markup Language\n" + "        Acronym: SGML\n" + "        Abbrev: ISO 8879:1986\n" + "        GlossDef: {\n" + "          para: A meta-markup language, used to create markup languages such as DocBook.\n" + "          GlossSeeAlso: [\n" + "            GML\n" + "            XML\n" + "          ]\n" + "        }\n" + "        GlossSee: markup\n" + "      }\n" + "    }\n" + "  }\n" + "}";

        Parameters ps = JsonToApon.from(json);

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testConvertJsonToApon3() throws IOException {
        String json = "{\"message\": \"11\\n22\"}";

        Parameters messagePayload = JsonToApon.from(json, MessagePayload.class);
        String result1 = messagePayload.toString();

        MessagePayload messagePayload2 = new MessagePayload();
        AponReader reader = new AponReader(messagePayload.toString());
        Parameters parameters = reader.read(messagePayload2);
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

}
