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

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Created: 2019/12/07</p>
 */
class FileSessionStoreFactoryTest {

    @Test
    void testFileSessionStore() throws Exception {

        String workerName = "t1";

        File storeDir = new File("./target/_sessions/" + workerName);
        storeDir.mkdir();

        FileSessionStoreFactory sessionStoreFactory = new FileSessionStoreFactory();
        sessionStoreFactory.setStoreDir(storeDir.getCanonicalPath());

        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setSessionStore(sessionStoreFactory.createSessionStore());
        sessionManager.initialize();

        sessionManager.setDefaultMaxIdleSecs(1);

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

                TimeUnit.MILLISECONDS.sleep(100);
            }
        } finally {
            agent.complete();
        }

        await().atMost(3, TimeUnit.SECONDS).until(()
            -> sessionManager.getStatistics().getNumberOfActives() == 0);

        sessionManager.destroy();
    }

}
