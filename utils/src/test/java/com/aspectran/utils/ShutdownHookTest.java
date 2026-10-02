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
package com.aspectran.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShutdownHookTest {

    @AfterEach
    void tearDown() {
        ShutdownHook.clearTasks();
    }

    @Test
    void testAddAndRemoveTask() {
        assertFalse(ShutdownHook.hasTasks());
        assertEquals(0, ShutdownHook.taskCount());

        AtomicBoolean executed = new AtomicBoolean(false);
        ShutdownHook.Task task1 = () -> executed.set(true);

        ShutdownHook.addTask(task1);
        assertTrue(ShutdownHook.hasTasks());
        assertEquals(1, ShutdownHook.taskCount());
        assertTrue(ShutdownHook.containsTask(task1));

        ShutdownHook.Task task2 = () -> {};
        ShutdownHook.addTask(task2);
        assertEquals(2, ShutdownHook.taskCount());
        assertTrue(ShutdownHook.containsTask(task2));

        ShutdownHook.removeTask(task1);
        assertEquals(1, ShutdownHook.taskCount());
        assertFalse(ShutdownHook.containsTask(task1));
        assertTrue(ShutdownHook.containsTask(task2));

        ShutdownHook.removeTask(task2);
        assertEquals(0, ShutdownHook.taskCount());
        assertFalse(ShutdownHook.hasTasks());
    }

    @Test
    void testManagerLifecycle() {
        AtomicBoolean cleaned = new AtomicBoolean(false);
        ShutdownHook.Manager manager = ShutdownHook.Manager.create(() -> cleaned.set(true));

        assertNotNull(manager);
        assertTrue(manager.isRegistered());
        assertEquals(1, ShutdownHook.taskCount());

        // Duplicate registration should be no-op
        manager.register(() -> {});
        assertEquals(1, ShutdownHook.taskCount());

        manager.close();
        assertFalse(manager.isRegistered());
        assertEquals(0, ShutdownHook.taskCount());

        // Try with resources
        try (ShutdownHook.Manager m = ShutdownHook.Manager.create(() -> {})) {
            assertTrue(m.isRegistered());
            assertEquals(1, ShutdownHook.taskCount());
        }
        assertEquals(0, ShutdownHook.taskCount());
    }

    @Test
    void testNullValidation() {
        assertThrows(IllegalArgumentException.class, () -> ShutdownHook.addTask(null));
        assertThrows(IllegalArgumentException.class, () -> ShutdownHook.Manager.create(null));
    }

}
