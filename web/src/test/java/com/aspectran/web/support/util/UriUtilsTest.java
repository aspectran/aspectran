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
package com.aspectran.web.support.util;

import com.aspectran.utils.MultiValueMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link UriUtils}.
 */
class UriUtilsTest {

    @Test
    void testParseQueryParamsEmptyOrNull() {
        assertTrue(UriUtils.parseQueryParams(null).isEmpty());
        assertTrue(UriUtils.parseQueryParams("").isEmpty());
        assertTrue(UriUtils.parseQueryParams("   ").isEmpty());
    }

    @Test
    void testParseQueryParamsSingle() {
        MultiValueMap<String, String> params = UriUtils.parseQueryParams("name=value");
        assertEquals(1, params.size());
        assertEquals("value", params.getFirst("name"));
    }

    @Test
    void testParseQueryParamsMultiple() {
        MultiValueMap<String, String> params = UriUtils.parseQueryParams("p1=v1&p2=v2&p3=v3");
        assertEquals(3, params.size());
        assertEquals("v1", params.getFirst("p1"));
        assertEquals("v2", params.getFirst("p2"));
        assertEquals("v3", params.getFirst("p3"));
    }

    @Test
    void testParseQueryParamsSameKeyMultipleValues() {
        MultiValueMap<String, String> params = UriUtils.parseQueryParams("fruit=apple&fruit=banana&fruit=orange");
        assertEquals(1, params.size());
        List<String> fruits = params.get("fruit");
        assertNotNull(fruits);
        assertEquals(3, fruits.size());
        assertEquals("apple", fruits.get(0));
        assertEquals("banana", fruits.get(1));
        assertEquals("orange", fruits.get(2));
    }

    @Test
    void testParseQueryParamsEmptyValueAndFlag() {
        MultiValueMap<String, String> params = UriUtils.parseQueryParams("flag&empty=&foo=bar");
        assertEquals(3, params.size());
        assertEquals("", params.getFirst("flag"));
        assertEquals("", params.getFirst("empty"));
        assertEquals("bar", params.getFirst("foo"));
    }

    @Test
    void testParseQueryParamsUrlEncoded() {
        MultiValueMap<String, String> params = UriUtils.parseQueryParams("greeting=hello+world&korean=%ED%95%9C%EA%B8%80");
        assertEquals("hello world", params.getFirst("greeting"));
        assertEquals("한글", params.getFirst("korean"));
    }

    @Test
    void testParseQueryParamsWithCharset() {
        MultiValueMap<String, String> params = UriUtils.parseQueryParams("key=%C7%D1%B1%DB", java.nio.charset.Charset.forName("EUC-KR"));
        assertEquals("한글", params.getFirst("key"));
    }

    @Test
    void testMakeAbsoluteUrl() {
        assertEquals("http://localhost:8080/auth/login",
                UriUtils.makeAbsoluteUrl("http", "localhost", 8080, "/auth/login"));
        assertEquals("http://localhost:8080/console/auth/login",
                UriUtils.makeAbsoluteUrl("http", "localhost", 8080, "/console", "/auth/login"));
        assertEquals("http://localhost:8080/console/auth/login",
                UriUtils.makeAbsoluteUrl("http", "localhost", 8080, "/console", "auth/login"));
        assertEquals("http://localhost:8080/console/auth/login",
                UriUtils.makeAbsoluteUrl("http", "localhost", 8080, "/console", "/console/auth/login"));
        assertEquals("http://localhost:8080/console?referrer=test",
                UriUtils.makeAbsoluteUrl("http", "localhost", 8080, "/console", "/console?referrer=test"));
        assertEquals("http://localhost:8080/auth/login",
                UriUtils.makeAbsoluteUrl("http", "localhost", 8080, "/", "/auth/login"));
        assertEquals("https://example.com/external",
                UriUtils.makeAbsoluteUrl("http", "localhost", 8080, "/console", "https://example.com/external"));
    }

}
