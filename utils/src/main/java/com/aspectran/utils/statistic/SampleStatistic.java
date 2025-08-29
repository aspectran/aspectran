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

import com.aspectran.utils.ObjectUtils;
import com.aspectran.utils.ToStringBuilder;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Provides statistics on a sampled value, including max, total, mean, count, variance, and standard deviation.
 * <p>This class is a clone of {@code org.eclipse.jetty.util.statistic.SampleStatistic}.</p>
 * <p>It calculates estimates of mean, variance, and standard deviation characteristics of a sample
 * using a non-synchronized approximation of the on-line algorithm presented in
 * <cite>Donald Knuth's Art of Computer Programming, Volume 2, Semi numerical Algorithms, 3rd edition, page 232, Boston: Addison-Wesley</cite>.
 * This algorithm is also described in Wikipedia in the section "Online algorithm":
 * <a href="https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance">Algorithms for calculating variance</a>.</p>
 * <p>This class is thread-safe due to its use of atomic operations.</p>
 */
public class SampleStatistic {

    private final LongAccumulator max = new LongAccumulator(Math::max,0L);

    private final AtomicLong total = new AtomicLong();

    private final AtomicLong count = new AtomicLong();

    private final LongAdder totalVariance100 = new LongAdder();

    /**
     * Resets all statistics (max, total, count, variance) to their initial zero values.
     */
    public void reset() {
        max.reset();
        total.set(0);
        count.set(0);
        totalVariance100.reset();
    }

    /**
     * Records a sample value and updates the statistics.
     * <p>This method updates the total sum, increments the count, and contributes to the variance calculation.</p>
     * @param sample the value to record
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
     * Returns the maximum value recorded among all samples.
     * @return the maximum sample value
     */
    public long getMax() {
        return max.get();
    }

    /**
     * Returns the sum of all recorded samples.
     * @return the total sum of samples
     */
    public long getTotal() {
        return total.get();
    }

    /**
     * Returns the number of samples recorded.
     * @return the count of samples
     */
    public long getCount() {
        return count.get();
    }

    /**
     * Returns the average (mean) value of the recorded samples.
     * @return the mean value, or 0.0 if no samples have been recorded
     */
    public double getMean() {
        long count = getCount();
        return (count > 0 ? (double)this.total.get() / this.count.get() : 0.0D);
    }

    /**
     * Returns the variance of the recorded samples.
     * @return the variance, or 0.0 if fewer than 2 samples have been recorded
     */
    public double getVariance() {
        long variance100 = totalVariance100.sum();
        long count = getCount();
        return (count > 1 ? variance100 / 100.0D / (count - 1) : 0.0D);
    }

    /**
     * Returns the standard deviation of the recorded samples.
     * @return the standard deviation
     */
    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(ObjectUtils.simpleIdentityToString(this));
        tsb.append("max", getMax());
        tsb.append("total", getTotal());
        tsb.append("count", getCount());
        tsb.append("mean", getMean());
        tsb.append("stddev", getStdDev());
        return tsb.toString();
    }

}
