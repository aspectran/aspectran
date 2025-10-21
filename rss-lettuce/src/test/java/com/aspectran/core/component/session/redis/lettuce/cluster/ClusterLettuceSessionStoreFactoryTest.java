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
package com.aspectran.core.component.session.redis.lettuce.cluster;

import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.Session;
import com.aspectran.core.component.session.SessionAgent;
import com.redis.testcontainers.RedisClusterContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import static com.redis.testcontainers.RedisClusterContainer.DEFAULT_IMAGE_NAME;
import static com.redis.testcontainers.RedisClusterContainer.DEFAULT_TAG;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * <p>Created: 2019/12/21</p>
 */
@Testcontainers(disabledWithoutDocker = true)
class ClusterLettuceSessionStoreFactoryTest {

    private static RedisClusterContainer redisCluster;

    private static RedisClusterConnectionPoolConfig poolConfig;

    private DefaultSessionManager sessionManager;

    @BeforeAll
    static void startContainer() {
        redisCluster = new RedisClusterContainer(DEFAULT_IMAGE_NAME.withTag(DEFAULT_TAG));
        redisCluster.start();

        poolConfig = new RedisClusterConnectionPoolConfig();
        poolConfig.setNodes(redisCluster.getRedisURIs());
    }

    @AfterAll
    static void stopContainer() {
        if (redisCluster != null) {
            redisCluster.stop();
        }
    }

    @BeforeEach
    void beforeEach() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        ClusterLettuceSessionStoreFactory sessionStoreFactory = new ClusterLettuceSessionStoreFactory();
        sessionStoreFactory.setPoolConfig(poolConfig);
        sessionManager.setSessionStore(sessionStoreFactory.createSessionStore());
        sessionManager.initialize();
        this.sessionManager = sessionManager;
    }

    @AfterEach
    void afterEach() {
        if (sessionManager != null && sessionManager.isInitialized()) {
            sessionManager.destroy();
        }
    }

    @Test
    void testSessionCreationAndAttributes() throws Exception {
        sessionManager.setDefaultMaxIdleSecs(2);

        SessionAgent agent = new SessionAgent(sessionManager);
        assertNotNull(agent.getSession(true));
        assertTrue(agent.isNew());

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j <= i; j++) {
                agent.setAttribute("key-" + j, "val-" + j);
            }

            Enumeration<String> enumer = agent.getAttributeNames();
            while (enumer.hasMoreElements()) {
                String key = enumer.nextElement();
                String val = agent.getAttribute(key);
                assertEquals(key, "key" + val.substring(val.indexOf('-')));
            }

            TimeUnit.MILLISECONDS.sleep(30);
        }
        agent.complete();

        await().atMost(3, TimeUnit.SECONDS).until(()
                -> sessionManager.getStatistics().getNumberOfActives() == 0);
    }

    @Test
    void testSessionInvalidation() {
        SessionAgent agent = new SessionAgent(sessionManager);
        String id = agent.getId();
        agent.setAttribute("test", "test");
        agent.invalidate();
        agent.complete();

        Session session = sessionManager.getSession(id);
        assertNull(session);
    }

    @Test
    void testSessionPersistence() {
        SessionAgent agent1 = new SessionAgent(sessionManager);
        agent1.setAttribute("name", "aspectran");
        String id = agent1.getId();
        agent1.complete();

        Session session2 = sessionManager.getSession(id);
        assertNotNull(session2);
        session2.access();
        assertFalse(session2.isNew());
        assertEquals("aspectran", session2.getAttribute("name"));
        session2.complete();
    }

    @Test
    void testAttributeRemoval() {
        SessionAgent agent = new SessionAgent(sessionManager);
        agent.setAttribute("attr1", "value1");
        agent.setAttribute("attr2", "value2");
        agent.removeAttribute("attr1");

        assertNull(agent.getAttribute("attr1"));
        assertNotNull(agent.getAttribute("attr2"));
        agent.complete();
    }

    @Test
    void testSessionExpiration() {
        sessionManager.setDefaultMaxIdleSecs(1);

        SessionAgent agent = new SessionAgent(sessionManager);
        String id = agent.getId();
        agent.setAttribute("key", "value");
        agent.complete();

        await().atMost(2, TimeUnit.SECONDS).until(() -> sessionManager.getSession(id) == null);

        Session newSession = sessionManager.getSession(id);
        assertNull(newSession);
    }

    @Test
    void testMultipleSessions() {
        SessionAgent agent1 = new SessionAgent(sessionManager);
        agent1.setAttribute("val", "1");
        String id1 = agent1.getId();
        agent1.complete();

        SessionAgent agent2 = new SessionAgent(sessionManager);
        agent2.setAttribute("val", "2");
        String id2 = agent2.getId();
        agent2.complete();

        Session session1 = sessionManager.getSession(id1);
        assertNotNull(session1);
        assertEquals("1", session1.getAttribute("val"));
        session1.access();
        session1.complete();

        Session session2 = sessionManager.getSession(id2);
        assertNotNull(session2);
        assertEquals("2", session2.getAttribute("val"));
        session2.access();
        session2.complete();
    }

}
