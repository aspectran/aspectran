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
package com.aspectran.core.component.session;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Test cases for Session Handler.
 *
 * <p>Created: 2017. 9. 12.</p>
 */
class SessionManagerTest {

    private final Logger logger = LoggerFactory.getLogger(SessionManagerTest.class);

    @Test
    void testShortLifecycle() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.initialize();

        SessionAgent agent = new SessionAgent(sessionManager);

        logger.info("Created Session {}", agent.getSession(true));

        agent.setAttribute("attr1", "val-1");
        agent.setAttribute("attr2", "val-2");
        agent.setAttribute("attr3", "val-3");

        Enumeration<String> enumer = agent.getAttributeNames();
        while(enumer.hasMoreElements()) {
            String key = enumer.nextElement();
            logger.info("getAttribute {}={}", key, agent.getAttribute(key));
        }

        agent.complete();
        agent.invalidate();

        sessionManager.destroy();
    }

    @Test
    void testInactivityTimer() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.initialize();

        sessionManager.setDefaultMaxIdleSecs(1);

        SessionAgent agent = new SessionAgent(sessionManager);

        logger.info("Created Session {}", agent.getSession(true));

        agent.complete();

        await().atMost(2, TimeUnit.SECONDS).until(()
            -> sessionManager.getStatistics().getNumberOfActives() == 0);

        sessionManager.destroy();
    }

}
