/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.core.component.session.redis.lettuce;

import com.aspectran.core.component.session.DefaultSessionManager;
import com.aspectran.core.component.session.SessionAgent;
import com.aspectran.core.component.session.SessionHandler;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019/12/08</p>
 */
class DefaultLettuceSessionStoreFactoryTest {

    public static void main(String[] args) {
        RedisConnectionPoolConfig poolConfig = new RedisConnectionPoolConfig();
        poolConfig.setUri("redis://localhost:6379/0");

        DefaultSessionManager sessionManager = new DefaultSessionManager();
        try {
            DefaultLettuceSessionStoreFactory sessionStoreFactory = new DefaultLettuceSessionStoreFactory();
            sessionStoreFactory.setPoolConfig(poolConfig);
            sessionManager.setSessionStore(sessionStoreFactory.getSessionStore());
            sessionManager.initialize();

            SessionHandler sessionHandler = sessionManager.getSessionHandler();
            sessionHandler.setDefaultMaxIdleSecs(1);

            SessionAgent agent = new SessionAgent(sessionHandler);

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
                -> sessionHandler.getStatistics().getActiveSessions() == 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sessionManager.destroy();
        }
    }

}
