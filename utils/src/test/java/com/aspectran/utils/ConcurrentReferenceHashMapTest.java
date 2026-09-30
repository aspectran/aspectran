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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for {@link ConcurrentReferenceHashMap}.
 */
class ConcurrentReferenceHashMapTest {

    @Test
    void testBasicOperations() {
        ConcurrentReferenceHashMap<String, String> map = new ConcurrentReferenceHashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");

        assertEquals("v1", map.get("k1"));
        assertEquals("v2", map.get("k2"));
        assertTrue(map.containsKey("k1"));
        assertTrue(map.containsValue("v2"));
        assertEquals(2, map.size());

        map.remove("k1");
        assertNull(map.get("k1"));
        assertEquals(1, map.size());
    }

    @Test
    void testComputeIfAbsentAtomicExecution() throws InterruptedException {
        ConcurrentReferenceHashMap<String, Integer> map = new ConcurrentReferenceHashMap<>();
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger calculationCounter = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    map.computeIfAbsent("key", k -> {
                        calculationCounter.incrementAndGet();
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return 42;
                    });
                } catch (InterruptedException ignored) {
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertEquals(42, map.get("key"));
        // Calculation should be performed exactly once under segment lock
        assertEquals(1, calculationCounter.get());
    }

    @Test
    void testComputeIfPresent() {
        ConcurrentReferenceHashMap<String, Integer> map = new ConcurrentReferenceHashMap<>();
        map.put("count", 10);

        Integer result = map.computeIfPresent("count", (k, v) -> v + 5);
        assertEquals(15, result);
        assertEquals(15, map.get("count"));

        // If function returns null, entry should be removed
        map.computeIfPresent("count", (k, v) -> null);
        assertNull(map.get("count"));
        assertFalse(map.containsKey("count"));
    }

    @Test
    void testCompute() {
        ConcurrentReferenceHashMap<String, String> map = new ConcurrentReferenceHashMap<>();
        map.compute("msg", (k, v) -> (v == null ? "hello" : v + " world"));
        assertEquals("hello", map.get("msg"));

        map.compute("msg", (k, v) -> (v == null ? "hello" : v + " world"));
        assertEquals("hello world", map.get("msg"));

        map.compute("msg", (k, v) -> null);
        assertNull(map.get("msg"));
    }

    @Test
    void testMerge() {
        ConcurrentReferenceHashMap<String, String> map = new ConcurrentReferenceHashMap<>();
        map.merge("tag", "alpha", (oldVal, newVal) -> oldVal + "," + newVal);
        assertEquals("alpha", map.get("tag"));

        map.merge("tag", "beta", (oldVal, newVal) -> oldVal + "," + newVal);
        assertEquals("alpha,beta", map.get("tag"));

        map.merge("tag", "gamma", (oldVal, newVal) -> null);
        assertNull(map.get("tag"));
    }

    @Test
    void testKeySetAndValuesView() {
        ConcurrentReferenceHashMap<String, String> map = new ConcurrentReferenceHashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");

        List<String> keys = new ArrayList<>(map.keySet());
        assertTrue(keys.contains("a"));
        assertTrue(keys.contains("b"));
        assertTrue(keys.contains("c"));
        assertEquals(3, keys.size());

        List<String> values = new ArrayList<>(map.values());
        assertTrue(values.contains("1"));
        assertTrue(values.contains("2"));
        assertTrue(values.contains("3"));
        assertEquals(3, values.size());

        map.keySet().remove("a");
        assertFalse(map.containsKey("a"));
        assertEquals(2, map.size());
    }

}
