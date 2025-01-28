/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
 * <p>This class is a clone of org.eclipse.jetty.util.statistic.CounterStatistic</p>
 *
 * Statistics on a counter value.
 *
 * <p>Keep total, current and maximum values of a counter that
 * can be incremented and decremented. The total refers only
 * to increments.</p>
 */
public class CounterStatistic {

    private final AtomicLong current = new AtomicLong();

    private final LongAccumulator max = new LongAccumulator(Math::max,0L);

    private final LongAdder total = new LongAdder();

    public void reset() {
        total.reset();
        max.reset();
        long current = this.current.get();
        total.add(current);
        max.accumulate(current);
    }

    public void reset(long value) {
        current.set(value);
        total.reset();
        max.reset();
        if (value > 0) {
            total.add(value);
            max.accumulate(value);
        }
    }

    public long add(long delta) {
        long value = current.addAndGet(delta);
        if (delta > 0) {
            total.add(delta);
            max.accumulate(value);
        }
        return value;
    }

    public long increment() {
        long value = current.incrementAndGet();
        total.increment();
        max.accumulate(value);
        return value;
    }

    public long decrement() {
        return current.decrementAndGet();
    }

    public long getCurrent() {
        return current.get();
    }

    public long getMax() {
        return max.get();
    }

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
