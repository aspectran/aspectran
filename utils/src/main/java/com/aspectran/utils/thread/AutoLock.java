/*
 * Copyright (c) 2008-2024 The Aspectran Project
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
package com.aspectran.utils.thread;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Reentrant lock that can be used in a try-with-resources statement.
 * <p>Typical usage:</p>
 * <pre>
 * try (AutoLock lock = this.lock.lock())
 * {
 *     // Something
 * }
 * </pre>
 */
public class AutoLock implements AutoCloseable, Serializable {

    private static final long serialVersionUID = -6052401301556858025L;

    private final ReentrantLock lock = new ReentrantLock();

    /**
     * Acquires the lock.
     * @return this AutoLock for unlocking
     */
    public AutoLock lock() {
        lock.lock();
        return this;
    }

    /**
     * @return whether this lock is held by the current thread
     * @see ReentrantLock#isHeldByCurrentThread()
     */
    public boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    /**
     * @return a {@link Condition} associated with this lock
     */
    public Condition newCondition() {
        return lock.newCondition();
    }

    // Package-private for testing only.
    boolean isLocked() {
        return lock.isLocked();
    }

    @Override
    public void close() {
        lock.unlock();
    }

    /**
     * A reentrant lock with a condition that can be used in a try-with-resources statement.
     * <p>Typical usage:</p>
     * <pre>
     * // Waiting
     * try (AutoLock lock = _lock.lock())
     * {
     *     lock.await();
     * }
     *
     * // Signaling
     * try (AutoLock lock = _lock.lock())
     * {
     *     lock.signalAll();
     * }
     * </pre>
     */
    public static class WithCondition extends AutoLock {

        private static final long serialVersionUID = -2065722551537577160L;

        private final Condition condition = newCondition();

        @Override
        public AutoLock.WithCondition lock() {
            return (WithCondition)super.lock();
        }

        /**
         * @see Condition#signal()
         */
        public void signal() {
            condition.signal();
        }

        /**
         * @see Condition#signalAll()
         */
        public void signalAll() {
            condition.signalAll();
        }

        /**
         * @throws InterruptedException if the current thread is interrupted
         * @see Condition#await()
         */
        public void await() throws InterruptedException {
            condition.await();
        }

        /**
         * @param time the time to wait
         * @param unit the time unit
         * @return false if the waiting time elapsed
         * @throws InterruptedException if the current thread is interrupted
         * @see Condition#await(long, TimeUnit)
         */
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return condition.await(time, unit);
        }

    }

}
