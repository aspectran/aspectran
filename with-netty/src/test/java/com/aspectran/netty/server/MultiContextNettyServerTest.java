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
package com.aspectran.netty.server;

import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.utils.ResourceUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for multi-context support in {@link NettyServer}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MultiContextNettyServerTest {

    private EmbeddedAspectran aspectran;

    private NettyServer nettyServer;

    private int port;

    @BeforeAll
    void ready() throws Exception {
        File configFile = ResourceUtils.getResourceAsFile("config/aspectran-multi-config.apon");
        AspectranConfig aspectranConfig = new AspectranConfig(configFile);

        aspectran = EmbeddedAspectran.run(aspectranConfig);
        nettyServer = aspectran.getBean("netty.server");
        nettyServer.start();

        port = nettyServer.getActivePort();
        assertTrue(port > 0, "Active port must be positive");
    }

    @AfterAll
    void finish() throws Exception {
        if (nettyServer != null && nettyServer.isRunning()) {
            nettyServer.stop();
        }
        if (aspectran != null) {
            aspectran.destroy();
        }
    }

    @Test
    void testRootContext() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://127.0.0.1:" + port + "/hello");
            httpClient.execute(request, response -> {
                assertEquals(200, response.getCode());
                String body = EntityUtils.toString(response.getEntity());
                assertTrue(body.contains("Hello from Root!"), "Expected root message, got: " + body);
                return null;
            });
        }
    }

    @Test
    void testApiContext() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://127.0.0.1:" + port + "/api/ping");
            httpClient.execute(request, response -> {
                assertEquals(200, response.getCode());
                String body = EntityUtils.toString(response.getEntity());
                assertTrue(body.contains("\"status\"") && body.contains("\"ok\""), "Expected status ok, got: " + body);
                return null;
            });

            HttpGet infoRequest = new HttpGet("http://127.0.0.1:" + port + "/api/info");
            httpClient.execute(infoRequest, response -> {
                assertEquals(200, response.getCode());
                String body = EntityUtils.toString(response.getEntity());
                assertTrue(body.contains("\"1.0.0\""), "Expected version 1.0.0, got: " + body);
                return null;
            });
        }
    }

    @Test
    void testAdminContext() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://127.0.0.1:" + port + "/admin/dashboard");
            httpClient.execute(request, response -> {
                assertEquals(200, response.getCode());
                String body = EntityUtils.toString(response.getEntity());
                assertTrue(body.contains("Admin Dashboard"), "Expected admin dashboard, got: " + body);
                return null;
            });
        }
    }

    @Test
    void testSessionIsolation() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Test root context session
            HttpGet rootSessionReq = new HttpGet("http://127.0.0.1:" + port + "/session");
            httpClient.execute(rootSessionReq, response -> {
                assertEquals(200, response.getCode());
                Header cookieHeader = response.getFirstHeader("Set-Cookie");
                assertNotNull(cookieHeader, "Root Set-Cookie header must be present");
                assertTrue(cookieHeader.getValue().contains("JSESSIONID="),
                        "Root cookie should be JSESSIONID: " + cookieHeader.getValue());
                assertTrue(cookieHeader.getValue().contains("Path=/"),
                        "Root cookie path should be /: " + cookieHeader.getValue());
                return null;
            });

            // Test admin context session
            HttpGet adminSessionReq = new HttpGet("http://127.0.0.1:" + port + "/admin/session");
            httpClient.execute(adminSessionReq, response -> {
                assertEquals(200, response.getCode());
                Header cookieHeader = response.getFirstHeader("Set-Cookie");
                assertNotNull(cookieHeader, "Admin Set-Cookie header must be present");
                assertTrue(cookieHeader.getValue().contains("ADMIN_SESSION_ID="),
                        "Admin cookie should be ADMIN_SESSION_ID: " + cookieHeader.getValue());
                assertTrue(cookieHeader.getValue().contains("Path=/admin"),
                        "Admin cookie path should be /admin: " + cookieHeader.getValue());
                return null;
            });
        }
    }

    @Test
    void testNotFoundInEachContext() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet req1 = new HttpGet("http://127.0.0.1:" + port + "/nonexistent");
            httpClient.execute(req1, response -> {
                assertEquals(404, response.getCode());
                return null;
            });

            HttpGet req2 = new HttpGet("http://127.0.0.1:" + port + "/api/nonexistent");
            httpClient.execute(req2, response -> {
                assertEquals(404, response.getCode());
                return null;
            });

            HttpGet req3 = new HttpGet("http://127.0.0.1:" + port + "/admin/nonexistent");
            httpClient.execute(req3, response -> {
                assertEquals(404, response.getCode());
                return null;
            });
        }
    }

}
