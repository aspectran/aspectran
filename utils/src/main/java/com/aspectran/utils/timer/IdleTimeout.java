/*
 * Copyright (c) 2008-2025 The Aspectran Project
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
package com.aspectran.utils.timer;

import com.aspectran.utils.logging.Logger;
import com.aspectran.utils.logging.LoggerFactory;
import com.aspectran.utils.thread.Scheduler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>This class is a clone of org.eclipse.jetty.io.IdleTimeout</p>
 *
 * An Abstract implementation of an Idle Timeout.
 * <p>
 * This implementation is optimised that timeout operations are not cancelled on
 * every operation. Rather timeout are allowed to expire and a check is then made
 * to see when the last operation took place.  If the idle timeout has not expired,
 * the timeout is rescheduled for the earliest possible time a timeout could occur.</p>
 */
public abstract class IdleTimeout {

    private static final Logger logger = LoggerFactory.getLogger(IdleTimeout.class);

    private final Scheduler scheduler;

    private final AtomicReference<Scheduler.Task> timeout = new AtomicReference<>();

    private volatile long idleTimeout;

    private volatile long idleTimestamp = System.nanoTime();

    /**
     * @param scheduler A scheduler used to schedule checks for the idle timeout
     */
    public IdleTimeout(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * @return the period of time, in milliseconds, that this object was idle
     */
    public long getIdleFor() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - idleTimestamp);
    }

    /**
     * @return the idle timeout in milliseconds
     * @see #setIdleTimeout(long)
     */
    public long getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets the idle timeout in milliseconds.
     * A value that is less than or zero disables the idle timeout checks.
     * @param idleTimeout the idle timeout in milliseconds
     * @see #getIdleTimeout()
     */
    public void setIdleTimeout(long idleTimeout) {
        long old = this.idleTimeout;
        this.idleTimeout = idleTimeout;

        // Do we have an old timeout
        if (old > 0) {
            // if the old was less than or equal to the new timeout, then nothing more to do
            if (old <= idleTimeout) {
                return;
            }

            // old timeout is too long, so cancel it.
            deactivate();
        }

        // If we have a new timeout, then check and reschedule
        if (isOpen()) {
            activate();
        }
    }

    /**
     * This method should be called when non-idle activity has taken place.
     */
    public void notIdle() {
        idleTimestamp = System.nanoTime();
    }

    private void idleCheck() {
        long idleLeft = checkIdleTimeout();
        if (idleLeft >= 0) {
            scheduleIdleTimeout(idleLeft > 0 ? idleLeft : getIdleTimeout());
        }
    }

    private void scheduleIdleTimeout(long delay) {
        Scheduler.Task newTimeout = null;
        if (isOpen() && delay > 0 && scheduler != null) {
            newTimeout = scheduler.schedule(this::idleCheck, delay, TimeUnit.MILLISECONDS);
        }
        Scheduler.Task oldTimeout = timeout.getAndSet(newTimeout);
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }

    public void onOpen() {
        activate();
    }

    private void activate() {
        if (idleTimeout > 0) {
            idleCheck();
        }
    }

    public void onClose() {
        deactivate();
    }

    private void deactivate() {
        Scheduler.Task oldTimeout = timeout.getAndSet(null);
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }

    protected long checkIdleTimeout() {
        if (isOpen()) {
            long idleTimestamp = this.idleTimestamp;
            long idleElapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - idleTimestamp);
            long idleTimeout = getIdleTimeout();
            long idleLeft = idleTimeout - idleElapsed;

            if (logger.isTraceEnabled()) {
                logger.trace(this + " idle timeout check, elapsed: " + idleElapsed + " ms, remaining: " + idleLeft + " ms");
            }

            if (idleTimeout > 0) {
                if (idleLeft <= 0) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(this + " idle timeout expired");
                    }
                    try {
                        onIdleExpired(new TimeoutException("Idle timeout expired: " + idleElapsed + "/" + idleTimeout + " ms"));
                    } finally {
                        notIdle();
                    }
                }
            }
            return (idleLeft >= 0 ? idleLeft : 0);
        }
        return -1;
    }

    /**
     * This abstract method is called when the idle timeout has expired.
     * @param timeout a TimeoutException
     */
    protected abstract void onIdleExpired(TimeoutException timeout);

    /**
     * This abstract method should be called to check if idle timeouts
     * should still be checked.
     * @return true if the entity monitored should still be checked for idle timeouts
     */
    public abstract boolean isOpen();

}
