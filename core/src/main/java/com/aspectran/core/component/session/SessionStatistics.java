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
package com.aspectran.core.component.session;

import com.aspectran.core.util.statistic.CounterStatistic;
import com.aspectran.core.util.statistic.SampleStatistic;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Created: 11/18/23</p>
 */
public class SessionStatistics {

    private final CounterStatistic activationCount = new CounterStatistic();

    private final AtomicLong creationCount = new AtomicLong();

    private final AtomicLong expirationCount = new AtomicLong();

    private final AtomicLong rejectionCount = new AtomicLong();

    private final SampleStatistic timeRecord = new SampleStatistic();

    private final AtomicLong startTime = new AtomicLong(System.currentTimeMillis());

    /**
     * @return total number of sessions created
     */
    public long getCreatedSessions() {
        return creationCount.get();
    }

    /**
     * @param createdSessionsToAdd number of created sessions to add
     * @return total number of sessions created before adding
     */
    public long getCreatedSessionsAndAdd(long createdSessionsToAdd) {
        return creationCount.getAndAdd(createdSessionsToAdd);
    }

    /**
     * @param createdSessionsToAdd number of created sessions to add
     * @return total number of sessions created after adding
     */
    public long addCreatedSessionsAndGet(long createdSessionsToAdd) {
        return creationCount.addAndGet(createdSessionsToAdd);
    }

    /**
     * @return the number of expired sessions
     */
    public long getExpiredSessions() {
        return expirationCount.get();
    }

    /**
     * @return the number of valid sessions in the session cache
     */
    public long getActiveSessions() {
        return activationCount.getCurrent();
    }

    /**
     * @return the highest number of sessions that have been active at a single time
     */
    public long getHighestActiveSessions() {
        return activationCount.getMax();
    }

    /**
     * @return the number of valid sessions temporarily evicted from the session cache
     */
    public long getEvictedSessions() {
        return Math.max(getCreatedSessions() - getExpiredSessions() - getActiveSessions(), 0L);
    }

    /**
     * @return the number of rejected sessions
     */
    public long getRejectedSessions() {
        return rejectionCount.get();
    }

    /**
     * @return the maximum amount of time session remained valid
     */
    public long getSessionTimeMax() {
        return timeRecord.getMax();
    }

    /**
     * @return the total amount of time all sessions remained valid
     */
    public long getSessionTimeTotal() {
        return timeRecord.getTotal();
    }

    /**
     * @return the mean amount of time session remained valid
     */
    public long getSessionTimeMean() {
        return Math.round(timeRecord.getMean());
    }

    /**
     * @return the standard deviation of amount of time session remained valid
     */
    public double getSessionTimeStdDev() {
        return timeRecord.getStdDev();
    }

    /**
     * @return the timestamp at which session statistics were reset and new counting started
     */
    public long getStartTime() {
        return startTime.get();
    }

    /**
     * Resets the session usage statistics.
     */
    public void reset() {
        activationCount.reset();
        creationCount.set(0L);
        expirationCount.set(0L);
        rejectionCount.set(0L);
        timeRecord.reset();
        startTime.set(System.currentTimeMillis());
    }

    protected void sessionCreated() {
        creationCount.incrementAndGet();
    }

    protected void sessionExpired() {
        expirationCount.incrementAndGet();
    }

    protected void sessionActivated() {
        activationCount.increment();
    }

    protected void sessionEvicted() {
        activationCount.decrement();
    }

    protected void sessionRejected() {
        rejectionCount.incrementAndGet();
    }

    /**
     * Record length of time session has been active. Called when the
     * session is about to be invalidated.
     * @param sample the value to record.
     */
    protected void recordTime(long sample) {
        timeRecord.record(sample);
    }

}
