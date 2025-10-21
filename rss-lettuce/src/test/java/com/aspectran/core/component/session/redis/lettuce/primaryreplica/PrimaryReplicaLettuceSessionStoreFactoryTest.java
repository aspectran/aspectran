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
package com.aspectran.core.component.session.redis.lettuce.primaryreplica;

import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019/12/24</p>
 */
@Testcontainers(disabledWithoutDocker = true)
class PrimaryReplicaLettuceSessionStoreFactoryTest {

    private static RedisContainer primary;

    private static RedisContainer replica;

    private static RedisPrimaryReplicaConnectionPoolConfig poolConfig;

    private DefaultSessionManager sessionManager;

    @BeforeAll
    static void startContainer() {
        primary = new RedisContainer(DockerImageName.parse("redis:7-alpine"));
        primary.start();
        replica = new RedisContainer(DockerImageName.parse("redis:7-alpine"));
        replica.start();

        poolConfig = new RedisPrimaryReplicaConnectionPoolConfig();
        poolConfig.setNodes(new String[] { primary.getRedisURI(), replica.getRedisURI() });
    }

    @AfterAll
    static void stopContainer() {
        if (primary != null) {
            primary.stop();
        }
        if (replica != null) {
            replica.stop();
        }
    }

    @BeforeEach
    void beforeEach() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        PrimaryReplicaLettuceSessionStoreFactory sessionStoreFactory = new PrimaryReplicaLettuceSessionStoreFactory();
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
    void testPrimaryReplica() throws Exception {
        sessionManager.setDefaultMaxIdleSecs(2);

        SessionAgent agent = new SessionAgent(sessionManager);

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

}
