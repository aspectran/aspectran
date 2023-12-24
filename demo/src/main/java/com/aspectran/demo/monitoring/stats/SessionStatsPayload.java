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
package com.aspectran.demo.monitoring.stats;

import com.aspectran.utils.json.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * <p>Created: 2020/01/11</p>
 */
public class SessionStatsPayload {

    private long createdSessionCount;

    private long expiredSessionCount;

    private long activeSessionCount;

    private long highestActiveSessionCount;

    private long evictedSessionCount;

    private long rejectedSessionCount;

    private String startTime;

    private String[] currentSessions;

    public long getCreatedSessionCount() {
        return createdSessionCount;
    }

    public void setCreatedSessionCount(long createdSessionCount) {
        this.createdSessionCount = createdSessionCount;
    }

    public long getExpiredSessionCount() {
        return expiredSessionCount;
    }

    public void setExpiredSessionCount(long expiredSessionCount) {
        this.expiredSessionCount = expiredSessionCount;
    }

    public long getEvictedSessionCount() {
        return evictedSessionCount;
    }

    public long getActiveSessionCount() {
        return activeSessionCount;
    }

    public void setActiveSessionCount(long activeSessionCount) {
        this.activeSessionCount = activeSessionCount;
    }

    public void setEvictedSessionCount(long evictedSessionCount) {
        this.evictedSessionCount = evictedSessionCount;
    }

    public long getHighestActiveSessionCount() {
        return highestActiveSessionCount;
    }

    public void setHighestActiveSessionCount(long highestActiveSessionCount) {
        this.highestActiveSessionCount = highestActiveSessionCount;
    }

    public long getRejectedSessionCount() {
        return rejectedSessionCount;
    }

    public void setRejectedSessionCount(long rejectedSessionCount) {
        this.rejectedSessionCount = rejectedSessionCount;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String[] getCurrentSessions() {
        return currentSessions;
    }

    public void setCurrentSessions(String[] currentSessions) {
        this.currentSessions = currentSessions;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SessionStatsPayload)) {
            return false;
        }
        SessionStatsPayload stats = (SessionStatsPayload)other;
        if (stats.createdSessionCount != createdSessionCount ||
                stats.expiredSessionCount != expiredSessionCount ||
                stats.evictedSessionCount != evictedSessionCount ||
                stats.activeSessionCount != activeSessionCount ||
                stats.highestActiveSessionCount != highestActiveSessionCount ||
                stats.rejectedSessionCount != rejectedSessionCount ||
                !Objects.equals(stats.startTime, startTime)) {
            return false;
        }
        return Arrays.equals(stats.currentSessions, currentSessions);
    }

    public String toJson() throws IOException {
        return new JsonWriter()
                .prettyPrint(false)
                .write(this)
                .toString();
    }

}
