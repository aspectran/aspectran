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
package com.aspectran.utils.timer;

import com.aspectran.utils.scheduling.ScheduledExecutorScheduler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link CyclicTimeout}.
 */
class CyclicTimeoutTest {

    private ScheduledExecutorScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ScheduledExecutorScheduler();
        scheduler.start();
    }

    @AfterEach
    void tearDown() {
        if (scheduler != null && scheduler.isRunning()) {
            scheduler.stop();
        }
    }

    @Test
    void testBasicScheduleAndExpire() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger(0);

        CyclicTimeout timeout = new CyclicTimeout(scheduler) {
            @Override
            public void onTimeoutExpired() {
                count.incrementAndGet();
                latch.countDown();
            }
        };

        assertSame(scheduler, timeout.getScheduler());
        assertFalse(timeout.isScheduled());

        boolean replaced = timeout.schedule(100, TimeUnit.MILLISECONDS);
        assertFalse(replaced);
        assertTrue(timeout.isScheduled());
        assertTrue(timeout.getRemainingTimeout(TimeUnit.MILLISECONDS) > 0);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, count.get());
        assertFalse(timeout.isScheduled());
        assertEquals(-1, timeout.getRemainingTimeout(TimeUnit.MILLISECONDS));
    }

    @Test
    void testScheduleWithDuration() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger(0);

        CyclicTimeout timeout = new CyclicTimeout(scheduler) {
            @Override
            public void onTimeoutExpired() {
                count.incrementAndGet();
                latch.countDown();
            }
        };

        boolean replaced = timeout.schedule(Duration.ofMillis(100));
        assertFalse(replaced);
        assertTrue(timeout.isScheduled());

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, count.get());
    }

    @Test
    void testCancel() throws Exception {
        AtomicInteger count = new AtomicInteger(0);

        CyclicTimeout timeout = new CyclicTimeout(scheduler) {
            @Override
            public void onTimeoutExpired() {
                count.incrementAndGet();
            }
        };

        timeout.schedule(1000, TimeUnit.MILLISECONDS);
        assertTrue(timeout.isScheduled());

        boolean cancelled = timeout.cancel();
        assertTrue(cancelled);
        assertFalse(timeout.isScheduled());

        Thread.sleep(100);
        assertEquals(0, count.get());
    }

    @Test
    void testRescheduleExtending() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger(0);

        CyclicTimeout timeout = new CyclicTimeout(scheduler) {
            @Override
            public void onTimeoutExpired() {
                count.incrementAndGet();
                latch.countDown();
            }
        };

        // Schedule first for 1000ms
        timeout.schedule(1000, TimeUnit.MILLISECONDS);

        // Before it expires, extend to 1500ms
        Thread.sleep(50);
        boolean replaced = timeout.schedule(1500, TimeUnit.MILLISECONDS);
        assertTrue(replaced);

        // At 200ms mark, it should not have expired yet
        Thread.sleep(150);
        assertEquals(0, count.get());

        // Wait for extended timeout
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        assertEquals(1, count.get());
    }

    @Test
    void testRescheduleShortening() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger(0);

        CyclicTimeout timeout = new CyclicTimeout(scheduler) {
            @Override
            public void onTimeoutExpired() {
                count.incrementAndGet();
                latch.countDown();
            }
        };

        // Schedule first for 3000ms
        timeout.schedule(3000, TimeUnit.MILLISECONDS);

        // Shorten to 80ms
        Thread.sleep(50);
        boolean replaced = timeout.schedule(80, TimeUnit.MILLISECONDS);
        assertTrue(replaced);

        // Should expire quickly
        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, count.get());
    }

    @Test
    void testDestroy() {
        CyclicTimeout timeout = new CyclicTimeout(scheduler) {
            @Override
            public void onTimeoutExpired() {
            }
        };

        assertFalse(timeout.isDestroyed());
        timeout.schedule(500, TimeUnit.MILLISECONDS);

        timeout.destroy();
        assertTrue(timeout.isDestroyed());
        assertFalse(timeout.isScheduled());

        // Scheduling after destroy should return false and not schedule
        boolean scheduled = timeout.schedule(100, TimeUnit.MILLISECONDS);
        assertFalse(scheduled);
        assertFalse(timeout.isScheduled());
    }

    @Test
    void testExceptionInCallbackHandledSafely() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        CyclicTimeout timeout = new CyclicTimeout(scheduler) {
            @Override
            public void onTimeoutExpired() {
                latch.countDown();
                throw new RuntimeException("Simulated exception in callback");
            }
        };

        timeout.schedule(50, TimeUnit.MILLISECONDS);
        assertTrue(latch.await(2, TimeUnit.SECONDS));

        // Scheduler should remain functional
        CountDownLatch nextLatch = new CountDownLatch(1);
        scheduler.schedule(nextLatch::countDown, 50, TimeUnit.MILLISECONDS);
        assertTrue(nextLatch.await(2, TimeUnit.SECONDS));
    }

}
