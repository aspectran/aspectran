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
package com.aspectran.core.component.schedule;

/**
 * Interface for providing distributed locking for scheduled jobs.
 * Implementations of this interface can be used to prevent multiple nodes from executing
 * the same job concurrently in a clustered environment.
 *
 * @since 9.6.0
 */
public interface ScheduledJobLockProvider {

    /**
     * Attempts to acquire a lock for the specified job.
     * @param lockKey the unique key identifying the job to lock
     * @return true if the lock was successfully acquired, false otherwise
     */
    boolean lock(String lockKey);

    /**
     * Releases the lock for the specified job.
     * @param lockKey the unique key identifying the job to unlock
     */
    void unlock(String lockKey);

}
