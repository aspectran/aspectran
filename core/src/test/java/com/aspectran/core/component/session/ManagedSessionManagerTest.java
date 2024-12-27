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
package com.aspectran.core.component.session;

import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.utils.apon.AponLines;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManagedSessionManagerTest {

    @Test
    void testFileSessionStore() throws Exception {
        String workerName = "t0";

        File storeDir = new File("./target/_sessions/" + workerName);
        storeDir.mkdir();

        SessionManagerConfig sessionManagerConfig = new SessionManagerConfig(new AponLines()
                .line("workerName", workerName)
                .line("maxActiveSessions", "9999")
                .line("maxIdleSeconds", "489")
                .line("evictionIdleSeconds", "1")
                .line("scavengingIntervalSeconds", "2")
                .line("clusterEnabled", "false")
                .block("fileStore")
                .line("storeDir", storeDir.getCanonicalFile())
                .line("gracePeriodSeconds", 0)
                .end()
                .toString());

        DefaultSessionManager sessionManager = new DefaultSessionManager(workerName);
        sessionManager.setSessionManagerConfig(sessionManagerConfig);
        sessionManager.initialize();

        SessionHandler sessionHandler = sessionManager.getSessionHandler();
        sessionHandler.setDefaultMaxIdleSecs(1);

        SessionAgent agent = new SessionAgent(sessionManager);
        try {
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
        } finally {
            agent.complete();
        }

        await().atMost(10, TimeUnit.SECONDS).until(() -> {
            // System.out.println(sessionHandler.getStatistics().getActiveSessions());
            return sessionHandler.getStatistics().getNumberOfActives() == 0;
        });

        sessionManager.destroy();
    }

}
