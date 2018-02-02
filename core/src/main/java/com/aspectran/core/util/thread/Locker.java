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

    private static final Lock LOCKED = new Lock();

    private final ReentrantLock lock = new ReentrantLock();

    private final Lock unlock = new UnLock();

    public Locker() {
    }

    public Lock lock() {
        if (lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Locker is not reentrant");
        }
        lock.lock();
        return unlock;
    }

    public Lock lockIfNotHeld (){
        if (lock.isHeldByCurrentThread()) {
            return LOCKED;
        }
        lock.lock();
        return unlock;
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public Condition newCondition() {
        return lock.newCondition();
    }

    public static class Lock implements AutoCloseable {

        @Override
        public void close() {
        }

    }

    public class UnLock extends Lock {

        @Override
        public void close() {
            lock.unlock();
        }

    }

}
