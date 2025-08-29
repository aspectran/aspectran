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
package com.aspectran.utils.timer;

import com.aspectran.utils.scheduling.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An abstract implementation for managing idle timeouts.
 * <p>This class is a clone of {@code org.eclipse.jetty.io.IdleTimeout}.</p>
 * <p>It is optimized such that timeout operations are not canceled on every activity.
 * Instead, timeouts are allowed to expire, and a check is then made to determine
 * when the last activity occurred. If the idle timeout has not truly expired,
 * the timeout is rescheduled for the earliest possible time a timeout could occur.</p>
 */
public abstract class IdleTimeout {

    private static final Logger logger = LoggerFactory.getLogger(IdleTimeout.class);

    private final Scheduler scheduler;

    private final AtomicReference<Scheduler.Task> timeout = new AtomicReference<>();

    private volatile long idleTimeout;

    private volatile long idleTimestamp = System.nanoTime();

    /**
     * Creates a new IdleTimeout instance.
     * @param scheduler a {@link Scheduler} used to schedule checks for the idle timeout
     */
    public IdleTimeout(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Returns the scheduler used by this IdleTimeout.
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Returns the period of time, in milliseconds, that this object has been idle.
     * @return the idle time in milliseconds
     */
    public long getIdleFor() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - idleTimestamp);
    }

    /**
     * Returns the idle timeout in milliseconds.
     * @return the idle timeout in milliseconds
     */
    public long getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Sets the idle timeout in milliseconds.
     * <p>A value less than or equal to zero disables the idle timeout checks.</p>
     * @param idleTimeout the idle timeout in milliseconds
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
     * It resets the idle timestamp to the current time.
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

    /**
     * Activates the idle timeout mechanism.
     * This method should be called when the monitored entity becomes active.
     */
    public void onOpen() {
        activate();
    }

    private void activate() {
        if (idleTimeout > 0) {
            idleCheck();
        }
    }

    /**
     * Deactivates the idle timeout mechanism.
     * This method should be called when the monitored entity is closed or no longer active.
     */
    public void onClose() {
        deactivate();
    }

    private void deactivate() {
        Scheduler.Task oldTimeout = timeout.getAndSet(null);
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }

    /**
     * Performs an idle timeout check.
     * <p>If the entity has been idle for longer than {@link #getIdleTimeout()},
     * {@link #onIdleExpired(TimeoutException)} is called.</p>
     * @return the remaining idle time in milliseconds, or -1 if idle timeout is disabled
     */
    protected long checkIdleTimeout() {
        if (isOpen()) {
            long idleTimestamp = this.idleTimestamp;
            long idleElapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - idleTimestamp);
            long idleTimeout = getIdleTimeout();
            long idleLeft = idleTimeout - idleElapsed;

            if (logger.isTraceEnabled()) {
                logger.trace("{} idle timeout check, elapsed: {} ms, remaining: {} ms", this, idleElapsed, idleLeft);
            }

            if (idleTimeout > 0) {
                if (idleLeft <= 0) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("{} idle timeout expired", this);
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
     * Subclasses must implement this method to define the action to be performed upon idle expiration.
     * @param timeout a {@link TimeoutException} indicating the timeout
     */
    protected abstract void onIdleExpired(TimeoutException timeout);

    /**
     * This abstract method should be implemented by subclasses to indicate if idle timeouts
     * should still be checked for the monitored entity.
     * @return {@code true} if the entity is open and should be checked for idle timeouts, {@code false} otherwise
     */
    public abstract boolean isOpen();

}
