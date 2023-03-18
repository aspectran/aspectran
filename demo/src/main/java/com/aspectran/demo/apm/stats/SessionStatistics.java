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
package com.aspectran.demo.apm.stats;

import com.aspectran.core.util.json.JsonWriter;

import java.io.IOException;
import java.util.Arrays;

/**
 * <p>Created: 2020/01/11</p>
 */
public class SessionStatistics {

    private long activeSessionCount;

    private long highestSessionCount;

    private long createdSessionCount;

    private long expiredSessionCount;

    private long rejectedSessionCount;

    private String[] currentUsers;

    public long getActiveSessionCount() {
        return activeSessionCount;
    }

    public void setActiveSessionCount(long activeSessionCount) {
        this.activeSessionCount = activeSessionCount;
    }

    public long getHighestSessionCount() {
        return highestSessionCount;
    }

    public void setHighestSessionCount(long highestSessionCount) {
        this.highestSessionCount = highestSessionCount;
    }

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

    public long getRejectedSessionCount() {
        return rejectedSessionCount;
    }

    public void setRejectedSessionCount(long rejectedSessionCount) {
        this.rejectedSessionCount = rejectedSessionCount;
    }

    public String[] getCurrentUsers() {
        return currentUsers;
    }

    public void setCurrentUsers(String[] currentUsers) {
        this.currentUsers = currentUsers;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof SessionStatistics)) {
            return false;
        }
        SessionStatistics stats = (SessionStatistics)other;
        if (stats.activeSessionCount != activeSessionCount ||
                stats.highestSessionCount != highestSessionCount ||
                stats.createdSessionCount != createdSessionCount ||
                stats.expiredSessionCount != expiredSessionCount ||
                stats.rejectedSessionCount != rejectedSessionCount) {
            return false;
        }
        return Arrays.equals(stats.currentUsers, currentUsers);
    }

    public String toJson() throws IOException {
        return new JsonWriter()
                .prettyPrint(false)
                .write(this)
                .toString();
    }

}
