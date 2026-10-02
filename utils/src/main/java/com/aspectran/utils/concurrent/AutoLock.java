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
package com.aspectran.utils.concurrent;

import com.aspectran.utils.Assert;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Reentrant lock that can be used in a try-with-resources statement.
 * <p>Typical usage:</p>
 * <pre>
 * try (AutoLock lock = this.lock.lock()) {
 *     // Something
 * }
 * </pre>
 */
public class AutoLock implements AutoCloseable, Serializable {

    @Serial
    private static final long serialVersionUID = -6052401301556858025L;

    private final ReentrantLock lock;

    /**
     * Creates an instance of {@code AutoLock} with non-fair ordering.
     */
    public AutoLock() {
        this(false);
    }

    /**
     * Creates an instance of {@code AutoLock} with the given fairness policy.
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    public AutoLock(boolean fair) {
        this.lock = new ReentrantLock(fair);
    }

    /**
     * Acquires the lock.
     * @return this AutoLock for unlocking
     */
    public @NonNull AutoLock lock() {
        lock.lock();
        return this;
    }

    /**
     * Acquires the lock unless the current thread is interrupted.
     * @return this AutoLock for unlocking
     * @throws InterruptedException if the current thread is interrupted
     */
    public @NonNull AutoLock lockInterruptibly() throws InterruptedException {
        lock.lockInterruptibly();
        return this;
    }

    /**
     * Acquires the lock only if it is free at the time of invocation.
     * @return this AutoLock for unlocking, or {@code null} if the lock was not acquired
     */
    public @Nullable AutoLock tryLock() {
        return (lock.tryLock() ? this : null);
    }

    /**
     * Acquires the lock if it is free within the given waiting time and the
     * current thread has not been interrupted.
     * @param timeout the time to wait for the lock
     * @param unit the time unit of the timeout argument
     * @return this AutoLock for unlocking, or {@code null} if the lock was not acquired
     * @throws InterruptedException if the current thread is interrupted
     */
    public @Nullable AutoLock tryLock(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
        Assert.notNull(unit, "unit must not be null");
        return (lock.tryLock(timeout, unit) ? this : null);
    }

    /**
     * Acquires the lock if it is free within the given duration and the
     * current thread has not been interrupted.
     * @param timeout the duration to wait for the lock
     * @return this AutoLock for unlocking, or {@code null} if the lock was not acquired
     * @throws InterruptedException if the current thread is interrupted
     */
    public @Nullable AutoLock tryLock(@NonNull Duration timeout) throws InterruptedException {
        Assert.notNull(timeout, "timeout must not be null");
        return tryLock(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Returns whether this lock is held by the current thread.
     * @return {@code true} if current thread holds this lock; {@code false} otherwise
     * @see ReentrantLock#isHeldByCurrentThread()
     */
    public boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    /**
     * Queries if this lock is held by any thread.
     * @return {@code true} if any thread holds this lock; {@code false} otherwise
     * @see ReentrantLock#isLocked()
     */
    public boolean isLocked() {
        return lock.isLocked();
    }

    /**
     * Returns {@code true} if this lock has fairness set true.
     * @return {@code true} if this lock has fairness set true
     * @see ReentrantLock#isFair()
     */
    public boolean isFair() {
        return lock.isFair();
    }

    /**
     * Queries the number of holds on this lock by the current thread.
     * @return the number of holds on this lock by the current thread,
     *      or zero if this lock is not held by the current thread
     * @see ReentrantLock#getHoldCount()
     */
    public int getHoldCount() {
        return lock.getHoldCount();
    }

    /**
     * Returns an estimate of the number of threads waiting to acquire this lock.
     * @return the estimated number of threads waiting for this lock
     * @see ReentrantLock#getQueueLength()
     */
    public int getQueueLength() {
        return lock.getQueueLength();
    }

    /**
     * Queries whether any threads are waiting to acquire this lock.
     * @return {@code true} if there may be other threads waiting to acquire the lock
     * @see ReentrantLock#hasQueuedThreads()
     */
    public boolean hasQueuedThreads() {
        return lock.hasQueuedThreads();
    }

    /**
     * Queries whether the given thread is waiting to acquire this lock.
     * @param thread the thread
     * @return {@code true} if the given thread is queued waiting for this lock
     * @see ReentrantLock#hasQueuedThread(Thread)
     */
    public boolean hasQueuedThread(Thread thread) {
        return lock.hasQueuedThread(thread);
    }

    /**
     * Returns a {@link Condition} instance for use with this lock.
     * @return a {@link Condition} associated with this lock
     */
    public @NonNull Condition newCondition() {
        return lock.newCondition();
    }

    /**
     * Returns the underlying {@link ReentrantLock} instance.
     * @return the underlying {@link ReentrantLock}
     */
    public @NonNull ReentrantLock getLock() {
        return lock;
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
     * try (AutoLock lock = _lock.lock()) {
     *     lock.await();
     * }
     *
     * // Signaling
     * try (AutoLock lock = _lock.lock()) {
     *     lock.signalAll();
     * }
     * </pre>
     */
    public static class WithCondition extends AutoLock {

        @Serial
        private static final long serialVersionUID = -2065722551537577160L;

        private final Condition condition;

        /**
         * Creates an instance of {@code WithCondition} with non-fair ordering.
         */
        public WithCondition() {
            this(false);
        }

        /**
         * Creates an instance of {@code WithCondition} with the given fairness policy.
         * @param fair {@code true} if this lock should use a fair ordering policy
         */
        public WithCondition(boolean fair) {
            super(fair);
            this.condition = newCondition();
        }

        @Override
        public @NonNull WithCondition lock() {
            super.lock();
            return this;
        }

        @Override
        public @NonNull WithCondition lockInterruptibly() throws InterruptedException {
            super.lockInterruptibly();
            return this;
        }

        @Override
        public @Nullable WithCondition tryLock() {
            return (super.tryLock() != null ? this : null);
        }

        @Override
        public @Nullable WithCondition tryLock(long timeout, @NonNull TimeUnit unit) throws InterruptedException {
            return (super.tryLock(timeout, unit) != null ? this : null);
        }

        @Override
        public @Nullable WithCondition tryLock(@NonNull Duration timeout) throws InterruptedException {
            return (super.tryLock(timeout) != null ? this : null);
        }

        /**
         * Returns the {@link Condition} instance associated with this lock.
         * @return the condition instance
         */
        public @NonNull Condition getCondition() {
            return condition;
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
         * Causes the current thread to wait until it is signalled or interrupted,
         * or the specified waiting time elapses.
         * @param time the maximum time to wait
         * @param unit the time unit of the {@code time} argument
         * @return {@code false} if the waiting time detectably elapsed
         *      before return from the method, else {@code true}
         * @throws InterruptedException if the current thread is interrupted
         * @see Condition#await(long, TimeUnit)
         */
        public boolean await(long time, @NonNull TimeUnit unit) throws InterruptedException {
            Assert.notNull(unit, "unit must not be null");
            return condition.await(time, unit);
        }

        /**
         * Causes the current thread to wait until it is signalled or interrupted,
         * or the specified duration elapses.
         * @param duration the maximum duration to wait
         * @return {@code false} if the waiting time detectably elapsed
         *      before return from the method, else {@code true}
         * @throws InterruptedException if the current thread is interrupted
         */
        public boolean await(@NonNull Duration duration) throws InterruptedException {
            Assert.notNull(duration, "duration must not be null");
            return condition.await(duration.toNanos(), TimeUnit.NANOSECONDS);
        }

        /**
         * Causes the current thread to wait until it is signalled without interruption.
         * @see Condition#awaitUninterruptibly()
         */
        public void awaitUninterruptibly() {
            condition.awaitUninterruptibly();
        }

        /**
         * Causes the current thread to wait until it is signalled or interrupted,
         * or the specified waiting time elapses.
         * @param nanosTimeout the maximum time to wait, in nanoseconds
         * @return an estimate of the {@code nanosTimeout} value minus
         *      the time spent waiting upon return from this method
         * @throws InterruptedException if the current thread is interrupted
         * @see Condition#awaitNanos(long)
         */
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return condition.awaitNanos(nanosTimeout);
        }

        /**
         * Causes the current thread to wait until it is signalled or interrupted,
         * or the specified deadline elapses.
         * @param deadline the absolute deadline to wait until
         * @return {@code false} if the deadline has elapsed upon return, else {@code true}
         * @throws InterruptedException if the current thread is interrupted
         * @see Condition#awaitUntil(Date)
         */
        public boolean awaitUntil(@NonNull Date deadline) throws InterruptedException {
            Assert.notNull(deadline, "deadline must not be null");
            return condition.awaitUntil(deadline);
        }

        /**
         * Causes the current thread to wait until it is signalled or interrupted,
         * or the specified deadline elapses.
         * @param deadline the absolute deadline to wait until
         * @return {@code false} if the deadline has elapsed upon return, else {@code true}
         * @throws InterruptedException if the current thread is interrupted
         */
        public boolean awaitUntil(@NonNull Instant deadline) throws InterruptedException {
            Assert.notNull(deadline, "deadline must not be null");
            return condition.awaitUntil(Date.from(deadline));
        }

        /**
         * Queries whether any threads are waiting on the condition associated with this lock.
         * @return {@code true} if there are any waiting threads
         */
        public boolean hasWaiters() {
            return getLock().hasWaiters(condition);
        }

        /**
         * Returns an estimate of the number of threads waiting on the condition
         * associated with this lock.
         * @return the estimated number of waiting threads
         */
        public int getWaitQueueLength() {
            return getLock().getWaitQueueLength(condition);
        }

    }

}
