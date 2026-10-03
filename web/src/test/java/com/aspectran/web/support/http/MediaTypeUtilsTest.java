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
package com.aspectran.web.support.http;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for {@link MediaTypeUtils} and {@link MediaType}.
 *
 * <p>Created: 2026-10-03</p>
 */
class MediaTypeUtilsTest {

    @Test
    void testParseMediaTypeBasic() {
        MediaType mediaType = MediaTypeUtils.parseMediaType("application/json; charset=UTF-8");
        assertNotNull(mediaType);
        assertEquals("application", mediaType.getType());
        assertEquals("json", mediaType.getSubtype());
        assertEquals(StandardCharsets.UTF_8, mediaType.getCharset());
        assertEquals("UTF-8", mediaType.getParameter("charset"));
    }

    @Test
    void testParseMediaTypeCaching() {
        MediaType first = MediaTypeUtils.parseMediaType("application/json");
        MediaType second = MediaTypeUtils.parseMediaType("application/json");
        assertSame(first, second);

        MediaType viaMediaType = MediaType.parseMediaType("application/json");
        assertSame(first, viaMediaType);
    }

    @Test
    void testParseMultipartNotCached() {
        String multipart1 = "multipart/form-data; boundary=----WebKitFormBoundary1";
        String multipart2 = "Multipart/form-data; boundary=----WebKitFormBoundary2";

        MediaType mt1 = MediaTypeUtils.parseMediaType(multipart1);
        MediaType mt2 = MediaTypeUtils.parseMediaType(multipart2);

        assertEquals("multipart", mt1.getType());
        assertEquals("form-data", mt1.getSubtype());
        assertEquals("----WebKitFormBoundary1", mt1.getParameter("boundary"));

        assertEquals("multipart", mt2.getType());
        assertEquals("form-data", mt2.getSubtype());
        assertEquals("----WebKitFormBoundary2", mt2.getParameter("boundary"));
    }

    @Test
    void testParseMediaTypeWithEscapedQuote() {
        String mediaTypeStr = "text/plain; title=\"hello \\\"world\\\"; foo\"";
        MediaType mediaType = MediaTypeUtils.parseMediaType(mediaTypeStr);
        assertNotNull(mediaType);
        assertEquals("text", mediaType.getType());
        assertEquals("plain", mediaType.getSubtype());
    }

    @Test
    void testParseMediaTypeLenientOnInvalidParameter() {
        MediaType mediaType = MediaTypeUtils.parseMediaType("text/html; invalidparam");
        assertNotNull(mediaType);
        assertEquals("text", mediaType.getType());
        assertEquals("html", mediaType.getSubtype());
        assertEquals(0, mediaType.getParameters().size());
    }

    @Test
    void testParseMediaTypeEmpty() {
        assertThrows(InvalidMediaTypeException.class, () ->
                MediaTypeUtils.parseMediaType(""));
        assertThrows(InvalidMediaTypeException.class, () ->
                MediaTypeUtils.parseMediaType(null));
    }

    @Test
    void testTokenize() {
        List<String> tokens = MediaTypeUtils.tokenize("text/html, application/json, text/plain; q=0.8");
        assertEquals(3, tokens.size());
        assertEquals("text/html", tokens.get(0));
        assertEquals("application/json", tokens.get(1));
        assertEquals("text/plain; q=0.8", tokens.get(2));

        List<String> emptyTokens = MediaTypeUtils.tokenize(null);
        assertEquals(Collections.emptyList(), emptyTokens);

        List<String> quotedTokens = MediaTypeUtils.tokenize("text/html; q=\"1,0\", text/plain");
        assertEquals(2, quotedTokens.size());
        assertEquals("text/html; q=\"1,0\"", quotedTokens.get(0));
        assertEquals("text/plain", quotedTokens.get(1));
    }

    @Test
    void testToString() {
        assertEquals("", MediaTypeUtils.toString(Collections.emptyList()));

        List<MediaType> singleList = List.of(MediaType.APPLICATION_JSON);
        assertEquals("application/json", MediaTypeUtils.toString(singleList));

        List<MediaType> multiList = List.of(MediaType.TEXT_HTML, MediaType.APPLICATION_JSON);
        assertEquals("text/html, application/json", MediaTypeUtils.toString(multiList));
    }

    @Test
    void testNewConstants() {
        assertEquals("application/problem+json", MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        assertEquals("application/problem+json", MediaType.APPLICATION_PROBLEM_JSON.toString());

        assertEquals("application/yaml", MediaType.APPLICATION_YAML_VALUE);
        assertEquals("application/yaml", MediaType.APPLICATION_YAML.toString());

        assertEquals("image/svg+xml", MediaType.IMAGE_SVG_XML_VALUE);
        assertEquals("image/svg+xml", MediaType.IMAGE_SVG_XML.toString());

        assertEquals("image/webp", MediaType.IMAGE_WEBP_VALUE);
        assertEquals("image/webp", MediaType.IMAGE_WEBP.toString());

        assertEquals("text/csv", MediaType.TEXT_CSV_VALUE);
        assertEquals("text/csv", MediaType.TEXT_CSV.toString());
    }

    @Test
    void testCompareTo() {
        assertEquals(0, MediaType.APPLICATION_JSON.compareTo(MediaType.APPLICATION_JSON));
        assertEquals(0, MediaType.parseMediaType("text/html").compareTo(MediaType.parseMediaType("text/html")));

        int result = MediaType.APPLICATION_JSON.compareTo(MediaType.TEXT_HTML);
        assertEquals("application".compareTo("text"), result);
    }

    @Test
    void testQualityValueAndSorting() {
        MediaType mt1 = MediaType.parseMediaType("text/html; q=0.5");
        MediaType mt2 = MediaType.parseMediaType("text/html; q=0.9");
        MediaType mt3 = MediaType.parseMediaType("text/html"); // default 1.0

        assertEquals(0.5D, mt1.getQualityValue());
        assertEquals(0.9D, mt2.getQualityValue());
        assertEquals(1.0D, mt3.getQualityValue());

        List<MediaType> list = new java.util.ArrayList<>(List.of(mt1, mt3, mt2));
        MediaType.sortByQualityValue(list);

        assertEquals(mt3, list.get(0)); // 1.0
        assertEquals(mt2, list.get(1)); // 0.9
        assertEquals(mt1, list.get(2)); // 0.5
    }

    @Test
    void testSpecificitySorting() {
        MediaType wildcard = MediaType.ALL; // */*
        MediaType typeWildcard = MediaType.parseMediaType("text/*");
        MediaType concrete = MediaType.TEXT_HTML;

        List<MediaType> list = new java.util.ArrayList<>(List.of(wildcard, concrete, typeWildcard));
        MediaType.sortBySpecificity(list);

        assertEquals(concrete, list.get(0));
        assertEquals(typeWildcard, list.get(1));
        assertEquals(wildcard, list.get(2));
    }

    @Test
    void testIsJson() {
        org.junit.jupiter.api.Assertions.assertTrue(MediaTypeUtils.isJson(MediaType.APPLICATION_JSON));
        org.junit.jupiter.api.Assertions.assertTrue(MediaTypeUtils.isJson(MediaType.APPLICATION_PROBLEM_JSON));
        org.junit.jupiter.api.Assertions.assertTrue(MediaTypeUtils.isJson(MediaType.parseMediaType("application/vnd.api+json")));
        org.junit.jupiter.api.Assertions.assertFalse(MediaTypeUtils.isJson(MediaType.APPLICATION_XML));
        org.junit.jupiter.api.Assertions.assertFalse(MediaTypeUtils.isJson(null));

        org.junit.jupiter.api.Assertions.assertTrue(MediaType.APPLICATION_JSON.isJson());
        org.junit.jupiter.api.Assertions.assertTrue(MediaType.APPLICATION_PROBLEM_JSON.isJson());
        org.junit.jupiter.api.Assertions.assertFalse(MediaType.APPLICATION_XML.isJson());
    }

    @Test
    void testIsXml() {
        org.junit.jupiter.api.Assertions.assertTrue(MediaTypeUtils.isXml(MediaType.APPLICATION_XML));
        org.junit.jupiter.api.Assertions.assertTrue(MediaTypeUtils.isXml(MediaType.TEXT_XML));
        org.junit.jupiter.api.Assertions.assertTrue(MediaTypeUtils.isXml(MediaType.APPLICATION_PROBLEM_XML));
        org.junit.jupiter.api.Assertions.assertTrue(MediaTypeUtils.isXml(MediaType.parseMediaType("application/soap+xml")));
        org.junit.jupiter.api.Assertions.assertFalse(MediaTypeUtils.isXml(MediaType.APPLICATION_JSON));
        org.junit.jupiter.api.Assertions.assertFalse(MediaTypeUtils.isXml(null));

        org.junit.jupiter.api.Assertions.assertTrue(MediaType.APPLICATION_XML.isXml());
        org.junit.jupiter.api.Assertions.assertTrue(MediaType.TEXT_XML.isXml());
        org.junit.jupiter.api.Assertions.assertFalse(MediaType.APPLICATION_JSON.isXml());
    }

    @Test
    void testIsURLEncodedForm() {
        org.junit.jupiter.api.Assertions.assertTrue(MediaTypeUtils.isURLEncodedForm(MediaType.APPLICATION_FORM_URLENCODED));
        org.junit.jupiter.api.Assertions.assertFalse(MediaTypeUtils.isURLEncodedForm(MediaType.APPLICATION_JSON));
        org.junit.jupiter.api.Assertions.assertFalse(MediaTypeUtils.isURLEncodedForm(null));

        org.junit.jupiter.api.Assertions.assertTrue(MediaType.APPLICATION_FORM_URLENCODED.isURLEncodedForm());
        org.junit.jupiter.api.Assertions.assertFalse(MediaType.APPLICATION_JSON.isURLEncodedForm());
    }

}
