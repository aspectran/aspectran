/*
 * Copyright 2008-2017 Juho Jeong
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

import static org.awaitility.Awaitility.await;

import java.util.Enumeration;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;

/**
 * Test cases for Session Handler.
 *
 * <p>Created: 2017. 9. 12.</p>
 */
public class SessionHandlerTest {

    private Log log = LogFactory.getLog(getClass());

    @Test
    public void testShortLifecycle() throws Exception {
        SessionHandler sessionHandler = new DefaultSessionHandler("TEST1-");
        sessionHandler.initialize();

        SessionAgent agent = sessionHandler.newSessionAgent();

        log.info("Created Session: " + agent.getSession(true));

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
    public void testInactivityTimer() throws Exception {
        SessionHandler sessionHandler = new DefaultSessionHandler("TEST2-");
        sessionHandler.setDefaultMaxIdleSecs(1);
        sessionHandler.initialize();

        SessionAgent agent = sessionHandler.newSessionAgent();

        log.info("Created Session: " + agent.getSession(true));

        await().atMost(2, TimeUnit.SECONDS).until(() -> sessionHandler.getSessionCache().getSessionsCurrent() == 0);
    }

}