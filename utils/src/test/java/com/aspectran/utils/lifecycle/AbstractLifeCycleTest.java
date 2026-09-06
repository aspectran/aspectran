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
package com.aspectran.utils.lifecycle;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractLifeCycleTest {

    private static class SampleComponent extends AbstractLifeCycle {
        final AtomicInteger startCount = new AtomicInteger();
        final AtomicInteger stopCount = new AtomicInteger();
        boolean failOnStart;

        @Override
        protected void doStart() throws Exception {
            if (failOnStart) {
                throw new RuntimeException("Intentional start failure");
            }
            startCount.incrementAndGet();
        }

        @Override
        protected void doStop() throws Exception {
            stopCount.incrementAndGet();
        }
    }

    @Test
    void testBasicLifecycle() throws Exception {
        SampleComponent component = new SampleComponent();
        assertEquals(LifeCycle.State.STOPPED, component.getState());
        assertTrue(component.isStopped());
        assertFalse(component.isStarted());
        assertFalse(component.isRunning());

        component.start();
        assertEquals(LifeCycle.State.STARTED, component.getState());
        assertTrue(component.isStarted());
        assertTrue(component.isRunning());
        assertFalse(component.isStopped());
        assertEquals(1, component.startCount.get());

        // Redundant start() should be a no-op
        component.start();
        assertEquals(1, component.startCount.get());

        component.stop();
        assertEquals(LifeCycle.State.STOPPED, component.getState());
        assertTrue(component.isStopped());
        assertFalse(component.isStarted());
        assertFalse(component.isRunning());
        assertEquals(1, component.stopCount.get());

        // Redundant stop() should be a no-op
        component.stop();
        assertEquals(1, component.stopCount.get());
    }

    @Test
    void testListenerNotifications() throws Exception {
        SampleComponent component = new SampleComponent();
        List<String> events = new ArrayList<>();

        component.addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                events.add("starting");
            }

            @Override
            public void lifeCycleStarted(LifeCycle event) {
                events.add("started");
            }

            @Override
            public void lifeCycleStopping(LifeCycle event) {
                events.add("stopping");
            }

            @Override
            public void lifeCycleStopped(LifeCycle event) {
                events.add("stopped");
            }
        });

        component.start();
        component.stop();

        assertEquals(List.of("starting", "started", "stopping", "stopped"), events);
    }

    @Test
    void testListenerExceptionIsolation() throws Exception {
        SampleComponent component = new SampleComponent();
        AtomicBoolean secondListenerCalled = new AtomicBoolean(false);

        // First listener throws an exception
        component.addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarted(LifeCycle event) {
                throw new RuntimeException("Listener error");
            }
        });

        // Second listener should still be invoked
        component.addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleStarted(LifeCycle event) {
                secondListenerCalled.set(true);
            }
        });

        component.start();
        assertTrue(component.isStarted());
        assertTrue(secondListenerCalled.get(), "Second listener must be called despite exception in first listener");
    }

    @Test
    void testStartFailure() {
        SampleComponent component = new SampleComponent();
        component.failOnStart = true;
        AtomicBoolean failureNotified = new AtomicBoolean(false);

        component.addLifeCycleListener(new LifeCycle.Listener() {
            @Override
            public void lifeCycleFailure(LifeCycle event, Throwable cause) {
                failureNotified.set(true);
            }
        });

        assertThrows(RuntimeException.class, component::start);
        assertTrue(component.isFailed());
        assertEquals(LifeCycle.State.FAILED, component.getState());
        assertTrue(failureNotified.get());
    }

    @Test
    void testAutoCloseable() throws Exception {
        SampleComponent component = new SampleComponent();
        try (component) {
            component.start();
            assertTrue(component.isStarted());
        }
        assertTrue(component.isStopped());
        assertEquals(1, component.stopCount.get());
    }

}
