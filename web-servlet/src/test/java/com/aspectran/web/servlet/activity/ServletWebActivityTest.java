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
package com.aspectran.web.servlet.activity;

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.test.web.servlet.ServletWebActivityTester;
import com.aspectran.test.web.servlet.ServletWebAspectranTest;
import com.aspectran.utils.json.JsonWriter;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for ServletWebActivit.
 *
 * <p>Created: 2026. 3. 16.</p>
 */
@ServletWebAspectranTest(rules = "classpath:config/web-test-config.xml")
class ServletWebActivityTest {

    @Test
    void testHello(@NonNull ServletWebActivityTester tester) {
        tester.perform("/hello");
        Assertions.assertEquals("Hello, Web World!", tester.getWrittenResponse());
    }

    @Test
    void testDispatch(@NonNull ServletWebActivityTester tester) {
        tester.perform("/dispatch");
        Assertions.assertEquals("/WEB-INF/jsp/templates/default.jsp",
                tester.getLastRequest().getAttribute("jakarta.servlet.forward.request_uri"));

        Object page = tester.getLastRequest().getAttribute("page");
        assertNotNull(page);
        assertInstanceOf(Map.class, page);
        assertEquals("Welcome to Aspectran Demo Site", ((Map<?, ?>)page).get("headline"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testParams(@NonNull ServletWebActivityTester tester) {
        Map<String, String> params = Collections.singletonMap("name", "aspectran");
        tester.perform("/test/params", MethodType.GET, params);

        Object echo = tester.getLastRequest().getAttribute("echo");
        assertNotNull(echo);
        assertInstanceOf(Map.class, echo);
        assertEquals("aspectran", ((Map<String, Object>)echo).get("name"));
    }

    @Test
    void testRedirect(@NonNull ServletWebActivityTester tester) {
        tester.perform("/test/redirect");
        Assertions.assertEquals(302, tester.getLastResponse().getStatus());
        Assertions.assertEquals("/hello", tester.getLastResponse().getRedirectLocation());
    }

    @Test
    void testDefaultEncoding(@NonNull ServletWebActivityTester tester) {
        tester.perform("/hello");
        Assertions.assertEquals("utf-8", tester.getLastResponse().getCharacterEncoding());
        Assertions.assertEquals("text/plain", tester.getLastResponse().getContentType());
    }

    @Test
    void testExplicitEncoding(@NonNull ServletWebActivityTester tester) {
        tester.perform("/test/encoding");
        Assertions.assertEquals("iso-8859-1", tester.getLastResponse().getCharacterEncoding());
        Assertions.assertEquals("application/xml", tester.getLastResponse().getContentType());
        String response = tester.getWrittenResponse();
        assertNotNull(response);
        assertTrue(response.contains("<xml>"), "Actual response: [" + response + "]");
        assertTrue(response.contains("<root>hello</root>"), "Actual response: [" + response + "]");
    }

    @Test
    void testMultipart(@NonNull ServletWebActivityTester tester) throws IOException {
        String boundary = "AspeCtranBoundary";
        String contentType = "multipart/form-data; boundary=" + boundary;
        String body = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"param1\"\r\n" +
                "\r\n" +
                "value1\r\n" +
                "--" + boundary + "--\r\n";

        tester.perform("/test/multipart", MethodType.POST, null, body.getBytes(), contentType);

        String response = tester.getWrittenResponse();
        assertNotNull(response);

        String expected = new JsonWriter(new StringWriter())
                .prettyPrint(true)
                .indentString("  ")
                .nullWritable(false)
                .beginObject()
                .name("echo")
                .beginObject()
                .name("param1")
                .value("value1")
                .endObject()
                .endObject()
                .toString();
        assertEquals(expected, response);
    }

}
