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

import com.aspectran.core.util.logging.Log;
import com.aspectran.core.util.logging.LogFactory;
import com.aspectran.core.util.thread.Scheduler.Task;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An Abstract implementation of an Idle Timeout.
 *
 * <p>Created: 2017. 6. 25.</p>
 */
public abstract class IdleTimeout {

    private static final Log log = LogFactory.getLog(IdleTimeout.class);

    private final AtomicReference<Task> timeout = new AtomicReference<>();

    private final Scheduler scheduler;

    private volatile long idleTimeout;

    private volatile long idleTimestamp = System.currentTimeMillis();

    private final Runnable idleTask = () -> {
        long idleLeft = checkIdleTimeout();
        if (idleLeft >= 0) {
            scheduleIdleTimeout(idleLeft > 0 ? idleLeft : getIdleTimeout());
        }
    };

    public IdleTimeout(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        long old = this.idleTimeout;
        this.idleTimeout = idleTimeout;

        if(old > 0L) {
            if(old <= idleTimeout) {
                return;
            }
            deactivate();
        }

        if(isValid()) {
            activate();
        }
    }

    public void notIdle() {
        idleTimestamp = System.currentTimeMillis();
    }

    private void activate() {
        if (idleTimeout > 0) {
            idleTask.run();
        }
    }

    private void deactivate() {
        Task oldTimeout = timeout.getAndSet(null);
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }

    private void scheduleIdleTimeout(long delay) {
        Task newTimeout = null;
        if (isValid() && delay > 0L) {
            newTimeout = scheduler.schedule(idleTask, delay, TimeUnit.MILLISECONDS);
        }

        Task oldTimeout = timeout.getAndSet(newTimeout);
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }

    private long checkIdleTimeout() {
        if (!isValid()) {
            return -1L;
        }

        long idleTimestamp = this.idleTimestamp;
        long idleTimeout = this.idleTimeout;
        long idleElapsed = System.currentTimeMillis() - idleTimestamp;
        long idleLeft = idleTimeout - idleElapsed;

        if (log.isTraceEnabled()) {
            log.trace(this + " idle timeout check, elapsed: " + idleElapsed + " ms, remaining: " + idleLeft + " ms");
        }

        if (idleTimestamp != 0L && idleTimeout > 0L) {
            if (idleLeft <= 0L) {
                try {
                    idleExpired();
                } finally {
                    notIdle();
                }
            }
        }

        return (idleLeft >= 0L ? idleLeft : 0L);
    }

    public abstract boolean isValid();

    protected abstract void idleExpired();

}
