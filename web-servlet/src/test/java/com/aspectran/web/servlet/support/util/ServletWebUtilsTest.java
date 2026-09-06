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
package com.aspectran.web.servlet.support.util;

import com.aspectran.test.web.servlet.mock.MockHttpServletRequest;
import com.aspectran.utils.MultiValueMap;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ServletWebUtils}.
 */
class ServletWebUtilsTest {

    @Test
    void testParseQueryParamsEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MultiValueMap<String, String> params = ServletWebUtils.parseQueryParams(request);
        assertTrue(params.isEmpty());

        Map<String, String[]> paramMap = ServletWebUtils.parseQueryParameters(request);
        assertTrue(paramMap.isEmpty());
    }

    @Test
    void testParseQueryParams() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("a=1&b=2&c=3&a=4");

        MultiValueMap<String, String> params = ServletWebUtils.parseQueryParams(request);
        assertEquals(3, params.size());
        List<String> aValues = params.get("a");
        assertNotNull(aValues);
        assertEquals(2, aValues.size());
        assertEquals("1", aValues.get(0));
        assertEquals("4", aValues.get(1));
        assertEquals("2", params.getFirst("b"));
        assertEquals("3", params.getFirst("c"));

        Map<String, String[]> paramMap = ServletWebUtils.parseQueryParameters(request);
        assertEquals(3, paramMap.size());
        assertArrayEquals(new String[]{"1", "4"}, paramMap.get("a"));
        assertArrayEquals(new String[]{"2"}, paramMap.get("b"));
        assertArrayEquals(new String[]{"3"}, paramMap.get("c"));
    }

    @Test
    void testParseQueryParamsUrlEncoded() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("title=Hello+World&msg=%ED%95%9C%EA%B8%80%20%ED%85%8C%EC%8A%A4%ED%8A%B8");

        Map<String, String[]> paramMap = ServletWebUtils.parseQueryParameters(request);
        assertArrayEquals(new String[]{"Hello World"}, paramMap.get("title"));
        assertArrayEquals(new String[]{"한글 테스트"}, paramMap.get("msg"));
    }

    @Test
    void testParseQueryParamsWithEncoding() throws java.io.UnsupportedEncodingException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCharacterEncoding("EUC-KR");
        request.setQueryString("korean=%C7%D1%B1%DB");

        Map<String, String[]> paramMap = ServletWebUtils.parseQueryParameters(request);
        assertArrayEquals(new String[]{"한글"}, paramMap.get("korean"));
    }

}
