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
class LettuceSessionStoreFactoryTest {

    public static void main(String args[]) throws Exception {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        sessionManager.setWorkerName("lettuce");

        LettucePoolConfig poolConfig = new LettucePoolConfig();
        poolConfig.setUri("redis://localhost:6379/0");

        LettuceSessionStoreFactory sessionStoreFactory = new LettuceSessionStoreFactory();
        sessionStoreFactory.setPoolConfig(poolConfig);
        sessionManager.setSessionStoreFactory(sessionStoreFactory);

        sessionManager.initialize();

        SessionHandler sessionHandler = sessionManager.getSessionHandler();
        sessionHandler.setDefaultMaxIdleSecs(1);

        SessionAgent agent = new SessionAgent(sessionManager);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j <= i; j++) {
                agent.setAttribute("key-" + j, "val-" + j);
            }

            Enumeration<String> enumeration = agent.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                String key = enumeration.nextElement();
                String val = agent.getAttribute(key);
                assertEquals(key, "key" + val.substring(val.indexOf('-')));
            }

            TimeUnit.MILLISECONDS.sleep(30);
        }
        agent.complete();

        await().atMost(3, TimeUnit.SECONDS).until(() -> sessionHandler.getSessionCache().getActiveSessionCount() == 0);

        sessionManager.destroy();
    }

}