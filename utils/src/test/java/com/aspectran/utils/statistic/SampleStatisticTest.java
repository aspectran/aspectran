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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for {@link SampleStatistic}.
 */
class SampleStatisticTest {

    @Test
    void testInitialState() {
        SampleStatistic stat = new SampleStatistic();
        assertTrue(stat.isEmpty());
        assertEquals(0, stat.getCount());
        assertEquals(0, stat.getTotal());
        assertEquals(0, stat.getMax());
        assertEquals(0, stat.getMin());
        assertEquals(0.0, stat.getMean());
        assertEquals(0.0, stat.getVariance());
        assertEquals(0.0, stat.getStdDev());
    }

    @Test
    void testRecordSamples() {
        SampleStatistic stat = new SampleStatistic();
        stat.record(10);
        stat.record(20);
        stat.record(30);

        assertFalse(stat.isEmpty());
        assertEquals(3, stat.getCount());
        assertEquals(60, stat.getTotal());
        assertEquals(30, stat.getMax());
        assertEquals(10, stat.getMin());
        assertEquals(20.0, stat.getMean(), 0.001);
        assertTrue(stat.getVariance() > 0);
        assertTrue(stat.getStdDev() > 0);
        assertTrue(stat.toString().contains("min=10"));
        assertTrue(stat.toString().contains("max=30"));
    }

    @Test
    void testReset() {
        SampleStatistic stat = new SampleStatistic();
        stat.record(100);
        stat.record(200);
        assertEquals(2, stat.getCount());

        stat.reset();
        assertTrue(stat.isEmpty());
        assertEquals(0, stat.getCount());
        assertEquals(0, stat.getTotal());
        assertEquals(0, stat.getMax());
        assertEquals(0, stat.getMin());
        assertEquals(0.0, stat.getMean());
    }

    @Test
    void testNegativeSamples() {
        SampleStatistic stat = new SampleStatistic();
        stat.record(-10);
        stat.record(-20);
        stat.record(-30);

        assertEquals(3, stat.getCount());
        assertEquals(-60, stat.getTotal());
        assertEquals(-10, stat.getMax());
        assertEquals(-30, stat.getMin());
        assertEquals(-20.0, stat.getMean(), 0.001);
    }

    @Test
    void testConcurrentRecording() throws Exception {
        SampleStatistic stat = new SampleStatistic();
        int threadCount = 10;
        int samplesPerThread = 1000;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    startLatch.await();
                    for (int j = 1; j <= samplesPerThread; j++) {
                        stat.record(j);
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

        assertEquals(threadCount * samplesPerThread, stat.getCount());
        assertEquals(1, stat.getMin());
        assertEquals(samplesPerThread, stat.getMax());
    }

}
