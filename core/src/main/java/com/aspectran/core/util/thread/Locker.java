/*
 * Copyright (c) 2008-2019 The Aspectran Project
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
package com.aspectran.core.util.thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Convenience Lock Wrapper.
 *
 * <pre>
 * try(Locker.Lock lock = locker.lock())
 * {
 *   // something
 * }
 * </pre>
 */
public class Locker {

    private final ReentrantLock lock = new ReentrantLock();

    private final Lock unlock = new Lock();

    /**
     * Acquires the lock.
     *
     * @return the lock to unlock
     */
    public Lock lock() {
        lock.lock();
        return unlock;
    }

    /**
     * @return whether this lock has been acquired
     */
    public boolean isLocked() {
        return lock.isLocked();
    }

    /**
     * @return a {@link Condition} associated with this lock
     */
    public Condition newCondition() {
        return lock.newCondition();
    }

    /**
     * The unlocker object that unlocks when it is closed.
     */
    public class Lock implements AutoCloseable {

        @Override
        public void close() {
            lock.unlock();
        }

    }

}
