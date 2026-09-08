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

import com.aspectran.core.context.config.SessionManagerConfig;
import com.aspectran.utils.apon.AponLines;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link DefaultSessionManager}, focusing on new session lifecycle
 * management including:
 * <ul>
 *   <li>New session expiration via {@code maxIdleSecondsForNew}</li>
 *   <li>New session eviction from cache via {@code evictionIdleSecondsForNew}</li>
 *   <li>Promotion from new session to normal session via {@code access()}</li>
 *   <li>Normal session expiration via {@code maxIdleSeconds}</li>
 *   <li>Eviction to store and re-loading for new sessions</li>
 * </ul>
 *
 * <p>Created: 2026. 9. 8.</p>
 */
class DefaultSessionManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSessionManagerTest.class);

    private DefaultSessionManager sessionManager;

    @AfterEach
    void tearDown() {
        if (sessionManager != null) {
            sessionManager.destroy();
            sessionManager = null;
        }
    }

    /**
     * A new session that is never accessed again should expire based on
     * {@code maxIdleSecondsForNew} rather than the longer {@code maxIdleSeconds}.
     *
     * <p>Configuration:
     * <ul>
     *   <li>maxIdleSeconds = 30 (normal sessions live 30 seconds)</li>
     *   <li>maxIdleSecondsForNew = 1 (new sessions expire after 1 second)</li>
     *   <li>scavengingIntervalSeconds = 1 (check every second)</li>
     * </ul>
     *
     * <p>Expected: The new session expires within ~2 seconds (maxIdleSecondsForNew + scavenging),
     * NOT after 30 seconds.
     */
    @Test
    void testNewSessionExpirationByMaxIdleSecondsForNew() throws Exception {
        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test1")
                .line("maxIdleSeconds", 30)
                .line("maxIdleSecondsForNew", 1)
                .line("scavengingIntervalSeconds", 1)
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        TrackingSessionListener listener = new TrackingSessionListener();
        sessionManager.addSessionListener(listener);

        // Create a new session and immediately complete the request
        SessionAgent agent = new SessionAgent(sessionManager);
        Session session = agent.getSession(true);
        assertNotNull(session);
        assertTrue(session.isNew(), "Session should be new");
        String sessionId = session.getId();
        logger.info("Created new session id={}, maxInactiveInterval={}s",
                sessionId, session.getMaxInactiveInterval());

        // The maxInactiveInterval for a new session should be reduced to maxIdleSecondsForNew
        assertEquals(1, session.getMaxInactiveInterval(),
                "New session should have reduced maxInactiveInterval");

        agent.complete();

        // Wait for the new session to expire (maxIdleSecondsForNew=1s + scavenging margin)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertNull(sessionManager.getSession(sessionId),
                    "New session should have expired via maxIdleSecondsForNew");
        });

        assertTrue(listener.destroyedSessionIds.contains(sessionId),
                "SessionListener.sessionDestroyed should have been called");
        logger.info("New session {} expired as expected via maxIdleSecondsForNew", sessionId);
    }

    /**
     * When a new session is accessed a second time (via {@code access()}), it should be
     * promoted to a normal session. The {@code inactiveInterval} should be restored to the
     * full {@code maxIdleSeconds} value, and the session should survive beyond the
     * {@code maxIdleSecondsForNew} timeout.
     */
    @Test
    void testNewSessionPromotionToNormalSession() throws Exception {
        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test2")
                .line("maxIdleSeconds", 10)
                .line("maxIdleSecondsForNew", 1)
                .line("scavengingIntervalSeconds", 1)
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        // Create a new session
        SessionAgent agent = new SessionAgent(sessionManager);
        Session session = agent.getSession(true);
        assertNotNull(session);
        assertTrue(session.isNew(), "Session should be new initially");
        String sessionId = session.getId();

        assertEquals(1, session.getMaxInactiveInterval(),
                "New session maxInactiveInterval should be 1s (maxIdleSecondsForNew)");

        agent.complete();

        // Simulate a second request: access() promotes the session to normal
        boolean accessed = session.access();
        assertTrue(accessed, "access() should succeed for a valid session");
        assertFalse(session.isNew(), "Session should no longer be new after access()");

        assertEquals(10, session.getMaxInactiveInterval(),
                "After promotion, maxInactiveInterval should be restored to maxIdleSeconds (10s)");

        session.complete();

        // Wait beyond maxIdleSecondsForNew (1s) to verify the session survives
        TimeUnit.MILLISECONDS.sleep(1500);

        Session retrieved = sessionManager.getSession(sessionId);
        assertNotNull(retrieved, "Promoted session should survive beyond maxIdleSecondsForNew");
        logger.info("Promoted session {} survived beyond maxIdleSecondsForNew as expected", sessionId);
    }

    /**
     * After a session is promoted to a normal session, it should expire based on
     * the full {@code maxIdleSeconds} timeout when no further access occurs.
     */
    @Test
    void testNormalSessionExpirationAfterPromotion() throws Exception {
        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test3")
                .line("maxIdleSeconds", 2)
                .line("maxIdleSecondsForNew", 1)
                .line("scavengingIntervalSeconds", 1)
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        TrackingSessionListener listener = new TrackingSessionListener();
        sessionManager.addSessionListener(listener);

        // Create and promote a session
        SessionAgent agent = new SessionAgent(sessionManager);
        Session session = agent.getSession(true);
        String sessionId = session.getId();
        agent.complete();

        // Promote to normal by accessing it again
        session.access();
        assertFalse(session.isNew());
        assertEquals(2, session.getMaxInactiveInterval(),
                "Normal session maxInactiveInterval should be maxIdleSeconds (2s)");
        session.complete();

        // Wait for the normal session to expire (maxIdleSeconds=2s + scavenging margin)
        await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
            assertNull(sessionManager.getSession(sessionId),
                    "Normal session should have expired via maxIdleSeconds");
        });

        assertTrue(listener.destroyedSessionIds.contains(sessionId),
                "SessionListener.sessionDestroyed should have been called for expired normal session");
        logger.info("Normal session {} expired as expected via maxIdleSeconds", sessionId);
    }

    /**
     * Tests that a new session is evicted from the in-memory cache (but NOT expired) based on
     * {@code evictionIdleSecondsForNew}. When a session store is configured, the session data
     * should be saved to the store before eviction, and retrievable upon the next access.
     *
     * <p>Configuration:
     * <ul>
     *   <li>maxIdleSeconds = 30 (long enough to not expire during the test)</li>
     *   <li>maxIdleSecondsForNew = 15 (long enough to not expire during the test)</li>
     *   <li>evictionIdleSeconds = 10</li>
     *   <li>evictionIdleSecondsForNew = 1 (evict from cache after 1 second)</li>
     *   <li>saveOnInactiveEviction = true</li>
     * </ul>
     */
    @Test
    void testNewSessionEvictionFromCacheToStore() throws Exception {
        File storeDir = new File("./target/_sessions/test4");
        storeDir.mkdirs();

        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test4")
                .line("maxIdleSeconds", 30)
                .line("maxIdleSecondsForNew", 15)
                .line("evictionIdleSeconds", 10)
                .line("evictionIdleSecondsForNew", 1)
                .line("scavengingIntervalSeconds", 1)
                .line("saveOnInactiveEviction", true)
                .block("fileStore")
                .line("storeDir", storeDir.getCanonicalFile())
                .line("gracePeriodSeconds", 0)
                .end()
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        TrackingSessionListener listener = new TrackingSessionListener();
        sessionManager.addSessionListener(listener);

        // Create a new session and set an attribute
        SessionAgent agent = new SessionAgent(sessionManager);
        Session session = agent.getSession(true);
        String sessionId = session.getId();
        session.setAttribute("testKey", "testValue");
        agent.complete();

        logger.info("Created new session id={}, waiting for cache eviction...", sessionId);

        // Wait for eviction from cache (evictionIdleSecondsForNew=1s)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(listener.evictedSessionIds.contains(sessionId),
                    "New session should have been evicted from cache");
        });

        assertFalse(sessionManager.getSessionCache().contains(sessionId),
                "Session should not be in the cache after eviction");

        // Now retrieve the session - it should be loaded from the store
        Session restored = sessionManager.getSession(sessionId);
        assertNotNull(restored, "Session should be restorable from the store after eviction");
        assertEquals("testValue", restored.getAttribute("testKey"),
                "Session attributes should be preserved after eviction and restore");

        logger.info("New session {} evicted from cache and restored from store successfully", sessionId);
    }

    /**
     * Tests the contrast between new session timeout and normal session timeout
     * to verify that the dual-timeout architecture works correctly.
     *
     * <p>Creates two sessions:
     * <ul>
     *   <li>Session A: left as a new session (simulating a bot/crawler)</li>
     *   <li>Session B: promoted to a normal session (simulating a real user)</li>
     * </ul>
     *
     * <p>Session A should expire quickly via maxIdleSecondsForNew while Session B
     * should survive longer via maxIdleSeconds.
     */
    @Test
    void testDualTimeoutNewVsNormalSession() throws Exception {
        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test5")
                .line("maxIdleSeconds", 10)
                .line("maxIdleSecondsForNew", 1)
                .line("scavengingIntervalSeconds", 1)
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        TrackingSessionListener listener = new TrackingSessionListener();
        sessionManager.addSessionListener(listener);

        // Session A: bot/crawler - never accessed again
        SessionAgent botAgent = new SessionAgent(sessionManager);
        Session botSession = botAgent.getSession(true);
        String botSessionId = botSession.getId();
        botAgent.complete();

        // Session B: real user - promoted to normal
        SessionAgent userAgent = new SessionAgent(sessionManager);
        Session userSession = userAgent.getSession(true);
        String userSessionId = userSession.getId();
        userAgent.complete();

        // Promote Session B by accessing it again
        userSession.access();
        userSession.complete();

        logger.info("Bot session id={}, User session id={}", botSessionId, userSessionId);

        // Wait for the bot session to expire (maxIdleSecondsForNew=1s)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertNull(sessionManager.getSession(botSessionId),
                    "Bot session should have expired via maxIdleSecondsForNew");
        });

        // User session should still be alive
        Session aliveSession = sessionManager.getSession(userSessionId);
        assertNotNull(aliveSession, "User session should still be alive after bot session expired");
        assertFalse(aliveSession.isNew(), "User session should not be new");

        logger.info("Dual-timeout verified: bot session expired, user session alive");
    }

    /**
     * Tests that a new session without {@code maxIdleSecondsForNew} configured
     * uses the default {@code maxIdleSeconds} timeout.
     */
    @Test
    void testNewSessionWithoutMaxIdleSecondsForNew() throws Exception {
        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test6")
                .line("maxIdleSeconds", 2)
                .line("scavengingIntervalSeconds", 1)
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        // Create a new session
        SessionAgent agent = new SessionAgent(sessionManager);
        Session session = agent.getSession(true);
        String sessionId = session.getId();

        // Without maxIdleSecondsForNew, the new session should use maxIdleSeconds
        assertEquals(2, session.getMaxInactiveInterval(),
                "Without maxIdleSecondsForNew, should use maxIdleSeconds");

        agent.complete();

        // The session should expire based on maxIdleSeconds (2s)
        await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> {
            assertNull(sessionManager.getSession(sessionId),
                    "New session should expire using default maxIdleSeconds");
        });

        logger.info("Session {} expired using default maxIdleSeconds as expected", sessionId);
    }

    /**
     * Tests that setting {@code maxIdleSecondsForNew} to a value greater than or equal to
     * {@code maxIdleSeconds} effectively means no reduction of the inactive interval for
     * new sessions.
     */
    @Test
    void testMaxIdleSecondsForNewGreaterThanMaxIdleSeconds() throws Exception {
        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test7")
                .line("maxIdleSeconds", 5)
                .line("maxIdleSecondsForNew", 10)
                .line("scavengingIntervalSeconds", 1)
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        SessionAgent agent = new SessionAgent(sessionManager);
        Session session = agent.getSession(true);

        // When maxIdleSecondsForNew >= maxIdleSeconds, no reduction should occur
        // because reduceInactiveInterval only reduces if the new value is smaller
        assertEquals(5, session.getMaxInactiveInterval(),
                "maxInactiveInterval should remain at maxIdleSeconds when maxIdleSecondsForNew is larger");

        agent.complete();
        logger.info("No reduction applied when maxIdleSecondsForNew >= maxIdleSeconds");
    }

    /**
     * Tests that multiple new sessions created by bots all expire quickly
     * while the session count reflects this cleanup.
     */
    @Test
    void testBulkNewSessionExpiration() throws Exception {
        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test8")
                .line("maxIdleSeconds", 30)
                .line("maxIdleSecondsForNew", 1)
                .line("scavengingIntervalSeconds", 1)
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        int botSessionCount = 10;

        // Simulate 10 bot hits - each creates a session and never comes back
        for (int i = 0; i < botSessionCount; i++) {
            SessionAgent agent = new SessionAgent(sessionManager);
            agent.getSession(true);
            agent.complete();
        }

        assertEquals(botSessionCount, sessionManager.getStatistics().getNumberOfActives(),
                "All bot sessions should be active initially");

        logger.info("Created {} bot sessions, waiting for expiration...", botSessionCount);

        // All should expire within maxIdleSecondsForNew + scavenging
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(0, sessionManager.getStatistics().getNumberOfActives(),
                    "All bot sessions should have expired");
        });

        logger.info("All {} bot sessions expired as expected", botSessionCount);
    }

    /**
     * Tests a complete lifecycle: create (new) → evict to store → access (promote) →
     * re-access with attributes → expire as normal session.
     *
     * <p>Key timing constraints:
     * <ul>
     *   <li>{@code maxIdleSecondsForNew (3)} &lt; {@code maxIdleSeconds (5)} so that
     *       the inactive interval is reduced and {@code evictionIdleSecsForNew} is used.</li>
     *   <li>{@code evictionIdleSecondsForNew (1)} triggers cache eviction before expiration.</li>
     * </ul>
     */
    @Test
    void testCompleteLifecycle() throws Exception {
        File storeDir = new File("./target/_sessions/test9");
        storeDir.mkdirs();

        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test9")
                .line("maxIdleSeconds", 5)
                .line("maxIdleSecondsForNew", 3)
                .line("evictionIdleSeconds", 10)
                .line("evictionIdleSecondsForNew", 1)
                .line("scavengingIntervalSeconds", 1)
                .line("saveOnCreate", true)
                .line("saveOnInactiveEviction", true)
                .block("fileStore")
                .line("storeDir", storeDir.getCanonicalFile())
                .line("gracePeriodSeconds", 0)
                .end()
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        TrackingSessionListener listener = new TrackingSessionListener();
        sessionManager.addSessionListener(listener);

        // Step 1: Create a new session (simulating first HTTP request)
        String sessionId = sessionManager.createSessionId();
        ManagedSession session = sessionManager.createSession(sessionId);
        assertNotNull(session);
        assertTrue(session.isNew());
        // maxIdleSecondsForNew (3) < maxIdleSeconds (5), so reduction occurs
        assertEquals(3, session.getMaxInactiveInterval());
        session.setAttribute("step", "created");
        session.complete();

        logger.info("Step 1: New session {} created", sessionId);

        // Step 2: Wait for eviction from cache (evictionIdleSecondsForNew=1s)
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(listener.evictedSessionIds.contains(sessionId),
                    "Session should be evicted from cache");
        });
        logger.info("Step 2: Session {} evicted from cache", sessionId);

        // Step 3: Second request arrives - session loaded from store and promoted
        Session loaded = sessionManager.getSession(sessionId);
        assertNotNull(loaded, "Session should be loadable from store after eviction");

        loaded.access();
        assertFalse(loaded.isNew(), "Session should be promoted to normal after access()");
        assertEquals(5, loaded.getMaxInactiveInterval(),
                "Promoted session should have full maxIdleSeconds");

        loaded.setAttribute("step", "promoted");
        loaded.complete();

        logger.info("Step 3: Session {} promoted to normal", sessionId);

        // Step 4: Wait for normal session to expire (maxIdleSeconds=5s)
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            assertNull(sessionManager.getSession(sessionId),
                    "Normal session should have expired via maxIdleSeconds");
        });

        assertTrue(listener.destroyedSessionIds.contains(sessionId),
                "Session should have been destroyed");
        logger.info("Step 4: Normal session {} expired. Full lifecycle complete.", sessionId);
    }

    /**
     * Tests that {@code isTempResident()} correctly identifies sessions that are
     * new and resident in cache (i.e., have a reduced inactive interval).
     */
    @Test
    void testTempResidentStatus() throws Exception {
        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test10")
                .line("maxIdleSeconds", 30)
                .line("maxIdleSecondsForNew", 5)
                .line("scavengingIntervalSeconds", 1)
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        String sessionId = sessionManager.createSessionId();
        ManagedSession session = sessionManager.createSession(sessionId);

        assertTrue(session.isTempResident(),
                "New session with reduced interval should be a temporary resident");

        session.complete();

        // Promote by accessing
        session.access();
        assertFalse(session.isTempResident(),
                "After promotion, session should no longer be a temporary resident");

        session.complete();

        // Promote by setting max inactive interval
        String sessionId2 = sessionManager.createSessionId();
        ManagedSession session2 = sessionManager.createSession(sessionId2);
        assertTrue(session2.isTempResident(),
                "New session with reduced interval should be a temporary resident");

        session2.setMaxInactiveInterval(1800);
        assertFalse(session2.isTempResident(),
                "After setMaxInactiveInterval, session should no longer be a temporary resident");
        assertEquals(1800, session2.getMaxInactiveInterval());

        session2.complete();

        logger.info("TempResident status transitions verified for session {} and {}", sessionId, sessionId2);
    }

    /**
     * Tests that the session statistics correctly track creation, active count,
     * and expiration across new and normal session lifecycles.
     */
    @Test
    void testSessionStatisticsTracking() throws Exception {
        SessionManagerConfig config = new SessionManagerConfig(new AponLines()
                .line("workerName", "test11")
                .line("maxIdleSeconds", 10)
                .line("maxIdleSecondsForNew", 1)
                .line("scavengingIntervalSeconds", 1)
                .toString());

        sessionManager = new DefaultSessionManager();
        sessionManager.setSessionManagerConfig(config);
        sessionManager.initialize();

        SessionStatistics stats = sessionManager.getStatistics();

        assertEquals(0, stats.getNumberOfActives(), "No active sessions initially");
        assertEquals(0, stats.getNumberOfCreated(), "No created sessions initially");

        // Create 3 new sessions (bots)
        for (int i = 0; i < 3; i++) {
            SessionAgent agent = new SessionAgent(sessionManager);
            agent.getSession(true);
            agent.complete();
        }

        assertEquals(3, stats.getNumberOfActives(), "3 active sessions");
        assertEquals(3, stats.getNumberOfCreated(), "3 total created sessions");

        // Wait for bot sessions to expire
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            assertEquals(0, stats.getNumberOfActives(), "All bot sessions should have expired");
        });

        assertTrue(stats.getNumberOfExpired() >= 3,
                "At least 3 sessions should have been recorded as expired");
        logger.info("Statistics: created={}, expired={}, actives={}",
                stats.getNumberOfCreated(), stats.getNumberOfExpired(), stats.getNumberOfActives());
    }

    // ========================================================================
    // Helper classes
    // ========================================================================

    /**
     * A {@link SessionListener} implementation that tracks session lifecycle events
     * for test assertions.
     */
    static class TrackingSessionListener implements SessionListener {

        final Set<String> createdSessionIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final Set<String> destroyedSessionIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final Set<String> evictedSessionIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
        final Set<String> residedSessionIds = Collections.newSetFromMap(new ConcurrentHashMap<>());

        @Override
        public void sessionCreated(Session session) {
            createdSessionIds.add(session.getId());
            logger.debug("Event: sessionCreated id={}", session.getId());
        }

        @Override
        public void sessionDestroyed(Session session) {
            destroyedSessionIds.add(session.getId());
            logger.debug("Event: sessionDestroyed id={}", session.getId());
        }

        @Override
        public void sessionEvicted(Session session) {
            evictedSessionIds.add(session.getId());
            logger.debug("Event: sessionEvicted id={}", session.getId());
        }

        @Override
        public void sessionResided(Session session) {
            residedSessionIds.add(session.getId());
            logger.debug("Event: sessionResided id={}", session.getId());
        }
    }

}
