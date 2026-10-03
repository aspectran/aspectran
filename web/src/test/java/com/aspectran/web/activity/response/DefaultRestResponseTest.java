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
package com.aspectran.web.activity.response;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.Translet;
import com.aspectran.core.adapter.DefaultRequestAdapter;
import com.aspectran.core.adapter.DefaultResponseAdapter;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.StringifyContext;
import com.aspectran.web.support.http.HttpStatus;
import com.aspectran.web.support.http.MediaType;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link DefaultRestResponse}.
 *
 * <p>Created: 2026-10-03</p>
 */
class DefaultRestResponseTest {

    private Activity createMockActivity(RequestAdapter requestAdapter, ResponseAdapter responseAdapter, String encoding) {
        return createMockActivity(null, requestAdapter, responseAdapter, encoding);
    }

    private Activity createMockActivity(String requestName, RequestAdapter requestAdapter, ResponseAdapter responseAdapter, String encoding) {
        Translet translet = (Translet) Proxy.newProxyInstance(
                Translet.class.getClassLoader(),
                new Class<?>[] { Translet.class },
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("getRequestName".equals(methodName)) {
                        return requestName;
                    }
                    if ("getDefinitiveResponseEncoding".equals(methodName)) {
                        return encoding;
                    }
                    return null;
                }
        );

        return (Activity) Proxy.newProxyInstance(
                Activity.class.getClassLoader(),
                new Class<?>[] { Activity.class },
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if ("getTranslet".equals(methodName)) {
                        return translet;
                    }
                    if ("getRequestAdapter".equals(methodName)) {
                        return requestAdapter;
                    }
                    if ("getResponseAdapter".equals(methodName)) {
                        return responseAdapter;
                    }
                    if ("getStringifyContext".equals(methodName)) {
                        return new StringifyContext();
                    }
                    return null;
                }
        );
    }

    @Test
    void testHttpStatusMethods() {
        RestResponse response = new DefaultRestResponse();

        response.ok();
        assertEquals(HttpStatus.OK.value(), response.getStatus());

        response.created();
        assertEquals(HttpStatus.CREATED.value(), response.getStatus());

        response.accepted();
        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatus());

        response.noContent();
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatus());

        response.badRequest();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());

        response.unauthorized();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

        response.forbidden();
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        response.notFound();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());

        response.methodNotAllowed();
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), response.getStatus());

        response.notAcceptable();
        assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatus());

        response.conflict();
        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());

        response.preconditionFailed();
        assertEquals(HttpStatus.PRECONDITION_FAILED.value(), response.getStatus());

        response.unsupportedMediaType();
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), response.getStatus());

        response.unprocessableEntity();
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), response.getStatus());

        response.tooManyRequests();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatus());

        response.internalServerError();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatus());

        response.serviceUnavailable();
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.value(), response.getStatus());
    }

    @Test
    void testDetermineResponseContentType() {
        DefaultRestResponse restResponse = new DefaultRestResponse();
        Activity activity = createMockActivity(null, null, "UTF-8");

        MediaType acceptWithoutCharset = MediaType.APPLICATION_JSON;
        MediaType result = restResponse.determineResponseContentType(activity, acceptWithoutCharset);
        assertNotNull(result);
        assertEquals(MediaType.APPLICATION_JSON.getType(), result.getType());
        assertEquals(MediaType.APPLICATION_JSON.getSubtype(), result.getSubtype());
        assertEquals(StandardCharsets.UTF_8, result.getCharset());

        MediaType acceptWithCharset = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);
        MediaType result2 = restResponse.determineResponseContentType(activity, acceptWithCharset);
        assertEquals(acceptWithCharset, result2);
    }

    @Test
    void testTransformJsonSuffix() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");

        DefaultRestResponse restResponse = new DefaultRestResponse(data);

        RequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.GET);
        StringWriter writer = new StringWriter();
        ResponseAdapter responseAdapter = new DefaultResponseAdapter(null, writer);
        Activity activity = createMockActivity(requestAdapter, responseAdapter, "UTF-8");

        MediaType problemJson = MediaType.parseMediaType("application/problem+json");
        restResponse.transformByContentType(activity, problemJson);

        String output = writer.toString();
        assertTrue(output.contains("\"key\": \"value\"") || output.contains("\"key\":\"value\""));
    }

    @Test
    void testTransformXmlSuffix() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "hello");

        DefaultRestResponse restResponse = new DefaultRestResponse("root", data);

        RequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.GET);
        StringWriter writer = new StringWriter();
        ResponseAdapter responseAdapter = new DefaultResponseAdapter(null, writer);
        Activity activity = createMockActivity(requestAdapter, responseAdapter, "UTF-8");

        MediaType problemXml = MediaType.parseMediaType("application/problem+xml");
        restResponse.transformByContentType(activity, problemXml);

        String output = writer.toString();
        assertTrue(output.contains("<root>"));
        assertTrue(output.contains("<message>hello</message>"));
        assertTrue(output.contains("</root>"));
    }

    @Test
    void testToXmlWithNullData() throws Exception {
        DefaultRestResponse restResponse = new DefaultRestResponse("root", null);

        RequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.GET);
        StringWriter writer = new StringWriter();
        ResponseAdapter responseAdapter = new DefaultResponseAdapter(null, writer);
        Activity activity = createMockActivity(requestAdapter, responseAdapter, "UTF-8");

        restResponse.transformByContentType(activity, MediaType.APPLICATION_XML);

        String output = writer.toString();
        assertNotNull(output);
    }

    @Test
    void testDetermineAcceptContentTypeWithExtension() throws Exception {
        DefaultRestResponse restResponse = new DefaultRestResponse();
        restResponse.setFavorPathExtension(true);
        restResponse.setIgnoreUnknownPathExtensions(false);

        // Path with uppercase extension
        Translet translet = (Translet) Proxy.newProxyInstance(
                Translet.class.getClassLoader(),
                new Class<?>[] { Translet.class },
                (proxy, method, args) -> {
                    if ("getRequestName".equals(method.getName())) {
                        return "/api/users.JSON";
                    }
                    return null;
                }
        );
        Activity activity = (Activity) Proxy.newProxyInstance(
                Activity.class.getClassLoader(),
                new Class<?>[] { Activity.class },
                (proxy, method, args) -> {
                    if ("getTranslet".equals(method.getName())) {
                        return translet;
                    }
                    return null;
                }
        );

        MediaType mediaType = restResponse.determineAcceptContentType(activity);
        assertEquals(MediaType.APPLICATION_JSON, mediaType);
    }

    @Test
    void testDetermineAcceptContentTypeNoExtensionIgnoreFalse() throws Exception {
        DefaultRestResponse restResponse = new DefaultRestResponse();
        restResponse.setFavorPathExtension(true);
        restResponse.setIgnoreUnknownPathExtensions(false);

        DefaultRequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.GET);
        requestAdapter.setHeader(com.aspectran.web.support.http.HttpHeaders.ACCEPT, "application/json");

        // Path with NO extension, but ignoreUnknownPathExtensions is false
        Translet translet = (Translet) Proxy.newProxyInstance(
                Translet.class.getClassLoader(),
                new Class<?>[] { Translet.class },
                (proxy, method, args) -> {
                    if ("getRequestName".equals(method.getName())) {
                        return "/api/users";
                    }
                    return null;
                }
        );
        Activity activity = (Activity) Proxy.newProxyInstance(
                Activity.class.getClassLoader(),
                new Class<?>[] { Activity.class },
                (proxy, method, args) -> {
                    if ("getTranslet".equals(method.getName())) {
                        return translet;
                    }
                    if ("getRequestAdapter".equals(method.getName())) {
                        return requestAdapter;
                    }
                    return null;
                }
        );

        MediaType mediaType = restResponse.determineAcceptContentType(activity);
        assertEquals(MediaType.APPLICATION_JSON, mediaType);
    }

    @Test
    void testDetermineAcceptContentTypeUnknownExtensionIgnoreFalse() {
        DefaultRestResponse restResponse = new DefaultRestResponse();
        restResponse.setFavorPathExtension(true);
        restResponse.setIgnoreUnknownPathExtensions(false);

        Translet translet = (Translet) Proxy.newProxyInstance(
                Translet.class.getClassLoader(),
                new Class<?>[] { Translet.class },
                (proxy, method, args) -> {
                    if ("getRequestName".equals(method.getName())) {
                        return "/api/users.unknown";
                    }
                    return null;
                }
        );
        Activity activity = (Activity) Proxy.newProxyInstance(
                Activity.class.getClassLoader(),
                new Class<?>[] { Activity.class },
                (proxy, method, args) -> {
                    if ("getTranslet".equals(method.getName())) {
                        return translet;
                    }
                    return null;
                }
        );

        org.junit.jupiter.api.Assertions.assertThrows(
                com.aspectran.web.support.http.HttpMediaTypeNotAcceptableException.class,
                () -> restResponse.determineAcceptContentType(activity)
        );
    }

    @Test
    void testTransformByPathExtensions() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Aspectran");

        // 1. JSON extension
        {
            DefaultRestResponse restResponse = new DefaultRestResponse(data);
            StringWriter writer = new StringWriter();
            ResponseAdapter responseAdapter = new DefaultResponseAdapter(null, writer);
            RequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.GET);
            Activity activity = createMockActivity("/test/sample.json", requestAdapter, responseAdapter, "UTF-8");

            restResponse.transform(activity);
            assertEquals("application/json;charset=UTF-8", responseAdapter.getContentType());
            assertTrue(writer.toString().contains("\"name\": \"Aspectran\"") || writer.toString().contains("\"name\":\"Aspectran\""));
        }

        // 2. XML extension
        {
            DefaultRestResponse restResponse = new DefaultRestResponse("root", data);
            StringWriter writer = new StringWriter();
            ResponseAdapter responseAdapter = new DefaultResponseAdapter(null, writer);
            RequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.GET);
            Activity activity = createMockActivity("/test/sample.xml", requestAdapter, responseAdapter, "UTF-8");

            restResponse.transform(activity);
            assertEquals("application/xml;charset=UTF-8", responseAdapter.getContentType());
            assertTrue(writer.toString().contains("<root>"));
            assertTrue(writer.toString().contains("<name>Aspectran</name>"));
            assertTrue(writer.toString().contains("</root>"));
        }

        // 3. APON extension
        {
            DefaultRestResponse restResponse = new DefaultRestResponse(data);
            StringWriter writer = new StringWriter();
            ResponseAdapter responseAdapter = new DefaultResponseAdapter(null, writer);
            RequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.GET);
            Activity activity = createMockActivity("/test/sample.apon", requestAdapter, responseAdapter, "UTF-8");

            restResponse.transform(activity);
            assertEquals("application/apon;charset=UTF-8", responseAdapter.getContentType());
            assertTrue(writer.toString().contains("name: Aspectran"));
        }

        // 4. TXT extension
        {
            DefaultRestResponse restResponse = new DefaultRestResponse(data);
            StringWriter writer = new StringWriter();
            ResponseAdapter responseAdapter = new DefaultResponseAdapter(null, writer);
            RequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.GET);
            Activity activity = createMockActivity("/test/sample.txt", requestAdapter, responseAdapter, "UTF-8");

            restResponse.transform(activity);
            assertEquals("text/plain;charset=UTF-8", responseAdapter.getContentType());
            assertTrue(writer.toString().contains("name=Aspectran"));
        }

        // 5. HTML extension
        {
            DefaultRestResponse restResponse = new DefaultRestResponse(data);
            StringWriter writer = new StringWriter();
            ResponseAdapter responseAdapter = new DefaultResponseAdapter(null, writer);
            RequestAdapter requestAdapter = new DefaultRequestAdapter(MethodType.GET);
            Activity activity = createMockActivity("/test/sample.html", requestAdapter, responseAdapter, "UTF-8");

            restResponse.transform(activity);
            assertEquals("text/html;charset=UTF-8", responseAdapter.getContentType());
            assertTrue(writer.toString().contains("name=Aspectran"));
        }
    }

}
