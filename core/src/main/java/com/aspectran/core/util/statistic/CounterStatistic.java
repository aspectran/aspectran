/*
 * Copyright (c) 2008-2018 The Aspectran Project
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
 * Statistics on a counter value.
 *
 * <p>Keep total, current and maximum values of a counter that
 * can be incremented and decremented. The total refers only
 * to increments.</p>
 */
public class CounterStatistic {

    protected final LongAccumulator max = new LongAccumulator(Math::max,0L);

    protected final AtomicLong current = new AtomicLong();

    protected final LongAdder total = new LongAdder();

    public void reset() {
        total.reset();
        max.reset();
        long current = this.current.get();
        total.add(current);
        max.accumulate(current);
    }

    public void reset(final long value) {
        current.set(value);
        total.reset();
        max.reset();
        if (value > 0) {
            total.add(value);
            max.accumulate(value);
        }
    }

    /**
     * @param delta the amount to add to the count
     * @return the new value
     */
    public long add(final long delta) {
        long value = current.addAndGet(delta);
        if (delta > 0) {
            total.add(delta);
            max.accumulate(value);
        }
        return value;
    }

    /**
     * increment the value by one
     * @return the new value, post increment
     */
    public long increment() {
        long value = current.incrementAndGet();
        total.increment();
        max.accumulate(value);
        return value;
    }

    /**
     * decrement by 1
     * @return the new value, post-decrement
     */
    public long decrement() {
        return current.decrementAndGet();
    }

    /**
     * @return max value
     */
    public long getMax() {
        return max.get();
    }

    /**
     * @return current value
     */
    public long getCurrent() {
        return current.get();
    }

    /**
     * @return total value
     */
    public long getTotal() {
        return total.sum();
    }

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(String.format("%s@%x", getClass().getSimpleName(), hashCode()));
        tsb.append("current", current.get());
        tsb.append("max", max.get());
        tsb.append("total", total.sum());
        return tsb.toString();
    }

}
