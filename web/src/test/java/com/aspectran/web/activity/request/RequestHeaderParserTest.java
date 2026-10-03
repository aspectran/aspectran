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
package com.aspectran.web.activity.request;

import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException;
import com.aspectran.web.support.http.HttpMediaTypeNotSupportedException;
import com.aspectran.web.support.http.MediaType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test cases for {@link RequestHeaderParser}.
 *
 * <p>Created: 2026-10-03</p>
 */
class RequestHeaderParserTest {

    @Test
    void testResolveAcceptContentTypesEmpty() throws HttpMediaTypeNotAcceptableException {
        DefaultRequestAdapter adapter = new DefaultRequestAdapter(MethodType.GET);
        List<MediaType> mediaTypes = RequestHeaderParser.resolveAcceptContentTypes(adapter);
        assertSame(RequestHeaderParser.MEDIA_TYPE_ALL_LIST, mediaTypes);
    }

    @Test
    void testResolveAcceptContentTypesSingleHeader() throws HttpMediaTypeNotAcceptableException {
        DefaultRequestAdapter adapter = new DefaultRequestAdapter(MethodType.GET);
        adapter.setHeader(HttpHeaders.ACCEPT, "text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8");

        List<MediaType> mediaTypes = RequestHeaderParser.resolveAcceptContentTypes(adapter);
        assertNotNull(mediaTypes);
        assertEquals(4, mediaTypes.size());
        assertEquals(MediaType.TEXT_HTML, mediaTypes.get(0));
        assertEquals(MediaType.APPLICATION_XHTML_XML, mediaTypes.get(1));
        assertEquals(MediaType.parseMediaType("application/xml;q=0.9"), mediaTypes.get(2));
        assertEquals(MediaType.parseMediaType("*/*;q=0.8"), mediaTypes.get(3));
    }

    @Test
    void testResolveAcceptContentTypesMultipleHeaders() throws HttpMediaTypeNotAcceptableException {
        DefaultRequestAdapter adapter = new DefaultRequestAdapter(MethodType.GET);
        adapter.addHeader(HttpHeaders.ACCEPT, "text/html");
        adapter.addHeader(HttpHeaders.ACCEPT, "application/json;q=0.9");

        List<MediaType> mediaTypes = RequestHeaderParser.resolveAcceptContentTypes(adapter);
        assertNotNull(mediaTypes);
        assertEquals(2, mediaTypes.size());
        assertEquals(MediaType.TEXT_HTML, mediaTypes.get(0));
        assertEquals(MediaType.parseMediaType("application/json;q=0.9"), mediaTypes.get(1));
    }

    @Test
    void testResolveAcceptContentTypesInvalid() {
        DefaultRequestAdapter adapter = new DefaultRequestAdapter(MethodType.GET);
        adapter.setHeader(HttpHeaders.ACCEPT, "invalid-type");

        assertThrows(HttpMediaTypeNotAcceptableException.class, () ->
                RequestHeaderParser.resolveAcceptContentTypes(adapter));
    }

    @Test
    void testResolveContentTypeEmpty() throws HttpMediaTypeNotSupportedException {
        DefaultRequestAdapter adapter = new DefaultRequestAdapter(MethodType.POST);
        MediaType mediaType = RequestHeaderParser.resolveContentType(adapter);
        assertNull(mediaType);
    }

    @Test
    void testResolveContentTypeValid() throws HttpMediaTypeNotSupportedException {
        DefaultRequestAdapter adapter = new DefaultRequestAdapter(MethodType.POST);
        adapter.setHeader(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

        MediaType mediaType = RequestHeaderParser.resolveContentType(adapter);
        assertNotNull(mediaType);
        assertEquals("application", mediaType.getType());
        assertEquals("json", mediaType.getSubtype());
        assertEquals(StandardCharsets.UTF_8, mediaType.getCharset());
    }

    @Test
    void testResolveContentTypeInvalid() {
        DefaultRequestAdapter adapter = new DefaultRequestAdapter(MethodType.POST);
        adapter.setHeader(HttpHeaders.CONTENT_TYPE, "invalid-content-type");

        assertThrows(HttpMediaTypeNotSupportedException.class, () ->
                RequestHeaderParser.resolveContentType(adapter));
    }

}
