/*
 * Copyright (c) 2008-2023 The Aspectran Project
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
//
//  ========================================================================
//  Copyright (c) 1995-2017 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//
package com.aspectran.core.util.statistic;

import com.aspectran.core.util.ToStringBuilder;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * <p>This class is a clone of org.eclipse.jetty.util.statistic.SampleStatistic</p>
 *
 * <p>Statistics on a sampled value.</p>
 * <p>Provides max, total, mean, count, variance, and standard deviation of continuous sequence of samples.</p>
 * <p>Calculates estimates of mean, variance, and standard deviation characteristics of a sample using a non synchronized
 * approximation of the on-line algorithm presented in <cite>Donald Knuth's Art of Computer Programming, Volume 2,
 * Semi numerical Algorithms, 3rd edition, page 232, Boston: Addison-Wesley</cite>. That cites a 1962 paper by B.P. Welford:
 * <a href="http://www.jstor.org/pss/1266577">Note on a Method for Calculating Corrected Sums of Squares and Products</a></p>
 * <p>This algorithm is also described in Wikipedia in the section "Online algorithm":
 * <a href="https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance">Algorithms for calculating variance</a>.</p>
 */
public class SampleStatistic {

    protected final LongAccumulator max = new LongAccumulator(Math::max,0L);

    protected final AtomicLong total = new AtomicLong();

    protected final AtomicLong count = new AtomicLong();

    protected final LongAdder totalVariance100 = new LongAdder();

    /**
     * Resets the statistics.
     */
    public void reset() {
        max.reset();
        total.set(0);
        count.set(0);
        totalVariance100.reset();
    }

    /**
     * Records a sample value.
     * @param sample the value to record.
     */

    public void record(long sample) {
        long total = this.total.addAndGet(sample);
        long count = this.count.incrementAndGet();
        if (count > 1) {
            long mean10 = total * 10 / count;
            long delta10 = sample * 10 - mean10;
            totalVariance100.add(delta10 * delta10);
        }
        max.accumulate(sample);
    }

    /**
     * @return the max value of the recorded samples
     */
    public long getMax() {
        return max.get();
    }

    /**
     * @return the sum of all the recorded samples
     */
    public long getTotal() {
        return total.get();
    }

    /**
     * @return the number of samples recorded
     */
    public long getCount() {
        return count.get();
    }

    /**
     * @return the average value of the samples recorded, or zero if there are no samples
     */
    public double getMean() {
        long count = getCount();
        return (count > 0 ? (double)this.total.get() / this.count.get() : 0.0D);
    }

    /**
     * @return the variance of the samples recorded, or zero if there are less than 2 samples
     */
    public double getVariance() {
        long variance100 = totalVariance100.sum();
        long count = getCount();
        return (count > 1 ? variance100 / 100.0D / (count - 1) : 0.0D);
    }

    /**
     * @return the standard deviation of the samples recorded
     */
    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(String.format("%s@%x", getClass().getSimpleName(), hashCode()));
        tsb.append("count", getCount());
        tsb.append("max", getMax());
        tsb.append("total", getTotal());
        tsb.append("stddev", getStdDev());
        return tsb.toString();
    }

}
