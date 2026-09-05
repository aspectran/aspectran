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
package com.aspectran.netty.support;

import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionListener;
import com.aspectran.core.component.session.SessionManager;
import com.aspectran.core.context.config.AspectranConfig;
import com.aspectran.core.service.CoreService;
import com.aspectran.embed.service.EmbeddedAspectran;
import com.aspectran.netty.server.NettyServer;
import com.aspectran.utils.ResourceUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link SessionListenerRegistrationBean} and session manager lookup in {@link NettyServer}.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SessionListenerRegistrationBeanTest {

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
    void testNettyServerSessionManagerLookup() {
        SessionManager rootSessionManager = nettyServer.getSessionManager();
        assertNotNull(rootSessionManager, "Root session manager should not be null");

        SessionManager rootByPath = nettyServer.getSessionManager("/");
        assertNotNull(rootByPath, "Session manager for '/' should not be null");
        assertEquals(rootSessionManager, rootByPath);

        SessionManager rootByName = nettyServer.getSessionManager("root");
        assertNotNull(rootByName, "Session manager for 'root' should not be null");
        assertEquals(rootSessionManager, rootByName);

        SessionManager adminSessionManager = nettyServer.getSessionManager("/admin");
        assertNotNull(adminSessionManager, "Admin session manager should not be null");

        SessionManager adminByPath = nettyServer.getSessionManagerByPath("/admin");
        assertNotNull(adminByPath, "Admin session manager by path should not be null");
        assertEquals(adminSessionManager, adminByPath);

        SessionManager nonExistent = nettyServer.getSessionManager("/nonexistent");
        assertNull(nonExistent, "Nonexistent context path should return null");
    }

    @Test
    void testRegisterAndRemoveRootSessionListener() throws IOException {
        SessionListenerRegistrationBean registrationBean = new SessionListenerRegistrationBean();
        registrationBean.setActivityContext(((CoreService) aspectran).getActivityContext());

        CountingSessionListener listener = new CountingSessionListener();
        registrationBean.register(listener);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet req1 = new HttpGet("http://127.0.0.1:" + port + "/session");
            httpClient.execute(req1, response -> {
                assertEquals(200, response.getCode());
                return null;
            });
        }

        assertEquals(1, listener.createdCount.get(), "SessionCreated event should have been fired once");

        // Remove listener
        registrationBean.remove(listener);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet req2 = new HttpGet("http://127.0.0.1:" + port + "/session");
            httpClient.execute(req2, response -> {
                assertEquals(200, response.getCode());
                return null;
            });
        }

        assertEquals(1, listener.createdCount.get(), "SessionCreated event should not be fired after listener is removed");
    }

    @Test
    void testRegisterAndRemoveAdminSessionListener() throws IOException {
        SessionListenerRegistrationBean registrationBean = new SessionListenerRegistrationBean(null, "/admin");
        registrationBean.setActivityContext(((CoreService) aspectran).getActivityContext());

        CountingSessionListener listener = new CountingSessionListener();
        registrationBean.register(listener);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet req = new HttpGet("http://127.0.0.1:" + port + "/admin/session");
            httpClient.execute(req, response -> {
                assertEquals(200, response.getCode());
                return null;
            });
        }

        assertEquals(1, listener.createdCount.get(), "Admin sessionCreated event should have been fired once");

        registrationBean.remove(listener);
    }

    @Test
    void testRegisterWithExplicitDeploymentName() throws IOException {
        SessionListenerRegistrationBean registrationBean = new SessionListenerRegistrationBean();
        registrationBean.setActivityContext(((CoreService) aspectran).getActivityContext());

        CountingSessionListener listener = new CountingSessionListener();
        registrationBean.register(listener, "/admin");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet req = new HttpGet("http://127.0.0.1:" + port + "/admin/session");
            httpClient.execute(req, response -> {
                assertEquals(200, response.getCode());
                return null;
            });
        }

        assertEquals(1, listener.createdCount.get(), "SessionCreated should fire for explicitly specified /admin context");

        registrationBean.remove(listener, "/admin");
    }

    @Test
    void testRegisterWithRootContextName() throws IOException {
        SessionListenerRegistrationBean registrationBean = new SessionListenerRegistrationBean();
        registrationBean.setActivityContext(((CoreService) aspectran).getActivityContext());

        CountingSessionListener listener = new CountingSessionListener();
        registrationBean.register(listener, "root");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet req = new HttpGet("http://127.0.0.1:" + port + "/session");
            httpClient.execute(req, response -> {
                assertEquals(200, response.getCode());
                return null;
            });
        }

        assertEquals(1, listener.createdCount.get(), "SessionCreated should fire when registered with context name 'root'");

        registrationBean.remove(listener, "root");
    }

    @Test
    void testRegisterWithNettyServerId() throws IOException {
        SessionListenerRegistrationBean registrationBean = new SessionListenerRegistrationBean("netty.server", "/admin");
        registrationBean.setActivityContext(((CoreService) aspectran).getActivityContext());

        CountingSessionListener listener = new CountingSessionListener();
        registrationBean.register(listener);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet req = new HttpGet("http://127.0.0.1:" + port + "/admin/session");
            httpClient.execute(req, response -> {
                assertEquals(200, response.getCode());
                return null;
            });
        }

        assertEquals(1, listener.createdCount.get(), "SessionCreated should fire when nettyServerId is explicitly provided");

        registrationBean.remove(listener);
    }

    private static class CountingSessionListener implements SessionListener {

        final AtomicInteger createdCount = new AtomicInteger();
        final AtomicInteger destroyedCount = new AtomicInteger();

        @Override
        public void sessionCreated(Session session) {
            createdCount.incrementAndGet();
        }

        @Override
        public void sessionDestroyed(Session session) {
            destroyedCount.incrementAndGet();
        }

    }

}
