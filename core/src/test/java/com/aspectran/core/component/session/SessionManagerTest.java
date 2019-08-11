/*
 * Copyright (c) 2008-2019 The Aspectran Project
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

import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.core.context.config.SessionFileStoreConfig;
import com.aspectran.core.context.rule.type.SessionStoreType;
import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

/**
 * Test cases for Session Handler.
 *
 * <p>Created: 2017. 9. 12.</p>
 */
class SessionManagerTest {

    private Log log = LogFactory.getLog(getClass());

    @Test
    void testShortLifecycle() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setWorkerName("TEST1-");
        sessionManager.initialize();

        SessionAgent agent = new SessionAgent(sessionManager);

        log.info("Created Session " + agent.getSession(true));

        agent.setAttribute("attr1", "val-1");
        agent.setAttribute("attr2", "val-2");
        agent.setAttribute("attr3", "val-3");

        Enumeration<String> enumeration = agent.getAttributeNames();
        while(enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            log.info("getAttribute " + key + "=" + agent.getAttribute(key));
        }

        agent.complete();
        agent.invalidate();
    }

    @Test
    void testInactivityTimer() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setWorkerName("TEST2-");
        sessionManager.initialize();

        SessionHandler sessionHandler = sessionManager.getSessionHandler();
        sessionHandler.setDefaultMaxIdleSecs(1);

        SessionAgent agent = new SessionAgent(sessionManager);

        log.info("Created Session " + agent.getSession(true));

        await().atMost(2, TimeUnit.SECONDS).until(() -> sessionHandler.getSessionCache().getSessionsCurrent() == 0);
    }

    @Test
    void testFileSessionStore() throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setWorkerName("TEST3-");

        File storeDir = new File("./target/sessions");
        storeDir.mkdir();

        SessionManagerConfig sessionManagerConfig = new SessionManagerConfig();
        sessionManagerConfig.setStoreType(SessionStoreType.FILE);
        SessionFileStoreConfig fileStoreConfig = sessionManagerConfig.newFileStoreConfig();
        fileStoreConfig.setStoreDir(storeDir.getCanonicalPath());

        sessionManager.initialize();

        SessionHandler sessionHandler = sessionManager.getSessionHandler();
        sessionHandler.setDefaultMaxIdleSecs(3);

        for (int i = 0; i < 10; i++) {
            SessionAgent agent = new SessionAgent(sessionManager);

            log.info("=================================================");
            log.info("Created Session: " + agent.getSession(true));

            for (int j = 0; j <= i; j++) {
                agent.setAttribute("attr" + j, "val-" + j);
            }

            Enumeration<String> enumeration = agent.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                log.info("getAttribute " + key + "=" + agent.getAttribute(key));
            }

            TimeUnit.MILLISECONDS.sleep(30);

            agent.complete();
            //agent.invalidate();
        }

        await().atMost(15, TimeUnit.SECONDS).until(() -> sessionHandler.getSessionCache().getSessionsCurrent() == 0);
    }

}
