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
package com.aspectran.utils.scheduling;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link ScheduledExecutorScheduler}.
 */
class ScheduledExecutorSchedulerTest {

    @Test
    void testLifecycle() {
        ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler("TestScheduler", true);
        assertFalse(scheduler.isRunning());
        assertEquals("TestScheduler", scheduler.getName());
        assertTrue(scheduler.isDaemon());
        assertEquals(1, scheduler.getThreads());

        scheduler.start();
        assertTrue(scheduler.isRunning());

        scheduler.stop();
        assertFalse(scheduler.isRunning());
    }

    @Test
    void testAutoCloseable() {
        ScheduledExecutorScheduler schedulerInstance;
        try (ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler("AutoCloseableScheduler", true)) {
            schedulerInstance = scheduler;
            scheduler.start();
            assertTrue(scheduler.isRunning());
        }
        assertFalse(schedulerInstance.isRunning());
    }

    @Test
    void testDoubleStartThrowsException() {
        try (ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler()) {
            scheduler.start();
            assertThrows(IllegalStateException.class, scheduler::start);
        }
    }

    @Test
    void testThreadCountConstructor() {
        try (ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler("MultiThreadScheduler", false, 4)) {
            assertEquals(4, scheduler.getThreads());
            scheduler.start();
            assertTrue(scheduler.isRunning());
        }
    }

    @Test
    void testBasicScheduling() throws Exception {
        try (ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler()) {
            scheduler.start();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger count = new AtomicInteger(0);

            Scheduler.Task task = scheduler.schedule(() -> {
                count.incrementAndGet();
                latch.countDown();
            }, 50, TimeUnit.MILLISECONDS);

            assertNotNull(task);
            assertFalse(task.isCancelled());
            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(1, count.get());

            for (int i = 0; i < 50 && !task.isDone(); i++) {
                Thread.sleep(10);
            }
            assertTrue(task.isDone());
        }
    }

    @Test
    void testDurationScheduling() throws Exception {
        try (ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler()) {
            scheduler.start();

            CountDownLatch latch = new CountDownLatch(1);
            AtomicInteger count = new AtomicInteger(0);

            Scheduler.Task task = scheduler.schedule(() -> {
                count.incrementAndGet();
                latch.countDown();
            }, Duration.ofMillis(50));

            assertNotNull(task);
            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertEquals(1, count.get());

            for (int i = 0; i < 50 && !task.isDone(); i++) {
                Thread.sleep(10);
            }
            assertTrue(task.isDone());
        }
    }

    @Test
    void testTaskCancellation() throws Exception {
        try (ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler()) {
            scheduler.start();

            AtomicInteger count = new AtomicInteger(0);

            Scheduler.Task task = scheduler.schedule(count::incrementAndGet, 200, TimeUnit.MILLISECONDS);
            assertFalse(task.isDone());
            assertFalse(task.isCancelled());

            boolean cancelled = task.cancel();
            assertTrue(cancelled);
            assertTrue(task.isCancelled());

            Thread.sleep(300);
            assertEquals(0, count.get());
        }
    }

    @Test
    void testTaskCancellationWithInterrupt() throws Exception {
        try (ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler()) {
            scheduler.start();

            CountDownLatch taskStarted = new CountDownLatch(1);
            AtomicBoolean interrupted = new AtomicBoolean(false);

            Scheduler.Task task = scheduler.schedule(() -> {
                taskStarted.countDown();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    interrupted.set(true);
                }
            }, 0, TimeUnit.MILLISECONDS, true);

            assertTrue(taskStarted.await(2, TimeUnit.SECONDS));
            task.cancel();

            Thread.sleep(200);
            assertTrue(interrupted.get());
        }
    }

    @Test
    void testNoOpTaskWhenNotStarted() {
        ScheduledExecutorScheduler scheduler = new ScheduledExecutorScheduler();
        Scheduler.Task task = scheduler.schedule(() -> {}, 10, TimeUnit.MILLISECONDS);

        assertNotNull(task);
        assertFalse(task.cancel());
        assertTrue(task.isCancelled());
        assertTrue(task.isDone());
        assertEquals("NoOpTask", task.toString());
    }

}
