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
package com.aspectran.web.support.rest.request;

import com.aspectran.core.context.rule.type.MethodType;
import com.aspectran.utils.json.JsonString;
import com.aspectran.web.activity.response.RestResponse;
import com.aspectran.web.support.http.HttpHeaders;
import com.aspectran.web.support.http.MediaType;
import com.aspectran.web.support.rest.response.FailureResponse;
import com.aspectran.web.support.rest.response.SuccessResponse;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.io.CloseMode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link RestRequest}.
 *
 * <p>Created: 2026-10-03</p>
 */
class RestRequestTest {

    private static class StubHttpClient extends CloseableHttpClient {

        private final BasicClassicHttpResponse responseToReturn;
        private final AtomicReference<ClassicHttpRequest> capturedRequest = new AtomicReference<>();

        StubHttpClient(BasicClassicHttpResponse responseToReturn) {
            this.responseToReturn = responseToReturn;
        }

        public ClassicHttpRequest getCapturedRequest() {
            return capturedRequest.get();
        }

        @Override
        protected CloseableHttpResponse doExecute(
                HttpHost target,
                ClassicHttpRequest request,
                HttpContext context) {
            capturedRequest.set(request);
            return CloseableHttpResponse.adapt(responseToReturn);
        }

        @Override
        public void close() {
        }

        @Override
        public void close(CloseMode closeMode) {
        }
    }

    @Test
    void testFluentBuilderMethods() {
        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(HttpStatus.SC_OK, "OK");
        StubHttpClient stubClient = new StubHttpClient(httpResponse);

        RestRequest request = new RestRequest(stubClient)
                .get()
                .url("https://api.example.com/items")
                .parameter("page", 1)
                .parameter("size", 10)
                .header("X-Custom", "value1")
                .addHeader("X-Custom", "value2")
                .accept(MediaType.APPLICATION_JSON)
                .bearerToken("test-token");

        assertNotNull(request);
    }

    @Test
    void testMethodShortcuts() {
        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(HttpStatus.SC_OK, "OK");
        StubHttpClient stubClient = new StubHttpClient(httpResponse);
        RestRequest request = new RestRequest(stubClient);

        request.get();
        request.post();
        request.put();
        request.patch();
        request.delete();
        request.head();
        request.options();
        request.trace();
        request.method(MethodType.GET);

        assertNotNull(request);
    }

    @Test
    void testJsonRequestBody() throws IOException {
        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(HttpStatus.SC_OK, "OK");
        StubHttpClient stubClient = new StubHttpClient(httpResponse);
        RestRequest request = new RestRequest(stubClient);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", "Aspectran");
        map.put("version", 9);

        request.json(map);
        assertNotNull(request);
    }

    @Test
    void testRetrieveSuccessJsonResponse() throws IOException {
        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(HttpStatus.SC_OK, "OK");
        httpResponse.setEntity(new StringEntity("{\"status\":\"ok\"}", ContentType.APPLICATION_JSON));
        StubHttpClient stubClient = new StubHttpClient(httpResponse);

        RestRequest request = new RestRequest(stubClient)
                .get()
                .url("https://api.example.com/data")
                .accept(MediaType.APPLICATION_JSON);

        RestResponse response = request.retrieve();
        assertInstanceOf(SuccessResponse.class, response);
        SuccessResponse successResponse = (SuccessResponse) response;
        assertEquals(HttpStatus.SC_OK, successResponse.getStatus());
        assertInstanceOf(JsonString.class, successResponse.getData().getData());
        assertEquals("{\"status\":\"ok\"}", successResponse.getData().getData().toString());
    }

    @Test
    void testRetrieveNoContentNullEntity() throws IOException {
        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(HttpStatus.SC_NO_CONTENT, "No Content");
        httpResponse.setEntity(null);
        StubHttpClient stubClient = new StubHttpClient(httpResponse);

        RestRequest request = new RestRequest(stubClient)
                .delete()
                .url("https://api.example.com/data/1");

        RestResponse response = request.retrieve();
        assertInstanceOf(SuccessResponse.class, response);
        SuccessResponse successResponse = (SuccessResponse) response;
        assertEquals(HttpStatus.SC_NO_CONTENT, successResponse.getStatus());
        assertNull(successResponse.getData().getData());
    }

    @Test
    void testRetrieveFailureResponse() throws IOException {
        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(HttpStatus.SC_NOT_FOUND, "Not Found");
        httpResponse.setEntity(new StringEntity("Item not found", ContentType.TEXT_PLAIN));
        StubHttpClient stubClient = new StubHttpClient(httpResponse);

        RestRequest request = new RestRequest(stubClient)
                .get()
                .url("https://api.example.com/items/999");

        RestResponse response = request.retrieve();
        assertInstanceOf(FailureResponse.class, response);
        FailureResponse failureResponse = (FailureResponse) response;
        assertEquals(HttpStatus.SC_NOT_FOUND, failureResponse.getStatus());
        assertEquals("Item not found", failureResponse.getData().getData());
    }

    @Test
    void testRetrievePropagatesHeaders() throws IOException {
        BasicClassicHttpResponse httpResponse = new BasicClassicHttpResponse(HttpStatus.SC_CREATED, "Created");
        httpResponse.setEntity(new StringEntity("created", ContentType.TEXT_PLAIN));
        httpResponse.addHeader(HttpHeaders.LOCATION, "/items/123");
        httpResponse.addHeader(HttpHeaders.ETAG, "\"abc\"");

        StubHttpClient stubClient = new StubHttpClient(httpResponse);

        RestRequest request = new RestRequest(stubClient)
                .post()
                .url("https://api.example.com/items")
                .header("X-App-Id", "myApp")
                .basicAuth("user", "secret");

        RestResponse response = request.retrieve();
        assertInstanceOf(SuccessResponse.class, response);
        assertEquals(HttpStatus.SC_CREATED, response.getStatus());
        assertEquals("/items/123", response.getHeader(HttpHeaders.LOCATION));
        assertEquals("\"abc\"", response.getHeader(HttpHeaders.ETAG));

        ClassicHttpRequest captured = stubClient.getCapturedRequest();
        assertNotNull(captured);
        assertEquals("myApp", captured.getFirstHeader("X-App-Id").getValue());
        assertTrue(captured.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue().startsWith("Basic "));
    }

}
