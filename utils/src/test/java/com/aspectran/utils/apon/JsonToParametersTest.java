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

        Parameters ps = JsonToParameters.from(sb.toString(), ArrayParameters.class);

        String s1 = apon.replace("\n", AponFormat.SYSTEM_NEW_LINE);
        String s2 = ps.toString().trim();

        assertEquals(s1, s2);
    }

    @Test
    void testConvertJsonToApon2() throws IOException {
        String json = "{\n" + "    \"glossary\": {\n" + "        \"title\": \"example glossary\",\n" + "\t\t\"GlossDiv\": {\n" + "            \"title\": \"S\",\n" + "\t\t\t\"GlossList\": {\n" + "                \"GlossEntry\": {\n" + "                    \"ID\": \"SGML\",\n" + "\t\t\t\t\t\"SortAs\": \"SGML\",\n" + "\t\t\t\t\t\"GlossTerm\": \"Standard Generalized Markup Language\",\n" + "\t\t\t\t\t\"Acronym\": \"SGML\",\n" + "\t\t\t\t\t\"Abbrev\": \"ISO 8879:1986\",\n" + "\t\t\t\t\t\"GlossDef\": {\n" + "                        \"para\": \"A meta-markup language, used to create markup languages such as DocBook.\",\n" + "\t\t\t\t\t\t\"GlossSeeAlso\": [\"GML\", \"XML\"]\n" + "                    },\n" + "\t\t\t\t\t\"GlossSee\": \"markup\"\n" + "                }\n" + "            }\n" + "        }\n" + "    }\n" + "}";
        String apon = "glossary: {\n" + "  title: example glossary\n" + "  GlossDiv: {\n" + "    title: S\n" + "    GlossList: {\n" + "      GlossEntry: {\n" + "        ID: SGML\n" + "        SortAs: SGML\n" + "        GlossTerm: Standard Generalized Markup Language\n" + "        Acronym: SGML\n" + "        Abbrev: ISO 8879:1986\n" + "        GlossDef: {\n" + "          para: A meta-markup language, used to create markup languages such as DocBook.\n" + "          GlossSeeAlso: [\n" + "            GML\n" + "            XML\n" + "          ]\n" + "        }\n" + "        GlossSee: markup\n" + "      }\n" + "    }\n" + "  }\n" + "}";

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

    static class MessagePayload extends AbstractParameters {

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
        String json = "{\n" +
                "  \"noncovered_detail\": [\n" +
                "    {\n" +
                "      \"business_no\": \"2108206683\",\n" +
                "      \"hospital_name\": \"한일병원\",\n" +
                "      \"receipt_date\": \"20250802\",\n" +
                "      \"deduction_name\": \"얼음주머니\",\n" +
                "      \"deduction_amount\": 9000,\n" +
                "      \"noncovered_reason\": \"치료재료대 비급여 항목으로 지원 불가\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        String expectedApon = "noncovered_detail: [\n" +
                "  {\n" +
                "    business_no: 2108206683\n" +
                "    hospital_name: 한일병원\n" +
                "    receipt_date: 20250802\n" +
                "    deduction_name: 얼음주머니\n" +
                "    deduction_amount: 9000\n" +
                "    noncovered_reason: 치료재료대 비급여 항목으로 지원 불가\n" +
                "  }\n" +
                "]";

        Parameters parameters = JsonToParameters.from(json);

        // Normalize line endings for comparison
        String actualApon = parameters.toString().trim().replace("\r\n", "\n");
        String normalizedExpectedApon = expectedApon.replace("\r\n", "\n");

        assertEquals(normalizedExpectedApon, actualApon);

        // Further assertions to ensure correct structure
        List<Parameters> noncoveredDetails = parameters.getParametersList("noncovered_detail");
        assertEquals(1, noncoveredDetails.size());

        Parameters detail = noncoveredDetails.get(0);
        assertEquals("2108206683", detail.getString("business_no"));
        assertEquals("한일병원", detail.getString("hospital_name"));
        assertEquals("20250802", detail.getString("receipt_date"));
        assertEquals("얼음주머니", detail.getString("deduction_name"));
        assertEquals(9000, detail.getInt("deduction_amount"));
        assertEquals("치료재료대 비급여 항목으로 지원 불가", detail.getString("noncovered_reason"));
    }

}
