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
package com.aspectran.utils.concurrent;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link AutoLock}.
 */
class AutoLockTest {

    @Test
    void testBasicLockAndUnlock() {
        AutoLock autoLock = new AutoLock();
        assertFalse(autoLock.isLocked());
        assertFalse(autoLock.isHeldByCurrentThread());

        try (AutoLock lock = autoLock.lock()) {
            assertNotNull(lock);
            assertTrue(autoLock.isLocked());
            assertTrue(autoLock.isHeldByCurrentThread());
            assertEquals(1, autoLock.getHoldCount());
        }

        assertFalse(autoLock.isLocked());
        assertFalse(autoLock.isHeldByCurrentThread());
        assertEquals(0, autoLock.getHoldCount());
    }

    @Test
    void testFairness() {
        AutoLock nonFairLock = new AutoLock();
        assertFalse(nonFairLock.isFair());

        AutoLock fairLock = new AutoLock(true);
        assertTrue(fairLock.isFair());

        AutoLock.WithCondition nonFairCond = new AutoLock.WithCondition();
        assertFalse(nonFairCond.isFair());

        AutoLock.WithCondition fairCond = new AutoLock.WithCondition(true);
        assertTrue(fairCond.isFair());
    }

    @Test
    void testReentrancy() {
        AutoLock autoLock = new AutoLock();

        try (AutoLock lock1 = autoLock.lock()) {
            assertEquals(1, autoLock.getHoldCount());
            try (AutoLock lock2 = autoLock.lock()) {
                assertEquals(2, autoLock.getHoldCount());
                assertTrue(autoLock.isHeldByCurrentThread());
            }
            assertEquals(1, autoLock.getHoldCount());
            assertTrue(autoLock.isHeldByCurrentThread());
        }

        assertEquals(0, autoLock.getHoldCount());
        assertFalse(autoLock.isLocked());
    }

    @Test
    void testTryLockSuccess() {
        AutoLock autoLock = new AutoLock();

        try (AutoLock lock = autoLock.tryLock()) {
            assertNotNull(lock);
            assertTrue(autoLock.isLocked());
            assertTrue(autoLock.isHeldByCurrentThread());
        }

        assertFalse(autoLock.isLocked());
    }

    @Test
    void testTryLockFailure() throws Exception {
        AutoLock autoLock = new AutoLock();
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch finishThread = new CountDownLatch(1);

        Thread holdingThread = new Thread(() -> {
            try (AutoLock lock = autoLock.lock()) {
                lockAcquired.countDown();
                finishThread.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        holdingThread.start();

        assertTrue(lockAcquired.await(2, TimeUnit.SECONDS));

        try (AutoLock lock = autoLock.tryLock()) {
            assertNull(lock);
        }

        try (AutoLock lock = autoLock.tryLock(50, TimeUnit.MILLISECONDS)) {
            assertNull(lock);
        }

        try (AutoLock lock = autoLock.tryLock(Duration.ofMillis(50))) {
            assertNull(lock);
        }

        finishThread.countDown();
        holdingThread.join();

        assertFalse(autoLock.isLocked());
    }

    @Test
    void testLockInterruptibly() throws Exception {
        AutoLock autoLock = new AutoLock();
        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);

        Thread holder = new Thread(() -> {
            try (AutoLock lock = autoLock.lock()) {
                lockAcquired.countDown();
                releaseLock.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        holder.start();
        assertTrue(lockAcquired.await(2, TimeUnit.SECONDS));

        AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread waitingThread = new Thread(() -> {
            try {
                autoLock.lockInterruptibly();
            } catch (InterruptedException e) {
                interrupted.set(true);
            }
        });
        waitingThread.start();

        Thread.sleep(100);
        waitingThread.interrupt();
        waitingThread.join(2000);

        assertTrue(interrupted.get());

        releaseLock.countDown();
        holder.join();
    }

    @Test
    void testMutualExclusion() throws Exception {
        AutoLock autoLock = new AutoLock();
        int threadCount = 10;
        int incrementsPerThread = 1000;
        AtomicInteger counter = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < incrementsPerThread; j++) {
                        try (AutoLock lock = autoLock.lock()) {
                            int current = counter.get();
                            counter.set(current + 1);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(10, TimeUnit.SECONDS));
        assertEquals(threadCount * incrementsPerThread, counter.get());
    }

    @Test
    void testWithConditionAwaitAndSignal() throws Exception {
        AutoLock.WithCondition lock = new AutoLock.WithCondition();
        AtomicBoolean conditionMet = new AtomicBoolean(false);
        CountDownLatch readyToWait = new CountDownLatch(1);

        Thread waiter = new Thread(() -> {
            try (AutoLock.WithCondition l = lock.lock()) {
                readyToWait.countDown();
                while (!conditionMet.get()) {
                    l.await();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        waiter.start();

        assertTrue(readyToWait.await(2, TimeUnit.SECONDS));

        // Let waiter enter await
        Thread.sleep(100);

        try (AutoLock.WithCondition l = lock.lock()) {
            conditionMet.set(true);
            l.signal();
        }

        waiter.join(2000);
        assertFalse(waiter.isAlive());
    }

    @Test
    void testWithConditionAwaitTimeout() throws Exception {
        AutoLock.WithCondition lock = new AutoLock.WithCondition();

        try (AutoLock.WithCondition l = lock.lock()) {
            boolean signaled = l.await(50, TimeUnit.MILLISECONDS);
            assertFalse(signaled);

            boolean signaledDuration = l.await(Duration.ofMillis(50));
            assertFalse(signaledDuration);

            long remainingNanos = l.awaitNanos(TimeUnit.MILLISECONDS.toNanos(50));
            assertTrue(remainingNanos <= 0);

            boolean signaledDate = l.awaitUntil(new Date(System.currentTimeMillis() + 50));
            assertFalse(signaledDate);

            boolean signaledInstant = l.awaitUntil(Instant.now().plusMillis(50));
            assertFalse(signaledInstant);
        }
    }

    @Test
    void testWithConditionSignalAll() throws Exception {
        AutoLock.WithCondition lock = new AutoLock.WithCondition();
        int waiterCount = 3;
        CountDownLatch readyLatch = new CountDownLatch(waiterCount);
        CountDownLatch finishedLatch = new CountDownLatch(waiterCount);
        AtomicBoolean ready = new AtomicBoolean(false);

        for (int i = 0; i < waiterCount; i++) {
            new Thread(() -> {
                try (AutoLock.WithCondition l = lock.lock()) {
                    readyLatch.countDown();
                    while (!ready.get()) {
                        l.await();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finishedLatch.countDown();
                }
            }).start();
        }

        assertTrue(readyLatch.await(2, TimeUnit.SECONDS));
        Thread.sleep(100);

        try (AutoLock.WithCondition l = lock.lock()) {
            ready.set(true);
            l.signalAll();
        }

        assertTrue(finishedLatch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testWithConditionTryLock() {
        AutoLock.WithCondition lock = new AutoLock.WithCondition();

        try (AutoLock.WithCondition l = lock.tryLock()) {
            assertNotNull(l);
            assertTrue(lock.isLocked());
            assertNotNull(l.getCondition());
        }

        assertFalse(lock.isLocked());
    }

}
