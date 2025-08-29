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
 * Provides statistics on a counter value, tracking total, current, and maximum values.
 * <p>This class is a clone of {@code org.eclipse.jetty.util.statistic.CounterStatistic}.</p>
 * <p>It supports incrementing and decrementing the current value, while maintaining
 * a running total (sum of all increments) and the highest value reached.</p>
 * <p>This class is thread-safe due to its use of {@link AtomicLong}, {@link LongAccumulator},
 * and {@link LongAdder}.</p>
 */
public class CounterStatistic {

    private final AtomicLong current = new AtomicLong();

    private final LongAccumulator max = new LongAccumulator(Math::max,0L);

    private final LongAdder total = new LongAdder();

    /**
     * Resets the total and maximum values to zero, and sets the current value to zero.
     */
    public void reset() {
        total.reset();
        max.reset();
        long current = this.current.get();
        total.add(current);
        max.accumulate(current);
    }

    /**
     * Resets the counter to a specific value.
     * The total and maximum values are reset, and the current value is set.
     * If the value is positive, it's added to total and accumulated in max.
     * @param value the value to reset the counter to
     */
    public void reset(long value) {
        current.set(value);
        total.reset();
        max.reset();
        if (value > 0) {
            total.add(value);
            max.accumulate(value);
        }
    }

    /**
     * Adds a delta to the current value.
     * If the delta is positive, it's also added to the total and accumulated in the maximum.
     * @param delta the value to add (can be positive or negative)
     * @return the new current value
     */
    public long add(long delta) {
        long value = current.addAndGet(delta);
        if (delta > 0) {
            total.add(delta);
            max.accumulate(value);
        }
        return value;
    }

    /**
     * Increments the current value by one.
     * The total is incremented, and the new current value is accumulated in the maximum.
     * @return the new current value
     */
    public long increment() {
        long value = current.incrementAndGet();
        total.increment();
        max.accumulate(value);
        return value;
    }

    /**
     * Decrements the current value by one.
     * @return the new current value
     */
    public long decrement() {
        return current.decrementAndGet();
    }

    /**
     * Returns the current value of the counter.
     * @return the current value
     */
    public long getCurrent() {
        return current.get();
    }

    /**
     * Returns the maximum value recorded by the counter.
     * @return the maximum value
     */
    public long getMax() {
        return max.get();
    }

    /**
     * Returns the total sum of all increments to the counter.
     * @return the total sum
     */
    public long getTotal() {
        return total.sum();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(ObjectUtils.simpleIdentityToString(this));
        tsb.append("current", getCurrent());
        tsb.append("max", getMax());
        tsb.append("total", getTotal());
        return tsb.toString();
    }

}
