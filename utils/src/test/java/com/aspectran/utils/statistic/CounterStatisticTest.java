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
package com.aspectran.utils.statistic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link CounterStatistic}.
 */
class CounterStatisticTest {

    @Test
    void testInitialState() {
        CounterStatistic stat = new CounterStatistic();
        assertEquals(0, stat.getCurrent());
        assertEquals(0, stat.getMax());
        assertEquals(0, stat.getMin());
        assertEquals(0, stat.getTotal());
    }

    @Test
    void testIncrementAndDecrement() {
        CounterStatistic stat = new CounterStatistic();

        assertEquals(1, stat.increment());
        assertEquals(2, stat.increment());
        assertEquals(2, stat.getCurrent());
        assertEquals(2, stat.getMax());
        assertEquals(0, stat.getMin());
        assertEquals(2, stat.getTotal());

        assertEquals(1, stat.decrement());
        assertEquals(1, stat.getCurrent());
        assertEquals(2, stat.getMax());
        assertEquals(0, stat.getMin());
        assertEquals(2, stat.getTotal());

        assertEquals(0, stat.decrement());
        assertEquals(-1, stat.decrement());
        assertEquals(-1, stat.getCurrent());
        assertEquals(2, stat.getMax());
        assertEquals(-1, stat.getMin());
        assertEquals(2, stat.getTotal());

        assertTrue(stat.toString().contains("current=-1"));
        assertTrue(stat.toString().contains("min=-1"));
        assertTrue(stat.toString().contains("max=2"));
    }

    @Test
    void testAdd() {
        CounterStatistic stat = new CounterStatistic();

        assertEquals(10, stat.add(10));
        assertEquals(10, stat.getCurrent());
        assertEquals(10, stat.getMax());
        assertEquals(0, stat.getMin());
        assertEquals(10, stat.getTotal());

        assertEquals(5, stat.add(-5));
        assertEquals(5, stat.getCurrent());
        assertEquals(10, stat.getMax());
        assertEquals(0, stat.getMin());
        assertEquals(10, stat.getTotal());

        assertEquals(-10, stat.add(-15));
        assertEquals(-10, stat.getCurrent());
        assertEquals(10, stat.getMax());
        assertEquals(-10, stat.getMin());
        assertEquals(10, stat.getTotal());
    }

    @Test
    void testReset() {
        CounterStatistic stat = new CounterStatistic();
        stat.add(20);
        stat.decrement();
        assertEquals(19, stat.getCurrent());

        stat.reset();
        assertEquals(19, stat.getCurrent());
        assertEquals(19, stat.getMax());
        assertEquals(19, stat.getMin());
        assertEquals(19, stat.getTotal());

        stat.reset(5);
        assertEquals(5, stat.getCurrent());
        assertEquals(5, stat.getMax());
        assertEquals(5, stat.getMin());
        assertEquals(5, stat.getTotal());
    }

    @Test
    void testConcurrentOperations() throws Exception {
        CounterStatistic stat = new CounterStatistic();
        int threadCount = 10;
        int opsPerThread = 1000;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < opsPerThread; j++) {
                        stat.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            }).start();
        }

        startLatch.countDown();
        assertTrue(doneLatch.await(5, TimeUnit.SECONDS));

        assertEquals(threadCount * opsPerThread, stat.getCurrent());
        assertEquals(threadCount * opsPerThread, stat.getMax());
        assertEquals(threadCount * opsPerThread, stat.getTotal());
    }

}
